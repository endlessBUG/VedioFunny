@echo off
setlocal enabledelayedexpansion
set ZIPKIN_VERSION=2.24.3
set ZIPKIN_URL=https://repo1.maven.org/maven2/io/zipkin/zipkin-server/%ZIPKIN_VERSION%/zipkin-server-%ZIPKIN_VERSION%-exec.jar
set SCRIPT_DIR=%~dp0
set LIB_DIR=%SCRIPT_DIR%\..\lib
if not exist "%LIB_DIR%" mkdir "%LIB_DIR%"
set ZIPKIN_JAR=%LIB_DIR%\zipkin.jar
echo Downloading Zipkin %ZIPKIN_VERSION%...
echo From: %ZIPKIN_URL%
echo To: %ZIPKIN_JAR%
powershell -Command "& {[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri '%ZIPKIN_URL%' -OutFile '%ZIPKIN_JAR%'}"
if %ERRORLEVEL% NEQ 0 (
    echo Failed to download Zipkin
    exit /b 1
)
echo Successfully downloaded Zipkin to: %ZIPKIN_JAR%