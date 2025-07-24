@echo off
setlocal enabledelayedexpansion
set SCRIPT_DIR=%~dp0
set LIB_DIR=%SCRIPT_DIR%\..\lib
set ZIPKIN_JAR=%LIB_DIR%\zipkin.jar
if not exist "%ZIPKIN_JAR%" (
    echo Zipkin jar not found. Downloading...
    call "%SCRIPT_DIR%\download-zipkin.bat"
    if !ERRORLEVEL! NEQ 0 (
        echo Failed to download Zipkin
        exit /b 1
    )
)
java -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo Java is not installed or not in PATH
    exit /b 1
)
set STORAGE_TYPE=mem
set QUERY_PORT=9411
echo Starting Zipkin server...
echo Storage: Memory
echo Port: 9411
echo UI will be available at: http://localhost:9411
java -jar "%ZIPKIN_JAR%"