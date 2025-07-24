#!/bin/bash

# =================================================================
# Ray集群启动脚本
# 用途: 在节点上启动Ray集群（主节点或工作节点）
# 作者: VedioFun Team
# =================================================================

# 配置
RAY_PORT=${RAY_PORT:-10001}
DASHBOARD_PORT=${DASHBOARD_PORT:-8265}
OBJECT_STORE_PORT=${OBJECT_STORE_PORT:-6379}
GCS_SERVER_PORT=${GCS_SERVER_PORT:-6379}
MIN_WORKER_PORT=${MIN_WORKER_PORT:-10002}
MAX_WORKER_PORT=${MAX_WORKER_PORT:-19999}
NUM_CPUS=${NUM_CPUS:-4}
NUM_GPUS=${NUM_GPUS:-0}
MEMORY=${MEMORY:-"8G"}

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🚀 Ray集群启动脚本${NC}"
echo "================================"

# 检查参数
if [ "$1" = "head" ]; then
    NODE_TYPE="head"
    echo -e "${YELLOW}启动Ray主节点集群${NC}"
elif [ "$1" = "worker" ]; then
    NODE_TYPE="worker"
    CLUSTER_ADDRESS=$2
    if [ -z "$CLUSTER_ADDRESS" ]; then
        echo -e "${RED}错误: 工作节点需要指定集群地址${NC}"
        echo "用法: $0 worker <cluster_address>"
        exit 1
    fi
    echo -e "${YELLOW}启动Ray工作节点 - 集群: ${CLUSTER_ADDRESS}${NC}"
else
    echo -e "${RED}错误: 无效的节点类型${NC}"
    echo "用法: $0 {head|worker} [cluster_address]"
    exit 1
fi

# 检查Ray环境
check_ray_environment() {
    echo -e "\n${YELLOW}📡 检查Ray环境...${NC}"
    
    # 检查Ray是否安装
    if ! command -v ray &> /dev/null; then
        echo -e "${RED}❌ Ray未安装，请先安装Ray${NC}"
        return 1
    fi
    
    # 检查Ray版本
    RAY_VERSION=$(ray --version 2>/dev/null | head -1)
    echo -e "${GREEN}✅ Ray版本: ${RAY_VERSION}${NC}"
    
    # 检查Python环境
    if ! command -v python &> /dev/null; then
        echo -e "${RED}❌ Python未安装${NC}"
        return 1
    fi
    
    PYTHON_VERSION=$(python --version 2>&1)
    echo -e "${GREEN}✅ Python版本: ${PYTHON_VERSION}${NC}"
    
    return 0
}

# 启动Ray主节点
start_ray_head() {
    echo -e "\n${YELLOW}🎯 启动Ray主节点...${NC}"
    
    # 检查端口是否被占用
    if lsof -Pi :$RAY_PORT -sTCP:LISTEN -t >/dev/null ; then
        echo -e "${RED}❌ 端口 ${RAY_PORT} 已被占用${NC}"
        return 1
    fi
    
    if lsof -Pi :$DASHBOARD_PORT -sTCP:LISTEN -t >/dev/null ; then
        echo -e "${RED}❌ 端口 ${DASHBOARD_PORT} 已被占用${NC}"
        return 1
    fi
    
    # 构建Ray启动命令
    RAY_CMD="ray start --head"
    RAY_CMD="$RAY_CMD --port=$RAY_PORT"
    RAY_CMD="$RAY_CMD --dashboard-port=$DASHBOARD_PORT"
    RAY_CMD="$RAY_CMD --object-store-port=$OBJECT_STORE_PORT"
    RAY_CMD="$RAY_CMD --gcs-server-port=$GCS_SERVER_PORT"
    RAY_CMD="$RAY_CMD --min-worker-port=$MIN_WORKER_PORT"
    RAY_CMD="$RAY_CMD --max-worker-port=$MAX_WORKER_PORT"
    RAY_CMD="$RAY_CMD --num-cpus=$NUM_CPUS"
    
    if [ "$NUM_GPUS" -gt 0 ]; then
        RAY_CMD="$RAY_CMD --num-gpus=$NUM_GPUS"
    fi
    
    RAY_CMD="$RAY_CMD --memory=$MEMORY"
    RAY_CMD="$RAY_CMD --include-dashboard=true"
    RAY_CMD="$RAY_CMD --dashboard-host=0.0.0.0"
    RAY_CMD="$RAY_CMD --temp-dir=/tmp/ray"
    
    echo -e "${BLUE}执行命令: ${RAY_CMD}${NC}"
    
    # 启动Ray主节点
    if eval $RAY_CMD; then
        echo -e "${GREEN}✅ Ray主节点启动成功${NC}"
        echo -e "${GREEN}📊 Ray Dashboard: http://localhost:${DASHBOARD_PORT}${NC}"
        echo -e "${GREEN}🔗 Ray集群地址: ray://localhost:${RAY_PORT}${NC}"
        return 0
    else
        echo -e "${RED}❌ Ray主节点启动失败${NC}"
        return 1
    fi
}

