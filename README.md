# HookHyper

HookHyper 是面向小米 HyperOS 的 Xposed 模块，基于 FeatHyper 重构，用于补全系统功能并改善界面体验。项目以 YukiHookAPI 实现 Hook，以 Hilt、MVI、Jetpack Compose 和 Navigation 3 组织应用，并支持 MIUIX 与 Material 3 两种界面风格。

> [!WARNING]
> Xposed Hook 依赖具体的 HyperOS 系统实现。系统升级可能导致部分功能失效；使用前请确保具备恢复环境的能力，并自行承担修改系统应用行为的风险。

## 当前功能

| 目标应用 | 作用域 | 功能 |
| --- | --- | --- |
| 系统界面 | `com.android.systemui` | 在锁屏状态栏显示 SIM 运营商名称；为通知栏和控制中心强制启用柔光玻璃效果；自定义状态栏时间格式（含 AM/PM 前缀选项）；替换指纹解锁图标样式 |
| 系统设置 | `com.android.settings` | 自定义”关于手机”中的设备名称、处理器、内存、电池、分辨率、屏幕尺寸、OS 版本、摄像头和基带信息 |

设置会通过 YukiHookPrefsBridge 与被 Hook 进程共享。功能开关或字段修改后，可在对应 feature 页面使用“重启应用”使其生效；该操作需要 Root 权限。

## 使用要求

- 小米 / Redmi / POCO 的 HyperOS 设备
- 已获取 Root 权限
- 已安装并正常运行兼容的 Xposed 框架（例如 LSPosed）
- Xposed API 版本不低于 93

不同机型、地区版本和 HyperOS 版本的内部实现可能不同，以上条件不代表所有功能都一定兼容。

## 安装与使用

1. 安装构建得到的 APK。
2. 在 LSPosed 中启用 HookHyper。
3. 确认作用域包含“系统界面”和“系统设置”。
4. 重启对应目标应用；首次安装或框架状态异常时建议重启设备。
5. 打开 HookHyper，在主页进入对应 feature，启用并配置所需功能。
6. 按页面提示重启目标应用，使 Hook 重新加载设置。

可在应用的“设置”页查看 LSPosed 连接状态，并在 MIUIX 与 Material 两种界面风格间切换。

## 项目结构

```text
HookHyper/
├─ app/                    # Activity 入口、应用级 MVI、导航、独立 Screen、Xposed 入口
│  └─ src/main/java/com/newbieeming/hookhyper/
│     ├─ MainActivity.kt       # Edge-to-Edge 与 Compose 启动入口
│     ├─ hook/                 # 唯一 Xposed Hook 入口（HookEntry）
│     └─ ui/
│        ├─ app/               # MVI Contract、ViewModel、应用级 Compose 宿主
│        ├─ navigation/        # Navigation 3 强类型路由
│        ├─ home/              # 主页 Screen
│        ├─ settings/          # 全局设置 Screen
│        ├─ feature/           # Feature 导航兜底页面
│        └─ component/         # App 内共享 Scaffold 与自适应组件
├─ core/
│  ├─ common/              # 跨模块共享的模型、常量、枚举和系统工具
│  ├─ data/                # 跨进程偏好仓库、模块状态、Root 应用重启
│  ├─ ui/                  # MVI 基类、主题、跨 Feature 通用组件
│  │  └─ feature/          # FeatureEntry 契约、FeatureEntryImpl、FeatureViewModel
│  │                       # FeatureScreen、LocalFeatureViewModel、HookFeatureScreen
│  │  └─ component/        # HookCategory、HookDef、FeatureHook、HookSwitchPreference
│  └─ hook/                # @HookModule 注解、SubHooker 接口、ModularHooker 基类
│  └─ hook-ksp-processor/  # KSP 处理器：编译期扫描 @HookModule，自动生成 HookRegistry
├─ feature/
│  ├─ systemui/            # com.android.systemui 的 UI、状态与 Hook 子模块
│  └─ settings/            # com.android.settings 的 UI、状态与 Hook 子模块
├─ build-logic/            # Gradle Convention Plugins 与统一构建配置
└─ gradle/                 # Version Catalog 与 Gradle Wrapper
```

一个 `feature` 对应一个目标应用。每个 feature 通过 Hilt `@IntoSet` 注册 `FeatureEntry`，宿主据此生成主页列表，无需维护重复的 UI 清单或运行时扫描 Dex。Xposed Hook 由 `app` 中唯一的 `HookEntry` 统一装配。

