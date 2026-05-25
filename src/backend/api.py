from fastapi import FastAPI, HTTPException
import uvicorn
import sqlite3
from typing import Optional

app = FastAPI(title='Film Dev Assistant API')
DB_FILE = 'devchart.db'

def get_db_connection():
    connection = sqlite3.connect(DB_FILE)
    connection.row_factory = sqlite3.Row
    return connection

@app.get('/api/v1/times/search')
def get_development_times(
    q: Optional[str] = None
):
    """Search endpoint. 
    Processes an arbitrary query to allow searching across multiple criterions. (film stock, developer, dilution).
    """
    connection = get_db_connection()
    cursor = connection.cursor()

    query = 'SELECT * FROM dev_times WHERE 1=1'
    params = []

    if q:
        terms = q.strip().split()
        for term in terms:
            query += ' AND (film LIKE ? OR developer LIKE ? OR dilution LIKE ?)'
            params.extend([f'%{term}%', f'%{term}%', f'%{term}%'])
    else:
        connection.close()
        return {"results": [], "length": 0}

    cursor.execute(query, params)
    rows = cursor.fetchall()
    connection.close()

    if not rows:
        raise HTTPException(status_code=404, detail='No development times found.')
    
    results = []
    for row in rows:
        results.append({
            "id": row["id"],
            "filmStock": row['film'] or "",
            "developer": row['developer'] or "",
            "dilution": row['dilution'] or "",
            "iso": row['iso'] or 0,
            "temp": row['temp'] or 20,
            "time135": row['time_135'] if row['time_135'] else 0,
            "time120": row['time_120'] if row['time_120'] else 0,
            "imageUrl135": row['image_url_135'] or "",
            "imageUrl120": row['image_url_120'] or ""
        })
    
    return {"results": results, "length": len(results)}

@app.get('/api/v1/times/random')
def get_random_development_times(limit: int = 20):
    """Random endpoint"""
    connection = get_db_connection()
    cursor = connection.cursor()

    query = 'SELECT * FROM dev_times ORDER BY RANDOM() LIMIT ?'
    cursor.execute(query, (limit,))
    rows = cursor.fetchall()
    connection.close()

    if not rows:
        raise HTTPException(status_code=404, detail='No development times found.')

    results = []
    for row in rows:
        results.append({
            "id": row["id"],
            "filmStock": row['film'] or "",
            "developer": row['developer'] or "",
            "dilution": row['dilution'] or "",
            "iso": row['iso'] or 0,
            "temp": row['temp'] or 20,
            "time135": row['time_135'] if row['time_135'] else 0,
            "time120": row['time_120'] if row['time_120'] else 0,
            "imageUrl135": row['image_url_135'] or "",
            "imageUrl120": row['image_url_120'] or ""
        })
    
    return {"results": results, "length": len(results)}

if __name__ == '__main__':
#    response = get_development_times("Adox CHS")
#    print(response['data'])    
#    print([entry['time_135'] for entry in response['data']])
    uvicorn.run(app, host='0.0.0.0', port=7493)
