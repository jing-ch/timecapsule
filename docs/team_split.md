# Time Capsule — 团队分工文档

> 项目周期：10 天 ｜ 团队：4 人 ｜ 技术栈：Java + MVVM + Firebase

---

## 分工总览

| 成员 | 负责模块 | 核心关键词 |
|------|----------|------------|
| A | Auth + 项目架构 | Firebase Auth、Firestore 数据结构、共用工具类 |
| B | 胶囊读取 & 展示 | 列表页、详情页、搜索、锁定弹窗 |
| C | 胶囊创建 & 写入 | 3 步向导、媒体上传、Firestore 写入 |
| D | 通知 & 定位 | FCM、Cloud Functions、FusedLocation |

> **切分原则：** B 负责所有"读"相关功能，C 负责所有"写"相关功能，两人 ViewModel 层互不交叉，依赖 A 在 D1 定好的数据结构并行开发。

---

## 成员 A — Auth + 项目架构

### 职责范围
- 项目初始化（Android 工程结构、Gradle 依赖、Git 仓库）
- Firebase 接入（Authentication、Firestore、Storage、FCM）
- **Firestore 数据结构定义**（胶囊文档字段、类型，D1 必须完成）
- 注册 / 登录 / 登出 / 会话持久化
- MVVM 基础架构（BaseViewModel、Repository 基类、LiveData 约定）
- 共用工具类（DateUtils、ErrorHandler、Constants）
- D8–D9 集成兜底、跨模块 bug 修复

### 任务清单
- [ ] 创建 Android 项目，配置 `google-services.json`
- [ ] 接入 Firebase Auth，实现邮箱注册 & 登录
- [ ] 定义 Firestore Capsule 文档结构（见下方约定）
- [ ] 搭建 MVVM 基础层（ViewModel / Repository / LiveData）
- [ ] 编写 DateUtils（Timestamp ↔ 显示文本互转）
- [ ] D4 起可协助其他成员排查问题

---

## 成员 B — 胶囊读取 & 展示

### 职责范围
所有从 Firestore **读取**数据并展示给用户的页面和组件。

- 胶囊列表页（首页）
- 锁定中胶囊弹窗（含倒计时）
- 已解锁胶囊详情页
- 搜索栏
- 删除二次确认弹窗
- 读取数据的 ViewModel & Repository

### 任务清单
- [ ] 列表页 UI：胶囊卡片、排序（已解锁在上 / 锁定在下）
- [ ] 从 Firestore 实时监听当前用户的胶囊列表
- [ ] 搜索栏：按标题过滤列表
- [ ] 锁定弹窗：显示解锁时间、倒计时（CountDownTimer）
- [ ] 已解锁详情页：展示标题、正文、图片/视频（Picasso 加载）、位置、创建时间
- [ ] 删除确认弹窗 + 调用删除接口（Firestore delete + Storage delete）
- [ ] CapsuleListViewModel & CapsuleDetailViewModel

### 注意
- 图片加载使用 **Picasso**，URL 由 C 写入 Firestore，格式见下方约定
- 视频播放可用 `VideoView`，MVP 阶段不需要自定义播放器

---

## 成员 C — 胶囊创建 & 写入

### 职责范围
所有**写入**数据到 Firestore / Storage 的流程，即三步创建向导的完整实现。

- Step 1：媒体上传 + 标题输入
- Step 2：正文编写 + 位置输入
- Step 3：日历选择器 + 隐私设置 + 最终提交
- CreateCapsuleViewModel & Repository 写入逻辑

### 任务清单
- [ ] 三步向导 Activity / Fragment + 进度条
- [ ] Step 1：图片/视频选择（系统相册）+ 上传到 Firebase Storage，获取下载 URL
- [ ] Step 1：标题输入（必填校验）
- [ ] Step 2：正文输入框，字数实时计数（上限 1000 字）
- [ ] Step 2：位置输入框（手动文字输入，可选可删除；D 封装好定位模块后接入）
- [ ] Step 3：日历 + 时间选择器（DatePickerDialog + TimePicker，精确到小时）
- [ ] Step 3：Private / Public 单选
- [ ] 点击"+ Create"→ 写入 Firestore → Toast 提示 → 返回列表页
- [ ] CreateCapsuleViewModel（持有草稿状态，跨步骤不丢失）

