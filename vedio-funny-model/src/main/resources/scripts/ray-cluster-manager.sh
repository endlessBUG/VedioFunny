#!/bin/bash

# =================================================================
# Ray集群管理脚本
# 用途: 管理Ray集群的启动、停止、状态检查等操作
# 作者: VedioFun Team
# =================================================================

# 配置
RAY_PORT=${RAY_PORT:-10001}
DASHBOARD_PORT=${DASHBOARD_PORT:-8265}
CLUSTER_ADDRESS=${CLUSTER_ADDRESS:-"ray://localhost:10001"}

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🔧 Ray集群管理脚本${NC}"
echo "================================"

# 显示帮助信息
show_help() {
    echo "用法: $0 [命令] [选项]"
    echo ""
    echo "命令:"
    echo "  start-head    启动Ray主节点"
    echo "  start-worker  启动Ray工作节点"
    echo "  stop          停止Ray集群"
    echo "  status        检查Ray集群状态"
    echo "  info          显示Ray集群信息"
    echo "  dashboard     打开Ray Dashboard"
    echo "  logs          查看Ray日志"
    echo "  clean         清理Ray临时文件"
    echo "  help          显示帮助信息"
    echo ""
    echo "选项:"
    echo "  --port PORT           指定Ray端口 (默认: 10001)"
    echo "  --dashboard-port PORT 指定Dashboard端口 (默认: 8265)"
    echo "  --address ADDRESS     指定集群地址 (用于工作节点)"
    echo "  --cpus NUM           指定CPU数量 (默认: 4)"
    echo "  --gpus NUM           指定GPU数量 (默认: 0)"
    echo "  --memory SIZE        指定内存大小 (默认: 8G)"
    echo ""
    echo "示例:"
    echo "  $0 start-head                    # 启动主节点"
    echo "  $0 start-worker --address ray://192.168.1.100:10001  # 启动工作节点"
    echo "  $0 status                        # 检查状态"
    echo "  $0 stop                          # 停止集群"
}

# 解析命令行参数
parse_args() {
    COMMAND=""
    WORKER_ADDRESS=""
    
    while [[ $# -gt 0 ]]; do
        case $1 in
            start-head|start-worker|stop|status|info|dashboard|logs|clean|help)
                COMMAND="$1"
                shift
                ;;
            --port)
                RAY_PORT="$2"
                shift 2
                ;;
            --dashboard-port)
                DASHBOARD_PORT="$2"
                shift 2
                ;;
            --address)
                WORKER_ADDRESS="$2"
                shift 2
                ;;
            --cpus)
                NUM_CPUS="$2"
                shift 2
                ;;
            --gpus)
                NUM_GPUS="$2"
                shift 2
                ;;
            --memory)
                MEMORY="$2"
                shift 2
                ;;
            *)
                echo -e "${RED}未知参数: $1${NC}"
                show_help
                exit 1
                ;;
        esac
    done
    
    if [ -z "$COMMAND" ]; then
        echo -e "${RED}请指定命令${NC}"
        show_help
        exit 1
    fi
}

# 检查Ray环境
check_ray_environment() {
    if ! command -v ray &> /dev/null; then
        echo -e "${RED}❌ Ray未安装，请先安装Ray${NC}"
        return 1
    fi
    
    if ! command -v python &> /dev/null; then
        echo -e "${RED}❌ Python未安装${NC}"
        return 1
    fi
    
    return 0
}

