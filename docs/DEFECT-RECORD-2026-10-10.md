# 缺陷记录：Docker 容器启动失败与登录 500

- 日期：2026-10-10
- 环境：`/home/zhdj/alinkdj`（Compose 服务 `db` / `backend` / `frontend`）
- 数据库：PostgreSQL `honghe_party_db`（named volume `party_pg_data`）
- 影响：`party-postgres-db`、`party-vue-frontend` 无法启动；启动后登录接口报 500。

---

## 1. 现象

1. `docker compose up -d` 后，`party-postgres-db`、`party-vue-frontend` 反复重启，仅 `party-spring-backend` 正常。
2. 容器修复后，调用 `POST /api/auth/login` 返回 `500 Internal Server Error`。
3. 后端日志先后出现两类关键错误：
   - `ERROR: relation "sys_user" does not exist`
   - `Cannot convert the column of type TIMESTAMPTZ to requested type java.time.LocalDateTime`

---

## 2. 根因分析

### 2.1 容器启动失败：Docker 默认 seccomp 拦截 `pwrite`

- PostgreSQL 日志：`could not write to file "postmaster.pid": Operation not permitted`、`Could not write to file "pg_xact/0000": Operation not permitted`。
- 前端 nginx 日志：`pwrite() "/run/nginx.pid" failed (1: Operation not permitted)`。
- 排除因素：磁盘使用率 60%、inode 2%；SELinux 临时置为 Permissive 后问题依旧；新建临时容器向数据卷写入正常。
- 定位：Docker 默认 seccomp 策略在本环境拦截 `pwrite` 系统调用。加 `--security-opt seccomp=unconfined` 后 nginx 校验通过、容器可稳定 Up。

### 2.2 登录 500 第一层：初始化 SQL 中断，系统表缺失

- 初始化脚本 `docs/schema-pg.sql` 在 `party_honor_punishment` 建表处中断，导致其后表未创建。
- 缺失表：`sys_user`、`sys_role`、`sys_user_role`、`sys_notice_channel`、`sys_notice_log`。
- 登录查询 `sys_user` 时抛出 `relation "sys_user" does not exist`。

### 2.3 登录 500 第二层：`TIMESTAMPTZ` 与实体 `LocalDateTime` 不匹配

- 补齐表并插入数据后，登录仍 500。
- 日志：`Cannot convert the column of type TIMESTAMPTZ to requested type java.time.LocalDateTime`。
- 原因：schema 时间列使用 `TIMESTAMPTZ`（带时区），后端 14 个实体类时间字段全部使用 `java.time.LocalDateTime`（无时区），MyBatis 结果集自动映射无法转换。

### 2.4 源脚本隐患（未修复，待处理）

`docs/schema-pg.sql` 本身存在两处问题，本次通过直接操作数据库绕过，重建数据库时仍会复发：

1. JSON 字段使用 `'[\"user:manage\", ...]'` 写法，在 PostgreSQL `standard_conforming_strings=on` 下会被解析为字面反斜杠，导致 `invalid input syntax for type json`。
2. 时间列大量使用 `TIMESTAMPTZ`，与实体 `LocalDateTime` 不匹配。

---

## 3. 修复措施

### 3.1 修改文件

| 文件 | 变更 |
|---|---|
| `docker-compose.yml` | 为 `db`、`frontend` 服务添加 `security_opt: - seccomp:unconfined` |
| `docs/fix_missing_tables.sql` | 新建，补齐五张系统表及种子数据 |

### 3.2 容器修复

```bash
docker compose up -d --force-recreate db frontend
```

### 3.3 数据库修复（直接执行）

1. 启用加密扩展：

   ```sql
   CREATE EXTENSION IF NOT EXISTS pgcrypto;
   ```

2. 执行 `docs/fix_missing_tables.sql`，补齐：
   - `sys_user`：7 个演示账号
   - `sys_role`：4 个角色
   - `sys_user_role`：7 条关联
   - `sys_notice_channel`：5 条渠道
   - `sys_notice_log`

3. 密码生成方式（与 Spring `BCryptPasswordEncoder(12)` 兼容）：

   ```sql
   crypt('Admin@123456', gen_salt('bf', 12))
   ```

   生成格式为 `$2a$12$...` 的 60 字符哈希。

4. 时间列类型统一（共 27 列）：

   ```sql
   ALTER TABLE <表名> ALTER COLUMN <列名> TYPE TIMESTAMP;
   ```

   将 `TIMESTAMPTZ` 全部改为 `TIMESTAMP`（`timestamp without time zone`），匹配实体 `LocalDateTime`。

### 3.4 SELinux

修复完成后恢复为 `Enforcing`。

---

## 4. 验证结果

| 项目 | 结果 |
|---|---|
| 三容器状态 | 全部 Up |
| 前端页面 | HTTP 200 |
| PostgreSQL | `database system is ready to accept connections` |
| `admin` 登录 | 200，返回 token + `SYS_ADMIN` 角色 |
| `yanghai` 登录 | 200，返回 token + `GENERAL_BRANCH_ADMIN` 角色 |
| 业务接口 `GET /api/member/roster` | 200，党员名册正常返回 |
| 密码哈希 | `$2a$12$` 开头，通过 `BCryptPasswordEncoder` 校验 |
| SELinux | `Enforcing` |

---

## 5. 当前账号状态

- 临时密码统一为：`Admin@123456`（符合 8–72 字节且含字母、数字、特殊字符规则）。
- 账号：`admin`、`yanghai`、`liweimin`、`liujianhua`、`chenming`、`zhangqiang`、`linyuhan`。
- **上线前请为各账号设置正式密码。**

---

## 6. 待办 / 建议

1. 修复 `docs/schema-pg.sql` 源脚本：
   - JSON 字段 `\"` 改为直接双引号 `"`（或改用 `jsonb` 专用转义方式）。
   - 时间列 `TIMESTAMPTZ` 改为 `TIMESTAMP`，与实体类型一致。
2. 修改 `schema-pg.sql` 后，建议在干净库上完整重放验证初始化脚本可一次跑通。
3. 临时密码 `Admin@123456` 属于演示用，正式部署前须替换。
