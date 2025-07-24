#!/bin/bash

# =================================================================
# VedioFun Model Service - Miniconda安装脚本
# 用途: 为Ray部署预安装Miniconda环境
# 作者: VedioFun Team
# =================================================================

set -e  # 遇到错误立即退出

# 颜色定义
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

# 脚本目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# 加载Ray环境变量配置
ENV_FILE="$SCRIPT_DIR/../resources/ray.env"
if [[ -f "$ENV_FILE" ]]; then
    log_info "加载Ray环境配置文件: $ENV_FILE"
    source "$ENV_FILE"
else
    log_warning "未找到Ray环境配置文件 $ENV_FILE，使用默认配置"
fi

# 配置变量（使用环境变量或默认值）
MINICONDA_VERSION="latest"
INSTALL_DIR="${CONDA_HOME:-$HOME/miniconda3}"
DOWNLOAD_DIR="/tmp"
CONDA_ENV_NAME="${CONDA_ENV_NAME:-ray-env}"
PYTHON_VERSION="${PYTHON_VERSION:-3.12}"

# 检测操作系统和架构
detect_system() {
    log_info "检测系统信息..."

    OS=""
    ARCH=""

    # 检测操作系统
    if [[ "$OSTYPE" == "linux-gnu"* ]]; then
        OS="Linux"
    elif [[ "$OSTYPE" == "darwin"* ]]; then
        OS="MacOSX"
    else
        log_error "不支持的操作系统: $OSTYPE"
        exit 1
    fi

    # 检测架构
    if [[ $(uname -m) == "x86_64" ]]; then
        ARCH="x86_64"
    elif [[ $(uname -m) == "arm64" ]] || [[ $(uname -m) == "aarch64" ]]; then
        ARCH="arm64"
    else
        log_error "不支持的架构: $(uname -m)"
        exit 1
    fi

    log_info "系统: $OS, 架构: $ARCH"
}

# 检查是否已安装conda
check_existing_conda() {
    log_info "检查现有Conda安装..."

    if command -v conda &> /dev/null; then
        log_warning "检测到已安装的Conda: $(conda --version)"
        log_warning "位置: $(which conda)"

        # 在自动化环境中，直接使用现有安装
        if [[ "${AUTO_INSTALL:-false}" == "true" ]]; then
            log_info "自动化模式：使用现有Conda安装"
            return 1
        fi

        # 非自动化模式才提示用户
        read -p "是否继续安装新的Miniconda? (y/N): " -r
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            log_info "跳过Miniconda安装"
            return 1
        fi
    fi

    return 0
}

# 获取或下载Miniconda
download_miniconda() {
    log_info "准备Miniconda安装包..."

    # 构建安装包文件名
    if [[ "$OS" == "Linux" ]]; then
        if [[ "$ARCH" == "x86_64" ]]; then
            INSTALLER_NAME="Miniconda3-latest-Linux-x86_64.sh"
        else
            INSTALLER_NAME="Miniconda3-latest-Linux-aarch64.sh"
        fi
    elif [[ "$OS" == "MacOSX" ]]; then
        if [[ "$ARCH" == "x86_64" ]]; then
            INSTALLER_NAME="Miniconda3-latest-MacOSX-x86_64.sh"
        else
            INSTALLER_NAME="Miniconda3-latest-MacOSX-arm64.sh"
        fi
    fi

    # 脚本目录
    SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

    # 预下载安装包路径
    PREBUILT_INSTALLER="$SCRIPT_DIR/../resources/installers/$INSTALLER_NAME"
    INSTALLER_PATH="$DOWNLOAD_DIR/$INSTALLER_NAME"

    # 优先使用预下载的安装包
    if [[ -f "$PREBUILT_INSTALLER" ]]; then
        log_info "发现预下载的安装包: $PREBUILT_INSTALLER"

        # 检查文件大小
        PREBUILT_SIZE=$(du -h "$PREBUILT_INSTALLER" | cut -f1)
        log_info "安装包大小: $PREBUILT_SIZE"

        # 复制到临时目录
        cp "$PREBUILT_INSTALLER" "$INSTALLER_PATH"

        if [[ -f "$INSTALLER_PATH" ]]; then
            log_success "使用预下载的Miniconda安装包"
            return 0
        else
            log_warning "复制预下载安装包失败，将尝试在线下载"
        fi
    else
        log_warning "未找到预下载的安装包: $PREBUILT_INSTALLER"
        log_info "将从网络下载Miniconda安装包"
    fi

    # 在线下载安装包（备用方案）
    DOWNLOAD_URL="https://repo.anaconda.com/miniconda/$INSTALLER_NAME"

    log_info "下载URL: $DOWNLOAD_URL"
    log_info "保存路径: $INSTALLER_PATH"

    # 删除旧的安装包
    if [[ -f "$INSTALLER_PATH" ]]; then
        rm -f "$INSTALLER_PATH"
    fi

    # 下载安装包
    if command -v wget &> /dev/null; then
        log_info "使用wget下载..."
        wget -O "$INSTALLER_PATH" "$DOWNLOAD_URL"
    elif command -v curl &> /dev/null; then
        log_info "使用curl下载..."
        curl -L -o "$INSTALLER_PATH" "$DOWNLOAD_URL"
    else
        log_error "需要wget或curl来下载Miniconda，且未找到预下载安装包"
        exit 1
    fi

    if [[ ! -f "$INSTALLER_PATH" ]]; then
        log_error "下载失败: $INSTALLER_PATH"
        exit 1
    fi

    log_success "Miniconda在线下载完成"
}

