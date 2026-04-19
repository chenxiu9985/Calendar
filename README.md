# Calendar App

这是一个基于 Jetpack Compose 的 Android 月历项目，目前核心功能已经完成：

- 进入应用默认显示当月
- 左右滑动切换月份
- 点击日期高亮，再点一次取消
- 顶部显示当前月份和“本月高亮天数”
- 高亮记录保存到本地，重新打开应用仍然存在

关键实现文件：

- `app/src/main/java/com/example/calendarapp/MainActivity.kt`
- `app/src/main/java/com/example/calendarapp/ui/CalendarViewModel.kt`
- `app/build.gradle.kts`

## 在 VS Code 中继续开发

当前项目适合直接作为一个 Gradle Android 工程导入 VS Code 编辑。仓库里已经补了 `.vscode` 工作区配置，建议安装这些扩展：

- `Extension Pack for Java`
- `Kotlin`
- `Gradle for Java`
- `Kotlin Formatter`

VS Code 中建议直接打开项目根目录：

- `C:\Users\26706\Desktop\日历`

可用任务：

- `Terminal > Run Task > Gradle Sync Check`
- `Terminal > Run Task > Assemble Debug APK`

## 当前状态

当前机器还没有完整 Android 构建环境，所以现在项目状态是：

- 代码完整，可继续修改
- 可被 Android Studio 或 VS Code 导入
- 还不能在本机稳定产出 APK

原因主要有两点：

- 缺少 Android SDK / Android Studio
- 当前环境对 Android Gradle Plugin 仓库解析还没跑通

另外，这个目录目前还没有 `gradlew` / `gradlew.bat` wrapper 文件，所以 VS Code 里的 Gradle 任务要等你补上 Gradle Wrapper 或在本机装好可用 Gradle 后再执行。

## 后续要想真正编译 APK

至少需要补齐下面几项：

1. 安装 Android Studio 或命令行 Android SDK
2. 配置 `ANDROID_HOME` 或 `ANDROID_SDK_ROOT`
3. 生成 Gradle Wrapper
4. 确认 `google()`、`mavenCentral()` 能正常访问
5. 执行 `assembleDebug`

如果你接下来想继续用 VS Code，我可以直接帮你把这个项目再补成更完整的“VS Code 开发版”，比如：

- 增加 `gradlew` wrapper
- 增加 `.gitignore`
- 补基础图标和应用名
- 加简单单元测试
- 优化 UI 细节和交互
