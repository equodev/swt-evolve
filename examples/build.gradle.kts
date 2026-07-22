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

    // JavaFX (for FXCanvas / embedded-scene snippets). Classifier pulls the
    // platform-specific classes + native libraries. JavaFX 21 targets JDK 21.
    val javafxVersion = libs.versions.javafx.get()
    val javafxClassifier = when {
        currentOs == "macos" && currentArch == "aarch64" -> "mac-aarch64"
        currentOs == "macos" -> "mac"
        currentOs == "windows" -> "win"
        currentArch == "aarch64" -> "linux-aarch64"
        else -> "linux"
    }
    implementation("org.openjfx:javafx-base:$javafxVersion:$javafxClassifier")
    implementation("org.openjfx:javafx-graphics:$javafxVersion:$javafxClassifier")
    implementation("org.openjfx:javafx-controls:$javafxVersion:$javafxClassifier")
    implementation("org.testfx:openjfx-monocle:${libs.versions.openjfx.monocle.get()}") { isTransitive = false }

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

sourceSets {
    main {
        java {
            exclude("dev/equo/internal/DisposeSnippet.java")
        }
    }
}

tasks.withType<JavaExec>().configureEach {
    if (currentOs == "macos" && name != "runWebExample") {
        jvmArgs("-XstartOnFirstThread")
    }
    jvmArgs("--add-exports", "java.desktop/sun.swing=ALL-UNNAMED",
            "--add-exports", "java.desktop/sun.awt=ALL-UNNAMED")
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

// fvm-aware flutter command for the -PdartDebug web launcher (mirrors swt_native's resolution): prefer the
// repo-pinned SDK via `fvm flutter` when flutter-lib has a .fvmrc and fvm is on PATH; else plain flutter.
fun resolveFlutterCmd(): String {
    val hasPin = file("../flutter-lib/.fvmrc").exists()
    val pathDirs = (System.getenv("PATH") ?: "").split(File.pathSeparator)
    val fvmOnPath = pathDirs.any { dir -> File(dir, "fvm").canExecute() }
    return if (hasPin && fvmOnPath) "fvm flutter" else "flutter"
}

// webOnlyAware: only "runWebExample" supports -PwebOnly=true, which swaps in the plain
// browser-only "webJar" instead of the hybrid "${currentPlatform}Jar" -- that hybrid jar always
// bundles the desktop Flutter build (see swt_native's `tasks.jar`), which on Windows needs the
// matching Visual Studio toolchain / Developer Mode for symlinks. "runDeskExample" always needs
// the desktop build, so it has no webOnly variant.
fun registerFlutterExample(name: String, mode: String, webOnlyAware: Boolean = false) =
    tasks.register<JavaExec>(name) {
        group = "examples"
        description = "Run an example in '$mode' render mode. Usage: ./gradlew :examples:$name -PmainClass=dev.equo.ButtonSnippet" +
                if (webOnlyAware) " (add -PwebOnly=true to use the plain browser-only web jar, skipping the desktop Flutter build)" else ""

        val jarTaskName = if (webOnlyAware && project.hasProperty("webOnly")) "webJar" else "${currentPlatform}Jar"
        val jar = project(":swt_native").tasks.named<Jar>(jarTaskName)
        dependsOn(jar)
        // A plain `.filter{}` on configurations["runtimeClasspath"] still resolves (and therefore
        // builds) swt_native's default jar before the filter removes it from the file list; excluding
        // the swt_native project dependency from a *copy* of the configuration skips resolving (and
        // building) it in the first place -- the jar picked above is added back explicitly.
        val runtimeClasspathWithoutSwtNative = configurations["runtimeClasspath"].copyRecursive { dep ->
            !(dep is ProjectDependency && dep.path == ":swt_native")
        }
        classpath = files(jar.map { it.archiveFile }) +
                sourceSets["main"].output +
                runtimeClasspathWithoutSwtNative
        mainClass.set(project.findProperty("mainClass")?.toString() ?: "dev.equo.ButtonSnippet")
        systemProperty("dev.equo.swt.crashReport.disabled", "true")
        systemProperty("dev.equo.swt.web.crossOriginIsolated", "false")
        systemProperty("dev.equo.swt.mode", mode)
        // -Pdebug turns on the Java-side DebugLog (Config.isDebug): prints every `send:` of a widget's
        // V* payload to Flutter. That's the Java side of the Phase 3 Flutter-vs-V* cross-check. It can
        // also still be enabled directly via -Ddev.equo.swt.debug.
        if (project.hasProperty("debug") || System.getProperty("dev.equo.swt.debug") != null)
            systemProperty("dev.equo.swt.debug", "true")
        // Forward e.g. -Dequo.swt.browser=none so an external driver (Playwright, etc.) can be
        // the only WebSocket client instead of racing the auto-launched system browser tab.
        System.getProperty("equo.swt.browser")?.let { systemProperty("equo.swt.browser", it) }
        // Forward a fixed HTTP port for an external driver (Playwright, etc.) to target.
        System.getProperty("dev.equo.swt.web.httpPort")?.let { systemProperty("dev.equo.swt.web.httpPort", it) }
        // Forward the runtime (no-rebuild) semantics toggle — see WebFlutterServer.Builder#enableTestSemantics.
        System.getProperty("dev.equo.swt.web.enableTestSemantics")?.let { systemProperty("dev.equo.swt.web.enableTestSemantics", it) }
        // -PdartDebug: run the Dart/Flutter rendering side in debug so its Dart VM Service is available
        // for DTD/MCP + flutter_driver. One flag for both render modes: desk builds the frameworks with
        // --debug (JIT) and FlutterLibraryLoader loads the Debug build-dir; web serves via a live
        // `flutter run -d chrome`. See docs/design/flutter-dtd-introspection.md (Phases 1 & 2).
        if (project.hasProperty("dartDebug")) {
            systemProperty("dev.equo.swt.dartDebug", "true")
            if (mode == "desktop") {
                // The desktop engine runs in-process (JNI), so this JVM's env IS the engine's env. Pin the
                // Dart VM Service to a predictable URL via the engine env-switch channel so tooling can
                // attach without scraping stdout. Result: http://127.0.0.1:<port>/. Override -PdartVmPort.
                val vmPort = (project.findProperty("dartVmPort") as String?) ?: "8181"
                environment("FLUTTER_ENGINE_SWITCHES", "2")
                environment("FLUTTER_ENGINE_SWITCH_1", "vm-service-port=$vmPort")
                environment("FLUTTER_ENGINE_SWITCH_2", "disable-service-auth-codes=true")
            } else {
                // Web: WebDisplayBridge spawns `flutter run` with the Display's comm port as --dart-define
                // instead of static WebFlutterServer. Give it the flutter-lib dir + fvm flutter command,
                // and disable the Java-side browser (flutter run opens Chrome itself).
                systemProperty("dev.equo.swt.flutterLibDir", file("../flutter-lib").absolutePath)
                systemProperty("dev.equo.swt.flutterCmd", resolveFlutterCmd())
                systemProperty("equo.swt.browser", "none")
            }
        }
        // Attach the JaCoCo agent when a driving E2E suite asks for it: this app JVM is the thing under
        // test, and it's an external process (the suite spawns this build), so it can't be covered by a
        // Gradle Test task's own instrumentation — the agent has to go on directly. The .exec it writes is
        // merged into the combined report. See e2e/build.gradle.kts in the parent repo.
        System.getProperty("dev.equo.e2e.jacocoAgent")?.let { agentJar ->
            val destFile = System.getProperty("dev.equo.e2e.jacocoDestFile")
                ?: error("dev.equo.e2e.jacocoAgent was set without dev.equo.e2e.jacocoDestFile")
            jvmArgs("-javaagent:$agentJar=destfile=$destFile,append=true")
        }

        if (System.getProperty("test.debug") != null)
            jvmArgs("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005")
    }

registerFlutterExample("runWebExample", if (chromiumMode) "chromium" else "web", webOnlyAware = true)
registerFlutterExample("runDeskExample", "desktop")