# 安装Miniconda
install_miniconda() {
    log_info "安装Miniconda到: $INSTALL_DIR"

    # 删除旧的安装目录
    if [[ -d "$INSTALL_DIR" ]]; then
        log_warning "删除旧的Miniconda安装: $INSTALL_DIR"
        rm -rf "$INSTALL_DIR"
    fi

    # 执行安装
    bash "$INSTALLER_PATH" -b -p "$INSTALL_DIR"

    if [[ ! -d "$INSTALL_DIR" ]]; then
        log_error "Miniconda安装失败"
        exit 1
    fi

    log_success "Miniconda安装完成"
}

# 初始化conda
initialize_conda() {
    log_info "初始化Conda环境..."

    # 添加conda到PATH
    export PATH="$INSTALL_DIR/bin:$PATH"

    # 设置环境变量以避免交互式提示
    export CONDA_ALWAYS_YES=true
    export CI=true

    # 清理可能冲突的配置
    log_info "清理Conda配置冲突..."
    conda config --remove-key yes 2>/dev/null || true

    # 配置conda设置，避免交互式提示
    log_info "配置Conda设置..."
    conda config --set auto_activate_base false
    conda config --set auto_update_conda false
    conda config --set channel_priority strict
    conda config --set always_yes true

    # 配置默认channels避免Terms of Service问题
    log_info "配置Conda channels..."
    conda config --add channels defaults
    conda config --add channels conda-forge

    # 初始化conda（非交互式）
    "$INSTALL_DIR/bin/conda" init bash --no-user
    "$INSTALL_DIR/bin/conda" init zsh --no-user

    # 更新conda
    log_info "更新Conda到最新版本..."
    conda update -n base -c defaults conda -y --quiet

    log_success "Conda初始化完成"
}

# 创建Ray环境
create_ray_environment() {
    log_info "创建Ray专用Python环境: $CONDA_ENV_NAME"

    # 删除现有环境
    if conda env list | grep -q "$CONDA_ENV_NAME"; then
        log_warning "删除现有环境: $CONDA_ENV_NAME"
        conda env remove -n "$CONDA_ENV_NAME" -y --quiet
    fi

    # 创建新环境（非交互式）
    conda create -n "$CONDA_ENV_NAME" python="$PYTHON_VERSION" -y --quiet

    log_success "Ray环境创建完成"
}

# 安装基础Python包
install_base_packages() {
    log_info "在Ray环境中安装基础Python包..."

    # 确保环境变量设置
    export CONDA_ALWAYS_YES=true
    export CI=true

    # 激活环境并安装包
    source "$INSTALL_DIR/etc/profile.d/conda.sh"
    conda activate "$CONDA_ENV_NAME"

    # 安装基础包（非交互式）
    log_info "安装pip, numpy, pandas..."
    conda install pip numpy pandas -y --quiet

    # 升级pip（静默）
    pip install --upgrade pip --quiet

    # 安装常用科学计算包（静默）
    log_info "安装科学计算包..."
    pip install scipy scikit-learn matplotlib seaborn --quiet

    # 安装深度学习相关包（静默）
    log_info "安装深度学习包..."
    pip install torch torchvision torchaudio --index-url https://download.pytorch.org/whl/cpu --quiet

    log_success "基础Python包安装完成"
}

