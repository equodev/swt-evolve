import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.kotlin.dsl.*
import java.io.File

plugins {
    java
    `java-library`
}

repositories {
    mavenCentral()
    mavenLocal()
}

val arch = System.getProperty("os.arch")

val oss = listOf("embedWindows", "embedLinux", "embedMacos", "nativeWindows", "nativeLinux", "nativeMacos", "web")
val platforms = listOf(
    "embed-windows-x86_64", "embed-windows-aarch64",
    "embed-linux-x86_64", "embed-linux-aarch64",
    "embed-macos-x86_64", "embed-macos-aarch64",
    "windows-x86_64", "windows-aarch64",
    "linux-x86_64", "linux-aarch64",
    "macos-x86_64", "macos-aarch64",
    "web",
)

val currentOs = when {
    org.gradle.internal.os.OperatingSystem.current().isWindows -> "windows"
    org.gradle.internal.os.OperatingSystem.current().isMacOsX -> "macos"
    else -> "linux"
}

fun getSwtWs(os: String): String = when (os) {
    "macos" -> "cocoa"
    "windows" -> "win32"
    "linux" -> "gtk"
    else -> error("Unknown WS OS $os")
}

fun getSwtOs(os: String): String = when (os) {
    "macos" -> "macosx"
    "windows" -> "win32"
    "linux" -> "linux"
    else -> error("Unknown OS $os")
}

fun getSwtArch(arch: String): String =
    if (arch.contains("aarch64") || arch.contains("arm")) "aarch64" else "x86_64"

val isWindowsOs = System.getProperty("os.name").lowercase().contains("windows")

fun findOnPath(name: String): String? {
    val pathEnv = System.getenv("PATH") ?: ""
    for (dir in pathEnv.split(if (isWindowsOs) ";" else ":")) {
        if (dir.isBlank()) continue
        val f = File(dir, name)
        if (f.isFile && f.canExecute()) return f.absolutePath
    }
    return null
}

fun resolveOnPath(name: String): String = findOnPath(name) ?: name

// Prefer the project's FVM-pinned Flutter SDK when available: when flutter-lib
// has a .fvmrc *and* `fvm` is on PATH, drive the toolchain through `fvm flutter`
// / `fvm dart` (fvm reads .fvmrc to pick the version; the Exec tasks run from
// flutter-lib so it resolves correctly). Otherwise fall back to the global
// flutter/dart on PATH — which is what CI uses, since fvm isn't installed there.
val fvmExe: String? by lazy {
    if (file("../flutter-lib/.fvmrc").exists())
        findOnPath(if (isWindowsOs) "fvm.bat" else "fvm")
    else null
}
val flutterExePath: String by lazy { resolveOnPath(if (isWindowsOs) "flutter.bat" else "flutter") }
val dartExePath: String by lazy { resolveOnPath(if (isWindowsOs) "dart.bat" else "dart") }

fun flutterCmd(): List<String> = fvmExe?.let { listOf(it, "flutter") } ?: listOf(flutterExePath)
fun dartCmd(): List<String> = fvmExe?.let { listOf(it, "dart") } ?: listOf(dartExePath)