# 启动Ray主节点
start_head() {
    echo -e "${YELLOW}🎯 启动Ray主节点...${NC}"
    
    # 检查端口是否被占用
    if lsof -Pi :$RAY_PORT -sTCP:LISTEN -t >/dev/null ; then
        echo -e "${RED}❌ 端口 ${RAY_PORT} 已被占用${NC}"
        return 1
    fi
    
    if lsof -Pi :$DASHBOARD_PORT -sTCP:LISTEN -t >/dev/null ; then
        echo -e "${RED}❌ 端口 ${DASHBOARD_PORT} 已被占用${NC}"
        return 1
    fi
    
    # 构建启动命令
    RAY_CMD="ray start --head"
    RAY_CMD="$RAY_CMD --port=$RAY_PORT"
    RAY_CMD="$RAY_CMD --dashboard-port=$DASHBOARD_PORT"
    RAY_CMD="$RAY_CMD --include-dashboard=true"
    RAY_CMD="$RAY_CMD --dashboard-host=0.0.0.0"
    RAY_CMD="$RAY_CMD --temp-dir=/tmp/ray"
    
    if [ ! -z "$NUM_CPUS" ]; then
        RAY_CMD="$RAY_CMD --num-cpus=$NUM_CPUS"
    fi
    
    if [ ! -z "$NUM_GPUS" ] && [ "$NUM_GPUS" -gt 0 ]; then
        RAY_CMD="$RAY_CMD --num-gpus=$NUM_GPUS"
    fi
    
    if [ ! -z "$MEMORY" ]; then
        RAY_CMD="$RAY_CMD --memory=$MEMORY"
    fi
    
    echo -e "${BLUE}执行命令: ${RAY_CMD}${NC}"
    
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
start_worker() {
    if [ -z "$WORKER_ADDRESS" ]; then
        echo -e "${RED}❌ 工作节点需要指定集群地址${NC}"
        echo "用法: $0 start-worker --address <cluster_address>"
        return 1
    fi
    
    echo -e "${YELLOW}🔧 启动Ray工作节点 - 集群: ${WORKER_ADDRESS}${NC}"
    
    # 检查端口是否被占用
    if lsof -Pi :$RAY_PORT -sTCP:LISTEN -t >/dev/null ; then
        echo -e "${RED}❌ 端口 ${RAY_PORT} 已被占用${NC}"
        return 1
    fi
    
    # 构建启动命令
    RAY_CMD="ray start"
    RAY_CMD="$RAY_CMD --address=$WORKER_ADDRESS"
    RAY_CMD="$RAY_CMD --port=$RAY_PORT"
    RAY_CMD="$RAY_CMD --temp-dir=/tmp/ray"
    
    if [ ! -z "$NUM_CPUS" ]; then
        RAY_CMD="$RAY_CMD --num-cpus=$NUM_CPUS"
    fi
    
    if [ ! -z "$NUM_GPUS" ] && [ "$NUM_GPUS" -gt 0 ]; then
        RAY_CMD="$RAY_CMD --num-gpus=$NUM_GPUS"
    fi
    
    if [ ! -z "$MEMORY" ]; then
        RAY_CMD="$RAY_CMD --memory=$MEMORY"
    fi
    
    echo -e "${BLUE}执行命令: ${RAY_CMD}${NC}"
    
    if eval $RAY_CMD; then
        echo -e "${GREEN}✅ Ray工作节点启动成功${NC}"
        echo -e "${GREEN}🔗 已加入集群: ${WORKER_ADDRESS}${NC}"
        return 0
    else
        echo -e "${RED}❌ Ray工作节点启动失败${NC}"
        return 1
    fi
}

# 停止Ray集群
stop_cluster() {
    echo -e "${YELLOW}🛑 停止Ray集群...${NC}"
    
    if ray stop; then
        echo -e "${GREEN}✅ Ray集群已停止${NC}"
        return 0
    else
        echo -e "${RED}❌ 停止Ray集群失败${NC}"
        return 1
    fi
}

# 检查Ray集群状态
check_status() {
    echo -e "${YELLOW}📊 检查Ray集群状态...${NC}"
    
    if ray status; then
        echo -e "${GREEN}✅ Ray集群运行正常${NC}"
        return 0
    else
        echo -e "${RED}❌ Ray集群状态异常${NC}"
        return 1
    fi
}

# 显示Ray集群信息
show_info() {
    echo -e "${YELLOW}ℹ️  Ray集群信息...${NC}"
    
    echo -e "${BLUE}Ray版本:${NC}"
    ray --version 2>/dev/null || echo "无法获取Ray版本"
    
    echo -e "\n${BLUE}集群状态:${NC}"
    ray status 2>/dev/null || echo "无法获取集群状态"
    
    echo -e "\n${BLUE}进程信息:${NC}"
    ps aux | grep ray | grep -v grep || echo "未找到Ray进程"
    
    echo -e "\n${BLUE}端口使用情况:${NC}"
    lsof -i :$RAY_PORT 2>/dev/null || echo "端口 $RAY_PORT 未使用"
    lsof -i :$DASHBOARD_PORT 2>/dev/null || echo "端口 $DASHBOARD_PORT 未使用"
}

# 打开Ray Dashboard
open_dashboard() {
    echo -e "${YELLOW}🌐 打开Ray Dashboard...${NC}"
    
    DASHBOARD_URL="http://localhost:${DASHBOARD_PORT}"
    
    if curl -s "$DASHBOARD_URL" > /dev/null 2>&1; then
        echo -e "${GREEN}✅ Ray Dashboard 正在运行: ${DASHBOARD_URL}${NC}"
        
        # 尝试打开浏览器
        if command -v open &> /dev/null; then
            open "$DASHBOARD_URL"
        elif command -v xdg-open &> /dev/null; then
            xdg-open "$DASHBOARD_URL"
        else
            echo "请手动打开浏览器访问: $DASHBOARD_URL"
        fi
    else
        echo -e "${RED}❌ Ray Dashboard 未运行${NC}"
        return 1
    fi
}

# 查看Ray日志
show_logs() {
    echo -e "${YELLOW}📋 查看Ray日志...${NC}"
    
    LOG_DIR="/tmp/ray/session_latest/logs"
    
    if [ -d "$LOG_DIR" ]; then
        echo -e "${BLUE}日志目录: ${LOG_DIR}${NC}"
        echo -e "${BLUE}最新日志文件:${NC}"
        ls -la "$LOG_DIR" | head -10
        
        echo -e "\n${BLUE}最新日志内容:${NC}"
        if [ -f "$LOG_DIR/raylet.out" ]; then
            echo "=== Raylet 日志 ==="
            tail -20 "$LOG_DIR/raylet.out"
        fi
        
        if [ -f "$LOG_DIR/raylet.err" ]; then
            echo -e "\n=== Raylet 错误日志 ==="
            tail -10 "$LOG_DIR/raylet.err"
        fi
        
        if [ -f "$LOG_DIR/dashboard.log" ]; then
            echo -e "\n=== Dashboard 日志 ==="
            tail -10 "$LOG_DIR/dashboard.log"
        fi
    else
        echo -e "${RED}❌ 未找到Ray日志目录${NC}"
        return 1
    fi
}

# 清理Ray临时文件
clean_temp() {
    echo -e "${YELLOW}🧹 清理Ray临时文件...${NC}"
    
    TEMP_DIR="/tmp/ray"
    
    if [ -d "$TEMP_DIR" ]; then
        echo -e "${BLUE}清理目录: ${TEMP_DIR}${NC}"
        
        # 停止Ray集群
        ray stop > /dev/null 2>&1
        
        # 删除临时文件
        if rm -rf "$TEMP_DIR"; then
            echo -e "${GREEN}✅ Ray临时文件清理完成${NC}"
            return 0
        else
            echo -e "${RED}❌ 清理Ray临时文件失败${NC}"
            return 1
        fi
    else
        echo -e "${YELLOW}⚠️  Ray临时目录不存在${NC}"
        return 0
    fi
}

# 主函数
main() {
    # 解析命令行参数
    parse_args "$@"
    
    # 检查Ray环境
    if ! check_ray_environment; then
        exit 1
    fi
    
    # 执行命令
    case "$COMMAND" in
        start-head)
            start_head
            ;;
        start-worker)
            start_worker
            ;;
        stop)
            stop_cluster
            ;;
        status)
            check_status
            ;;
        info)
            show_info
            ;;
        dashboard)
            open_dashboard
            ;;
        logs)
            show_logs
            ;;
        clean)
            clean_temp
            ;;
        help)
            show_help
            ;;
        *)
            echo -e "${RED}未知命令: $COMMAND${NC}"
            show_help
            exit 1
            ;;
    esac
}

