#!/bin/bash

# =================================================================
# VedioFun Model Service - Conda环境检测脚本
# 用途: 检测节点上的conda环境是否正确安装
# 作者: VedioFun Team
# =================================================================

set -e  # 遇到错误立即退出

# 脚本目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# 加载Ray环境变量配置
ENV_FILE="$SCRIPT_DIR/../resources/ray.env"
if [[ -f "$ENV_FILE" ]]; then
    source "$ENV_FILE"
else
    echo "警告: 未找到Ray环境配置文件 $ENV_FILE，使用默认配置"
    # 默认配置
    CONDA_HOME="${HOME}/miniconda3"
    CONDA_ENV_NAME="ray-env"
    PYTHON_VERSION="3.12"
fi

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

# 检测结果变量
CONDA_INSTALLED=false
CONDA_VERSION=""
CONDA_PATH=""
RAY_ENV_EXISTS=false
RAY_INSTALLED=false
RAY_VERSION=""
PYTHON_AVAILABLE=false
PYTHON_VERSION_ACTUAL=""

# 1. 检测conda是否安装
check_conda_installation() {
    log_info "检测Conda安装状态..."

    # 方法1: 检查默认安装路径
    if [[ -f "$CONDA_HOME/bin/conda" ]]; then
        CONDA_INSTALLED=true
        CONDA_PATH="$CONDA_HOME/bin/conda"
        CONDA_VERSION=$($CONDA_PATH --version 2>/dev/null | cut -d' ' -f2 || echo "unknown")
        log_success "发现Conda安装: $CONDA_PATH (版本: $CONDA_VERSION)"
        return 0
    fi

    # 方法2: 检查系统PATH
    if command -v conda &> /dev/null; then
        CONDA_INSTALLED=true
        CONDA_PATH=$(which conda)
        CONDA_VERSION=$(conda --version 2>/dev/null | cut -d' ' -f2 || echo "unknown")
        log_success "发现系统Conda: $CONDA_PATH (版本: $CONDA_VERSION)"
        return 0
    fi

    # 方法3: 搜索常见安装路径
    local search_paths=(
        "${HOME}/miniconda3/bin/conda"
        "${HOME}/anaconda3/bin/conda"
        "/opt/miniconda3/bin/conda"
        "/opt/anaconda3/bin/conda"
        "/usr/local/miniconda3/bin/conda"
        "/usr/local/anaconda3/bin/conda"
    )

    for path in "${search_paths[@]}"; do
        if [[ -f "$path" ]]; then
            CONDA_INSTALLED=true
            CONDA_PATH="$path"
            CONDA_VERSION=$($path --version 2>/dev/null | cut -d' ' -f2 || echo "unknown")
            log_success "发现Conda安装: $path (版本: $CONDA_VERSION)"
            return 0
        fi
    done

    log_error "未找到Conda安装"
    return 1
}

# 2. 检测Ray环境是否存在
check_ray_environment() {
    log_info "检测Ray环境: $CONDA_ENV_NAME"

    if [[ "$CONDA_INSTALLED" != "true" ]]; then
        log_error "Conda未安装，无法检测Ray环境"
        return 1
    fi

    # 确保conda可用
    if [[ ! -f "$CONDA_PATH" ]]; then
        log_error "Conda路径无效: $CONDA_PATH"
        return 1
    fi

    # 添加conda到PATH
    local conda_dir=$(dirname "$CONDA_PATH")
    export PATH="$conda_dir:$PATH"

    # 检查环境是否存在
    if $CONDA_PATH env list 2>/dev/null | grep -q "^$CONDA_ENV_NAME "; then
        RAY_ENV_EXISTS=true
        log_success "Ray环境存在: $CONDA_ENV_NAME"
        return 0
    else
        log_warning "Ray环境不存在: $CONDA_ENV_NAME"
        return 1
    fi
}

# 3. 检测Ray是否安装
check_ray_installation() {
    log_info "检测Ray安装状态..."

    if [[ "$RAY_ENV_EXISTS" != "true" ]]; then
        log_error "Ray环境不存在，无法检测Ray安装"
        return 1
    fi

    # 临时激活环境并检测Ray
    local conda_dir=$(dirname "$CONDA_PATH")
    local conda_base_dir=$(dirname "$conda_dir")

    # 设置conda环境
    if [[ -f "$conda_base_dir/etc/profile.d/conda.sh" ]]; then
        source "$conda_base_dir/etc/profile.d/conda.sh"

        # 激活环境
        conda activate "$CONDA_ENV_NAME" 2>/dev/null || {
            log_error "无法激活Ray环境: $CONDA_ENV_NAME"
            return 1
        }

        # 检测Ray
        if python -c "import ray; print(ray.__version__)" &>/dev/null; then
            RAY_INSTALLED=true
            RAY_VERSION=$(python -c "import ray; print(ray.__version__)" 2>/dev/null || echo "unknown")
            log_success "Ray已安装: 版本 $RAY_VERSION"
        else
            log_warning "Ray未安装或导入失败"
        fi

        # 检测Python版本
        if command -v python &>/dev/null; then
            PYTHON_AVAILABLE=true
            PYTHON_VERSION_ACTUAL=$(python --version 2>&1 | cut -d' ' -f2 || echo "unknown")
            log_info "Python版本: $PYTHON_VERSION_ACTUAL"
        fi

        # 退出环境
        conda deactivate 2>/dev/null || true
    else
        log_error "未找到conda初始化脚本"
        return 1
    fi
}

