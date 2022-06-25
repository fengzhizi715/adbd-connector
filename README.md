# adbd-connector

基于 ADB Daemon 和 ADB Server 之间的通信协议实现的库

连接前，先要打开手机手机的开发者模式，以及 5555 端口使用 adb 命令：adb tcpip 5555

![](images/1.png)

install app 需要分成2步：
1. 使用 push 命令将 apk 推送到手机的目录 /data/local/tmp/
2. 使用 adb shell pm install 命令进行安装

uninstall app
adb shell pm uninstall 包名 