fun requiredFlutterVersion(): String {
    val content = file("../flutter-lib/pubspec.yaml").readText()
    val constraint = Regex("""environment:.*?flutter:\s*"([^"]+)"""", RegexOption.DOT_MATCHES_ALL)
        .find(content)?.groupValues?.get(1)
        ?: error("flutter version constraint not found in pubspec.yaml environment section")
    return Regex("""\d+\.\d+\.\d+""").find(constraint)?.value
        ?: error("No version number found in flutter constraint: $constraint")
}

val checkFlutterVersion by tasks.registering {
    group = "build"
    description = "Verifies installed Flutter version matches pubspec.yaml"
    inputs.file("../flutter-lib/pubspec.yaml")
    outputs.upToDateWhen { true }
    doFirst {
        val required = requiredFlutterVersion()
        val process = ProcessBuilder(flutterCmd() + "--version")
            .directory(file("../flutter-lib"))
            .redirectErrorStream(true).start()
        val output = process.inputStream.bufferedReader().readText()
        process.waitFor()
        val installed = Regex("""Flutter (\d+\.\d+\.\d+)""").find(output)?.groupValues?.get(1)
            ?: error("Could not parse Flutter version from output:\n$output")
        if (installed != required)
            error("Flutter version mismatch: installed=$installed, required=$required (from pubspec.yaml)")
        logger.lifecycle("Flutter version check passed: $installed == $required")
    }
}

val currentPlatform = "$currentOs-${getSwtArch(arch)}"

// Read swtVersion from gradle.properties:
val swtVersionFull = (project.parent?.parent?.findProperty("swtVersionFull")
    ?: project.parent?.findProperty("swtVersionFull")
    ?: error("Required property 'swtVersionFull' is not defined in gradle.properties. Please add it to your gradle.properties file.")) as String

val swtVersion = swtVersionFull.substringBefore(".v")

val swtVersionConfig by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

val swtVersionProvider = provider {
    val versionFiles = swtVersionConfig.files
    if (versionFiles.isNotEmpty() && versionFiles.first().exists()) versionFiles.first().readText().trim() else swtVersionFull
}

dependencies {
    if (gradle.parent != null)
        swtVersionConfig("dev.equo:eclipse_run")
    implementation(libs.java.websocket)
    // Alternative WS impl, selectable at runtime via -Dcomm.impl=jetty (Jetty 12 core, no servlets).
    // compileOnly so Jetty's transitive jars never ship in the platform JARs; it's a bench-only
    // alternative (production always uses java-websocket). testRuntimeOnly puts it back on the
    // bench JVM's classpath so -Dcomm.impl=jetty can load JettyBinaryCommService.
    compileOnly(libs.jetty.server)
    compileOnly(libs.jetty.websocket.server)
    testRuntimeOnly(libs.jetty.server)
    testRuntimeOnly(libs.jetty.websocket.server)
    compileOnly(libs.equo.chromium)

    implementation(libs.dsl.json)
    annotationProcessor(libs.dsl.json)
    testAnnotationProcessor(libs.dsl.json)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.assertj)
    testImplementation(libs.json.unit.assertj)
    // Gson, two test-only roles: (1) the JSON backend json-unit 4.x discovers at runtime (without one
    // assertThatJson(...) fails to init) — its lenient parser also accepts the bare unquoted string
    // expecteds the generated serialize tests use (e.g. isEqualTo("two")); (2) the bench code generator
    // (WorkbenchTreeGenerator) parses workbench.json to emit the hardcoded WorkbenchTree.
    testImplementation(libs.gson)
    testImplementation(libs.mockito.core)
    testImplementation(libs.instancio.junit)

    // JavaFX — required to COMPILE the FXCanvas embedded-scene bridge
    // (javafx.embed.swt + com.sun.javafx.embed internals). compileOnly so the FX
    // jars + native libs are NOT flattened into the Evolve platform jars (that
    // breaks JavaFX's NativeLibLoader); the FX runtime is provided as separate
    // jars on the application classpath instead. JavaFX is on the classpath
    // (unnamed module) so com.sun.* embed packages need no --add-exports.
    run {
        val javafxVersion = libs.versions.javafx.get()
        val fxArch = getSwtArch(arch)
        val javafxClassifier = when (currentOs) {
            "macos" -> if (fxArch == "aarch64") "mac-aarch64" else "mac"
            "windows" -> "win"
            else -> if (fxArch == "aarch64") "linux-aarch64" else "linux"
        }
        compileOnly("org.openjfx:javafx-base:$javafxVersion:$javafxClassifier")
        compileOnly("org.openjfx:javafx-graphics:$javafxVersion:$javafxClassifier")
        compileOnly("org.openjfx:javafx-controls:$javafxVersion:$javafxClassifier")
    }
}

val nativeFlutterExcludes = listOf("dev/equo/swt/ConfigDyn.java", "**/GraphicsUtilsSwt.java")

