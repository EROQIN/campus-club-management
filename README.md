# 校园社团管理平台

基于 Spring Boot 与 Vue 3 的全栈应用，实现《需求规格.md》中定义的核心 MVP，覆盖用户入驻、社团展示、活动管理、消息通知以及资源共享等场景。

## 技术栈
- 后端：Java 21、Spring Boot 3.5、Spring Security（JWT）、Spring Data JPA、MySQL
- 前端：Vite + Vue 3 + TypeScript、Pinia、Vue Router、Element Plus

## 快速开始

### 环境依赖
- JDK 21+
- Maven 3.9+
- Node.js 18+（npm 或 pnpm）
- MySQL 8+（默认库名 `campus_club`）

### 后端启动
1. 配置数据库、安全参数与对象存储凭证（可通过环境变量覆盖）：
   ```bash
   export DB_URL="jdbc:mysql://localhost:3306/campus_club?useSSL=false&serverTimezone=Asia/Shanghai"
   export DB_USERNAME="root"
   export DB_PASSWORD="root"
   export JWT_SECRET="replace-with-32-char-secret"
   # 启用 OSS 集成并使用环境变量凭证（默认关闭）
   export ALIYUN_OSS_ENABLED=true
   export ALIYUN_OSS_ENDPOINT="https://oss-cn-hangzhou.aliyuncs.com"
   export ALIYUN_OSS_REGION="cn-hangzhou"
   export ALIYUN_OSS_BUCKET="your-bucket-name"
   export ALIYUN_OSS_BASE_PATH="campus-club/dev"
   export OSS_ACCESS_KEY_ID="your-access-key-id"
   export OSS_ACCESS_KEY_SECRET="your-access-key-secret"
   ```
2. 启动 Spring Boot 服务：
   ```bash
   mvn spring-boot:run
   ```
3. 默认内置账号（首次启动自动注入）：
   - 系统管理员：`admin@campus.test` / `Admin123`
   - 社团负责人示例：`manager@campus.test` / `Manager123`
4. 如需手动初始化数据库或同步结构，可执行 `sql/schema.sql` 中的建表语句；从旧版本升级时，请确保将 `messages` 表中的 `read` 列重命名为 `is_read` 并新增 `resource_applications`、`club_announcements` 等表。

### 前端启动
```bash
cd frontend
npm install
npm run dev
```
默认开发地址 <http://localhost:5173>，后端 API 基础路径 <http://localhost:8123>。

### 阿里云 OSS 集成说明
- 框架使用 `OSS_ACCESS_KEY_ID` 与 `OSS_ACCESS_KEY_SECRET` 环境变量初始化客户端，便于与容器服务或 RAM 用户策略对接。
- 未在上传时单独设置 `Object ACL`，全部对象继承 Bucket ACL，权限调整可在 Bucket 级统一修改。
- `ALIYUN_OSS_PUBLIC_DOMAIN` 支持配置自定义域名/CDN，加速二维码、图片等资源访问；留空时自动构造 `bucket.endpoint` 访问路径。
- `FileStorageService` 暴露上传、删除、公共 URL 以及临时下载 URL 能力，可直接用于二维码签到、社团 Logo/短视频等文件流场景。

## 已实现能力
- **认证与权限**：注册时绑定兴趣标签，JWT 登录，基于角色（学生/负责人/团委/系统管理员）动态展示菜单
- **功能总览**：提供按角色切换的功能地图、流程指引与实时关键指标，帮助管理者和用户快速熟悉能力入口
- **社团管理**：社团浏览、搜索、创建与编辑，查看详情，申请加入及审核流程，近期活动时间线
- **活动管理**：活动发布与查询、学生活动报名、负责人审核报名
- **消息中心**：站内通知列表，支持未读标记和状态同步
- **资源共享**：负责人发布/更新共享资源，成员线上申请、负责人/团委审批与消息通知
- **社团管理面板**：负责人在“社团管理”查看成员名单、处理入团申请、发布/编辑活动、推送公告
- **数据看板**：核心指标卡片、活跃社团 Top 5、活动趋势及类别分布统计
- **初始化数据**：兴趣标签、管理员/负责人示例账号、示例社团与活动
- **系统管理**：管理员可检索用户、切换启用状态并分配角色；自动为普通用户提供兴趣匹配的社团推荐

## 手动验证清单
1. **功能总览**
   - 登录后访问“功能总览”，切换不同角色视角，查看功能卡片、流程及统计卡片
2. **注册 / 登录**
   - 访问 `/register`，填写信息并选择 3–5 个兴趣标签，确认自动登录成功
   - 退出并通过 `/login` 重新登录，验证流程无误
3. **社团管理**
   - 使用负责人账号在“社团广场”新建社团
   - 学生账号申请加入，负责人可在“社团管理 > 入团审核”中审批，并在“成员管理”查看变更
4. **活动流程**
   - 负责人在“社团管理 > 活动管理”或“活动日程”发布新活动，学生端确认列表可见
   - 学生报名后消息中心出现通知，负责人可审批
5. **消息与资源**
   - 审批通过/拒绝后消息列表更新，并可标记已读
   - 学生在“资源共享”申请资源，负责人/团委在“申请处理”标签中完成审批；负责人可在“社团管理 > 消息公告”推送公告
6. **数据看板**
   - 管理员登录查看“数据总览”，验证指标与表格正常加载
7. **系统管理（管理员）**
   - 访问“账号管理”，检索用户、切换角色与启用状态并验证接口返回

## 测试
- 后端编译校验：`mvn -DskipTests compile`
- 前端类型与构建校验：`cd frontend && npm run build`

## 项目结构
```
├── src/main/java/com/erokin/campusclubmanagement
│   ├── config/          # 跨域、JPA 配置及数据初始化
│   ├── controller/      # REST 控制器
│   ├── dto/             # 请求/响应数据传输对象
│   ├── entity/          # JPA 实体定义
│   ├── repository/      # Spring Data 仓储接口
│   ├── security/        # JWT 与安全配置
│   └── service/         # 业务服务实现
├── frontend/            # Vue 3 单页应用源码
└── docs/mvp-scope.md    # MVP 范围说明
```

## 后续迭代建议
- 引入智能推荐与匹配算法，提升招新体验
- 接入实际文件存储与字幕生成服务（OSS / AI API）
- 补充自动化测试（Spring MockMvc、Cypress 等）
- 拓展数据可视化维度与报表导出功能
