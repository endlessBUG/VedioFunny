#!/bin/bash

# VedioFun Nacos 配置导入工具 (Shell 版本)
# 将配置文件导入到 Nacos 配置中心

# 配置参数
NACOS_SERVER="http://localhost:8848"
NACOS_USERNAME="nacos"
NACOS_PASSWORD="nacos"
GROUP="DEFAULT_GROUP"
CONFIG_DIR="configs"

# 解析命令行参数
while [[ $# -gt 0 ]]; do
    case "$1" in
        -server)
            NACOS_SERVER="$2"
            shift 2
            ;;
        -dir)
            CONFIG_DIR="$2"
            shift 2
            ;;
        *)
            shift
            ;;
    esac
done

echo "========================================"
echo "VedioFun Nacos 配置导入工具"
echo "========================================"
echo

# 检查配置目录
if [ ! -d "$CONFIG_DIR" ]; then
    echo "[ERROR] 配置目录不存在: $CONFIG_DIR"
    exit 1
fi

# 检查 curl 是否可用
if ! command -v curl &> /dev/null; then
    echo "[ERROR] curl 命令未找到，请安装 curl"
    exit 1
fi

echo "[INFO] 检查 Nacos 服务状态..."
if ! curl -s "${NACOS_SERVER}/nacos/v1/console/health" > /dev/null; then
    echo "[ERROR] 无法连接到 Nacos 服务: $NACOS_SERVER"
    echo "[ERROR] 请确保 Nacos 服务正在运行"
    exit 1
fi
echo "[INFO] Nacos 服务正常运行"

echo "[INFO] 获取 Nacos 访问令牌..."

# 登录获取访问令牌
LOGIN_RESPONSE=$(curl -s -X POST "${NACOS_SERVER}/nacos/v1/auth/login" \
     -d "username=${NACOS_USERNAME}&password=${NACOS_PASSWORD}")

# 提取访问令牌
ACCESS_TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)

if [ -z "$ACCESS_TOKEN" ]; then
    echo "[ERROR] 无法获取访问令牌"
    exit 1
fi

echo "[INFO] 成功获取访问令牌"

# 导入配置文件
echo "[INFO] 开始导入配置文件..."
SUCCESS_COUNT=0
TOTAL_COUNT=0

# 处理所有 YAML 文件
for config_file in "${CONFIG_DIR}"/*.yml; do
    if [ ! -f "$config_file" ]; then
        continue
    fi
    
    TOTAL_COUNT=$((TOTAL_COUNT + 1))
    DATA_ID=$(basename "$config_file")
    echo "[INFO] 导入配置: $DATA_ID"
    
    # 上传配置到 Nacos
    RESPONSE=$(curl -s -X POST "${NACOS_SERVER}/nacos/v1/cs/configs" \
         -d "dataId=$DATA_ID" \
         -d "group=$GROUP" \
         -d "tenant=" \
         -d "type=yaml" \
         -d "accessToken=$ACCESS_TOKEN" \
         --data-urlencode "content@$config_file")
    
    # 检查响应
    if [ "$RESPONSE" == "true" ]; then
        echo "[INFO] ✓ 成功导入: $DATA_ID"
        SUCCESS_COUNT=$((SUCCESS_COUNT + 1))
    else
        echo "[ERROR] ✗ 导入失败: $DATA_ID"
    fi
done

echo
echo "========================================"
echo "导入完成! 成功: $SUCCESS_COUNT/$TOTAL_COUNT"
echo "========================================"
echo
echo "[INFO] Nacos 控制台: ${NACOS_SERVER}/nacos"
echo "[INFO] 用户名: $NACOS_USERNAME"
echo "[INFO] 密码: $NACOS_PASSWORD"
echo
echo "[INFO] 配置使用说明:"
echo "[INFO] 在每个微服务的 application.yml 中添加以下内容:"
echo "[WARN] spring:"
echo "[WARN]   config:"
echo "[WARN]     import:"
echo "[WARN]       - optional:nacos:vedio-funny-common.yml?group=DEFAULT_GROUP&refreshEnabled=true"
echo 