sourceSets {
    main {
        java {
            setSrcDirs(listOf(
                "src/main/java",
                "src/native/java",
                "src/native${currentOs.replaceFirstChar { it.titlecase() }}/java"
            ))
            exclude(nativeFlutterExcludes)
        }
    }

    // The web comm benchmark drives the production Flutter web build through a browser, so it
    // reuses the production WebFlutterServer (from src/native) to serve it — rather than duplicating
    // an HTTP server in test scope. WebFlutterServer is pure JDK + FlutterLibraryLoader (which is
    // in main), so we expose just that one file on the test classpath via this tiny source set,
    // instead of pulling the whole web tree (thousands of files) into every test build.
    val webShared = create("webShared") {
        java {
            setSrcDirs(listOf("src/native/java"))
            include("dev/equo/swt/WebFlutterServer.java")
        }
        compileClasspath += sourceSets.main.get().output + sourceSets.main.get().compileClasspath
    }

    // Create source sets for all platform combinations
    oss.forEach { os ->
        create(os) {
            java {
                when {
                    os.startsWith("native") || os == "web" -> setSrcDirs(listOf("src/main/java", "src/native/java", "src/${os}/java"))
                    else -> setSrcDirs(listOf("src/main/java", "src/${os}/java"))
                }
                if (os.startsWith("native") || os == "web") {
                    exclude(nativeFlutterExcludes)
                }
            }
            annotationProcessorPath += sourceSets.main.get().annotationProcessorPath
            compileClasspath += sourceSets.main.get().compileClasspath
            runtimeClasspath += sourceSets.main.get().runtimeClasspath

            test {
                resources {
                    srcDirs("src/${os}/java")
                    include("**/*.css", "**/*.png", "**/*.bmp", "**/*.gif", "**/*.svg", "**/*.jpg", "**/SWTMessages*.properties", "**/SWTMessages.properties", "**/bench/*.json")
                }
            }
        }
    }

    // The default `main` backend is whole-tree Flutter (desk/web), but the unit-test suite
    // (serialize round-trips + Mocks) is written against the EMBEDDED Swt* classes. So compile and
    // run `test` against the embedded backend for the current OS (src/main + src/embed<OS>) rather
    // than against `main`. Dependencies come from the test* configurations; webShared adds
    // the production WebFlutterServer the web comm benchmark drives.
    val embedBackend = getByName("embed${currentOs.replaceFirstChar { it.titlecase() }}")
    test {
        compileClasspath = configurations["testCompileClasspath"] + embedBackend.output + webShared.output
        runtimeClasspath = output + configurations["testRuntimeClasspath"] + embedBackend.output + webShared.output
    }

    // Web-backed integration test source set: the SAME test code (src/test/java), but compiled
    // against the whole-tree-Flutter Java backend (src/main + src/native + src/native<currentOs>)
    // instead of the embedded one — so 'flutter-it' tests exercise web-specific server logic.
    // Driven by the `webTest` task.
    val webBackend = getByName("native${currentOs.replaceFirstChar { it.titlecase() }}")
    create("webTest") {
        java {
            // src/test/java: the shared suite — flutter-it tests there (named *FlutterTest by
            // convention) must also compile against the EMBEDDED backend the default `test` task
            // uses, so they reach web-only classes by reflection (see DisplayWakeFlutterTest).
            // src/webTest/java: flutter-it tests that CANNOT compile against embedded — they
            // sub-class the native-only Display bridges (DeskDisplayBridge/WebDisplayBridge) for
            // their test seams. Compiled only here, never by the default `test` source set.
            setSrcDirs(listOf("src/test/java", "src/webTest/java"))
            // Only the harness + flutter-it tests (named *FlutterTest by convention): the rest of
            // src/test/java (Mocks, SerializeTestBase, …) imports native-only Swt* classes absent
            // from the web backend.
            include("dev/equo/swt/harness/**", "**/*FlutterTest.java")
        }
        compileClasspath += webBackend.output + webBackend.compileClasspath
        runtimeClasspath += output + webBackend.output + webBackend.runtimeClasspath
    }
}

// webTest reuses the test dependencies (JUnit, AssertJ, Mockito, Gson, …) and annotation processor.
configurations["webTestImplementation"].extendsFrom(configurations["testImplementation"])
configurations["webTestRuntimeOnly"].extendsFrom(configurations["testRuntimeOnly"])
configurations["webTestAnnotationProcessor"].extendsFrom(configurations["testAnnotationProcessor"])

val chromiumMode = System.getProperty("mode.chromium", "false") == "true"
if (chromiumMode) {
    dependencies {
        "webTestRuntimeOnly"(libs.equo.chromium)
        "webTestRuntimeOnly"("com.equo:com.equo.chromium.cef.${getSwtWs(currentOs)}.${getSwtOs(currentOs)}.${getSwtArch(arch)}:${libs.versions.equo.chromium.cef.get()}")
    }
}

