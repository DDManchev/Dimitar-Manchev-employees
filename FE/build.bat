@echo off
echo Building Employee Pair Finder application...

REM Install dependencies
echo Installing dependencies...
call npm install

REM Build the application
echo Building the application...
call npm run build

echo Build completed successfully!
echo You can find the built application in the dist/employee-pair-finder directory.
