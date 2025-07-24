#!/bin/bash

# =================================================================
# 通过API停止Ray集群脚本
# 用途: 通过Spring Boot API停止Ray集群
# =================================================================

# 配置
API_BASE_URL="http://localhost:8080"
MODEL_SERVICE_URL="${API_BASE_URL}/model"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查服务是否运行
check_service() {
    log_info "检查模型服务是否运行..."
    
    local health_response=$(curl -s "${API_BASE_URL}/actuator/health" 2>/dev/null)
    
    if [ $? -eq 0 ] && echo "$health_response" | grep -q "UP"; then
        log_success "模型服务正在运行"
        return 0
    else
        log_error "模型服务未运行"
        return 1
    fi
}

# 停止Ray集群（通过API）
stop_ray_cluster_via_api() {
    log_info "通过API停止Ray集群..."
    
    # 首先检查集群状态
    local status_request='{
        "clusterAddress": "ray://localhost:6379"
    }'
    
    local status_response=$(curl -s -X POST "${MODEL_SERVICE_URL}/ray/cluster-status" \
        -H "Content-Type: application/json" \
        -d "$status_request")
    
    echo "集群状态: $status_response"
    
    # 构建停止请求
    local stop_request='{
        "clusterAddress": "ray://localhost:6379",
        "forceStop": true
    }'
    
    # 注意：这里假设有一个停止集群的API端点
    # 如果没有，我们可以通过其他方式停止
    local stop_response=$(curl -s -X POST "${MODEL_SERVICE_URL}/ray/stop-cluster" \
        -H "Content-Type: application/json" \
        -d "$stop_request" 2>/dev/null)
    
    if [ $? -eq 0 ] && [ -n "$stop_response" ]; then
        log_success "通过API停止集群成功"
        echo "停止响应: $stop_response"
    else
        log_error "通过API停止集群失败或API端点不存在"
        return 1
    fi
}

# 停止RayLLM服务（通过API）
stop_rayllm_via_api() {
    log_info "通过API停止RayLLM服务..."
    
    # 构建停止RayLLM请求
    local stop_request='{
        "modelName": "all",
        "forceStop": true
    }'
    
    # 注意：这里假设有一个停止RayLLM的API端点
    local stop_response=$(curl -s -X POST "${MODEL_SERVICE_URL}/rayllm/stop" \
        -H "Content-Type: application/json" \
        -d "$stop_request" 2>/dev/null)
    
    if [ $? -eq 0 ] && [ -n "$stop_response" ]; then
        log_success "通过API停止RayLLM成功"
        echo "停止响应: $stop_response"
    else
        log_error "通过API停止RayLLM失败或API端点不存在"
        return 1
    fi
}

# 主函数
main() {
    log_info "开始通过API停止Ray集群..."
    echo "=================================="
    
    # 检查服务状态
    if check_service; then
        # 停止Ray集群
        stop_ray_cluster_via_api
        
        # 停止RayLLM服务
        stop_rayllm_via_api
        
        log_info "API停止流程完成"
        log_info "注意：如果API端点不存在，请使用 ./scripts/stop-ray-cluster.sh 进行本地停止"
    else
        log_error "模型服务未运行，无法通过API停止"
        log_info "请使用 ./scripts/stop-ray-cluster.sh 进行本地停止"
    fi
    
    echo "=================================="
}

# 执行主函数
main 

# =================================================================
# 通过API停止Ray集群脚本
# 用途: 通过Spring Boot API停止Ray集群
# =================================================================

# 配置
API_BASE_URL="http://localhost:8080"
MODEL_SERVICE_URL="${API_BASE_URL}/model"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查服务是否运行
check_service() {
    log_info "检查模型服务是否运行..."
    
    local health_response=$(curl -s "${API_BASE_URL}/actuator/health" 2>/dev/null)
    
    if [ $? -eq 0 ] && echo "$health_response" | grep -q "UP"; then
        log_success "模型服务正在运行"
        return 0
    else
        log_error "模型服务未运行"
        return 1
    fi
}

# 停止Ray集群（通过API）
stop_ray_cluster_via_api() {
    log_info "通过API停止Ray集群..."
    
    # 首先检查集群状态
    local status_request='{
        "clusterAddress": "ray://localhost:6379"
    }'
    
    local status_response=$(curl -s -X POST "${MODEL_SERVICE_URL}/ray/cluster-status" \
        -H "Content-Type: application/json" \
        -d "$status_request")
    
    echo "集群状态: $status_response"
    
    # 构建停止请求
    local stop_request='{
        "clusterAddress": "ray://localhost:6379",
        "forceStop": true
    }'
    
    # 注意：这里假设有一个停止集群的API端点
    # 如果没有，我们可以通过其他方式停止
    local stop_response=$(curl -s -X POST "${MODEL_SERVICE_URL}/ray/stop-cluster" \
        -H "Content-Type: application/json" \
        -d "$stop_request" 2>/dev/null)
    
    if [ $? -eq 0 ] && [ -n "$stop_response" ]; then
        log_success "通过API停止集群成功"
        echo "停止响应: $stop_response"
    else
        log_error "通过API停止集群失败或API端点不存在"
        return 1
    fi
}

# 停止RayLLM服务（通过API）
stop_rayllm_via_api() {
    log_info "通过API停止RayLLM服务..."
    
    # 构建停止RayLLM请求
    local stop_request='{
        "modelName": "all",
        "forceStop": true
    }'
    
    # 注意：这里假设有一个停止RayLLM的API端点
    local stop_response=$(curl -s -X POST "${MODEL_SERVICE_URL}/rayllm/stop" \
        -H "Content-Type: application/json" \
        -d "$stop_request" 2>/dev/null)
    
    if [ $? -eq 0 ] && [ -n "$stop_response" ]; then
        log_success "通过API停止RayLLM成功"
        echo "停止响应: $stop_response"
    else
        log_error "通过API停止RayLLM失败或API端点不存在"
        return 1
    fi
}

# 主函数
main() {
    log_info "开始通过API停止Ray集群..."
    echo "=================================="
    
    # 检查服务状态
    if check_service; then
        # 停止Ray集群
        stop_ray_cluster_via_api
        
        # 停止RayLLM服务
        stop_rayllm_via_api
        
        log_info "API停止流程完成"
        log_info "注意：如果API端点不存在，请使用 ./scripts/stop-ray-cluster.sh 进行本地停止"
    else
        log_error "模型服务未运行，无法通过API停止"
        log_info "请使用 ./scripts/stop-ray-cluster.sh 进行本地停止"
    fi
    
    echo "=================================="
}

# 执行主函数
main 