// Configure Java compilation
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
}

tasks.test {
    useJUnitPlatform {
        // Bench tests are slow + write artifacts; always excluded from the default test run.
        excludeTags("bench")
        // Flutter integration tests need a Flutter web build + Chrome; not run by default.
        excludeTags("flutter-it")
        val excludeTagsProp = System.getProperty("excludeTags")
        if (excludeTagsProp != null) excludeTags(*excludeTagsProp.split(",").toTypedArray())
    }
    configureTestLogging()
    dependsOn("${currentPlatform}ExtractNatives", "${currentPlatform}CopyFlutterBinaries")
    if (org.gradle.internal.os.OperatingSystem.current().isMacOsX)
        jvmArgs = listOf("-XstartOnFirstThread")
    systemProperty("swt.library.path", layout.buildDirectory.dir("natives/$currentPlatform").get().toString())
    if (System.getProperty("test.debug") != null)
        jvmArgs("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005")
}

tasks.register<Test>("webTest") {
    group = "verification"
    description = "Runs Flutter integration tests (tagged 'flutter-it') against the WEB Java backend + Flutter WEB build."
    testClassesDirs = sourceSets["webTest"].output.classesDirs
    classpath = if (chromiumMode)
        sourceSets["native${currentOs.replaceFirstChar { it.titlecase() }}"].output + sourceSets["webTest"].runtimeClasspath
    else
        sourceSets["webTest"].runtimeClasspath
    useJUnitPlatform { includeTags("flutter-it") }
    configureTestLogging()
    if (System.getProperty("skipFlutterLib") == null)
        dependsOn("webFlutterLib")
    systemProperty("harness.client", "web")
    systemProperty("dev.equo.swt.loadLibrary", "false")
    systemProperty("dev.equo.swt.mode", if (chromiumMode) "chromium" else "web")
    // The Browser scripting / same-origin cases (evaluate, BrowserFunction,
    // Title/StatusText, redirect) need the iframe content to be same-origin,
    // which the opt-in reverse proxy provides. Default it ON for tests so the
    // suite reflects the supported feature set; override with
    // -Ddev.equo.swt.web.proxy=<all|host,host> or = (empty) to disable.
    systemProperty("dev.equo.swt.web.proxy", System.getProperty("dev.equo.swt.web.proxy") ?: "all")
    if (chromiumMode && org.gradle.internal.os.OperatingSystem.current().isMacOsX)
        jvmArgs = listOf("-XstartOnFirstThread")
    forwardSystemProperties("harness.client", "harness.web.headless", "harness.web.console", "harness.readyTimeoutMs", "harness.queryTimeoutMs", "harness.holdMs", "equo.swt.browser", "dev.equo.swt.mode", "harness.bootAttempts", "harness.bootAttemptMs", "harness.web.failBoots")
}

// Config shared by both bench Test tasks (native `benchmark` + browser `webBenchmark`): the
// 'bench'-tagged test run, full logging, and always-rerun. Per-task specifics (deps, jvmArgs,
// system properties) are layered on by the caller.
fun Test.configureCommBench() {
    group = "verification"
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath
    useJUnitPlatform { includeTags("bench") }
    configureTestLogging()
    outputs.upToDateWhen { false } // always rerun benchmarks
}

fun Test.configureTestLogging() {
    testLogging {
        if (System.getProperty("quietTests") != null) {
            events = setOf(TestLogEvent.FAILED)
            showStandardStreams = false
        } else {
            events = setOf(TestLogEvent.PASSED, TestLogEvent.FAILED, TestLogEvent.SKIPPED, TestLogEvent.STANDARD_OUT, TestLogEvent.STANDARD_ERROR)
            showStandardStreams = true
        }
    }
}

// Forward each given -D system property from the Gradle CLI JVM into the test JVM (only those set).
fun Test.forwardSystemProperties(vararg keys: String) {
    keys.forEach { key -> System.getProperty(key)?.let { systemProperty(key, it) } }
}

