@echo off
setlocal enabledelayedexpansion

:: Check if nacos directory exists
if not exist nacos (
    echo Nacos not found. Downloading Nacos 3.0.2...
    
    :: Download Nacos
    powershell -Command "& {Invoke-WebRequest -Uri 'https://github.com/alibaba/nacos/releases/download/3.0.2/nacos-server-3.0.2.zip' -OutFile 'nacos.zip'}"
    
    :: Extract Nacos
    powershell -Command "& {Expand-Archive -Path 'nacos.zip' -DestinationPath '.'}"
    
    :: Delete zip file
    del nacos.zip
    
    :: Copy application.properties to conf directory
    copy /Y application.properties nacos\conf\application.properties
    
    echo Nacos setup completed.
) else (
    echo Nacos directory found.
)

:: Start Nacos in standalone mode
cd nacos\bin
start /b call startup.cmd -m standalone
cd ..\..\

:: Wait for Nacos to start (max 60 seconds)
echo Waiting for Nacos to start...
set MAX_RETRIES=30
set RETRY_COUNT=0

:wait_loop
timeout /t 2 /nobreak > nul
curl -s "http://localhost:8848/nacos/v1/console/health" > nul 2>&1
if !errorlevel! equ 0 (
    echo Nacos is now running
    goto import_configs
)
set /a RETRY_COUNT+=1
if !RETRY_COUNT! lss !MAX_RETRIES! (
    echo Waiting... Attempt !RETRY_COUNT! of !MAX_RETRIES!
    goto wait_loop
) else (
    echo Timeout waiting for Nacos to start
    goto end
)

:import_configs
echo Starting configuration import...
call import-configs.cmd

:end
endlocal