# 启动Ray工作节点
start_ray_worker() {
    echo -e "\n${YELLOW}🔧 启动Ray工作节点...${NC}"
    
    # 检查端口是否被占用
    if lsof -Pi :$RAY_PORT -sTCP:LISTEN -t >/dev/null ; then
        echo -e "${RED}❌ 端口 ${RAY_PORT} 已被占用${NC}"
        return 1
    fi
    
    # 构建Ray启动命令
    RAY_CMD="ray start"
    RAY_CMD="$RAY_CMD --address=$CLUSTER_ADDRESS"
    RAY_CMD="$RAY_CMD --port=$RAY_PORT"
    RAY_CMD="$RAY_CMD --min-worker-port=$MIN_WORKER_PORT"
    RAY_CMD="$RAY_CMD --max-worker-port=$MAX_WORKER_PORT"
    RAY_CMD="$RAY_CMD --num-cpus=$NUM_CPUS"
    
    if [ "$NUM_GPUS" -gt 0 ]; then
        RAY_CMD="$RAY_CMD --num-gpus=$NUM_GPUS"
    fi
    
    RAY_CMD="$RAY_CMD --memory=$MEMORY"
    RAY_CMD="$RAY_CMD --temp-dir=/tmp/ray"
    
    echo -e "${BLUE}执行命令: ${RAY_CMD}${NC}"
    
    # 启动Ray工作节点
    if eval $RAY_CMD; then
        echo -e "${GREEN}✅ Ray工作节点启动成功${NC}"
        echo -e "${GREEN}🔗 已加入集群: ${CLUSTER_ADDRESS}${NC}"
        return 0
    else
        echo -e "${RED}❌ Ray工作节点启动失败${NC}"
        return 1
    fi
}

# 检查集群状态
check_cluster_status() {
    echo -e "\n${YELLOW}📊 检查集群状态...${NC}"
    
    if [ "$NODE_TYPE" = "head" ]; then
        # 主节点检查
        if ray status; then
            echo -e "${GREEN}✅ Ray集群运行正常${NC}"
            return 0
        else
            echo -e "${RED}❌ Ray集群状态异常${NC}"
            return 1
        fi
    else
        # 工作节点检查
        if ray status --address=$CLUSTER_ADDRESS; then
            echo -e "${GREEN}✅ 工作节点连接正常${NC}"
            return 0
        else
            echo -e "${RED}❌ 工作节点连接异常${NC}"
            return 1
        fi
    fi
}

# 主函数
main() {
    # 检查Ray环境
    if ! check_ray_environment; then
        exit 1
    fi
    
    # 根据节点类型启动
    if [ "$NODE_TYPE" = "head" ]; then
        if start_ray_head; then
            # 等待一下让服务完全启动
            sleep 3
            check_cluster_status
        else
            exit 1
        fi
    else
        if start_ray_worker; then
            # 等待一下让服务完全启动
            sleep 3
            check_cluster_status
        else
            exit 1
        fi
    fi
    
    echo -e "\n${GREEN}🎉 Ray集群启动完成！${NC}"
}