# 预安装Ray
install_ray() {
    log_info "预安装Ray..."

    # 确保环境变量设置
    export CONDA_ALWAYS_YES=true
    export CI=true
    
    # 确保在正确的环境中
    source "$INSTALL_DIR/etc/profile.d/conda.sh"
    conda activate "$CONDA_ENV_NAME"
    
    # 安装Ray（静默）
    pip install "ray[default]" --quiet
    
    # 验证Ray安装
    python -c "import ray; print(f'Ray version: {ray.__version__}')"
    
    log_success "Ray安装完成"
}

# 创建环境配置文件
create_environment_config() {
    log_info "创建环境配置文件..."
    
    # 创建配置目录
    CONFIG_DIR="$(dirname "$0")/../config"
    mkdir -p "$CONFIG_DIR"
    
    # 创建环境配置文件
    cat > "$CONFIG_DIR/miniconda-env.sh" << EOF
#!/bin/bash
# Miniconda环境配置文件
# 由install-miniconda.sh自动生成

export CONDA_HOME="$INSTALL_DIR"
export CONDA_ENV_NAME="$CONDA_ENV_NAME"
export PATH="\$CONDA_HOME/bin:\$PATH"

# 激活Ray环境的函数
activate_ray_env() {
    source "\$CONDA_HOME/etc/profile.d/conda.sh"
    conda activate "\$CONDA_ENV_NAME"
    echo "已激活Ray环境: \$CONDA_ENV_NAME"
}

# 检查Ray环境的函数
check_ray_env() {
    source "\$CONDA_HOME/etc/profile.d/conda.sh"
    conda activate "\$CONDA_ENV_NAME"
    python -c "import ray; print(f'Ray version: {ray.__version__}')"
}

# 显示环境信息
show_env_info() {
    echo "Conda安装路径: \$CONDA_HOME"
    echo "Ray环境名称: \$CONDA_ENV_NAME"
    echo "Python版本: $PYTHON_VERSION"
    echo ""
    echo "使用方法:"
    echo "  source $CONFIG_DIR/miniconda-env.sh"
    echo "  activate_ray_env"
}

EOF

    log_success "环境配置文件创建完成: $CONFIG_DIR/miniconda-env.sh"
}

# 清理临时文件
cleanup() {
    log_info "清理临时文件..."
    
    if [[ -f "$INSTALLER_PATH" ]]; then
        rm -f "$INSTALLER_PATH"
        log_info "删除安装包: $INSTALLER_PATH"
    fi
    
    log_success "清理完成"
}

# 显示安装总结
show_summary() {
    log_success "==================== 安装完成 ===================="
    echo ""
    echo "✅ Miniconda安装路径: $INSTALL_DIR"
    echo "✅ Ray环境名称: $CONDA_ENV_NAME"
    echo "✅ Python版本: $PYTHON_VERSION"
    echo ""
    echo "🎯 使用方法:"
    echo "   source $CONFIG_DIR/miniconda-env.sh"
    echo "   activate_ray_env"
    echo ""
    echo "🔍 验证安装:"
    echo "   check_ray_env"
    echo ""
    echo "📋 下一步:"
    echo "   - Ray环境已准备就绪"
    echo "   - 可以开始部署Ray集群"
    echo "   - 建议重启终端或重新加载shell配置"
    echo ""
    log_success "===================================================="
}

# 主函数
main() {
    log_info "开始安装Miniconda for Ray..."
    
    # 检测系统
    detect_system
    
    # 检查现有安装
    if ! check_existing_conda; then
        exit 0
    fi
    
    # 安装步骤
    download_miniconda
    install_miniconda
    initialize_conda
    create_ray_environment
    install_base_packages
    install_ray
    create_environment_config
    cleanup
    
    # 显示总结
    show_summary
}

# 执行主函数
main "$@" 