tasks.register<Test>("benchmark") {
    description = "Runs comm-protocol benchmarks (tagged 'bench'). Writes results to build/bench-results/."
    configureCommBench()
    dependsOn("${currentPlatform}ExtractNatives", "${currentPlatform}CopyFlutterBinaries")
    // 1g heap headroom for LARGE-shape serialization churn (~50KB byte[] per iter × 1000+ iters).
    // Default 512m goes into GC thrash territory; 1g is comfortable.
    if (org.gradle.internal.os.OperatingSystem.current().isMacOsX)
        jvmArgs = listOf("-XstartOnFirstThread", "-Xmx1g")
    else
        jvmArgs = listOf("-Xmx1g")
    systemProperty("swt.library.path", layout.buildDirectory.dir("natives/$currentPlatform").get().toString())
    forwardSystemProperties("bench.warmup", "bench.measured", "bench.timeoutMs", "comm.impl", "bench.comm.label")
}

// Web comm benchmark: drives the Flutter WEB build in a browser instead of the native engine.
// Reuses CommBenchTest/BenchBridge; BenchBridge branches on -Dbench.client=web. Requires Stage B
// (binary equo-comm.js) for the new comm — otherwise the browser's text frames are ignored.
tasks.register<Test>("webBenchmark") {
    description = "Runs the comm benchmark against the Flutter WEB build (browser client)."
    configureCommBench()
    if (System.getProperty("skipFlutterLib") == null)
        dependsOn("webFlutterLib")
    jvmArgs = listOf("-Xmx1g")
    systemProperty("bench.client", "web")
    systemProperty("dev.equo.swt.loadLibrary", "false")
    systemProperty("bench.comm.label", System.getProperty("bench.comm.label", "web-json"))
    forwardSystemProperties("bench.warmup", "bench.measured", "bench.timeoutMs", "comm.impl", "equo.swt.browser", "bench.web.headless")
}

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(layout.buildDirectory.dir("natives/$currentPlatform"))

    dependsOn(copyWebBinaries)
    from(layout.buildDirectory.dir("natives/web")) {
        into("web")
    }

    // Add all dependencies to the JAR
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA", "META-INF/LICENSE*", "META-INF/NOTICE*", "OSGI-OPT/")

    manifest {
        attributes(
            "SWT-WS" to getSwtWs(currentOs),
            "SWT-OS" to getSwtOs(currentOs),
            "SWT-Arch" to getSwtArch(arch)
        )
    }

    dependsOn("${currentPlatform}ExtractNatives")
    dependsOn("${currentPlatform}CopyFlutterBinaries")
}

val pub = tasks.register<Exec>("pubGet") {
    group = "build"
    description = "Get flutter dependencies"
    dependsOn(checkFlutterVersion)
    workingDir = file("../flutter-lib")
    inputs.file("../flutter-lib/pubspec.yaml")
    outputs.file("../flutter-lib/pubspec.lock")
    commandLine = flutterCmd() + listOf("pub", "get")
}

tasks.register<Exec>("analyze") {
    group = "build"
    description = "Flutter analyze"
    workingDir = file("../flutter-lib")
    commandLine = flutterCmd() + listOf("analyze")
}

val dart = tasks.register<Exec>("dartRunner") {
    group = "build"
    description = "Generate dart files"
    dependsOn(pub)
    workingDir = file("../flutter-lib")
    inputs.files(fileTree("../flutter-lib/lib/src") {
        include("gen/*.dart")
        include("swt/*.dart")
        include("theme/theme_extensions/*.dart")
        exclude("**/*.g.dart")
        exclude("**/*.tailor.dart")
    })
    outputs.files(fileTree("../flutter-lib/lib/src") {
        include("**/*.g.dart")
        include("**/*.tailor.dart")
    })
    commandLine = dartCmd() + listOf("run", "build_runner", "build", "--delete-conflicting-outputs")
}

data class WebPlatformMeta(
    val os: String,
    val arch: String,
    val isHybrid: Boolean,
    val isWeb: Boolean = false,
) {
    val desktopPlatform: String get() = "$os-$arch"
    val sourceSet: String get() = when { isWeb -> "web"; isHybrid -> "native${os.replaceFirstChar { it.titlecase() }}"; else -> "embed${os.replaceFirstChar { it.titlecase() }}" }
    val swtWs: String get() = getSwtWs(os)
    val swtOs: String get() = getSwtOs(os)
}

