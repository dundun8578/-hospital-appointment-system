@echo off
chcp 65001 >nul
echo ========================================
echo Web儿童健康帮扶系统 - 启动脚本
echo ========================================
echo.

echo [1/5] 检查MySQL服务...
sc query MySQL80 | find "RUNNING" >nul
if errorlevel 1 (
    echo [警告] MySQL服务未启动，尝试启动...
    net start MySQL80 >nul 2>&1
    if errorlevel 1 (
        echo [错误] MySQL服务启动失败！
        echo 请先安装并启动MySQL数据库
        pause
        exit /b 1
    )
)
echo [OK] MySQL服务运行正常
echo.

echo [2/5] 检查Java环境...
java -version >nul 2>&1
if errorlevel 1 (
    echo [错误] 未检测到Java环境！
    pause
    exit /b 1
)
echo [OK] Java环境正常
echo.

echo [3/5] 检查Maven...
mvn --version >nul 2>&1
if errorlevel 1 (
    echo [错误] 未检测到Maven！
    pause
    exit /b 1
)
echo [OK] Maven环境正常
echo.

echo [4/5] 启动项目...
echo 正在启动儿童健康帮扶系统...
echo 端口：12306
echo.

mvn spring-boot:run

if errorlevel 1 (
    echo.
    echo [错误] 项目启动失败！
    echo 请检查：
    echo 1. MySQL数据库是否已创建
    echo 2. 数据库配置是否正确
    echo 3. 查看上方错误信息
    pause
    exit /b 1
)

echo.
echo ========================================
echo 项目已停止
echo ========================================
pause
