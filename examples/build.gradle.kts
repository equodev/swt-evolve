plugins {
    id("java")
    id("application")
}

val eclipseUrl: String by project
val draw2dUrl: String = project.findProperty("draw2dUrl")?.toString() ?: eclipseUrl

repositories {
    ivy {
        url = uri("$draw2dUrl/plugins")
        name = "Eclipse Plugins"
        patternLayout {
            artifact("[organisation].[artifact]_[revision].[ext]")
        }
        metadataSources {
            artifact()
        }
        content {
            includeModule("org.eclipse", "draw2d")
        }
    }
    maven {
        url = uri("https://gitlab.com/api/v4/projects/72079350/packages/maven")
        name = "SWT Evolve DEV"
        content {
            includeGroup("dev.equo")
        }
    }
    mavenLocal()
}

val currentOs = when {
    org.gradle.internal.os.OperatingSystem.current().isWindows -> "windows"
    org.gradle.internal.os.OperatingSystem.current().isMacOsX -> "macos"
    else -> "linux"
}
val arch = System.getProperty("os.arch")
val currentArch = if (arch.contains("aarch64") || arch.contains("arm")) "aarch64" else "x86_64"
val currentPlatform = "$currentOs-$currentArch"

tasks.compileJava {
    options.encoding = "UTF-8"
}

val draw2dVersion: String by project
val jfaceVersion: String by project
val coreCommandsVersion: String by project
val equinoxCommonVersion: String by project
val jfaceTextVersion: String by project
val eclipseTextVersion: String by project
val nattableVersion: String by project

val chromiumMode = System.getProperty("mode.chromium", "false") == "true"
dependencies {
    if (gradle.parent != null)
        implementation(project(":swt_native"))
    else
        implementation("dev.equo:swt-evolve:+:$currentPlatform")

    implementation("org.jetbrains.kotlin:kotlin-stdlib:${libs.versions.kotlin.get()}")
    // JFace dependencies
    implementation("org.eclipse.platform:org.eclipse.jface:$jfaceVersion") {
        exclude(group = "org.eclipse.platform", module = "org.eclipse.swt")
    }
    implementation("org.eclipse.platform:org.eclipse.core.commands:$coreCommandsVersion")
    implementation("org.eclipse.platform:org.eclipse.equinox.common:$equinoxCommonVersion")
    implementation("org.eclipse.platform:org.eclipse.jface.text:$jfaceTextVersion") {
        exclude(group = "org.eclipse.platform", module = "org.eclipse.swt")
    }
    implementation("org.eclipse.platform:org.eclipse.text:$eclipseTextVersion")
    implementation("org.eclipse:draw2d:$draw2dVersion")

    implementation("org.eclipse.nebula.widgets.nattable:org.eclipse.nebula.widgets.nattable.core:$nattableVersion") {
        exclude(group = "org.eclipse.platform", module = "org.eclipse.swt")
    }
    runtimeOnly("org.slf4j:slf4j-simple:2.0.13")

    if (chromiumMode) {
        runtimeOnly(libs.equo.chromium)
        val cefArtifact = when (currentOs) {
            "windows" -> "win32.win32"
            "macos" -> "cocoa.macosx"
            else -> "gtk.linux"
        }
        runtimeOnly("com.equo:com.equo.chromium.cef.$cefArtifact.$currentArch:${libs.versions.equo.chromium.cef.get()}")
    }
}

tasks.withType<JavaExec>().configureEach {
    if (currentOs == "macos") {
        jvmArgs("-XstartOnFirstThread")
    }
}

tasks.register<JavaExec>("runExample") {
    group = "examples"
    description = "Run an example class. Usage: ./gradlew :examples:runExample -PmainClass=dev.equo.StyledTextSnippet1"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set(project.findProperty("mainClass")?.toString() ?: "dev.equo.GCCopyAreaImageSnippet")

    if (currentOs == "macos")
        jvmArgs("-XstartOnFirstThread")
    if (System.getProperty("test.debug") != null)
        jvmArgs("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005")
}

tasks.register<JavaExec>("runWebExample") {
    group = "examples"
    description = "Run an example class for web. Usage: ./gradlew :examples:runWebExample -PmainClass=dev.equo.StyledTextSnippet1"

    val webJar = project(":swt_native").tasks.named<Jar>("web-${currentPlatform}Jar")
    dependsOn(webJar)
    classpath = files(webJar.map { it.archiveFile }) +
            sourceSets["main"].output +
            configurations["runtimeClasspath"].filter { !it.path.contains("swt_native") }
    mainClass.set(project.findProperty("mainClass")?.toString() ?: "dev.equo.GCFillOvalSnippet")
    systemProperty("dev.equo.swt.crashReport.disabled", "true")
    systemProperty("dev.equo.swt.web.crossOriginIsolated", "false")
    systemProperty("dev.equo.swt.mode", if (chromiumMode) "chromium" else "web")

    if (System.getProperty("test.debug") != null)
        jvmArgs("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005")
}
