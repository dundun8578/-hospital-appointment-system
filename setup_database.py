"""
一键自动创建 blog_db 数据库和所有表
使用方法：
1. 确保MySQL服务已启动
2. 直接运行此脚本：python setup_database.py
"""

import pymysql
from pymysql.cursors import DictCursor

# ==================== 数据库配置 ====================
DB_HOST = "localhost"
DB_PORT = 3306
DB_USER = "root"  # 你的MySQL用户名，默认root
DB_PASSWORD = ""  # 你的MySQL密码，如果没有密码就留空
DB_NAME = "blog_db"

def create_database():
    """创建数据库"""
    try:
        # 先连接MySQL（不指定数据库）
        connection = pymysql.connect(
            host=DB_HOST,
            port=DB_PORT,
            user=DB_USER,
            password=DB_PASSWORD,
            charset='utf8mb4'
        )

        with connection.cursor() as cursor:
            # 创建数据库
            sql = f"CREATE DATABASE IF NOT EXISTS `{DB_NAME}` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci"
            cursor.execute(sql)
            print(f"✅ 数据库 {DB_NAME} 创建成功！")

        connection.commit()
        connection.close()
        return True

    except Exception as e:
        print(f"❌ 创建数据库失败: {e}")
        return False

def create_tables():
    """创建所有表"""
    try:
        # 连接到刚创建的数据库
        connection = pymysql.connect(
            host=DB_HOST,
            port=DB_PORT,
            user=DB_USER,
            password=DB_PASSWORD,
            database=DB_NAME,
            charset='utf8mb4',
            cursorclass=DictCursor
        )

        with connection.cursor() as cursor:
            print("\n开始创建表...")

            # 1. 创建SQL模板表
            sql_template = """
            CREATE TABLE IF NOT EXISTS `sql_template` (
              `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
              `template_name` VARCHAR(100) NOT NULL COMMENT '模板名称',
              `template_category` VARCHAR(50) NOT NULL COMMENT '模板分类（如：用户管理、文章管理）',
              `sql_content` TEXT NOT NULL COMMENT 'SQL语句内容',
              `description` VARCHAR(500) DEFAULT NULL COMMENT '模板描述',
              `params_json` TEXT DEFAULT NULL COMMENT '参数说明（JSON格式）',
              `is_public` TINYINT(1) DEFAULT '0' COMMENT '是否公开（0:私有 1:公开）',
              `created_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
              `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
              `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
              `deleted` TINYINT(1) DEFAULT '0' COMMENT '逻辑删除（0:未删除 1:已删除）',
              PRIMARY KEY (`id`),
              KEY `idx_category` (`template_category`),
              KEY `idx_created_by` (`created_by`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='SQL模板表';
            """
            cursor.execute(sql_template)
            print("✅ sql_template 表创建成功")

            # 2. 创建SQL执行记录表
            sql_execution_log = """
            CREATE TABLE IF NOT EXISTS `sql_execution_log` (
              `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
              `template_id` BIGINT DEFAULT NULL COMMENT '关联模板ID',
              `sql_content` TEXT NOT NULL COMMENT '实际执行的SQL',
              `execution_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '执行时间',
              `execution_duration` INT DEFAULT NULL COMMENT '执行耗时（毫秒）',
              `affected_rows` INT DEFAULT NULL COMMENT '影响行数',
              `executed_by` BIGINT DEFAULT NULL COMMENT '执行人ID',
              `status` TINYINT DEFAULT '1' COMMENT '执行状态（0:失败 1:成功）',
              `error_message` TEXT DEFAULT NULL COMMENT '错误信息',
              `params_json` TEXT DEFAULT NULL COMMENT '执行参数（JSON格式）',
              PRIMARY KEY (`id`),
              KEY `idx_template_id` (`template_id`),
              KEY `idx_executed_by` (`executed_by`),
              KEY `idx_execution_time` (`execution_time`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='SQL执行记录表';
            """
            cursor.execute(sql_execution_log)
            print("✅ sql_execution_log 表创建成功")

            # 3. 创建SQL文件存储表
            sql_file = """
            CREATE TABLE IF NOT EXISTS `sql_file` (
              `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
              `file_name` VARCHAR(255) NOT NULL COMMENT '文件名称',
              `file_category` VARCHAR(50) DEFAULT NULL COMMENT '文件分类',
              `sql_content` LONGTEXT NOT NULL COMMENT 'SQL文件完整内容',
              `description` VARCHAR(500) DEFAULT NULL COMMENT '文件描述',
              `file_size` INT DEFAULT NULL COMMENT '文件大小（字节）',
              `line_count` INT DEFAULT NULL COMMENT 'SQL行数',
              `created_by` BIGINT DEFAULT NULL COMMENT '创建人ID',
              `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
              `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
              `deleted` TINYINT(1) DEFAULT '0' COMMENT '逻辑删除（0:未删除 1:已删除）',
              PRIMARY KEY (`id`),
              KEY `idx_file_name` (`file_name`),
              KEY `idx_category` (`file_category`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='SQL文件存储表';
            """
            cursor.execute(sql_file)
            print("✅ sql_file 表创建成功")

        connection.commit()
        return True

    except Exception as e:
        print(f"❌ 创建表失败: {e}")
        return False
    finally:
        if connection:
            connection.close()