### Hook 模块化架构

每个 feature 的 Hook 逻辑通过 `@HookModule` 注解 + KSP 自动生成，`HookEntry` 通过 `ServiceLoader` 自动发现所有模块：

```text
HookEntry (app)  ── ServiceLoader.load(Registrar) ── 自动发现所有 feature 模块
  ├─ SystemuiHooker (KSP 自动生成)  ─── 遍历 HookRegistry
  │   ├─ LockScreenCarrierHook    @HookModule(packageName = "com.android.systemui")
  │   ├─ SoftLightGlassHook
  │   ├─ TimeFormatHook
  │   └─ FingerprintIconHook
  └─ SettingsHooker (KSP 自动生成)  ─── 遍历 HookRegistry
      └─ DeviceInfoHook            @HookModule(packageName = "com.android.settings")
```

- `@HookModule` 注解仅声明目标包名，KSP 据此生成 `HookRegistry`、主 Hooker 和 `Registrar`。
- Hook 类实现 `SubHooker`（运行时 Hook 逻辑）和 `FeatureHook<T>`（UI 元数据），`preferenceKey` 从实例读取。
- `HookEntry` 通过 `ServiceLoader` 扫描所有 `Registrar` 实现，无需手动注册。
- 新增功能只需实现 `SubHooker` + `@HookModule` + `FeatureHook<T>`，新增 feature 模块只需应用 `hook.module` 插件。

## 应用 UI 架构

`MainActivity` 只负责 Activity 生命周期、`enableEdgeToEdge()` 与 `setContent`。`HookHyperApp` 收集 `AppViewModel` 暴露的状态、持有 Navigation 3 返回栈，并将状态和事件处理函数传给独立 Screen。

应用级 MVI 数据流如下：

```text
用户操作 → AppIntent → AppViewModel → AppState → HookHyperApp / Screen
                               └────→ AppEffect（一次性事件）
```

- `HomeScreen` 展示 Hilt 多绑定得到的 feature 列表，并通过回调请求导航。
- `SettingsScreen` 展示模块连接状态、界面风格和关于信息，所有持久化操作通过 `AppIntent` 交给 ViewModel。
- feature 自己维护 State、Intent、Effect、ViewModel 与 Screen，再通过 `FeatureEntry.Content` 接入应用导航。
- MIUIX 与 Material 共享同一份状态和导航，只在 Scaffold、导航栏和基础组件层进行自适应渲染。

### Feature 架构

每个 feature 通过继承 `FeatureEntryImpl` 实现，只需提供 `metadata` 和 `provideViewModel`：

```text
FeatureEntryImpl (core:ui)
  ├─ hooks：自动探测 HookRegistry.modules（反射扫描子包）
  ├─ Content：provideViewModel → FeatureScreen
  └─ FeatureScreen
       ├─ 收集 FeatureViewModel 状态（isRestarting、effects）
       ├─ 提供 LocalPreferencesRepository、LocalFeatureViewModel
       └─ HookFeatureScreen
            └─ 按 HookCategory 分组 → stickyHeader 磁吸 + 折叠
                 └─ 每个 HookContent.Content() 自提供 UI
```

- `FeatureViewModel` 封装公共重启逻辑与 effect 通道，子类 override `packageName` 和 `restartSuccessMessage`。
- `FeatureScreen` 自动处理 ViewModel 状态收集、Snackbar effect 和 CompositionLocal 注入。
- `LocalFeatureViewModel` 供 hook UI 获取当前 ViewModel，配合 `featureViewModel<T>()` 类型安全访问。
- `HookCategory` 定义分类（锁屏、状态栏、通知栏等），带 `id`、`order`、`titleResId`。
- `HookDef` 定义 hook 元数据（`preferenceKey`、`category`、`order`），各 feature 枚举实现。
- `FeatureHook<T>` 统一 `SubHooker` + `HookContent` + `HookDef`，hook 类只需声明 `def` 枚举条目。

应用使用嵌套 Scaffold：外层负责底部导航，Screen/Feature 内层负责顶部栏和内容。外层 Padding 在传入 `NavDisplay` 后会通过 `consumeWindowInsets` 消费，避免内层 `SystemSettingsTopBar` 再次应用状态栏 Insets，造成顶部间距翻倍。

## 技术栈

- Kotlin 2.2.10、KSP 2.2.10-2.0.2
- Android Gradle Plugin 9.2.1、Gradle 9.5
- YukiHookAPI 1.3.2、KavaRef 1.0.2
- Hilt 2.60、MVI、Kotlin Coroutines
- Jetpack Compose、Material 3、Navigation 3
- MIUIX 0.9.1

