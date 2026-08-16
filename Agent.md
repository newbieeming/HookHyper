# HookHyper Agent 开发指南

本文档约束在 HookHyper 仓库中工作的开发者与自动化 Agent。开始修改前先阅读相关模块源码；实现、构建脚本与资源文件是事实来源，本文档与代码冲突时应同步修正文档。

## 项目定位

HookHyper 是面向小米 HyperOS 的 Xposed 模块，基于 YukiHookAPI 开发。项目采用 Kotlin、Hilt、MVI、Jetpack Compose、Navigation 3，并同时支持 MIUIX 与 Material 3 界面。

核心约定：一个 `feature` 模块对应一个目标应用，例如：

- `feature:systemui` → `com.android.systemui`
- `feature:settings` → `com.android.settings`

每个 feature 自己维护目标应用的页面、状态、设置项和 Hook；跨 feature 的稳定能力放到 `core`，应用装配与唯一 Xposed 入口放到 `app`。

## 模块职责与依赖方向

| 模块 | 职责 |
| --- | --- |
| `app` | Application、Activity 入口、Navigation 3、应用级 MVI、独立 Screen、唯一 Hook 入口及 feature 装配 |
| `core:model` | 纯 Kotlin 模型、设置键、可复用匹配规则；不得依赖 Android UI |
| `core:data` | YukiHookPrefsBridge 设置读写、模块连接状态、需要 Root 的通用操作 |
| `core:ui` | MVI 基类、主题、通用 Compose 组件、`FeatureEntry` 契约 |
| `feature:<name>` | 单个目标应用的 UI、State/Intent/Effect、ViewModel、Hilt 注册和 Hooker |
| `build-logic` | SDK、Java 版本与 Android/Kotlin/Compose/Hilt 约定插件 |

依赖应保持单向：`app → feature → core`。feature 之间不得互相依赖；只有被至少两个模块复用且职责稳定的代码才应下沉到 `core`，不要为“可能复用”提前抽象。

### App UI 目录约定

`app/src/main/java/com/newbieeming/hookhyper` 的界面层按职责拆分：

| 路径 | 职责 |
| --- | --- |
| `MainActivity.kt` | 只处理 Activity 生命周期、Edge-to-Edge 和 `setContent` |
| `ui/app` | `AppState`、`AppIntent`、`AppEffect`、`AppViewModel` 与应用级 Compose 宿主 |
| `ui/navigation` | Navigation 3 的强类型路由 |
| `ui/home` | 主页 Screen 与目标应用列表 |
| `ui/settings` | 全局设置、模块连接状态、界面风格与关于信息 |
| `ui/feature` | feature 导航相关的宿主/兜底页面，不放具体目标应用实现 |
| `ui/component` | 仅供 `app` 内多个 Screen 复用的 Scaffold 和自适应组件 |

应用级数据流保持单向：Screen 展示 `AppState`，用户操作转换为 `AppIntent`，`AppViewModel` 更新 State 或发出一次性 `AppEffect`。导航栈由 `HookHyperApp` 持有；Screen 不直接修改导航栈，只通过回调表达导航意图。

## 实现规范

### Feature 与 UI

- 每个 feature 提供一个实现 `FeatureEntry` 的入口，`metadata.id` 必须全局唯一，包名应定义为该入口的常量。
- 使用 Hilt `@Binds`、`@IntoSet` 注册 `FeatureEntry`；宿主通过集合注入生成主页列表，不维护重复的 UI 清单，也不扫描 Dex。
- `MainActivity` 只负责 Activity 生命周期与 `setContent`；应用级 MVI 放在 `ui/app`，路由放在 `ui/navigation`，页面按 `ui/<screen>` 分包，跨页面组件放在 `ui/component`。
- 页面状态采用不可变 `State`，用户操作使用 `Intent`，一次性事件使用 `Effect`；ViewModel 继承 `MviViewModel`。
- Composable 只负责渲染状态和分发 Intent。状态收集使用 `collectAsStateWithLifecycle()`，副作用在合适的 effect/协程作用域中处理。
- 通用设置控件优先复用 `core:ui`，并确保 MIUIX 与 Material 两种主题均可用。不要在 feature 中复制全局 Scaffold、主题或 MVI 基础设施。
- 可见文案不得硬编码。默认资源放在 `values/strings.xml`（英文），简体中文放在 `values-zh-rCN/strings.xml`；新增、修改或删除文案时同步维护两份资源。

### Edge-to-Edge 与 Insets