fun parsePlatform(platform: String): WebPlatformMeta {
    if (platform == "web") return WebPlatformMeta("web", "", isHybrid = true, isWeb = true)
    val isEmbed = platform.startsWith("embed-")
    var parts = platform.split("-")
    if (isEmbed) parts = parts.drop(1)
    return WebPlatformMeta(parts[0], parts[1], isHybrid = !isEmbed)
}

fun CopySpec.copyFlutterNatives(os: String, flutterArch: String) {
    when (os) {
        "macos" -> from("../flutter-lib/build/macos/Build/Products/Release/swtflutter.app") {
            into("swtflutter.app")
        }
        "linux" -> {
            from("../flutter-lib/build/linux/$flutterArch/release/runner") {
                include("libflutter_bridge.so")
                into("runner")
            }
            from("../flutter-lib/build/linux/$flutterArch/release/bundle/lib") {
                include("libapp.so", "libflutter_linux_gtk.so")
                into("bundle/lib")
            }
            from("../flutter-lib/build/linux/$flutterArch/release/bundle/data") {
                include("icudtl.dat", "flutter_assets/**")
                into("bundle/data")
            }
        }
        "windows" -> from("../flutter-lib/build/windows/$flutterArch/runner/Release/") {
            include("*.dll", "data/")
            into("runner")
        }
    }
}

// Single shared Flutter web build — all web platform JARs depend on this one task.
val webFlutterLib = tasks.register<Exec>("webFlutterLib") {
    group = "build"
    description = "Builds Flutter web app (shared by all web platform JARs)"
    dependsOn(dart, pub)
    workingDir = file("../flutter-lib")
    inputs.dir("../flutter-lib/lib")
    inputs.dir("../flutter-lib/web")
    outputs.dir("../flutter-lib/build/web")
    commandLine = flutterCmd() + listOf("build", "web")
}

val copyWebBinaries = tasks.register<Copy>("webCopyFlutterBinaries") {
    group = "build"
    description = "Copies Flutter binaries for web"
    if (System.getProperty("skipFlutterLib") == null)
        dependsOn(webFlutterLib)
    from("../flutter-lib/build/web")
    into(layout.buildDirectory.dir("natives/web"))
}

val bundleVendor = "Equo Tech, Inc."
val chromiumImportPackage = "com.equo.chromium;resolution:=optional," +
        "com.equo.chromium.utils;resolution:=optional," +
        "com.equo.chromium.events;resolution:=optional"
fun fragmentHostHeader() =
    provider { "org.eclipse.swt;bundle-version=\"[${swtVersionProvider.get().substring(0..6)},4.0.0)\"" }
fun evolveVersion(): Any = gradle.parent?.rootProject?.version ?: project.version
fun hostVersion(suffix: String) = swtVersionProvider.map {
    // When eclipse_run provided the SWT version file, swtVersionProvider already holds the
    // product's version: use it as-is (no evolve/suffix). Otherwise compose from the
    // swtVersion property + evolve version + suffix.
    val versionFiles = swtVersionConfig.files
    val fromEclipseRun = versionFiles.isNotEmpty() && versionFiles.first().exists()
    if (fromEclipseRun) it
    else "${it.substringBefore(".v")}.v${evolveVersion().toString().replace('.', '_')}-$suffix"
}
fun swtExportPackage(swtWs: String?): String = (
    listOf(
        "org.eclipse.swt", "org.eclipse.swt.accessibility", "org.eclipse.swt.awt",
        "org.eclipse.swt.browser", "org.eclipse.swt.custom", "org.eclipse.swt.dnd",
        "org.eclipse.swt.events", "org.eclipse.swt.graphics", "org.eclipse.swt.layout",
        "org.eclipse.swt.opengl", "org.eclipse.swt.printing", "org.eclipse.swt.program",
        "org.eclipse.swt.widgets",
        "org.eclipse.swt.internal; x-friends:=\"org.eclipse.ui\"",
        "org.eclipse.swt.internal.image; x-internal:=true",
    )
        + (swtWs?.let { listOf("org.eclipse.swt.internal.$it; x-friends:=\"org.eclipse.ui\"") } ?: emptyList())
        + "com.equo.chromium.swt"
    ).joinToString(",")
val webExportPackage = swtExportPackage(null) +
    ",org.eclipse.swt.internal.cloudready,dev.equo.swt.spi"

