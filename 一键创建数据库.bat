@echo off
chcp 65001 >nul
echo ========================================
echo 🚀 一键创建 blog_db 数据库
echo ========================================
echo.

REM 检查Python是否安装
python --version >nul 2>&1
if errorlevel 1 (
    echo ❌ 错误：未检测到Python，请先安装Python
    echo 下载地址：https://www.python.org/downloads/
    pause
    exit /b 1
)

REM 安装依赖
echo 📦 正在检查依赖...
pip install pymysql -q

REM 运行脚本
echo.
echo 🚀 开始执行...
echo.
python setup_database.py

echo.
pause
