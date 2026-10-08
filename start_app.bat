@echo off
echo =========================================
echo    Starting TokenQueue Full-Stack App
echo =========================================
echo.

echo [1/2] Starting Spring Boot Backend...
cd backend
set "PATH=%CD%\apache-maven-3.9.6\bin;%PATH%"
start cmd /k "mvn spring-boot:run"
cd ..

echo [2/2] Starting React Frontend...
cd frontend
start cmd /k "npm run dev"
cd ..

echo.
echo Both servers are starting up in separate terminal windows!
echo Once they are ready, your frontend will be available at:
echo http://localhost:5173
echo.
pause
