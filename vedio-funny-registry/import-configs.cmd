@echo off
setlocal enabledelayedexpansion

REM VedioFun Nacos Configuration Import Tool (CMD Version)
REM Import configuration files to Nacos configuration center

REM Configuration parameters
set NACOS_SERVER=http://localhost:8848
set NACOS_USERNAME=nacos
set NACOS_PASSWORD=nacos
set GROUP=DEFAULT_GROUP
set CONFIG_DIR=configs

REM Parse command line arguments
:parse_args
if "%~1"=="" goto start_import
if "%~1"=="-server" (
    set NACOS_SERVER=%~2
    shift
    shift
    goto parse_args
)
if "%~1"=="-dir" (
    set CONFIG_DIR=%~2
    shift
    shift
    goto parse_args
)
shift
goto parse_args

:start_import
echo ========================================
echo VedioFun Nacos Configuration Import Tool
echo ========================================
echo.

REM Check configuration directory
if not exist "%CONFIG_DIR%" (
    echo [ERROR] Configuration directory does not exist: %CONFIG_DIR%
    pause
    exit /b 1
)

REM Check if curl is available
where curl >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] curl command not found, please install curl or use Windows 10 1803+
    pause
    exit /b 1
)

echo [INFO] Checking Nacos service status...
curl -s "%NACOS_SERVER%/nacos/v1/console/health" >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Cannot connect to Nacos service: %NACOS_SERVER%
    echo [ERROR] Please ensure Nacos service is running
    pause
    exit /b 1
)
echo [INFO] Nacos service is running

echo [INFO] Getting Nacos access token...

REM Create temporary files for storing response
set TEMP_FILE=%TEMP%\nacos_response_%RANDOM%.txt
set TOKEN_FILE=%TEMP%\nacos_token_%RANDOM%.txt

REM Login to get access token
curl -s -X POST "%NACOS_SERVER%/nacos/v1/auth/login" ^
     -d "username=%NACOS_USERNAME%&password=%NACOS_PASSWORD%" ^
     -o "%TEMP_FILE%" 2>nul

if %errorlevel% neq 0 (
    echo [ERROR] Failed to get access token
    if exist "%TEMP_FILE%" del "%TEMP_FILE%"
    pause
    exit /b 1
)

REM Parse access token (simple string extraction)
for /f "tokens=2 delims=:" %%a in ('findstr "accessToken" "%TEMP_FILE%"') do (
    set TOKEN_RAW=%%a
)

REM Clean token string (remove quotes and commas)
set ACCESS_TOKEN=!TOKEN_RAW:"=!
set ACCESS_TOKEN=!ACCESS_TOKEN:,=!
set ACCESS_TOKEN=!ACCESS_TOKEN: =!

if "!ACCESS_TOKEN!"=="" (
    echo [ERROR] Cannot parse access token
    if exist "%TEMP_FILE%" del "%TEMP_FILE%"
    pause
    exit /b 1
)

echo [INFO] Successfully obtained access token

REM Import configuration files
echo [INFO] Starting to import configuration files...
set SUCCESS_COUNT=0
set TOTAL_COUNT=0

for %%f in ("%CONFIG_DIR%\*.yml") do (
    set /a TOTAL_COUNT+=1
    set DATA_ID=%%~nxf
    echo [INFO] Importing configuration: !DATA_ID!
    
    REM Create temporary file for configuration content
    set CONTENT_FILE=%TEMP%\config_content_%RANDOM%.txt
    copy "%%f" "!CONTENT_FILE!" >nul 2>&1
    
    REM Use curl to upload configuration
    curl -s -X POST "%NACOS_SERVER%/nacos/v1/cs/configs" ^
         -d "dataId=!DATA_ID!" ^
         -d "group=%GROUP%" ^
         -d "tenant=" ^
         -d "type=yaml" ^
         -d "accessToken=!ACCESS_TOKEN!" ^
         --data-urlencode "content@!CONTENT_FILE!" ^
         -o "%TEMP_FILE%" 2>nul
    
    REM Check response
    set RESPONSE=
    for /f %%r in ("%TEMP_FILE%") do set RESPONSE=%%r
    
    if "!RESPONSE!"=="true" (
        echo [INFO] ✓ Successfully imported: !DATA_ID!
        set /a SUCCESS_COUNT+=1
    ) else (
        echo [ERROR] ✗ Failed to import: !DATA_ID!
    )
    
    REM Clean temporary files
    if exist "!CONTENT_FILE!" del "!CONTENT_FILE!"
)

REM Clean temporary files
if exist "%TEMP_FILE%" del "%TEMP_FILE%"
if exist "%TOKEN_FILE%" del "%TOKEN_FILE%"

echo.
echo ========================================
echo Import completed! Success: %SUCCESS_COUNT%/%TOTAL_COUNT%
echo ========================================
echo.
echo [INFO] Nacos Console: %NACOS_SERVER%/nacos
echo [INFO] Username: %NACOS_USERNAME%
echo [INFO] Password: %NACOS_PASSWORD%
echo.
echo [INFO] Configuration Usage:
echo [INFO] Add the following to each microservice's application.yml:
echo [WARN] spring:
echo [WARN]   config:
echo [WARN]     import:
echo [WARN]       - optional:nacos:vedio-funny-common.yml?group=DEFAULT_GROUP^&refreshEnabled=true
echo.

pause
exit /b 0

:show_help
echo Usage: import-configs.cmd [options]
echo.
echo Options:
echo   -server ^<url^>    Specify Nacos server address (default: http://localhost:8848)
echo   -dir ^<path^>      Specify configuration directory (default: configs)
echo.
echo Examples:
echo   import-configs.cmd
echo   import-configs.cmd -server http://192.168.1.100:8848
echo   import-configs.cmd -dir my-configs
echo.
pause
exit /b 0 