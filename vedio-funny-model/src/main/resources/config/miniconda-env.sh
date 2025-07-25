#!/bin/bash
# Miniconda环境配置文件
# 由install-miniconda.sh自动生成

export CONDA_HOME="/Users/caiwei/miniconda3"
export CONDA_ENV_NAME="ray-env"
export PATH="$CONDA_HOME/bin:$PATH"

# 激活Ray环境的函数
activate_ray_env() {
    source "$CONDA_HOME/etc/profile.d/conda.sh"
    conda activate "$CONDA_ENV_NAME"
    echo "已激活Ray环境: $CONDA_ENV_NAME"
}

# 检查Ray环境的函数
check_ray_env() {
    source "$CONDA_HOME/etc/profile.d/conda.sh"
    conda activate "$CONDA_ENV_NAME"
    python -c "import ray; print(f'Ray version: {ray.__version__}')"
}

# 显示环境信息
show_env_info() {
    echo "Conda安装路径: $CONDA_HOME"
    echo "Ray环境名称: $CONDA_ENV_NAME"
    echo "Python版本: 3.12"
    echo ""
    echo "使用方法:"
    echo "  source /Users/caiwei/IdeaProjects/agent-cloud/vedio-funny-model/scripts/../config/miniconda-env.sh"
    echo "  activate_ray_env"
}

