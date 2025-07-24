#!/bin/bash

# VedioFun 服务文件自动下载脚本
# 用于下载Nacos和Sentinel服务文件

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

echo "🚀 VedioFun 服务文件下载脚本"
echo "=================================="

# 创建目录
mkdir -p "$PROJECT_ROOT/vedio-funny-registry/nacos/target"
mkdir -p "$PROJECT_ROOT/vedio-funny-sentinel/lib"

# 下载Nacos Server
NACOS_VERSION="3.0.2"
NACOS_URL="https://github.com/alibaba/nacos/releases/download/${NACOS_VERSION}/nacos-server-${NACOS_VERSION}.tar.gz"
NACOS_TARGET="$PROJECT_ROOT/vedio-funny-registry/nacos/target"

echo "📦 下载 Nacos Server ${NACOS_VERSION}..."
if [ ! -f "$NACOS_TARGET/nacos-server.jar" ]; then
    echo "  从 $NACOS_URL 下载..."
    
    # 下载并解压
    curl -L "$NACOS_URL" -o "/tmp/nacos-server.tar.gz"
    cd /tmp
    tar -xzf nacos-server.tar.gz
    
    # 复制JAR文件
    cp nacos/target/nacos-server.jar "$NACOS_TARGET/"
    
    # 清理临时文件
    rm -rf /tmp/nacos-server.tar.gz /tmp/nacos
    
    echo "  ✅ Nacos Server 下载完成"
else
    echo "  ⏭️  Nacos Server 已存在，跳过下载"
fi

# 下载Sentinel Dashboard
SENTINEL_VERSION="1.8.6"
SENTINEL_URL="https://github.com/alibaba/Sentinel/releases/download/${SENTINEL_VERSION}/sentinel-dashboard-${SENTINEL_VERSION}.jar"
SENTINEL_TARGET="$PROJECT_ROOT/vedio-funny-sentinel/lib"

echo "📦 下载 Sentinel Dashboard ${SENTINEL_VERSION}..."
if [ ! -f "$SENTINEL_TARGET/sentinel-dashboard-${SENTINEL_VERSION}.jar" ]; then
    echo "  从 $SENTINEL_URL 下载..."
    
    curl -L "$SENTINEL_URL" -o "$SENTINEL_TARGET/sentinel-dashboard-${SENTINEL_VERSION}.jar"
    
    echo "  ✅ Sentinel Dashboard 下载完成"
else
    echo "  ⏭️  Sentinel Dashboard 已存在，跳过下载"
fi

# 验证文件
echo ""
echo "🔍 验证下载的文件："
echo "Nacos Server: $(ls -lh "$NACOS_TARGET/nacos-server.jar" 2>/dev/null || echo "❌ 未找到")"
echo "Sentinel Dashboard: $(ls -lh "$SENTINEL_TARGET/sentinel-dashboard-${SENTINEL_VERSION}.jar" 2>/dev/null || echo "❌ 未找到")"

echo ""
echo "🎉 服务文件下载完成！"
echo "🚀 现在可以启动服务了" 