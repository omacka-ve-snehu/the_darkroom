import requests
from bs4 import BeautifulSoup
import sqlite3
import time
import re
import urllib.parse
import csv
import io
import math

LIST_URL = "https://www.digitaltruth.com/chart/print.php" # for gathering the list of listed films
DEV_URL = "https://www.digitaltruth.com/chart/" # for gathering specific-film data 
IMAGE_CSV_URL = "https://raw.githubusercontent.com/dekuNukem/Film-Packaging/master/film_packaging/database.csv" # for gathering image ids
IMAGE_URL = "https://fp-archive.com/film_packaging/archive/" # for gathering full image urls

DB_FILE = "devchart.db"


def setup_database():
    connection = sqlite3.connect(DB_FILE)
    cursor = connection.cursor()
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS dev_times (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            film TEXT,
            developer TEXT,
            dilution TEXT,
            iso INTEGER,
            temp INTEGER,
            time_135 INTEGER,
            time_120 INTEGER,
            image_url_135 TEXT,
            image_url_120 TEXT        
        )    
    ''')
    cursor.execute('DELETE FROM dev_times')
    connection.commit()
    return connection

def fetch_fp_database() -> list:
    """Downloads and filters the FP CSV database."""

    print('Downloading Film Packaging Archive CSV database')
    response = requests.get(IMAGE_CSV_URL)

    csv_data = io.StringIO(response.text)
    reader = csv.DictReader(csv_data)

    boxes = []
    for row in reader:
        if row.get('item_type') == 'film_box_outside':
            boxes.append(row)

    print(f'Loaded {len(boxes)} image urls into memory.')
    return boxes

def normalize_string(s: str) -> str:
    s = re.sub(r'[^a-zA-Z0-9]', ' ', s)
    return set(s.lower().split())

def find_best_image_match(film_name: str, requested_format: str, db_boxes: list) -> str:
    """
    Finds a matching FPA image for an MDC entry based on intersection in normalized names.
    """
    film_words = normalize_string(film_name)
    film_name_lower = film_name.lower()

    dev_brand = film_name.split()[0].lower() # MDC states the name of the brand first

    best_match_filename = ""
    max_intersection = 0

    for box in db_boxes:
        if box.get('film_format', '').lower() != requested_format:
            continue

        box_brand = box.get('brand', '').lower()
        is_brand_match = (
            box_brand in film_name_lower or 
            dev_brand in box_brand or 
            ('fuji' in dev_brand and 'fuji' in box_brand) # MDC uses 'fuji' while FPA 'fujifilm'
        )
        if not is_brand_match:
            continue

        box_name = f"{box.get('brand', '')} {box.get('product', '')}"
        box_words = normalize_string(box_name)

        intersection = len(film_words.intersection(box_words))
        if intersection > max_intersection and intersection >= 2: # at least two words match
            max_intersection = intersection
            best_match_filename = box.get('filename')

    if best_match_filename:
        return IMAGE_URL + best_match_filename
    return ""

def time_to_seconds(time_str: str) -> int:
    time_str = time_str.strip()
    if not time_str:
        return 0

    try:
        if '+' in time_str:
            parts = time_str.split('+')
            total_minutes = sum(float(part) for part in parts if part.strip())
            
            return int(total_minutes * 60)

        elif '-' in time_str:
            parts = time_str.split('-')
            times = [float(part) for part in parts if part.strip()]

            if times:
                average_minutes = sum(times)/len(times)
                return int(average_minutes * 60)

            return 0

        else:
            total_minutes = float(time_str)
            return int(total_minutes * 60)

    except ValueError:
        print(f'Warning: Could not parse time string "{time_str}"')
        return 0

def parse_iso(iso_str: str) -> int:
    if not iso_str:
        return 0
    match = re.search(r'\d+', str(iso_str))
    if match:
        return int(match.group())
    return 0

def parse_temp(temp_str: str) -> int:
    if not temp_str:
        return 20 
    match = re.search(r'\d+', str(temp_str))
    if match:
        return int(match.group())
    return 20

def run_scraper():
    connection = setup_database()
    cursor = connection.cursor()

    session = requests.Session()
    
    # mimicking browser request
    headers = {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
        'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8',
        'Referer': 'https://www.digitaltruth.com/'
    }
    session.headers.update(headers)

    db_boxes = fetch_fp_database()
    
    session.get("https://www.digitaltruth.com/devchart.php")
    response = session.get(LIST_URL, headers=headers)
    soup = BeautifulSoup(response.text, 'html.parser')

    film_entries = []
    for link in soup.find_all('a', href=True):
        href = link['href']
        if href.startswith('search_text.php?Film='): # filter film entries' links in print.php 
            film_entries.append({
                'name': link.text.strip(),
                'href': href
            })

    unique_films = []
    seen_hrefs = set()
    for entry in film_entries:
        if entry['href'] not in seen_hrefs:
            unique_films.append(entry)
            seen_hrefs.add(entry['href'])

    print(f"Found {len(unique_films)} unique films to scrape.")

    for entry in unique_films:
        film_name = entry['name']
        href = entry['href']
        
        print(f'Scraping: {film_name}')
        
        image_cache_135 = {}
        image_cache_120 = {}
        
        scrape_url = DEV_URL + href
        film_res = session.get(scrape_url, headers=headers)
        film_soup = BeautifulSoup(film_res.text, 'html.parser')
        
        table = film_soup.find('table')
        if table:
            rows = table.find_all('tr')[1:]
            for row in rows:
                cols = row.find_all('td')
                if len(cols) >= 9:
                    raw_data = [col.text.strip() for col in cols]
                    
                    specific_film_name = raw_data[0] 
                    if "* User Notes *" in specific_film_name: continue  
                    
                    time_135 = time_to_seconds(raw_data[4])
                    time_120 = time_to_seconds(raw_data[5])

                    if time_135 and specific_film_name not in image_cache_135:
                        image_cache_135[specific_film_name] = find_best_image_match(specific_film_name, "35mm", db_boxes)
                        
                    if time_120 and specific_film_name not in image_cache_120:
                        image_cache_120[specific_film_name] = find_best_image_match(specific_film_name, "120", db_boxes)

                    matched_135 = image_cache_135.get(specific_film_name, "") if time_135 else ""
                    matched_120 = image_cache_120.get(specific_film_name, "") if time_120 else ""

                    if time_135 or time_120:
                        cursor.execute('''
                            INSERT INTO dev_times (
                                film, developer, dilution, iso, temp,
                                time_135, time_120, image_url_135, image_url_120
                            )
                            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                        ''', (
                                specific_film_name, raw_data[1], raw_data[2], parse_iso(raw_data[3]), parse_temp(raw_data[7]), 
                                time_135, time_120, 
                                matched_135, matched_120
                            )
                        )
            connection.commit()
            
        time.sleep(0.25)
        
    connection.close()
    print('Database populated.')

if __name__ == '__main__':
    run_scraper()