- `MainActivity` 已启用 `enableEdgeToEdge()`，系统栏间距必须通过 Compose `WindowInsets` 统一处理，不使用固定 dp 模拟状态栏或导航栏高度。
- 应用存在外层 `AppScaffold` 与 Screen/Feature 内层 Scaffold。外层传给 `NavDisplay` 的 `PaddingValues` 必须先通过 `Modifier.padding(padding)` 应用，再通过 `Modifier.consumeWindowInsets(padding)` 标记为已消费。
- `SystemSettingsTopBar` 负责自己的 `WindowInsets.statusBars`。不要在 Screen、`NavDisplay` 或其他父容器重复添加 `statusBarsPadding()` / `windowInsetsPadding(WindowInsets.statusBars)`。
- 新增或调整 Scaffold 时，同时检查顶部状态栏、底部导航栏、横屏和手势导航；MIUIX 与 Material 两种界面风格都要验证。

### 设置与 Hook

- 设置键集中维护在 `core:model` 的 `PreferenceKeys` 或对应领域模型中，命名需带 feature 前缀，避免跨模块冲突。
- App 进程通过 `HookPreferencesRepository` 读写设置；Hook 进程使用相同的 `PreferenceKeys.FILE_NAME` 和设置键读取 YukiHookPrefsBridge。
- Hooker 继承 `YukiBaseHooker`，必须通过 `loadApp(name = ...)` 限定目标包。新增 Hook 默认应由设置项控制，未启用时尽早返回。
- HyperOS 内部类和方法可能随版本变化。反射或 Hook 失败应限制在单个功能内，使用 `runCatching` 与带 feature 名称的日志标签记录，不能导致目标进程因非关键功能崩溃。
- 不在 Hook 热路径执行阻塞 I/O、长耗时遍历或无界重试；不要持有目标进程中 Activity/View 的长期引用。
- 设置变化若需重启目标应用才生效，UI 文案必须明确提示；调用通用重启能力时要保留 Root 缺失和执行失败的反馈。

## 新增 feature 检查清单

新增 `feature:<name>` 时，至少完成以下事项：

1. 在 `settings.gradle.kts` 中 `include(":feature:<name>")`，创建模块构建脚本并复用 `build-logic` 中的约定插件。
2. 在 `app/build.gradle.kts` 添加对新 feature 的 `implementation(project(...))` 依赖。
3. 创建 `FeatureEntry` 实现，提供唯一 ID、目标包名、名称、描述和页面内容。
4. 创建 Hilt Module，通过 `@IntoSet` 绑定 `FeatureEntry`。
5. 创建目标应用的 Hooker，并在 `app/.../hook/HookEntry.kt` 的 `YukiHookAPI.encase(...)` 中显式注册。
6. 将目标包加入 `app/src/main/res/values/arrays.xml` 的 `xposed_scope`；如 App 需要查询目标应用信息，同时更新 `AndroidManifest.xml` 的 `<queries>`。
7. 在 `core:model` 添加共享设置键，在 feature 内补齐 State、Intent、Effect、ViewModel、Screen 和设置读写。
8. 同步添加英文与简体中文资源，并为纯逻辑、匹配规则和状态转换补充测试。
9. 检查根 `.gitignore` 是否已覆盖新模块产物；只有出现新的模块专属生成物时才添加更具体的忽略规则，不提交 `build/`、本地缓存、机器配置或密钥。
10. 运行相关模块测试，并至少完成一次 Debug APK 构建。

## 构建与验证

构建基线由仓库配置决定：Gradle 9.5、JDK 21、compile SDK 37、target SDK 36、min SDK 24，Java/Kotlin JVM 字节码目标为 17。

Windows：

```powershell
.\gradlew.bat :core:model:test :app:testDebugUnitTest :app:assembleDebug
```

macOS / Linux：

```bash
./gradlew :core:model:test :app:testDebugUnitTest :app:assembleDebug
```

按改动范围选择验证方式：

- 文档或资源：检查 Markdown、资源引用和中英文键是否成对。
- `core:model` 纯逻辑：运行对应单元测试。
- UI 或 feature：运行相关单元测试与 `:app:assembleDebug`，并检查 MIUIX/Material、状态栏 Insets、底部导航和返回行为。
- Hook、依赖或构建脚本：运行完整命令，并在真实目标进程验证启用、禁用、重启和失败路径。

Debug APK 位于 `app/build/outputs/apk/debug/app-debug.apk`。

## 变更原则

- 修改前先搜索现有实现与调用点，保持现有命名、包结构和格式风格。
- 优先做范围明确、可验证的改动；不要顺带重构无关代码，也不要覆盖工作区中已有的用户修改。
- 更新架构、构建要求、功能或注册流程时，同步维护 `README.md` 和本文档。
- 不提交本地路径、调试产物、账户信息或新的签名凭据。涉及签名与发布配置的变更必须明确说明风险。
- 完成后报告改动内容、执行过的验证，以及仍需真机或特定 HyperOS 版本确认的事项。
