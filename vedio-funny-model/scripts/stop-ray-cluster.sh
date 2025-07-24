#!/bin/bash

# =================================================================
# Ray集群停止脚本
# 用途: 停止Ray集群和相关服务
# =================================================================

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

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 停止Ray集群
stop_ray_cluster() {
    log_info "开始停止Ray集群..."
    
    # 获取脚本所在目录的上级目录（项目根目录）
    local script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
    local project_root="$(dirname "$script_dir")"
    
    # 加载ray.env环境
    if [ -f "$project_root/resources/ray.env" ]; then
        source "$project_root/resources/ray.env"
    else
        log_error "找不到ray.env文件: $project_root/resources/ray.env"
        return 1
    fi
    
    # 激活conda环境
    if [ -n "$CONDA_HOME" ] && [ -f "$CONDA_HOME/etc/profile.d/conda.sh" ]; then
        source "$CONDA_HOME/etc/profile.d/conda.sh"
        conda activate "$RAY_ENV_NAME"
    else
        log_error "找不到conda环境或ray.env配置不正确"
        return 1
    fi
    
    # 检查Ray是否在运行
    log_info "检查Ray集群状态..."
    ray_status=$(ray status 2>&1)
    
    if echo "$ray_status" | grep -q "ConnectionError"; then
        log_warning "Ray集群未运行或无法连接"
        return 0
    fi
    
    # 停止Ray集群
    log_info "执行 ray stop 命令..."
    ray_stop_output=$(ray stop 2>&1)
    
    if [ $? -eq 0 ]; then
        log_success "Ray集群停止成功"
        echo "停止输出: $ray_stop_output"
        return 0
    else
        log_error "Ray集群停止失败: $ray_stop_output"
        return 1
    fi
}

# 停止RayLLM服务
stop_rayllm_services() {
    log_info "停止RayLLM服务..."
    
    # 查找RayLLM相关进程
    rayllm_processes=$(ps aux | grep -E "(rayllm|vllm|tgi)" | grep -v grep)
    
    if [ -n "$rayllm_processes" ]; then
        log_info "找到RayLLM进程:"
        echo "$rayllm_processes"
        
        # 停止RayLLM进程
        pkill -f "rayllm"
        pkill -f "vllm"
        pkill -f "tgi"
        
        sleep 2
        
        # 检查是否还有进程在运行
        remaining_processes=$(ps aux | grep -E "(rayllm|vllm|tgi)" | grep -v grep)
        if [ -z "$remaining_processes" ]; then
            log_success "RayLLM服务停止成功"
        else
            log_warning "部分RayLLM进程可能仍在运行"
            echo "剩余进程: $remaining_processes"
        fi
    else
        log_info "未找到运行中的RayLLM服务"
    fi
}

