#!/bin/bash

# =================================================================
# VedioFun Model Service - Ray环境快速设置脚本
# 用途: 快速设置和验证Ray环境
# 作者: VedioFun Team
# =================================================================

set -e

# 颜色定义
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# 配置文件路径
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CONFIG_DIR="$SCRIPT_DIR/../config"
ENV_CONFIG="$CONFIG_DIR/miniconda-env.sh"

log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

# 检查Miniconda是否已安装
check_miniconda() {
    if [[ -f "$ENV_CONFIG" ]]; then
        log_info "发现环境配置文件: $ENV_CONFIG"
        source "$ENV_CONFIG"
        return 0
    else
        log_warning "未找到Miniconda环境配置"
        log_warning "请先运行: $SCRIPT_DIR/install-miniconda.sh"
        return 1
    fi
}

# 验证Ray环境
verify_ray_environment() {
    log_info "验证Ray环境..."
    
    if check_miniconda; then
        if command -v conda &> /dev/null; then
            activate_ray_env
            
            # 检查Python版本
            PYTHON_VER=$(python --version)
            log_info "Python版本: $PYTHON_VER"
            
            # 检查Ray版本
            if python -c "import ray" &> /dev/null; then
                RAY_VER=$(python -c "import ray; print(ray.__version__)")
                log_success "Ray版本: $RAY_VER"
                return 0
            else
                log_warning "Ray未正确安装"
                return 1
            fi
        else
            log_warning "Conda命令不可用"
            return 1
        fi
    else
        return 1
    fi
}

# 自动安装Ray（如果需要）
auto_install_ray() {
    log_info "自动安装Ray环境..."
    
    if check_miniconda; then
        source "$ENV_CONFIG"
        activate_ray_env
        
        # 检查Ray是否已安装
        if ! python -c "import ray" &> /dev/null; then
            log_info "安装Ray..."
            
            # 设置非交互式安装
            export CONDA_ALWAYS_YES=true
            
            # 安装Ray
            pip install "ray[default]" --quiet
            
            # 验证安装
            if python -c "import ray" &> /dev/null; then
                RAY_VER=$(python -c "import ray; print(ray.__version__)")
                log_success "Ray安装成功，版本: $RAY_VER"
                return 0
            else
                log_warning "Ray安装失败"
                return 1
            fi
        else
            RAY_VER=$(python -c "import ray; print(ray.__version__)")
            log_success "Ray已安装，版本: $RAY_VER"
            return 0
        fi
    else
        log_warning "Miniconda环境未找到，请先安装Miniconda"
        return 1
    fi
}

# 显示环境状态
show_environment_status() {
    echo ""
    log_info "==================== Ray环境状态 ===================="
    
    if verify_ray_environment; then
        show_env_info
        echo ""
        log_success "✅ Ray环境准备就绪!"
        echo ""
        log_info "🚀 可以开始Ray集群部署了"
    else
        echo ""
        log_warning "❌ Ray环境未准备就绪"
        echo ""
        log_info "🔧 安装步骤:"
        echo "   1. chmod +x $SCRIPT_DIR/install-miniconda.sh"
        echo "   2. $SCRIPT_DIR/install-miniconda.sh"
        echo "   3. source ~/.bashrc (或重启终端)"
        echo "   4. $SCRIPT_DIR/setup-ray-env.sh"
    fi
    
    echo ""
    log_info "===================================================="
}

# 主函数
main() {
    log_info "检查Ray环境设置..."
    
    # 如果是自动化模式，尝试自动安装Ray
    if [[ "${AUTO_INSTALL:-false}" == "true" ]]; then
        log_info "自动化模式：尝试安装Ray..."
        if auto_install_ray; then
            log_success "Ray环境已准备就绪（自动化安装）"
        else
            log_warning "Ray自动安装失败"
            exit 1
        fi
    else
        show_environment_status
    fi
}

# 如果直接运行此脚本
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi 