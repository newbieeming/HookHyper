# v1.0.3

## ✨ 新功能

- **超级岛尺寸**：SystemUI 新增“超级岛”分类，可自定义最小宽度与高度

## 🔧 改进

- **Feature 架构重构**：抽取公共 `FeatureEntryImpl`、`FeatureViewModel`、`FeatureScreen`，新增 feature 只需继承并提供 metadata + ViewModel
- **Hook UI 合并**：设备信息相关的三个 Hook（DeviceCard、VersionInfo、DeviceSettingsContent）合并为单一 `DeviceInfoHook`
- **Hook 自动发现**：`FeatureEntryImpl` 通过反射自动探测 `HookRegistry`，无需手动注册 hooks 列表
- **ViewModel 公共化**：重启逻辑、effect 通道、偏好注入统一由 `FeatureViewModel` 基类管理
- **Hook 注册表增量生成**：KSP 改为聚合生成，新增 Hook 不会导致同模块既有 Hook 从注册表中丢失

## 🏠 其他

- 更新 README 和 Agent.md 文档，反映新架构
- 移除 `AppState.features` 冗余依赖
- 更新版本号至 1.0.3

---

# v1.0.2

## ✨ 新功能

- **预测性返回手势**：支持系统预测性返回动画
- **HorizontalPager 重构 Tab 导航**：优化页面切换体验
- **补全 OS 版本信息**：新增 XMS 版本、RO XMS 版本字段

## 🔧 改进

- **系统设置展示原始值**：编辑设备信息时，输入框下方显示系统实际值，方便对比
- **我的设备"OS版本"同步**：优化版本信息的读取与展示逻辑

## 🐛 修复

- **拉起应用方式优化**：用 `am start -n` 替换 `monkey`，避免锁定自动旋转开关

## 🏠 其他

- 删除 `.idea` 目录并更新 `.gitignore`
- 更新版本号至 1.0.2

---

# v1.0.1

## ✨ 新功能

- **可预见性返回手势开关**：新增预测性返回手势的开关控制
- **状态栏时钟 AM/PM 标记**：状态栏时间显示追加 AM/PM 标识
- **完善设备信息字段**：新增认证型号、硬件版本、Android 版本、内核版本等字段

## 🔧 改进

- **设备信息字段匹配优化**：
  - 通过 stringsName 从应用资源读取实际字符串匹配
  - 处理器通过 GHz 区分简称与详情
  - 基带正则覆盖高通/联发科/展锐等平台
  - DeviceCardInfo.setValue hook 统一 title/value 匹配

## 🐛 修复

- **背景模糊刷新延时**：延长背景模糊刷新延时，避免闪烁

---

# v1.0.0

## 🎉 首次发布

- **设备信息修改**：支持修改"关于手机"中的设备硬件与系统信息
- **模块化架构**：基于 core 与 feature 模块的职责分离
- **输入框优化**：设置页面输入框支持键盘导航与焦点滚动