# 执行主函数
main "$@" 

# =================================================================
# Ray集群管理脚本
# 用途: 管理Ray集群的启动、停止、状态检查等操作
# 作者: VedioFun Team
# =================================================================

# 配置
RAY_PORT=${RAY_PORT:-10001}
DASHBOARD_PORT=${DASHBOARD_PORT:-8265}
CLUSTER_ADDRESS=${CLUSTER_ADDRESS:-"ray://localhost:10001"}

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🔧 Ray集群管理脚本${NC}"
echo "================================"

# 显示帮助信息
show_help() {
    echo "用法: $0 [命令] [选项]"
    echo ""
    echo "命令:"
    echo "  start-head    启动Ray主节点"
    echo "  start-worker  启动Ray工作节点"
    echo "  stop          停止Ray集群"
    echo "  status        检查Ray集群状态"
    echo "  info          显示Ray集群信息"
    echo "  dashboard     打开Ray Dashboard"
    echo "  logs          查看Ray日志"
    echo "  clean         清理Ray临时文件"
    echo "  help          显示帮助信息"
    echo ""
    echo "选项:"
    echo "  --port PORT           指定Ray端口 (默认: 10001)"
    echo "  --dashboard-port PORT 指定Dashboard端口 (默认: 8265)"
    echo "  --address ADDRESS     指定集群地址 (用于工作节点)"
    echo "  --cpus NUM           指定CPU数量 (默认: 4)"
    echo "  --gpus NUM           指定GPU数量 (默认: 0)"
    echo "  --memory SIZE        指定内存大小 (默认: 8G)"
    echo ""
    echo "示例:"
    echo "  $0 start-head                    # 启动主节点"
    echo "  $0 start-worker --address ray://192.168.1.100:10001  # 启动工作节点"
    echo "  $0 status                        # 检查状态"
    echo "  $0 stop                          # 停止集群"
}