### 注意
- 媒体上传完成后才能拿到 URL，再一次性写入 Firestore，避免文档字段缺失
- 位置字段可先用手动输入占位，D 完成定位模块后替换

---

## 成员 D — 通知 & 定位

### 职责范围
- FCM 推送通知（接入 + token 管理 + 点击跳转）
- Cloud Functions（定时任务：检测解锁时间到期，触发推送）
- 设备定位模块封装（交给 C 集成到创建流程）

### 任务清单
- [ ] 接入 FCM，获取并存储设备 token（写入 Firestore 用户文档）
- [ ] 实现 `MyFirebaseMessagingService`，处理前台 / 后台通知
- [ ] Cloud Function（Node.js）：定时轮询 Firestore，对到期胶囊发送推送
- [ ] 通知点击 → 解析 capsuleId → 跳转详情页（Intent + Deep Link）
- [ ] `LocationHelper` 封装：FusedLocationProviderClient 获取坐标 → Geocoder 转地名
- [ ] 将 `LocationHelper` 提供给 C，接入 Step 2 位置输入

### 注意
- Cloud Functions 可先在本地用 Firebase Emulator 调试，不需要先部署
- 定位权限申请逻辑在 `LocationHelper` 内部处理，C 只需调用

---

## D1 必须敲定的接口约定

> 所有人在 D2 开始编码前，必须对齐以下约定。由 **A** 负责定义并同步给全员。

### Firestore Capsule 文档结构

```
capsules/{capsuleId}
├── userId        : String       // 创建者 UID
├── title         : String       // 标题
├── content       : String       // 正文
├── mediaUrl      : String?      // Firebase Storage 下载 URL（无媒体则为 null）
├── mediaType     : String?      // "image" | "video" | null
├── location      : String?      // 地名文字（无位置则为 null）
├── unlockTime    : Timestamp    // 解锁时间
├── isPublic      : Boolean      // true = Public / false = Private
└── createdAt     : Timestamp    // 创建时间
```

### 其他约定

| 约定项 | 规则 |
|--------|------|
| Storage 路径 | `media/{userId}/{capsuleId}/{filename}` |
| 解锁判断 | `unlockTime.toDate() <= Date()` 即为已解锁 |
| FCM token 存储 | `users/{userId}/fcmToken : String` |
| 列表查询条件 | `where("userId", "==", currentUid).orderBy("createdAt", DESC)` |

---

## 10 天时间线

```
        D1      D2      D3      D4      D5      D6      D7      D8      D9      D10
A    [架构&Auth────────][ 协助 ]                [ 协助 ][────集成&修复────────][Demo]
B            [──────────读取模块开发──────────────][──联调──][  修复  ][ 打磨 ]
C            [──────────写入模块开发──────────────][──联调──][  修复  ][ 打磨 ]
D                    [──────────通知&定位开发──────────────][  集成  ][ 打磨 ]
```

### 关键节点

| 时间 | 事项 |
|------|------|
| D1 结束 | A 完成项目骨架 & Firestore 结构定义，push 到 main，全员 pull |
| D6 结束 | B、C、D 各自模块功能自测通过 |
| D7 | B + C 读写联调：C 创建的胶囊，B 能正确读出并展示 |
| D8–D9 | 全链路集成：通知跳转、定位接入创建流程、端到端走查 |
| D10 | Demo 准备，不再合并新功能 |

---

## 分支管理建议

```
main
├── feature/auth          → A
├── feature/capsule-read  → B
├── feature/capsule-write → C
└── feature/notification  → D
```

- 各分支独立开发，D7 联调前合并到 `main`
- Merge 前跑一遍完整用户流程，冲突由 A 处理