# 清理临时文件
cleanup_temp_files() {
    log_info "清理临时文件..."
    
    # 清理Ray临时目录
    if [ -d "/tmp/ray" ]; then
        log_info "清理 /tmp/ray 目录..."
        rm -rf /tmp/ray/*
        log_success "Ray临时文件清理完成"
    fi
    
    # 清理Ray测试目录
    if [ -d "/tmp/ray-test" ]; then
        log_info "清理 /tmp/ray-test 目录..."
        rm -rf /tmp/ray-test
        log_success "Ray测试目录清理完成"
    fi
    
    # 清理模型缓存
#    if [ -d "/tmp/vedio-funny" ]; then
#        log_info "清理模型缓存目录..."
#        rm -rf /tmp/vedio-funny/*
#        log_success "模型缓存清理完成"
#    fi
}

# 检查端口占用
check_port_usage() {
    log_info "检查Ray相关端口占用情况..."
    
    local ports=(6379 8265 6380 6381 10001 10002)
    
    for port in "${ports[@]}"; do
        if lsof -i :$port >/dev/null 2>&1; then
            log_warning "端口 $port 仍被占用:"
            lsof -i :$port
        else
            log_success "端口 $port 已释放"
        fi
    done
}

# 强制停止（如果需要）
force_stop() {
    log_warning "执行强制停止..."
    
    # 强制杀死所有Ray相关进程
    pkill -9 -f "ray"
    pkill -9 -f "rayllm"
    pkill -9 -f "vllm"
    pkill -9 -f "tgi"
    
    sleep 2
    
    log_info "强制停止完成"
}

# 显示帮助信息
show_help() {
    echo "Ray集群停止脚本"
    echo ""
    echo "用法: $0 [选项]"
    echo ""
    echo "选项:"
    echo "  -h, --help     显示此帮助信息"
    echo "  -f, --force    强制停止所有Ray相关进程"
    echo "  -c, --cleanup  清理临时文件和缓存"
    echo "  -p, --ports    检查端口占用情况"
    echo "  -a, --all      执行完整停止流程（默认）"
    echo ""
    echo "示例:"
    echo "  $0              # 正常停止Ray集群"
    echo "  $0 --force      # 强制停止所有进程"
    echo "  $0 --cleanup    # 只清理临时文件"
}

# 主函数
main() {
    local force_stop_flag=false
    local cleanup_flag=false
    local check_ports_flag=false
    local all_flag=true
    
    # 解析命令行参数
    while [[ $# -gt 0 ]]; do
        case $1 in
            -h|--help)
                show_help
                exit 0
                ;;
            -f|--force)
                force_stop_flag=true
                all_flag=false
                shift
                ;;
            -c|--cleanup)
                cleanup_flag=true
                all_flag=false
                shift
                ;;
            -p|--ports)
                check_ports_flag=true
                all_flag=false
                shift
                ;;
            -a|--all)
                all_flag=true
                shift
                ;;
            *)
                log_error "未知选项: $1"
                show_help
                exit 1
                ;;
        esac
    done
    
    log_info "开始Ray集群停止流程..."
    echo "=================================="
    
    if [ "$all_flag" = true ] || [ "$force_stop_flag" = true ]; then
        # 停止Ray集群
        stop_ray_cluster
        
        # 停止RayLLM服务
        stop_rayllm_services
        
        # 如果需要强制停止
        if [ "$force_stop_flag" = true ]; then
            force_stop
        fi
    fi
    
    if [ "$all_flag" = true ] || [ "$cleanup_flag" = true ]; then
        # 清理临时文件
        cleanup_temp_files
    fi
    
    if [ "$all_flag" = true ] || [ "$check_ports_flag" = true ]; then
        # 检查端口占用
        check_port_usage
    fi
    
    echo "=================================="
    log_success "Ray集群停止流程完成"
}

# 执行主函数
main "$@" 

# =================================================================
# Ray集群停止脚本
# 用途: 停止Ray集群和相关服务
# =================================================================

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

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 停止Ray集群
stop_ray_cluster() {
    log_info "开始停止Ray集群..."
    
    # 获取脚本所在目录的上级目录（项目根目录）
    local script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
    local project_root="$(dirname "$script_dir")"
    
    # 加载ray.env环境
    if [ -f "$project_root/resources/ray.env" ]; then
        source "$project_root/resources/ray.env"
    else
        log_error "找不到ray.env文件: $project_root/resources/ray.env"
        return 1
    fi
    
    # 激活conda环境
    if [ -n "$CONDA_HOME" ] && [ -f "$CONDA_HOME/etc/profile.d/conda.sh" ]; then
        source "$CONDA_HOME/etc/profile.d/conda.sh"
        conda activate "$RAY_ENV_NAME"
    else
        log_error "找不到conda环境或ray.env配置不正确"
        return 1
    fi
    
    # 检查Ray是否在运行
    log_info "检查Ray集群状态..."
    ray_status=$(ray status 2>&1)
    
    if echo "$ray_status" | grep -q "ConnectionError"; then
        log_warning "Ray集群未运行或无法连接"
        return 0
    fi
    
    # 停止Ray集群
    log_info "执行 ray stop 命令..."
    ray_stop_output=$(ray stop 2>&1)
    
    if [ $? -eq 0 ]; then
        log_success "Ray集群停止成功"
        echo "停止输出: $ray_stop_output"
        return 0
    else
        log_error "Ray集群停止失败: $ray_stop_output"
        return 1
    fi
}

# 停止RayLLM服务
stop_rayllm_services() {
    log_info "停止RayLLM服务..."
    
    # 查找RayLLM相关进程
    rayllm_processes=$(ps aux | grep -E "(rayllm|vllm|tgi)" | grep -v grep)
    
    if [ -n "$rayllm_processes" ]; then
        log_info "找到RayLLM进程:"
        echo "$rayllm_processes"
        
        # 停止RayLLM进程
        pkill -f "rayllm"
        pkill -f "vllm"
        pkill -f "tgi"
        
        sleep 2
        
        # 检查是否还有进程在运行
        remaining_processes=$(ps aux | grep -E "(rayllm|vllm|tgi)" | grep -v grep)
        if [ -z "$remaining_processes" ]; then
            log_success "RayLLM服务停止成功"
        else
            log_warning "部分RayLLM进程可能仍在运行"
            echo "剩余进程: $remaining_processes"
        fi
    else
        log_info "未找到运行中的RayLLM服务"
    fi
}

# 清理临时文件
cleanup_temp_files() {
    log_info "清理临时文件..."
    
    # 清理Ray临时目录
    if [ -d "/tmp/ray" ]; then
        log_info "清理 /tmp/ray 目录..."
        rm -rf /tmp/ray/*
        log_success "Ray临时文件清理完成"
    fi
    
    # 清理Ray测试目录
    if [ -d "/tmp/ray-test" ]; then
        log_info "清理 /tmp/ray-test 目录..."
        rm -rf /tmp/ray-test
        log_success "Ray测试目录清理完成"
    fi
    
    # 清理模型缓存
#    if [ -d "/tmp/vedio-funny" ]; then
#        log_info "清理模型缓存目录..."
#        rm -rf /tmp/vedio-funny/*
#        log_success "模型缓存清理完成"
#    fi
}

# 检查端口占用
check_port_usage() {
    log_info "检查Ray相关端口占用情况..."
    
    local ports=(6379 8265 6380 6381 10001 10002)
    
    for port in "${ports[@]}"; do
        if lsof -i :$port >/dev/null 2>&1; then
            log_warning "端口 $port 仍被占用:"
            lsof -i :$port
        else
            log_success "端口 $port 已释放"
        fi
    done
}

# 强制停止（如果需要）
force_stop() {
    log_warning "执行强制停止..."
    
    # 强制杀死所有Ray相关进程
    pkill -9 -f "ray"
    pkill -9 -f "rayllm"
    pkill -9 -f "vllm"
    pkill -9 -f "tgi"
    
    sleep 2
    
    log_info "强制停止完成"
}

# 显示帮助信息
show_help() {
    echo "Ray集群停止脚本"
    echo ""
    echo "用法: $0 [选项]"
    echo ""
    echo "选项:"
    echo "  -h, --help     显示此帮助信息"
    echo "  -f, --force    强制停止所有Ray相关进程"
    echo "  -c, --cleanup  清理临时文件和缓存"
    echo "  -p, --ports    检查端口占用情况"
    echo "  -a, --all      执行完整停止流程（默认）"
    echo ""
    echo "示例:"
    echo "  $0              # 正常停止Ray集群"
    echo "  $0 --force      # 强制停止所有进程"
    echo "  $0 --cleanup    # 只清理临时文件"
}

# 主函数
main() {
    local force_stop_flag=false
    local cleanup_flag=false
    local check_ports_flag=false
    local all_flag=true
    
    # 解析命令行参数
    while [[ $# -gt 0 ]]; do
        case $1 in
            -h|--help)
                show_help
                exit 0
                ;;
            -f|--force)
                force_stop_flag=true
                all_flag=false
                shift
                ;;
            -c|--cleanup)
                cleanup_flag=true
                all_flag=false
                shift
                ;;
            -p|--ports)
                check_ports_flag=true
                all_flag=false
                shift
                ;;
            -a|--all)
                all_flag=true
                shift
                ;;
            *)
                log_error "未知选项: $1"
                show_help
                exit 1
                ;;
        esac
    done
    
    log_info "开始Ray集群停止流程..."
    echo "=================================="
    
    if [ "$all_flag" = true ] || [ "$force_stop_flag" = true ]; then
        # 停止Ray集群
        stop_ray_cluster
        
        # 停止RayLLM服务
        stop_rayllm_services
        
        # 如果需要强制停止
        if [ "$force_stop_flag" = true ]; then
            force_stop
        fi
    fi
    
    if [ "$all_flag" = true ] || [ "$cleanup_flag" = true ]; then
        # 清理临时文件
        cleanup_temp_files
    fi
    
    if [ "$all_flag" = true ] || [ "$check_ports_flag" = true ]; then
        # 检查端口占用
        check_port_usage
    fi
    
    echo "=================================="
    log_success "Ray集群停止流程完成"
}

# 执行主函数
main "$@" 