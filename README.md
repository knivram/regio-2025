# Regionalmeisterschaft 2024

## Run the app

You can start the application by first installing the latest release from the `releases` folder.
After that you can go into the folder `C:\Program Files\AthliTrack\` there will be a `.exe` which then can be executed.

The app trys to connect to a mysql database with the following credentials:
- Host: `localhost`
- Port: `3306`
- Database: `regio`
- User: `root`
- Password: `password`

If there is no such database, the app will fall back to a local in memory h2 database which won't persist any data.