# 执行主函数
main "$@" 

# =================================================================
# Ray集群启动脚本
# 用途: 在节点上启动Ray集群（主节点或工作节点）
# 作者: VedioFun Team
# =================================================================

# 配置
RAY_PORT=${RAY_PORT:-10001}
DASHBOARD_PORT=${DASHBOARD_PORT:-8265}
OBJECT_STORE_PORT=${OBJECT_STORE_PORT:-6379}
GCS_SERVER_PORT=${GCS_SERVER_PORT:-6379}
MIN_WORKER_PORT=${MIN_WORKER_PORT:-10002}
MAX_WORKER_PORT=${MAX_WORKER_PORT:-19999}
NUM_CPUS=${NUM_CPUS:-4}
NUM_GPUS=${NUM_GPUS:-0}
MEMORY=${MEMORY:-"8G"}

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🚀 Ray集群启动脚本${NC}"
echo "================================"

# 检查参数
if [ "$1" = "head" ]; then
    NODE_TYPE="head"
    echo -e "${YELLOW}启动Ray主节点集群${NC}"
elif [ "$1" = "worker" ]; then
    NODE_TYPE="worker"
    CLUSTER_ADDRESS=$2
    if [ -z "$CLUSTER_ADDRESS" ]; then
        echo -e "${RED}错误: 工作节点需要指定集群地址${NC}"
        echo "用法: $0 worker <cluster_address>"
        exit 1
    fi
    echo -e "${YELLOW}启动Ray工作节点 - 集群: ${CLUSTER_ADDRESS}${NC}"
else
    echo -e "${RED}错误: 无效的节点类型${NC}"
    echo "用法: $0 {head|worker} [cluster_address]"
    exit 1
fi

# 检查Ray环境
check_ray_environment() {
    echo -e "\n${YELLOW}📡 检查Ray环境...${NC}"
    
    # 检查Ray是否安装
    if ! command -v ray &> /dev/null; then
        echo -e "${RED}❌ Ray未安装，请先安装Ray${NC}"
        return 1
    fi
    
    # 检查Ray版本
    RAY_VERSION=$(ray --version 2>/dev/null | head -1)
    echo -e "${GREEN}✅ Ray版本: ${RAY_VERSION}${NC}"
    
    # 检查Python环境
    if ! command -v python &> /dev/null; then
        echo -e "${RED}❌ Python未安装${NC}"
        return 1
    fi
    
    PYTHON_VERSION=$(python --version 2>&1)
    echo -e "${GREEN}✅ Python版本: ${PYTHON_VERSION}${NC}"
    
    return 0
}

# 启动Ray主节点
start_ray_head() {
    echo -e "\n${YELLOW}🎯 启动Ray主节点...${NC}"
    
    # 检查端口是否被占用
    if lsof -Pi :$RAY_PORT -sTCP:LISTEN -t >/dev/null ; then
        echo -e "${RED}❌ 端口 ${RAY_PORT} 已被占用${NC}"
        return 1
    fi
    
    if lsof -Pi :$DASHBOARD_PORT -sTCP:LISTEN -t >/dev/null ; then
        echo -e "${RED}❌ 端口 ${DASHBOARD_PORT} 已被占用${NC}"
        return 1
    fi
    
    # 构建Ray启动命令
    RAY_CMD="ray start --head"
    RAY_CMD="$RAY_CMD --port=$RAY_PORT"
    RAY_CMD="$RAY_CMD --dashboard-port=$DASHBOARD_PORT"
    RAY_CMD="$RAY_CMD --object-store-port=$OBJECT_STORE_PORT"
    RAY_CMD="$RAY_CMD --gcs-server-port=$GCS_SERVER_PORT"
    RAY_CMD="$RAY_CMD --min-worker-port=$MIN_WORKER_PORT"
    RAY_CMD="$RAY_CMD --max-worker-port=$MAX_WORKER_PORT"
    RAY_CMD="$RAY_CMD --num-cpus=$NUM_CPUS"
    
    if [ "$NUM_GPUS" -gt 0 ]; then
        RAY_CMD="$RAY_CMD --num-gpus=$NUM_GPUS"
    fi
    
    RAY_CMD="$RAY_CMD --memory=$MEMORY"
    RAY_CMD="$RAY_CMD --include-dashboard=true"
    RAY_CMD="$RAY_CMD --dashboard-host=0.0.0.0"
    RAY_CMD="$RAY_CMD --temp-dir=/tmp/ray"
    
    echo -e "${BLUE}执行命令: ${RAY_CMD}${NC}"
    
    # 启动Ray主节点
    if eval $RAY_CMD; then
        echo -e "${GREEN}✅ Ray主节点启动成功${NC}"
        echo -e "${GREEN}📊 Ray Dashboard: http://localhost:${DASHBOARD_PORT}${NC}"
        echo -e "${GREEN}🔗 Ray集群地址: ray://localhost:${RAY_PORT}${NC}"
        return 0
    else
        echo -e "${RED}❌ Ray主节点启动失败${NC}"
        return 1
    fi
}

