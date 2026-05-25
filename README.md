# The DARKROOM

**The DARKROOM** serves as an assitant for home development of analog film. Unlike traditional timers, the timer in the app 
is designed with the film development process in mind. A unified timer, which is set in advance and automatically transitions between 
different stages of the process, allows the user to concentrate on crucial tasks such as chemistry handling and agitation, without 
having to needlessly fiddle with timer controls.

In addition to a timer, the app provides acess to a large database with established development times for different film stocks,
developers, dilutions, push/pull processing, etc. The user is free to save different recipes and can quickly start a development process
with a selected recipe. 

## Getting started

The app implements its own API on the backend, which is hosted locally. Without the API, the user loses access to the database
of recipes and only the timer itself remains functional.

To start the API, make sure you are in the root directory of this project and run:

```
source src/backend/backend_venv/bin/activate
python src/backend/api.py
```

The app binds to port **7493**.  
If you plan to run the app in an emulator, you will have to replace the base URL in `ApiClient.kt` from `localhost` to `10.0.2.2`.  
If you plan to run the app on a physically connected device, to enable communication between the app and the API running on your computer, run this command:

```
adb reverse tcp:7493 tcp:7493
```

Now you can open the project in Android Studio, build, and run on an emulator or a physical device.

## Future work

The app is work-in-progress. There are many more features that could expand the functionality even further:
* Editing the parameters and names of saved recipes.
* Adding different processes (e.g., C-41, E-6, etc.) and allowing user-defined process configurations.
* Tracking fixer/developer exhaustion.
* A more comprehensive search with filtering, both for discovering new recipes and browsing stored recipes.