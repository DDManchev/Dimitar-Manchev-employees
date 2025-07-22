@echo off
echo Starting Employee Pair Finder application...

REM Install dependencies if needed
if not exist node_modules (
  echo Installing dependencies...
  call npm install
)

REM Start the application
echo Starting the development server...
call npm start

echo Server started. Access the application at http://localhost:4200