def insert_test_data():
    """插入测试数据"""
    try:
        connection = pymysql.connect(
            host=DB_HOST,
            port=DB_PORT,
            user=DB_USER,
            password=DB_PASSWORD,
            database=DB_NAME,
            charset='utf8mb4'
        )

        with connection.cursor() as cursor:
            print("\n开始插入测试数据...")

            # 插入SQL模板测试数据
            cursor.execute("""
                INSERT INTO `sql_template` (`template_name`, `template_category`, `sql_content`, `description`, `is_public`) VALUES
                ('查询所有文章', '文章管理', 'SELECT * FROM article WHERE deleted = 0', '查询所有未删除的文章', 1),
                ('添加新文章', '文章管理', 'INSERT INTO article (title, content, author) VALUES (?, ?, ?)', '插入一篇新文章', 1),
                ('删除用户', '用户管理', 'UPDATE user SET deleted = 1 WHERE id = ?', '逻辑删除指定用户', 0),
                ('统计文章数', '数据统计', 'SELECT COUNT(*) as total FROM article WHERE deleted = 0', '统计文章总数', 1)
            """)
            print(f"✅ 插入了 {cursor.rowcount} 条SQL模板数据")

            # 插入SQL文件测试数据
            cursor.execute("""
                INSERT INTO `sql_file` (`file_name`, `file_category`, `sql_content`, `description`, `file_size`, `line_count`) VALUES
                ('user_management.sql', '用户管理', 'SELECT * FROM user WHERE deleted = 0;', '用户查询模板', 256, 1),
                ('article_list.sql', '文章管理', 'SELECT a.*, u.username FROM article a LEFT JOIN user u ON a.author_id = u.id WHERE a.deleted = 0;', '文章列表查询', 512, 1)
            """)
            print(f"✅ 插入了 {cursor.rowcount} 条SQL文件数据")

            # 插入SQL执行记录测试数据
            cursor.execute("""
                INSERT INTO `sql_execution_log` (`sql_content`, `execution_duration`, `affected_rows`, `status`) VALUES
                ('SELECT * FROM sql_template', 15, 3, 1),
                ('INSERT INTO sql_file VALUES (...)', 23, 2, 1)
            """)
            print(f"✅ 插入了 {cursor.rowcount} 条执行记录数据")

        connection.commit()
        return True

    except Exception as e:
        print(f"❌ 插入测试数据失败: {e}")
        return False
    finally:
        if connection:
            connection.close()

def verify():
    """验证结果"""
    try:
        connection = pymysql.connect(
            host=DB_HOST,
            port=DB_PORT,
            user=DB_USER,
            password=DB_PASSWORD,
            database=DB_NAME,
            charset='utf8mb4'
        )

        with connection.cursor() as cursor:
            print("\n" + "="*50)
            print("✅ 验证结果：")
            print("="*50)

            # 查看所有表
            cursor.execute("SHOW TABLES")
            tables = cursor.fetchall()
            print(f"\n📊 数据库 {DB_NAME} 中有 {len(tables)} 张表：")
            for table in tables:
                table_name = list(table.values())[0]
                cursor.execute(f"SELECT COUNT(*) as count FROM `{table_name}`")
                count = cursor.fetchone()['count']
                print(f"  - {table_name}（{count} 条数据）")

        connection.close()
        return True

    except Exception as e:
        print(f"❌ 验证失败: {e}")
        return False

if __name__ == "__main__":
    print("="*50)
    print("🚀 开始创建 blog_db 数据库和表结构")
    print("="*50)

    # 步骤1：创建数据库
    if not create_database():
        print("\n❌ 数据库创建失败，请检查MySQL是否启动和配置是否正确")
        exit(1)

    # 步骤2：创建表
    if not create_tables():
        print("\n❌ 表创建失败")
        exit(1)

    # 步骤3：插入测试数据
    if not insert_test_data():
        print("\n❌ 测试数据插入失败")
        exit(1)

    # 步骤4：验证
    verify()

    print("\n" + "="*50)
    print("🎉 全部搞定！数据库已准备就绪！")
    print("="*50)