# 启动Ray工作节点
start_ray_worker() {
    echo -e "\n${YELLOW}🔧 启动Ray工作节点...${NC}"
    
    # 检查端口是否被占用
    if lsof -Pi :$RAY_PORT -sTCP:LISTEN -t >/dev/null ; then
        echo -e "${RED}❌ 端口 ${RAY_PORT} 已被占用${NC}"
        return 1
    fi
    
    # 构建Ray启动命令
    RAY_CMD="ray start"
    RAY_CMD="$RAY_CMD --address=$CLUSTER_ADDRESS"
    RAY_CMD="$RAY_CMD --port=$RAY_PORT"
    RAY_CMD="$RAY_CMD --min-worker-port=$MIN_WORKER_PORT"
    RAY_CMD="$RAY_CMD --max-worker-port=$MAX_WORKER_PORT"
    RAY_CMD="$RAY_CMD --num-cpus=$NUM_CPUS"
    
    if [ "$NUM_GPUS" -gt 0 ]; then
        RAY_CMD="$RAY_CMD --num-gpus=$NUM_GPUS"
    fi
    
    RAY_CMD="$RAY_CMD --memory=$MEMORY"
    RAY_CMD="$RAY_CMD --temp-dir=/tmp/ray"
    
    echo -e "${BLUE}执行命令: ${RAY_CMD}${NC}"
    
    # 启动Ray工作节点
    if eval $RAY_CMD; then
        echo -e "${GREEN}✅ Ray工作节点启动成功${NC}"
        echo -e "${GREEN}🔗 已加入集群: ${CLUSTER_ADDRESS}${NC}"
        return 0
    else
        echo -e "${RED}❌ Ray工作节点启动失败${NC}"
        return 1
    fi
}

# 检查集群状态
check_cluster_status() {
    echo -e "\n${YELLOW}📊 检查集群状态...${NC}"
    
    if [ "$NODE_TYPE" = "head" ]; then
        # 主节点检查
        if ray status; then
            echo -e "${GREEN}✅ Ray集群运行正常${NC}"
            return 0
        else
            echo -e "${RED}❌ Ray集群状态异常${NC}"
            return 1
        fi
    else
        # 工作节点检查
        if ray status --address=$CLUSTER_ADDRESS; then
            echo -e "${GREEN}✅ 工作节点连接正常${NC}"
            return 0
        else
            echo -e "${RED}❌ 工作节点连接异常${NC}"
            return 1
        fi
    fi
}

# 主函数
main() {
    # 检查Ray环境
    if ! check_ray_environment; then
        exit 1
    fi
    
    # 根据节点类型启动
    if [ "$NODE_TYPE" = "head" ]; then
        if start_ray_head; then
            # 等待一下让服务完全启动
            sleep 3
            check_cluster_status
        else
            exit 1
        fi
    else
        if start_ray_worker; then
            # 等待一下让服务完全启动
            sleep 3
            check_cluster_status
        else
            exit 1
        fi
    fi
    
    echo -e "\n${GREEN}🎉 Ray集群启动完成！${NC}"
}

# 执行主函数
main "$@" 