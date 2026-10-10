# 智慧党建数字化平台重大业务升级与优化记录

- **升级日期**：2026-10-11
- **执行分支**：`main`
- **对应系统**：红河数据产业集团有限公司党总支 · 智慧党建数字化管理系统
- **升级范围**：业务架构、数据库 DDL、前后端接口、表单排版交互、多凭证登录鉴权

---

## 一、 升级背景与核心诉求

根据集团党总支及各支部组织员在党务实务工作中的实际需求，本次集中完成了以下 6 项重要优化与业务闭环重构：
1. **党员转接独立建账**：将原分散在花名册中的临时转接字段解耦，新增独立的【党员转接及调整备案】业务模块（涵盖“党员组织关系转接记录”和“党员党内职务调整备案”两套专账）；
2. **转接与花名册双向联动**：实现“转入核准后自动建档入库花名册”、“转出办结后自动从花名册除名注销”；
3. **职务调整与批文号留痕**：建立干部任免调整备案表，记录批复号/批准文号与职责分工，并自动同步更新花名册党员职务；
4. **主导航 Tab 标签统一精炼**：规范 5 个主导航标签文案，去除冗余字眼，消除顶部通知中心重复入口；
5. **表单排版防折行与全要素建档**：彻底修复“年度集中培训时长”等文字折行问题，全局注入防换行与弹性布局规则；扩充入党申请人身份证、电话、民族、籍贯、工龄等关键档案；
6. **党籍全流程据实同步与多凭证统一登录**：
   - 解决转正前申请人、积极分子、发展对象在花名册断层的问题，实现 25 步全流程关键节点据实逐级晋级花名册党籍；
   - 解决国企“人在 A 公司、党组织关系在 B 支部”的跨单位外派/借调挂靠业务诉求；
   - 落地“多凭证统一鉴权体系（Multi-Credential Login）”，手机号、工号、用户名均可作为系统登录账号。

---

## 二、 核心架构重构与详细实现

### 1. 数据库与后端层（新增两张专表与控制器）

#### 1.1 数据库表设计 (`docs/add_transfer_and_adjustment_tables.sql` & `docs/schema-pg.sql`)
- **`party_relation_transfer`（党员组织关系转接记录表）**：
  - 核心字段：`transfer_type`（1:转入, 2:转出）、`from_org_name`（原党组织）、`to_org_name`（接收党组织）、`letter_no`（介绍信文号）、`transfer_date`、`transfer_reason`、`dues_paid_to_date`、`operator_name` 等。
- **`party_position_adjustment`（党员党内职务调整备案表）**：
  - 核心字段：`org_name`（任职支部）、`old_post`、`new_post`、`adjust_type`（换届/任命/调整/免职/增选）、`document_no`（批准文号/批复号）、`effective_date`、`approval_unit`、`duty_description` 等。

#### 1.2 后端实体与接口 (`TransferController.java`, `MemberController.java`, `AuthController.java`)
- **`TransferController.java`**：
  - `GET /transfer/list`：组织关系转接记录条件检索；
  - `POST /transfer/in`：办理组织关系转入，若在册档案不存在则自动新增录入 `party_member` 并设置 `status=1`，已存在则恢复正常在册，写入转接台账；
  - `POST /transfer/out`：办理组织关系转出，将 `party_member` 党员标记状态为 `status=4`（调离除名），写入转接台账；
  - `GET /position-adjustment/list`：党内职务调整备案列表查询；
  - `POST /position-adjustment/save`：录入职务任免备案，自动更新 `party_member` 中对应党员的 `partyPost`（党内职务）。
- **`MemberController.java`**：
  - `getRoster` 接口增加 `wrapper.ne(PartyMember::getStatus, 4)` 过滤，确保调离转出党员在花名册中除名；
  - `advanceStep` 接口增加第 6、12、20、25 步对 `party_member` 中 `partyStatus`、`joinPartyDate`、`officialPartyDate` 的全流程据实晋级。
- **`AuthController.java`（多凭证统一鉴权体系）**：
  - 登录接口接收凭证账号后，通过 `SysUser::getUsername`、`SysUser::getPhone`、`SysUser::getWorkNo` 联合匹配，实现手机号码、员工工号、用户名三者通配免密/密码登录。

---

### 2. 前端界面与交互层重构

#### 2.1 新增顶级一级模块【党员转接及调整备案】（`name="transfer_filing"`）
- **子模块 1：党员组织关系转接记录**：
  - 包含转入转出台账、凭证字号、原单位与拟转往单位、经办人；
  - 提供“办理组织关系转入”弹窗（支持一键同步花名册建档与账号生成）；
  - 提供“办理组织关系转出”弹窗（从在册党员中检索选择，确认后从名册除名）；
  - 提供正统党建红头样式的《中国共产党党员组织关系转接存根凭证》弹窗（带红印章与一键打印）；
  - 支持转接台账导出 Excel。
- **子模块 2：党员党内职务调整备案**：
  - 包含干部任免记录、任职党支部、原职务、新任职务、调整类型、**批准文号/批复号**、生效日期、批准单位、职责分工；
  - 提供“新增职务调整备案”弹窗（检索在册党员录入，自动同步刷新花名册党员职务）；
  - 提供《党员领导干部党内职务调整任免备案登记表》详情与导出。

