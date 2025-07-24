@echo off
setlocal enabledelayedexpansion

echo ===============================================
echo VedioFun Tracing Module Quick Start
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

rem Check if Zipkin exists
if not exist lib\zipkin.jar (
    echo Downloading Zipkin Server...
    
    rem Download using PowerShell
    powershell -Command "& {$ProgressPreference = 'SilentlyContinue'; Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/io/zipkin/zipkin-server/2.24.3/zipkin-server-2.24.3-exec.jar' -OutFile 'lib\zipkin.jar'}"
    
    if not exist lib\zipkin.jar (
        echo ERROR: Failed to download Zipkin Server
        pause
        exit /b 1
    )
    
    echo Download completed successfully.
) else (
    echo Zipkin Server JAR found in lib directory.
)

echo.
echo Starting Zipkin Server...
echo Access URL: http://localhost:9411
echo Press Ctrl+C to stop
echo.

rem Start Zipkin Server
start "Zipkin Server" java -jar lib/zipkin.jar

echo Zipkin Server is starting...
echo.
echo ===============================================
echo Usage Instructions:
echo 1. Access Zipkin UI at: http://localhost:9411
echo 2. Add the following to your application.yml:
echo.
echo spring:
echo   zipkin:
echo     base-url: http://localhost:9411
echo     sender:
echo       type: web
echo   sleuth:
echo     sampler:
echo       probability: 1.0
echo ===============================================

pause 