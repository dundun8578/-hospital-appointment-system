# MySQL密码配置说明

## 如果MySQL没有密码（默认）
直接双击运行「一键创建数据库.bat」即可

## 如果MySQL有密码

1. 打开 `setup_database.py` 文件
2. 找到第13行：
   ```python
   DB_PASSWORD = ""  # 你的MySQL密码，如果没有密码就留空
   ```
3. 把引号里的内容改成你的MySQL密码，例如：
   ```python
   DB_PASSWORD = "你的密码"
   ```
4. 保存文件，再双击运行「一键创建数据库.bat」

---

## 常见问题

### MySQL服务没启动
- Windows：按 `Win + R`，输入 `services.msc`，找到 `MySQL80`，右键「启动」

### 用户名不是root
- 如果你MySQL用户名不是 `root`，也修改 `setup_database.py` 第12行的 `DB_USER`

### Python未安装
- 脚本会自动提示你下载安装Python