platforms.forEach { platform ->
    val info = parsePlatform(platform)

    if (!info.isHybrid) {
        configurations.create("${info.desktopPlatform}SwtImpl") {
            exclude(group = "org.eclipse.platform", module = "org.eclipse.swt")
        }

        dependencies {
            configurations["${info.desktopPlatform}SwtImpl"]("org.eclipse.platform:org.eclipse.swt.${info.swtWs}.${info.swtOs}.${info.arch}:$swtVersion")
        }

        tasks.register<Exec>("${info.desktopPlatform}FlutterLib") {
            group = "build"
            description = "Builds Flutter lib for ${info.desktopPlatform}"
            dependsOn(dart, pub)
            workingDir = file("../flutter-lib")
            inputs.dir("../flutter-lib/lib")
            inputs.files(fileTree("../flutter-lib/${info.os}") {
                exclude("**/ephemeral/**", "Pods/**", "**/*.bak")
            })
            outputs.dir("../flutter-lib/build/${info.os}")
            when (info.os) {
                "macos" -> {
                    val flutterArch = if (info.arch == "aarch64") "arm64" else "x86_64"
                    commandLine = listOf("bash", "-c", "./set-arch.sh $flutterArch && ${flutterCmd().joinToString(" ")} build macos")
                }
                else -> commandLine = flutterCmd() + listOf("build", info.os)
            }
        }

        tasks.register<Copy>("${info.desktopPlatform}CopyFlutterBinaries") {
            group = "build"
            description = "Copies Flutter binaries for ${info.desktopPlatform}"

            if ((currentPlatform == info.desktopPlatform || info.os == "macos") && System.getProperty("skipFlutterLib") == null)
                dependsOn("${info.desktopPlatform}FlutterLib")
            if (info.os == "macos")
                mustRunAfter("macos-aarch64FlutterLib", "macos-x86_64FlutterLib")

            val flutterArch = if (info.arch == "aarch64") "arm64" else "x64"
            copyFlutterNatives(info.os, flutterArch)

            into(layout.buildDirectory.dir("natives/${info.desktopPlatform}"))
        }

        tasks.register<Copy>("${info.desktopPlatform}ExtractNatives") {
            from(configurations["${info.desktopPlatform}SwtImpl"].map { zipTree(it) })
            into(layout.buildDirectory.dir("natives/${info.desktopPlatform}"))
            include("*.so", "*.dll", "*.dylib", "*.jnilib", "**/*.css", "**/SWTMessages*.properties", "**/SWTMessages.properties")
            includeEmptyDirs = false
        }
    }

    tasks.register<Jar>("${platform}Jar") {
        group = "build"
        description = "Assembles a jar archive for $platform"
        archiveBaseName.set("swt_evolve-$platform")
        // Flattening many dependency jars + natives into one archive can collide on shared
        // resource paths (e.g. duplicate META-INF/services entries). Match the main `tasks.jar`
        // and keep the first occurrence.
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        from(sourceSets[info.sourceSet].output)
        if (!info.isWeb)
            from(layout.buildDirectory.dir("natives/${info.desktopPlatform}"))
        if (info.isHybrid) {
            dependsOn(copyWebBinaries)
            from(layout.buildDirectory.dir("natives/web")) {
                into("web")
            }
        }

        // Add all dependencies to the JAR
        from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
        exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA", "META-INF/LICENSE*", "META-INF/NOTICE*", "OSGI-OPT/")

        val bsn = if (info.isWeb) "dev.equo.swt_evolve.web"
                  else "org.eclipse.swt.${info.swtWs}.${info.swtOs}.${info.arch}"
        manifest {
            attributes(
                "Fragment-Host" to fragmentHostHeader(),
                "Bundle-Name" to (if (info.isWeb) "SWT Evolve for Web" else "SWT Evolve for ${info.swtOs} on ${info.arch}"),
                "Bundle-Vendor" to bundleVendor,
                "Bundle-SymbolicName" to "$bsn; singleton:=true",
                "Bundle-Version" to when {
                    info.isWeb -> evolveVersion()
                    info.isHybrid -> hostVersion("hyb")
                    else -> hostVersion("embed")
                },
                "Bundle-ManifestVersion" to 2,
                "Export-Package" to (if (info.isWeb) webExportPackage else swtExportPackage(info.swtWs)),
                "SWT-WS" to (if (info.isWeb) "web" else info.swtWs),
                "Automatic-Module-Name" to bsn,
                "Evolve-Version" to evolveVersion(),
            )
            if (!info.isWeb)
                attributes(
                    "Eclipse-PlatformFilter" to "(& (osgi.ws=${info.swtWs}) (osgi.os=${info.swtOs}) (osgi.arch=${info.arch}) )",
                    "SWT-OS" to info.swtOs,
                    "SWT-Arch" to info.arch,
                )
            if (info.isHybrid)
                attributes("Import-Package" to chromiumImportPackage)
        }
        if (!info.isWeb) {
            dependsOn("${info.desktopPlatform}ExtractNatives")
            dependsOn("${info.desktopPlatform}CopyFlutterBinaries")
        }
    }
}

