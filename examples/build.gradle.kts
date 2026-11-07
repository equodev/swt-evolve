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
            includeModule("org.eclipse", "gef")
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
val gefVersion: String by project
val jfaceVersion: String by project
val coreCommandsVersion: String by project
val equinoxCommonVersion: String by project
val jfaceTextVersion: String by project
val eclipseTextVersion: String by project
val uiFormsVersion: String by project
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
    implementation("org.eclipse.platform:org.eclipse.ui.forms:$uiFormsVersion") {
        exclude(group = "org.eclipse.platform", module = "org.eclipse.swt")
    }
    implementation("org.eclipse.platform:org.eclipse.core.commands:$coreCommandsVersion")
    implementation("org.eclipse.platform:org.eclipse.equinox.common:$equinoxCommonVersion")
    implementation("org.eclipse.platform:org.eclipse.jface.text:$jfaceTextVersion") {
        exclude(group = "org.eclipse.platform", module = "org.eclipse.swt")
    }
    implementation("org.eclipse.platform:org.eclipse.text:$eclipseTextVersion")
    implementation("org.eclipse:draw2d:$draw2dVersion")
    // GEF re-exports draw2d and declares org.eclipse.ui.workbench / core.runtime in its OSGi
    // manifest, but a viewer driven from a plain Shell only reaches org.eclipse.core.runtime.Assert,
    // which equinox.common above already provides -- so no workbench on the classpath.
    implementation("org.eclipse:gef:$gefVersion")

    implementation("org.eclipse.nebula.widgets.nattable:org.eclipse.nebula.widgets.nattable.core:$nattableVersion") {
        exclude(group = "org.eclipse.platform", module = "org.eclipse.swt")
    }
    runtimeOnly("org.slf4j:slf4j-simple:2.0.13")

    // JavaFX (for FXCanvas / embedded-scene snippets). Classifier pulls the
    // platform-specific classes + native libraries. JavaFX 21 targets JDK 21.
    // OpenJFX 21 publishes no linux-aarch64 (nor any win-aarch64) jars, so the FXCanvas snippets
    // only run on the x64 and macOS hosts.
    val javafxVersion = libs.versions.javafx.get()
    val javafxClassifier = when {
        currentOs == "macos" && currentArch == "aarch64" -> "mac-aarch64"
        currentOs == "macos" -> "mac"
        currentOs == "windows" -> "win"
        else -> "linux"
    }
    implementation("org.openjfx:javafx-base:$javafxVersion:$javafxClassifier")
    implementation("org.openjfx:javafx-graphics:$javafxVersion:$javafxClassifier")
    implementation("org.openjfx:javafx-controls:$javafxVersion:$javafxClassifier")
    implementation("org.testfx:openjfx-monocle:${libs.versions.openjfx.monocle.get()}") { isTransitive = false }

    // LWJGL (for the GLCanvas snippet): the GL bindings plus this host's natives.
    val lwjglNatives = when {
        currentOs == "macos" && currentArch == "aarch64" -> "natives-macos-arm64"
        currentOs == "macos" -> "natives-macos"
        currentOs == "windows" && currentArch == "aarch64" -> "natives-windows-arm64"
        currentOs == "windows" -> "natives-windows"
        currentArch == "aarch64" -> "natives-linux-arm64"
        else -> "natives-linux"
    }
    implementation(libs.lwjgl)
    implementation(libs.lwjgl.opengl)
    runtimeOnly(variantOf(libs.lwjgl) { classifier(lwjglNatives) })
    runtimeOnly(variantOf(libs.lwjgl.opengl) { classifier(lwjglNatives) })

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
    if (currentOs == "macos") {
        jvmArgs("-XstartOnFirstThread")
    }
    jvmArgs("--add-exports", "java.desktop/sun.swing=ALL-UNNAMED",
            "--add-exports", "java.desktop/sun.awt=ALL-UNNAMED")
    // Forwarded when the caller sets them, because -D on the Gradle command line sets a property
    // on the Gradle JVM, where nothing reads it, and the run then looks silently unconfigured.
    // These are the switches a reproduction is driven with: `workbench.plainSwing` runs the
    // workbench as an ordinary SWT_AWT application, which is the oracle every defect reproduced
    // there is measured against, and `map.scriptedBandDrag` drives the gesture that found one.
    for (name in listOf("workbench.plainSwing", "map.scriptedBandDrag", "map.statusFollowsPointer",
            "tabs", "dev.equo.swing.traceFrames"))
        System.getProperty(name)?.let { systemProperty(name, it) }
    // And every switch of the two reproduction families by prefix rather than by name: `gl.` is
    // the 3D-like canvas and its producer, `dev.equo.swing.nativeSurface` is the engine's own
    // configuration of the route. Naming each one here meant a new switch looked ignored until
    // somebody remembered this list, which happened.
    System.getProperties().stringPropertyNames()
        .filter { it.startsWith("gl.") || it.startsWith("dev.equo.swing.nativeSurface") }
        .forEach { systemProperty(it, System.getProperty(it)) }
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

