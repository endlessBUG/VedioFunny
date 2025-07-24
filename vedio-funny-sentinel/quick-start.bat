@echo off
echo ===============================================
echo Sentinel Dashboard Quick Start
echo ===============================================

rem Check Java
java -version
if %errorlevel% neq 0 (
    echo ERROR: Java not found, please install Java 17+
    pause
    exit /b 1
)

rem Check and create lib directory
if not exist lib (
    mkdir lib
)

rem Check if Sentinel Dashboard exists
if not exist lib\sentinel-dashboard-1.8.6.jar (
    echo Downloading Sentinel Dashboard 1.8.6...
    
    rem Download using PowerShell
    powershell -Command "& {$ProgressPreference = 'SilentlyContinue'; Invoke-WebRequest -Uri 'https://github.com/alibaba/Sentinel/releases/download/1.8.6/sentinel-dashboard-1.8.6.jar' -OutFile 'lib\sentinel-dashboard-1.8.6.jar'}"
    
    if not exist lib\sentinel-dashboard-1.8.6.jar (
        echo ERROR: Failed to download Sentinel Dashboard
        pause
        exit /b 1
    )
    
    echo Download completed successfully.
) else (
    echo Sentinel Dashboard JAR found in lib directory.
)

echo.
echo Starting Sentinel Dashboard...
echo Access URL: http://localhost:8084
echo Default username/password: sentinel/sentinel
echo Press Ctrl+C to stop
echo.

rem Start Sentinel Dashboard
java -Dserver.port=8084 -Dcsp.sentinel.dashboard.server=localhost:8084 -jar lib/sentinel-dashboard-1.8.6.jar

pause 