MIUIX 仍处于快速迭代阶段。升级时需要同时验证 Kotlin、Compose、KSP 及 MIUIX 的二进制兼容性。

## 本地构建

### 环境

- JDK 21（Gradle Daemon Toolchain）
- Android SDK 37
- Windows、macOS 或 Linux
- Android Studio 需安装以下插件（Settings → Plugins）：
  - **detekt**：Kotlin 静态分析，检查代码复杂度、命名规范、潜在 Bug 等，提供 IDE 内联告警
  - **Spotless Gradle**：代码格式检查，基于 ktlint 统一代码风格，提交前自动格式化

项目的 compile SDK 为 37、target SDK 为 36、min SDK 为 24，Java/Kotlin JVM 字节码目标为 17。依赖版本集中维护在 `gradle/libs.versions.toml`，SDK 与 Java 版本集中维护在 `build-logic/convention/src/main/kotlin/DevKitBuildConfig.kt`。

### 命令

Windows：

```powershell
.\gradlew.bat :core:common:test :feature:settings:testDebugUnitTest :app:testDebugUnitTest :app:assembleDebug
```

macOS / Linux：

```bash
./gradlew :core:common:test :feature:settings:testDebugUnitTest :app:testDebugUnitTest :app:assembleDebug
```

Debug APK 输出到：

```text
app/build/outputs/apk/debug/app-debug.apk
```

## 新增目标应用

新增 `feature:<name>` 时需要同时完成以下装配：

1. 在 `settings.gradle.kts` 注册模块，并在 `app/build.gradle.kts` 添加依赖。
2. 创建 `FeatureEntry` 实现，继承 `FeatureEntryImpl`，提供 `metadata` 和 `provideViewModel`。通过 Hilt `@IntoSet` 注册。
3. 创建 `FeatureViewModel` 子类，override `packageName` 和 `restartSuccessMessage`。
4. 在 feature 模块的 `build.gradle.kts` 中应用 `alias(libs.plugins.hook.module)` 插件。
5. 定义 `HookCategory` 枚举（分类）和 `HookDef` 枚举（hook 元数据）。
6. 为每个 Hook 功能创建类，实现 `SubHooker` + `FeatureHook<T>`，加上 `@HookModule(packageName)` 注解。KSP 会自动生成 `HookRegistry`、主 Hooker 和 `Registrar`。
7. 在 `HookContent.Content()` 中实现该 hook 的设置 UI，可通过 `featureViewModel<T>()` 获取 ViewModel。
8. 将目标包加入 `xposed_scope`；如果宿主需要读取应用信息，同时更新 Manifest 的 `<queries>`。
9. 添加必要测试，运行单元测试和 Debug 构建。

`HookEntry` 通过 `ServiceLoader` 自动发现所有 `Registrar`，无需手动注册新模块的 Hooker。`FeatureEntryImpl` 自动探测 `HookRegistry` 并实现 `Content()`，无需手动编写 Screen。

更完整的开发约束与检查清单见 [Agent.md](Agent.md)。

## 开发说明

- `core` 只承载原子化、跨 feature 复用的模型、数据能力和 UI 组件；feature 之间不直接依赖。
- 一个 feature 被移除后，`core` 不应保留该目标应用的偏好键、字段模型、匹配规则、资源、表单、页面组合或测试；这些内容属于对应 feature。
- `MainActivity` 不承载页面布局；新增页面放入独立 `ui/<screen>` 包，并使用 State / Intent / Effect 组织交互。
- Edge-to-Edge 布局中，父层应用 Scaffold Padding 后必须正确消费 Insets，避免子层重复处理系统栏间距。
- Hook 以 `SubHooker` 子模块为单位组织，通过 `@HookModule` 注解声明目标包名，KSP 自动生成 `List<SubHooker>` 注册表；主 Hooker 遍历执行，失败应局部降级并记录日志，避免非关键功能导致系统进程崩溃。
- 设备信息匹配等 feature 专用纯逻辑应在对应 feature 模块中使用 JVM 单元测试覆盖。
- 提交前不要包含 `build/`、`.gradle/`、本地 SDK 配置或新的签名凭据。

## 免责声明

本项目仅用于学习和研究。刷机、Root、Xposed 模块以及系统进程 Hook 都可能带来稳定性或数据风险，请在理解风险后使用。