// Identifies the caller's session (one per git worktree) so its JVMs can be found and torn down
// without touching another session's. -PsessionId, else EQUO_SESSION_ID, else unstamped.
fun sessionId(): String? =
    (project.findProperty("sessionId") as String?)?.takeIf { it.isNotBlank() }
        ?: System.getenv("EQUO_SESSION_ID")?.takeIf { it.isNotBlank() }

// jarTaskName is the swt_native jar the example runs on.
fun registerFlutterExample(name: String, mode: String, jarTaskName: String = "${currentPlatform}Jar") =
    tasks.register<JavaExec>(name) {
        group = "examples"
        description = "Run an example in '$mode' render mode. Usage: ./gradlew :examples:$name -PmainClass=dev.equo.ButtonSnippet"

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
        // -PsessionId stamps this JVM so a caller can tear down exactly its own run. Several
        // worktrees drive their own app on one machine; matching on the task name instead
        // (`pkill -f runDeskExample`) reaps all of them.
        sessionId()?.let { systemProperty("dev.equo.session", it) }
        systemProperty("dev.equo.swt.crashReport.disabled", "true")
        systemProperty("dev.equo.swt.mode", mode)
        // -Pdebug turns on the Java-side DebugLog (Config.isDebug): prints every `send:` of a widget's
        // V* payload to Flutter. That's the Java side of the Phase 3 Flutter-vs-V* cross-check. It can
        // also still be enabled directly via -Ddev.equo.swt.debug.
        if (project.hasProperty("debug") || System.getProperty("dev.equo.swt.debug") != null)
            systemProperty("dev.equo.swt.debug", "true")
        // Forward the theming switches, so a snippet can be run the way a themed product runs:
        // an application that paints itself (e4 CSS) needs its own colors/fonts, not Evolve's theme.
        for (name in listOf("swt.use_swt_colors", "swt.use_swt_fonts",
                // Which shells get a window of their own -- see dev.equo.swt.WindowPolicy. Forwarded
                // so MultiWindowSnippet can be run under each policy without editing anything.
                "dev.equo.swt.windows", "dev.equo.swt.windows.dialogs", "dev.equo.swt.windows.modal",
                // Client-Side Decorations. -Ddev.equo.swt.csd=false gives the window its system
                // title bar and buttons back, which is the only way to exercise a window-manager
                // close rather than the one Flutter draws.
                "dev.equo.swt.csd", "dev.equo.swt.csd.os", "dev.equo.swt.csd.maximize",
                // swt.autoScale picks the zoom the UI is drawn at, and updateOnRuntime is what
                // rejects a mode monitor-specific scaling cannot honour -- so a snippet can be run
                // at a zoom of its own.
                "swt.autoScale", "swt.autoScale.updateOnRuntime")) {
            System.getProperty(name)?.let { systemProperty(name, it) }
        }
        // Every swt.evolve.* config flag (theme_color_* included), so a snippet runs under any of them.
        System.getProperties().stringPropertyNames()
            .filter { it.startsWith("swt.evolve.") }
            .forEach { systemProperty(it, System.getProperty(it)) }
        // -PdartDebug: run the Dart/Flutter rendering side in debug so its Dart VM Service is available
        // for DTD/MCP + flutter_driver: the frameworks are built with --debug (JIT) and
        // FlutterLibraryLoader loads the Debug build-dir. See docs/design/flutter-dtd-introspection.md.
        if (project.hasProperty("dartDebug")) {
            systemProperty("dev.equo.swt.dartDebug", "true")
            if (mode == "desktop") {
                // The desktop engine runs in-process (JNI), so this JVM's env IS the engine's env. Pin the
                // Dart VM Service to a predictable URL via the engine env-switch channel so tooling can
                // attach without scraping stdout. Result: http://127.0.0.1:<port>/. Override -PdartVmPort.
                // With a session id the default is derived per worktree instead: a fixed 8181 means the
                // second concurrent desktop session silently fails to bind and tooling attaches to the first.
                val vmPort = (project.findProperty("dartVmPort") as String?)
                    ?: sessionId()?.let { (8181 + Math.floorMod(it.hashCode(), 50) * 5).toString() }
                    ?: "8181"
                environment("FLUTTER_ENGINE_SWITCHES", "2")
                environment("FLUTTER_ENGINE_SWITCH_1", "vm-service-port=$vmPort")
                environment("FLUTTER_ENGINE_SWITCH_2", "disable-service-auth-codes=true")
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

// The screenshot capture forks one JVM per snippet. Resolving the classpath once and reusing it for
// all of them saves a whole nested Gradle build each time, which dominated that job's runtime. The
// desktop twin serves a host that launches snippets with JVM flags of its own.
fun registerExampleClasspath(name: String, jarTaskName: String, fileName: String, renderMode: String) =
    tasks.register(name) {
        group = "examples"
        description = "Writes the $renderMode example runtime classpath to a file, one entry per line."
        val jar = project(":swt_native").tasks.named<Jar>(jarTaskName)
        dependsOn(jar)
        // The snippet classes are read out of sourceSets in doLast, which carries no task dependency of
        // its own, so without this the file names a classes directory nothing ever compiled.
        dependsOn(tasks.named("classes"))
        val runtimeClasspathWithoutSwtNative = configurations["runtimeClasspath"].copyRecursive { dep ->
            !(dep is ProjectDependency && dep.path == ":swt_native")
        }
        val mainOutput = sourceSets["main"].output
        val outputFile = layout.buildDirectory.file(fileName)
        outputs.file(outputFile)
        doLast {
            val entries = files(jar.map { it.archiveFile }) + mainOutput + runtimeClasspathWithoutSwtNative
            val file = outputFile.get().asFile
            file.parentFile.mkdirs()
            file.writeText(entries.files.joinToString("\n") { it.absolutePath })
        }
    }
registerExampleClasspath("deskExampleClasspath", "${currentPlatform}Jar", "desk-example-classpath.txt", "desktop")

registerFlutterExample("runDeskExample", "desktop")

// A build that includes this one registers runs of its own with the same setup.
extra["registerFlutterExample"] = { name: String, mode: String, jarTaskName: String ->
    registerFlutterExample(name, mode, jarTaskName) }
extra["registerExampleClasspath"] = { name: String, jarTaskName: String, fileName: String, renderMode: String ->
    registerExampleClasspath(name, jarTaskName, fileName, renderMode) }

// Embedded: the per-widget native-SWT scenario (embed-<os>-<arch>.jar) -- a real native SWT shell
// that renders Equo-enabled widgets (per Config.equoEnabled) through Flutter and everything else
// through native SWT, same backend eclipse_run swaps into a real RCP app. Unlike SnippetCapturer
// (:swt_eclipse_examples), this keeps the window open for interactive use instead of
// screenshotting and exiting after 1s.
tasks.register<JavaExec>("runEmbedExample") {
    group = "examples"
    description = "Run an example against the embedded (per-widget native-SWT) backend. " +
            "Usage: ./gradlew :examples:runEmbedExample -PmainClass=dev.equo.ButtonSnippet"

    val jarTaskName = "embed-${currentPlatform}Jar"
    val jar = project(":swt_native").tasks.named<Jar>(jarTaskName)
    dependsOn(jar)
    val runtimeClasspathWithoutSwtNative = configurations["runtimeClasspath"].copyRecursive { dep ->
        !(dep is ProjectDependency && dep.path == ":swt_native")
    }
    classpath = files(jar.map { it.archiveFile }) +
            sourceSets["main"].output +
            runtimeClasspathWithoutSwtNative
    mainClass.set(project.findProperty("mainClass")?.toString() ?: "dev.equo.ButtonSnippet")
    systemProperty("dev.equo.swt.crashReport.disabled", "true")

    if (System.getProperty("test.debug") != null)
        jvmArgs("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005")
}

// The oracle runs with no `-XstartOnFirstThread`: that flag is SWT's on macOS and it is exactly
// what AWT cannot live with — Swing's own toolkit wants the primordial thread for AppKit, and with
// the flag set it spins in `LWCToolkit.initAppkit` and never opens a window. A pure-Swing snippet
// is the one example here that must not get it.
tasks.register<JavaExec>("runPlainSwing") {
    group = "examples"
    description = "Run a plain-Swing example with no SWT flags. Usage: -PmainClass=…"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set(project.findProperty("mainClass")?.toString()
            ?: "dev.equo.swing.PlainSwingWorkbenchSnippet")
    jvmArgs("--add-exports", "java.desktop/sun.swing=ALL-UNNAMED",
            "--add-exports", "java.desktop/sun.awt=ALL-UNNAMED")
}


// The oracle is launched with `java` directly, not through this task: a Gradle JavaExec does not
// hand the application the primordial thread, and AWT on macOS spins in `LWCToolkit.initAppkit`
// waiting for it. This prints what to put on `-cp`.
tasks.register("printClasspath") {
    group = "examples"
    description = "The runtime class path, for launching a plain-Swing example with java directly."
    val cp = sourceSets["main"].runtimeClasspath
    doLast { println(cp.asPath) }
}
