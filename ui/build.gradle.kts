import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java")
    id("kotlin")
    id("org.jetbrains.compose") version ("1.1.0")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    jcenter()
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(compose.desktop.currentOs)
    implementation(compose.desktop.common)
    implementation ("io.netty:netty-all:4.1.48.Final")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${rootProject.extra["kotlin_version"]}")

    implementation(project(":core"))
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

compose.desktop {
    application {
        mainClass = "cn.netdiscovery.adbd.ui.LaunchKt"
        nativeDistributions {
            outputBaseDir.set(project.buildDir.resolve("output"))   //build/output
//            appResourcesRootDir.set(project.layout.projectDirectory.dir("resources")) //设置无效
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Exe, TargetFormat.Deb)
            packageName = "adbd-connector"
            packageVersion = "1.0.0"
            jvmArgs += listOf("-Xmx2G")
            vendor = "ATRenew Inc."
            includeAllModules = true    //包含所有模块
            windows {
                console = true  //为应用程序添加一个控制台启动器
                shortcut = true // 桌面快捷方式
                dirChooser = true  //允许在安装过程中自定义安装路径
                perUserInstall = false   //允许在每个用户的基础上安装应用程序
                menuGroup = "start-menu-group"
                upgradeUuid = "31575EDF-D0D5-4CEF-A4D2-7562083D6D88"
                iconFile.set(project.file("src/main/resources/image/ic_logo.ico"))
            }
        }
    }
}