#!/bin/bash

# =================================================================
# VedioFun Model Service - Conda环境快速设置脚本
# 用途: 快速加载conda环境变量和激活Ray环境
# 作者: VedioFun Team
# =================================================================

# 脚本目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# 加载Ray环境变量配置
ENV_FILE="$SCRIPT_DIR/../resources/ray.env"
if [[ -f "$ENV_FILE" ]]; then
    source "$ENV_FILE"
    echo "✅ 已加载Ray环境配置: $ENV_FILE"
else
    echo "❌ 未找到Ray环境配置文件: $ENV_FILE"
    exit 1
fi

# 颜色定义
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 显示环境信息
show_env_info() {
    echo -e "${BLUE}==================== Conda环境信息 ====================${NC}"
    echo "📦 Conda安装目录: ${CONDA_HOME}"
    echo "🐍 Ray环境名称: ${CONDA_ENV_NAME}"
    echo "🐍 Python版本: ${PYTHON_VERSION}"
    echo "🚀 Ray临时目录: ${RAY_TMPDIR}"
    echo "🔧 Ray Head端口: ${RAY_HEAD_PORT}"
    echo "📊 Dashboard端口: ${RAY_DASHBOARD_PORT}"
    echo -e "${BLUE}=====================================================${NC}"
}

# 检查conda是否安装
check_conda() {
    if [[ -f "${CONDA_HOME}/bin/conda" ]]; then
        echo -e "${GREEN}✅ Conda已安装: ${CONDA_HOME}${NC}"
        return 0
    elif command -v conda &> /dev/null; then
        echo -e "${GREEN}✅ 系统Conda可用: $(which conda)${NC}"
        return 0
    else
        echo -e "${YELLOW}⚠️  Conda未安装或不可用${NC}"
        return 1
    fi
}

# 激活Ray环境的函数
activate_ray_env() {
    if [[ -f "${CONDA_HOME}/etc/profile.d/conda.sh" ]]; then
        source "${CONDA_HOME}/etc/profile.d/conda.sh"
        conda activate "${CONDA_ENV_NAME}"
        echo -e "${GREEN}✅ 已激活Ray环境: ${CONDA_ENV_NAME}${NC}"

        # 设置Ray相关环境变量
        export RAY_TMPDIR="${RAY_TMPDIR}"
        mkdir -p "${RAY_TMPDIR}"

        echo "🚀 Ray环境已准备就绪！"
        echo ""
        echo "可用命令:"
        echo "  ray start --head --port=${RAY_HEAD_PORT} --dashboard-port=${RAY_DASHBOARD_PORT}"
        echo "  ray start --address=<head_node_ip>:${RAY_HEAD_PORT}"
        echo "  ray status"
        echo "  ray stop"
    else
        echo "❌ 无法找到conda初始化脚本"
        return 1
    fi
}

# 快速检查环境的函数
quick_check() {
    echo "🔍 快速环境检查..."
    bash "$SCRIPT_DIR/check-conda-environment.sh" --json | python3 -m json.tool 2>/dev/null || {
        echo "JSON解析失败，执行详细检查:"
        bash "$SCRIPT_DIR/check-conda-environment.sh"
    }
}

# 主函数
main() {
    case "${1:-info}" in
        "info"|"show")
            show_env_info
            check_conda
            ;;
        "activate"|"env")
            if check_conda; then
                activate_ray_env
            else
                echo "请先安装Conda环境:"
                echo "  bash $SCRIPT_DIR/install-miniconda.sh"
            fi
            ;;
        "check")
            quick_check
            ;;
        "install")
            echo "开始安装Conda环境..."
            bash "$SCRIPT_DIR/install-miniconda.sh"
            ;;
        "help"|"--help"|"-h")
            echo "用法: $0 [命令]"
            echo ""
            echo "命令:"
            echo "  info       显示环境配置信息 (默认)"
            echo "  activate   激活Ray环境"
            echo "  check      检查环境状态"
            echo "  install    安装Conda环境"
            echo "  help       显示帮助信息"
            echo ""
            echo "示例:"
            echo "  source $0 activate    # 激活环境（需要source）"
            echo "  $0 check             # 检查环境"
            echo "  $0 install           # 安装环境"
            ;;
        *)
            echo "未知命令: $1"
            echo "使用 '$0 help' 查看帮助"
            exit 1
            ;;
    esac
}

# 执行主函数
main "$@"