# 4. 生成检测报告
generate_report() {
    log_info "生成环境检测报告..."

    echo ""
    echo "=========================================="
    echo "           Conda环境检测报告"
    echo "=========================================="
    echo ""

    # Conda安装状态
    echo "📦 Conda安装状态:"
    if [[ "$CONDA_INSTALLED" == "true" ]]; then
        echo "  ✅ 已安装"
        echo "  📍 路径: $CONDA_PATH"
        echo "  🏷️  版本: $CONDA_VERSION"
    else
        echo "  ❌ 未安装"
    fi
    echo ""

    # Ray环境状态
    echo "🐍 Ray Python环境:"
    if [[ "$RAY_ENV_EXISTS" == "true" ]]; then
        echo "  ✅ 环境存在: $CONDA_ENV_NAME"
        if [[ "$PYTHON_AVAILABLE" == "true" ]]; then
            echo "  🐍 Python版本: $PYTHON_VERSION_ACTUAL"
        fi
    else
        echo "  ❌ 环境不存在: $CONDA_ENV_NAME"
    fi
    echo ""

    # Ray安装状态
    echo "🚀 Ray安装状态:"
    if [[ "$RAY_INSTALLED" == "true" ]]; then
        echo "  ✅ 已安装"
        echo "  🏷️  版本: $RAY_VERSION"
    else
        echo "  ❌ 未安装"
    fi
    echo ""

    # 总体状态
    echo "📊 总体状态:"
    if [[ "$CONDA_INSTALLED" == "true" && "$RAY_ENV_EXISTS" == "true" && "$RAY_INSTALLED" == "true" ]]; then
        echo "  ✅ 环境完整，可以部署Ray集群"
        echo ""
        echo "🎯 下一步操作:"
        echo "  source $CONDA_PATH/../etc/profile.d/conda.sh"
        echo "  conda activate $CONDA_ENV_NAME"
        echo "  ray start --head  # 启动Ray Head节点"
    elif [[ "$CONDA_INSTALLED" == "true" ]]; then
        echo "  ⚠️  Conda已安装，但Ray环境不完整"
        echo ""
        echo "🛠️  建议操作:"
        echo "  bash $SCRIPT_DIR/install-miniconda.sh  # 重新安装/修复环境"
    else
        echo "  ❌ 环境未安装"
        echo ""
        echo "🛠️  安装命令:"
        echo "  bash $SCRIPT_DIR/install-miniconda.sh  # 安装完整环境"
    fi
    echo ""
    echo "=========================================="
}

# 5. 输出JSON格式结果（用于程序调用）
output_json() {
    if [[ "$1" == "--json" ]]; then
        cat << EOF
{
  "conda": {
    "installed": $CONDA_INSTALLED,
    "version": "$CONDA_VERSION",
    "path": "$CONDA_PATH"
  },
  "rayEnvironment": {
    "exists": $RAY_ENV_EXISTS,
    "name": "$CONDA_ENV_NAME",
    "pythonVersion": "$PYTHON_VERSION_ACTUAL"
  },
  "ray": {
    "installed": $RAY_INSTALLED,
    "version": "$RAY_VERSION"
  },
  "ready": $([ "$CONDA_INSTALLED" == "true" ] && [ "$RAY_ENV_EXISTS" == "true" ] && [ "$RAY_INSTALLED" == "true" ] && echo "true" || echo "false"),
  "timestamp": "$(date -u +"%Y-%m-%dT%H:%M:%SZ")"
}
EOF
    fi
}

# 主函数
main() {
    local json_output=false

    # 检查命令行参数
    for arg in "$@"; do
        case $arg in
            --json)
                json_output=true
                ;;
            --help|-h)
                echo "用法: $0 [选项]"
                echo ""
                echo "选项:"
                echo "  --json    输出JSON格式结果"
                echo "  --help    显示帮助信息"
                echo ""
                echo "示例:"
                echo "  $0              # 显示详细报告"
                echo "  $0 --json       # 输出JSON格式"
                exit 0
                ;;
        esac
    done

    if [[ "$json_output" != "true" ]]; then
        log_info "开始检测Conda环境..."
        echo "配置文件: $ENV_FILE"
        echo ""
    fi

    # 执行检测
    check_conda_installation
    check_ray_environment
    check_ray_installation

    # 输出结果
    if [[ "$json_output" == "true" ]]; then
        output_json --json
    else
        generate_report
    fi

    # 设置退出码
    if [[ "$CONDA_INSTALLED" == "true" && "$RAY_ENV_EXISTS" == "true" && "$RAY_INSTALLED" == "true" ]]; then
        exit 0  # 环境完整
    elif [[ "$CONDA_INSTALLED" == "true" ]]; then
        exit 1  # 部分安装
    else
        exit 2  # 未安装
    fi
}

# 执行主函数
main "$@"