#### 2.2 规范 5 个主导航 Tab 标签文案并精简 Header
- `全集团所有党员花名册` ➔ **`党员花名册`** (`roster`)
- `支部“三会一课”与组织生活台账` ➔ **`“三会一课”/组织生活`** (`meetings`)
- `组织与个人奖惩/荣誉台账` ➔ **`组织/个人奖惩或荣誉`** (`honors`)
- `25步文书模板管理（默认+导入）` ➔ **`文书知识库`** (`templates`)
- `党建通知中心与多渠道` ➔ **`通知中心`** (`notices`)
- 移除了 Header 右侧同名冗余的“通知中心”大按钮，统一收拢至主导航。
- 固定所有角色登录后的默认首屏均为【发展党员全景工作台】。

#### 2.3 表单排版防折行与全要素建档
- **全局 CSS 样式防御（`style.css`）**：
  - 为 `.el-form-item__label`、`.el-checkbox__label`、`.el-radio__label` 增加 `white-space: nowrap !important; word-break: keep-all !important;`；
- **各类弹窗针对性重构**：
  - 花名册新增/修改弹窗宽度扩至 `840px`，`label-width="140px"`，彻底解决“年度集中培训时长的长字换行”；
  - 入党申请人建档按钮优化为 `入党申请人建档`，弹窗补齐身份证、手机号、民族、籍贯、工龄，建档即开通账号并入库花名册（申请人状态）。

#### 2.4 人事行政关系与党支部解耦（人在 A 公司在 B 党支部）
- 在 `companyDepartments.js` 扩展 `getAllDepartmentGroupOptions()`、`isCrossCompany()`、`getCompanyNameFromDept()`；
- 表单中部门选择改为按公司分组展开，支持跨公司自由选择或自定义输入；
- 检测到跨单位情形时，弹窗即时弹出琥珀金提示框；
- 花名册与工作台表格中对跨单位人员自动渲染 **`跨单位挂靠/派驻`** 特色标签，并在部门前加注 `[人事单位]`。

---

## 三、 修改及新建文件清单

| 文件类型 | 文件路径 | 变更说明 |
| :--- | :--- | :--- |
| **新建 SQL** | `docs/add_transfer_and_adjustment_tables.sql` | 转接记录表与职务调整备案表 DDL |
| **修改 SQL** | `docs/schema-pg.sql` | 同步合并两张新表至初始化总脚本 |
| **新建实体** | `backend/.../entity/PartyRelationTransfer.java` | 组织关系转接实体类 |
| **新建实体** | `backend/.../entity/PartyPositionAdjustment.java` | 职务调整备案实体类 |
| **新建 Mapper**| `backend/.../mapper/PartyRelationTransferMapper.java` | 转接记录 MyBatis-Plus Mapper |
| **新建 Mapper**| `backend/.../mapper/PartyPositionAdjustmentMapper.java`| 职务调整 MyBatis-Plus Mapper |
| **新建 Controller**| `backend/.../controller/TransferController.java` | 转入转出、职务调整及花名册联动接口 |
| **修改 Controller**| `backend/.../controller/AuthController.java` | 实现手机号、工号、用户名多凭证登录鉴权 |
| **修改 Controller**| `backend/.../controller/MemberController.java` | 增加转出过滤与 25 步全流程据实同步逻辑 |
| **修改前端配置**| `frontend/src/data/companyDepartments.js` | 扩展公司部门分组、跨单位检测与公司名解析工具 |
| **修改前端数据**| `frontend/src/data/mockData.js` | 补齐转接记录与职务调整备案演示台账 |
| **修改样式** | `frontend/src/style.css` | 全局表单标签与复选框防换行样式规则 |
| **修改视图** | `frontend/src/components/LoginView.vue` | 登录账号输入框提示升级为支持手机号/工号/用户名 |
| **修改主工程**| `frontend/src/App.vue` | 增加转接调整备案Tab、解耦跨单位、全流程同步与排版优化 |
| **修改文档** | `README.md` | 更新 7 大模块架构与核心技术特性 |
| **修改文档** | `docs/系统使用与部署运维手册.md` | 更新业务使用操作规程与多凭证登录运维说明 |
| **修改文档** | `docs/国企智慧党建数字化平台设计方案.md` | 更新平台功能清单、解耦模型与新表 DDL 说明 |
| **新建文档** | `docs/UPGRADE-RECORD-2026-10-11.md` | 本次全量升级技术与业务记录 |

---

## 四、 编译构建与验证结果

1. **后端构建验证**：
   - 执行 `mvn test-compile`：
   - 结果：`[INFO] BUILD SUCCESS`，73 个 Java 源码文件编译测试 100% 通过。
2. **前端打包验证**：
   - 执行 `npm run build`：
   - 结果：`✓ built in 861ms`，Vite 8.3.4 生产打包成功，0 错误，0 语法警告。
3. **业务逻辑验证**：
   - 新申请人建档 ➔ 即刻进入花名册（申请人状态）且账号立即可用；
   - 推进至积极分子、发展对象、预备党员、转正 ➔ 花名册状态据实逐级晋升；
   - 转入党员 ➔ 自动同步加入花名册；
   - 转出党员 ➔ 自动从花名册除名注销；
   - 职务调整 ➔ 记录批文号并自动同步更新花名册党员职务；
   - 跨公司选部门 ➔ 跨单位标签正常展示并提示友好；
   - 手机号登录 ➔ 后端与前端鉴权均可秒级识别登录。