# 解析命令行参数
parse_args() {
    COMMAND=""
    WORKER_ADDRESS=""
    
    while [[ $# -gt 0 ]]; do
        case $1 in
            start-head|start-worker|stop|status|info|dashboard|logs|clean|help)
                COMMAND="$1"
                shift
                ;;
            --port)
                RAY_PORT="$2"
                shift 2
                ;;
            --dashboard-port)
                DASHBOARD_PORT="$2"
                shift 2
                ;;
            --address)
                WORKER_ADDRESS="$2"
                shift 2
                ;;
            --cpus)
                NUM_CPUS="$2"
                shift 2
                ;;
            --gpus)
                NUM_GPUS="$2"
                shift 2
                ;;
            --memory)
                MEMORY="$2"
                shift 2
                ;;
            *)
                echo -e "${RED}未知参数: $1${NC}"
                show_help
                exit 1
                ;;
        esac
    done
    
    if [ -z "$COMMAND" ]; then
        echo -e "${RED}请指定命令${NC}"
        show_help
        exit 1
    fi
}

# 检查Ray环境
check_ray_environment() {
    if ! command -v ray &> /dev/null; then
        echo -e "${RED}❌ Ray未安装，请先安装Ray${NC}"
        return 1
    fi
    
    if ! command -v python &> /dev/null; then
        echo -e "${RED}❌ Python未安装${NC}"
        return 1
    fi
    
    return 0
}

