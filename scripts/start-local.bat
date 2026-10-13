@echo off
cd /d C:\Users\Nishaant\myproject

taskkill /F /IM java.exe /T >NUL 2>&1

set DB_HOST=localhost
set DB_PORT=3306
set DB_NAME=whats_in_my_kitchen
set DB_USER=root
set DB_PASSWORD=
set DB_USE_SSL=false
set PORT=8080

"C:\Program Files\Java\jdk-25.0.4.1\bin\java.exe" --add-modules jdk.httpserver -cp "C:\Users\Nishaant\myproject\build\classes;C:\Users\Nishaant\myproject\lib\mysql-connector-j-8.4.0.jar" com.whatsinmykitchen.Main
