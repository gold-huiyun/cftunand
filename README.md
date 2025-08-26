
# Cloudflared Android Tunnel App (v5-debug & CGO)

- 使用 **Android NDK + CGO** 为 Android/arm64 构建 cloudflared，可改善在 Android 上的 DNS 解析稳定性（与纯 Go resolver 相比）。参见 Go/NDK 交叉编译实践。  
- App 内提供 **实时滚动日志**、**固定 metrics 端口**（`--metrics 127.0.0.1:43100`），用于运行状态检测。  
- Manifest 已适配 **Android 14 前台服务类型权限** 与 **Android 13 通知运行时权限**。

## 参考
- Android 14 前台服务类型与权限：https://developer.android.com/about/versions/14/changes/fgs-types-required ；https://developer.android.com/develop/background-work/services/fgs/service-types
- Android 13 通知运行时权限：https://developer.android.com/develop/ui/views/notifications/notification-permission
- cloudflared 运行参数（logfile/metrics 等）：https://developers.cloudflare.com/cloudflare-one/connections/connect-networks/configure-tunnels/cloudflared-parameters/run-parameters/ ；https://fig.io/manual/cloudflared/tunnel
- Android Q+ 执行二进制放原生库目录：PSA 帖与说明（官方建议）：https://www.reddit.com/r/androiddev/comments/b2inbu/psa_android_q_blocks_executing_binaries_in_your/
- Go 在 Android 上使用 NDK/cgo 交叉编译：示例与讨论：https://stackoverflow.com/questions/74078344/how-to-cross-compile-from-linux-to-android-with-cgo ；https://jasonplayne.com/programming-2/how-to-cross-compile-golang-for-android

## 使用
- 运行 Actions → Build cloudflared Android APK (v5 debug)
- 安装 APK → 粘贴 Token → 启动；主界面会**实时打印日志**与**显示状态**（来自 `/metrics`）。
- 如失败，请将 **日志末尾**与**通知中的退出码**发我继续定位。
