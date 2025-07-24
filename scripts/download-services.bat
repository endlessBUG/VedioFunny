@echo off
setlocal

REM VedioFun 服务文件自动下载脚本 (Windows版本)
REM 用于下载Nacos和Sentinel服务文件

echo 🚀 VedioFun 服务文件下载脚本 (Windows)
echo ==================================

REM 获取项目根目录
set "SCRIPT_DIR=%~dp0"
set "PROJECT_ROOT=%SCRIPT_DIR%.."

REM 创建目录
if not exist "%PROJECT_ROOT%\vedio-funny-registry\nacos\target" mkdir "%PROJECT_ROOT%\vedio-funny-registry\nacos\target"
if not exist "%PROJECT_ROOT%\vedio-funny-sentinel\lib" mkdir "%PROJECT_ROOT%\vedio-funny-sentinel\lib"

REM 下载Nacos Server
set "NACOS_VERSION=3.0.2"
set "NACOS_URL=https://github.com/alibaba/nacos/releases/download/%NACOS_VERSION%/nacos-server-%NACOS_VERSION%.tar.gz"
set "NACOS_TARGET=%PROJECT_ROOT%\vedio-funny-registry\nacos\target"

echo 📦 下载 Nacos Server %NACOS_VERSION%...
if not exist "%NACOS_TARGET%\nacos-server.jar" (
    echo   从 %NACOS_URL% 下载...
    
    REM 使用PowerShell下载文件
    powershell -Command "& { Invoke-WebRequest -Uri '%NACOS_URL%' -OutFile '%TEMP%\nacos-server.tar.gz' -UseBasicParsing }"
    
    REM 解压需要7zip或其他工具，这里提供手动指导
    echo   ⚠️  请手动解压 %TEMP%\nacos-server.tar.gz
    echo   ⚠️  并将 nacos/target/nacos-server.jar 复制到 %NACOS_TARGET%\
    echo   ⚠️  或者使用Git Bash运行 scripts/download-services.sh
    
    pause
) else (
    echo   ⏭️  Nacos Server 已存在，跳过下载
)

REM 下载Sentinel Dashboard
set "SENTINEL_VERSION=1.8.6"
set "SENTINEL_URL=https://github.com/alibaba/Sentinel/releases/download/%SENTINEL_VERSION%/sentinel-dashboard-%SENTINEL_VERSION%.jar"
set "SENTINEL_TARGET=%PROJECT_ROOT%\vedio-funny-sentinel\lib"

echo 📦 下载 Sentinel Dashboard %SENTINEL_VERSION%...
if not exist "%SENTINEL_TARGET%\sentinel-dashboard-%SENTINEL_VERSION%.jar" (
    echo   从 %SENTINEL_URL% 下载...
    
    REM 使用PowerShell下载
    powershell -Command "& { Invoke-WebRequest -Uri '%SENTINEL_URL%' -OutFile '%SENTINEL_TARGET%\sentinel-dashboard-%SENTINEL_VERSION%.jar' -UseBasicParsing }"
    
    if exist "%SENTINEL_TARGET%\sentinel-dashboard-%SENTINEL_VERSION%.jar" (
        echo   ✅ Sentinel Dashboard 下载完成
    ) else (
        echo   ❌ Sentinel Dashboard 下载失败
    )
) else (
    echo   ⏭️  Sentinel Dashboard 已存在，跳过下载
)

REM 验证文件
echo.
echo 🔍 验证下载的文件：
if exist "%NACOS_TARGET%\nacos-server.jar" (
    for %%F in ("%NACOS_TARGET%\nacos-server.jar") do echo Nacos Server: %%~nxF - %%~zF bytes
) else (
    echo Nacos Server: ❌ 未找到
)

if exist "%SENTINEL_TARGET%\sentinel-dashboard-%SENTINEL_VERSION%.jar" (
    for %%F in ("%SENTINEL_TARGET%\sentinel-dashboard-%SENTINEL_VERSION%.jar") do echo Sentinel Dashboard: %%~nxF - %%~zF bytes
) else (
    echo Sentinel Dashboard: ❌ 未找到
)

echo.
echo 🎉 服务文件下载完成！
echo 🚀 现在可以启动服务了
echo.
echo 💡 提示：如果Nacos下载失败，请使用Git Bash运行 scripts/download-services.sh
echo.
pause 