# 启动Ray主节点
start_head() {
    echo -e "${YELLOW}🎯 启动Ray主节点...${NC}"
    
    # 检查端口是否被占用
    if lsof -Pi :$RAY_PORT -sTCP:LISTEN -t >/dev/null ; then
        echo -e "${RED}❌ 端口 ${RAY_PORT} 已被占用${NC}"
        return 1
    fi
    
    if lsof -Pi :$DASHBOARD_PORT -sTCP:LISTEN -t >/dev/null ; then
        echo -e "${RED}❌ 端口 ${DASHBOARD_PORT} 已被占用${NC}"
        return 1
    fi
    
    # 构建启动命令
    RAY_CMD="ray start --head"
    RAY_CMD="$RAY_CMD --port=$RAY_PORT"
    RAY_CMD="$RAY_CMD --dashboard-port=$DASHBOARD_PORT"
    RAY_CMD="$RAY_CMD --include-dashboard=true"
    RAY_CMD="$RAY_CMD --dashboard-host=0.0.0.0"
    RAY_CMD="$RAY_CMD --temp-dir=/tmp/ray"
    
    if [ ! -z "$NUM_CPUS" ]; then
        RAY_CMD="$RAY_CMD --num-cpus=$NUM_CPUS"
    fi
    
    if [ ! -z "$NUM_GPUS" ] && [ "$NUM_GPUS" -gt 0 ]; then
        RAY_CMD="$RAY_CMD --num-gpus=$NUM_GPUS"
    fi
    
    if [ ! -z "$MEMORY" ]; then
        RAY_CMD="$RAY_CMD --memory=$MEMORY"
    fi
    
    echo -e "${BLUE}执行命令: ${RAY_CMD}${NC}"
    
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
start_worker() {
    if [ -z "$WORKER_ADDRESS" ]; then
        echo -e "${RED}❌ 工作节点需要指定集群地址${NC}"
        echo "用法: $0 start-worker --address <cluster_address>"
        return 1
    fi
    
    echo -e "${YELLOW}🔧 启动Ray工作节点 - 集群: ${WORKER_ADDRESS}${NC}"
    
    # 检查端口是否被占用
    if lsof -Pi :$RAY_PORT -sTCP:LISTEN -t >/dev/null ; then
        echo -e "${RED}❌ 端口 ${RAY_PORT} 已被占用${NC}"
        return 1
    fi
    
    # 构建启动命令
    RAY_CMD="ray start"
    RAY_CMD="$RAY_CMD --address=$WORKER_ADDRESS"
    RAY_CMD="$RAY_CMD --port=$RAY_PORT"
    RAY_CMD="$RAY_CMD --temp-dir=/tmp/ray"
    
    if [ ! -z "$NUM_CPUS" ]; then
        RAY_CMD="$RAY_CMD --num-cpus=$NUM_CPUS"
    fi
    
    if [ ! -z "$NUM_GPUS" ] && [ "$NUM_GPUS" -gt 0 ]; then
        RAY_CMD="$RAY_CMD --num-gpus=$NUM_GPUS"
    fi
    
    if [ ! -z "$MEMORY" ]; then
        RAY_CMD="$RAY_CMD --memory=$MEMORY"
    fi
    
    echo -e "${BLUE}执行命令: ${RAY_CMD}${NC}"
    
    if eval $RAY_CMD; then
        echo -e "${GREEN}✅ Ray工作节点启动成功${NC}"
        echo -e "${GREEN}🔗 已加入集群: ${WORKER_ADDRESS}${NC}"
        return 0
    else
        echo -e "${RED}❌ Ray工作节点启动失败${NC}"
        return 1
    fi
}

# 停止Ray集群
stop_cluster() {
    echo -e "${YELLOW}🛑 停止Ray集群...${NC}"
    
    if ray stop; then
        echo -e "${GREEN}✅ Ray集群已停止${NC}"
        return 0
    else
        echo -e "${RED}❌ 停止Ray集群失败${NC}"
        return 1
    fi
}

# 检查Ray集群状态
check_status() {
    echo -e "${YELLOW}📊 检查Ray集群状态...${NC}"
    
    if ray status; then
        echo -e "${GREEN}✅ Ray集群运行正常${NC}"
        return 0
    else
        echo -e "${RED}❌ Ray集群状态异常${NC}"
        return 1
    fi
}

# 显示Ray集群信息
show_info() {
    echo -e "${YELLOW}ℹ️  Ray集群信息...${NC}"
    
    echo -e "${BLUE}Ray版本:${NC}"
    ray --version 2>/dev/null || echo "无法获取Ray版本"
    
    echo -e "\n${BLUE}集群状态:${NC}"
    ray status 2>/dev/null || echo "无法获取集群状态"
    
    echo -e "\n${BLUE}进程信息:${NC}"
    ps aux | grep ray | grep -v grep || echo "未找到Ray进程"
    
    echo -e "\n${BLUE}端口使用情况:${NC}"
    lsof -i :$RAY_PORT 2>/dev/null || echo "端口 $RAY_PORT 未使用"
    lsof -i :$DASHBOARD_PORT 2>/dev/null || echo "端口 $DASHBOARD_PORT 未使用"
}

# 打开Ray Dashboard
open_dashboard() {
    echo -e "${YELLOW}🌐 打开Ray Dashboard...${NC}"
    
    DASHBOARD_URL="http://localhost:${DASHBOARD_PORT}"
    
    if curl -s "$DASHBOARD_URL" > /dev/null 2>&1; then
        echo -e "${GREEN}✅ Ray Dashboard 正在运行: ${DASHBOARD_URL}${NC}"
        
        # 尝试打开浏览器
        if command -v open &> /dev/null; then
            open "$DASHBOARD_URL"
        elif command -v xdg-open &> /dev/null; then
            xdg-open "$DASHBOARD_URL"
        else
            echo "请手动打开浏览器访问: $DASHBOARD_URL"
        fi
    else
        echo -e "${RED}❌ Ray Dashboard 未运行${NC}"
        return 1
    fi
}

# 查看Ray日志
show_logs() {
    echo -e "${YELLOW}📋 查看Ray日志...${NC}"
    
    LOG_DIR="/tmp/ray/session_latest/logs"
    
    if [ -d "$LOG_DIR" ]; then
        echo -e "${BLUE}日志目录: ${LOG_DIR}${NC}"
        echo -e "${BLUE}最新日志文件:${NC}"
        ls -la "$LOG_DIR" | head -10
        
        echo -e "\n${BLUE}最新日志内容:${NC}"
        if [ -f "$LOG_DIR/raylet.out" ]; then
            echo "=== Raylet 日志 ==="
            tail -20 "$LOG_DIR/raylet.out"
        fi
        
        if [ -f "$LOG_DIR/raylet.err" ]; then
            echo -e "\n=== Raylet 错误日志 ==="
            tail -10 "$LOG_DIR/raylet.err"
        fi
        
        if [ -f "$LOG_DIR/dashboard.log" ]; then
            echo -e "\n=== Dashboard 日志 ==="
            tail -10 "$LOG_DIR/dashboard.log"
        fi
    else
        echo -e "${RED}❌ 未找到Ray日志目录${NC}"
        return 1
    fi
}

# 清理Ray临时文件
clean_temp() {
    echo -e "${YELLOW}🧹 清理Ray临时文件...${NC}"
    
    TEMP_DIR="/tmp/ray"
    
    if [ -d "$TEMP_DIR" ]; then
        echo -e "${BLUE}清理目录: ${TEMP_DIR}${NC}"
        
        # 停止Ray集群
        ray stop > /dev/null 2>&1
        
        # 删除临时文件
        if rm -rf "$TEMP_DIR"; then
            echo -e "${GREEN}✅ Ray临时文件清理完成${NC}"
            return 0
        else
            echo -e "${RED}❌ 清理Ray临时文件失败${NC}"
            return 1
        fi
    else
        echo -e "${YELLOW}⚠️  Ray临时目录不存在${NC}"
        return 0
    fi
}

# 主函数
main() {
    # 解析命令行参数
    parse_args "$@"
    
    # 检查Ray环境
    if ! check_ray_environment; then
        exit 1
    fi
    
    # 执行命令
    case "$COMMAND" in
        start-head)
            start_head
            ;;
        start-worker)
            start_worker
            ;;
        stop)
            stop_cluster
            ;;
        status)
            check_status
            ;;
        info)
            show_info
            ;;
        dashboard)
            open_dashboard
            ;;
        logs)
            show_logs
            ;;
        clean)
            clean_temp
            ;;
        help)
            show_help
            ;;
        *)
            echo -e "${RED}未知命令: $COMMAND${NC}"
            show_help
            exit 1
            ;;
    esac
}

# 执行主函数
main "$@" 