fun genHostP2Inf(taskName: String, outFile: Provider<RegularFile>, suffix: String, frags: () -> List<WebPlatformMeta>) =
    tasks.register(taskName) {
        val verProvider = hostVersion(suffix)
        inputs.property("${suffix}HostVersion", verProvider)
        outputs.file(outFile)
        doLast {
            val v = verProvider.get()
            outFile.get().asFile.apply { parentFile.mkdirs() }.writeText(buildString {
                frags().forEachIndexed { i, info ->
                    val n = i + 1
                    appendLine("requires.$n.namespace = org.eclipse.equinox.p2.iu")
                    appendLine("requires.$n.name = org.eclipse.swt.${info.swtWs}.${info.swtOs}.${info.arch}")
                    appendLine("requires.$n.range = [$v,$v]")
                    appendLine("requires.$n.filter = (&(osgi.os=${info.swtOs})(osgi.ws=${info.swtWs})(osgi.arch=${info.arch})(!(org.eclipse.swt.buildtime=true)))")
                }
            })
        }
    }

val hybridHostP2Inf = layout.buildDirectory.file("host-hybrid/p2.inf")
val genHybridHostP2Inf = genHostP2Inf("genHybridHostP2Inf", hybridHostP2Inf, "hyb") {
    platforms.map { parsePlatform(it) }.filter { it.isHybrid && !it.isWeb }
}
val embedHostP2Inf = layout.buildDirectory.file("host-embed/p2.inf")
val genEmbedHostP2Inf = genHostP2Inf("genEmbedHostP2Inf", embedHostP2Inf, "embed") {
    platforms.map { parsePlatform(it) }.filter { !it.isHybrid }
}

fun registerHostJar(taskName: String, archiveName: String, p2infFrom: Any, version: Provider<String>) = tasks.register<Jar>(taskName) {
    group = "build"
    description = "Assembles the metadata org.eclipse.swt host bundle ($archiveName)"
    archiveBaseName.set(archiveName)

    from(p2infFrom) { into("META-INF") }

    manifest {
        attributes(
            "Bundle-Name" to "SWT Evolve",
            "Bundle-Vendor" to bundleVendor,
            "Bundle-SymbolicName" to "org.eclipse.swt; singleton:=true",
            "Bundle-Version" to version,
            "Bundle-ManifestVersion" to 2,
            "Export-Package" to webExportPackage,
            "Eclipse-ExtensibleAPI" to "true",
            "Automatic-Module-Name" to "org.eclipse.swt",
            "Evolve-Version" to evolveVersion(),
        )
    }
}
registerHostJar("swtHostJar", "org.eclipse.swt", "metadata/host/p2.inf", hostVersion("web"))
registerHostJar("swtHostHybridJar", "org.eclipse.swt-hybrid", files(hybridHostP2Inf).builtBy(genHybridHostP2Inf), hostVersion("hyb"))
registerHostJar("swtHostEmbedJar", "org.eclipse.swt-embed", files(embedHostP2Inf).builtBy(genEmbedHostP2Inf), hostVersion("embed"))

tasks.register("buildAllPlatforms") {
    group = "build"
    description = "Builds JARs for all platforms"
    dependsOn(platforms.map { "${it}Jar" })
}

// Configure the default build task to include all platform JARs
tasks.build {
    dependsOn("buildAllPlatforms", "swtHostJar", "swtHostHybridJar", "swtHostEmbedJar")
}
