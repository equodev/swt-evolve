import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.kotlin.dsl.*
import java.io.File

plugins {
    java
    `java-library`
}

repositories {
    mavenCentral()
}

val arch = System.getProperty("os.arch")

val oss = listOf("windows", "linux", "macos", "webWindows", "webLinux", "webMacos")
val platforms = listOf(
    "windows-x86_64", "windows-aarch64",
    "linux-x86_64", "linux-aarch64",
    "macos-x86_64", "macos-aarch64",
    "web-windows-x86_64", "web-windows-aarch64",
    "web-linux-x86_64", "web-linux-aarch64",
    "web-macos-x86_64", "web-macos-aarch64",
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

fun resolveOnPath(name: String): String {
    val pathEnv = System.getenv("PATH") ?: ""
    for (dir in pathEnv.split(if (isWindowsOs) ";" else ":")) {
        if (dir.isBlank()) continue
        val f = File(dir, name)
        if (f.isFile && f.canExecute()) return f.absolutePath
    }
    return name
}

val flutterExePath: String by lazy { resolveOnPath(if (isWindowsOs) "flutter.bat" else "flutter") }
val dartExePath: String by lazy { resolveOnPath(if (isWindowsOs) "dart.bat" else "dart") }

fun flutterExe(): String = flutterExePath
fun dartExe(): String = dartExePath

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
        val process = ProcessBuilder(flutterExe(), "--version").redirectErrorStream(true).start()
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
    testImplementation(libs.assertj)
    testImplementation(libs.json.unit.assertj)
    // Gson, two test-only roles: (1) the JSON backend json-unit 4.x discovers at runtime (without one
    // assertThatJson(...) fails to init) — its lenient parser also accepts the bare unquoted string
    // expecteds the generated serialize tests use (e.g. isEqualTo("two")); (2) the bench code generator
    // (WorkbenchTreeGenerator) parses workbench.json to emit the hardcoded WorkbenchTree.
    testImplementation(libs.gson)
    testImplementation(libs.mockito.core)
    testImplementation(libs.instancio.junit)
}

sourceSets {
    main {
        java {
            setSrcDirs(listOf(
                "src/main/java",
                "src/${currentOs}/java"
            ))
        }
    }

    // The web comm benchmark drives the production Flutter web build through a browser, so it
    // reuses the production WebFlutterServer (from webMain) to serve it — rather than duplicating
    // an HTTP server in test scope. WebFlutterServer is pure JDK + FlutterLibraryLoader (which is
    // in main), so we expose just that one file on the test classpath via this tiny source set,
    // instead of pulling the whole web tree (thousands of files) into every test build.
    val webShared = create("webShared") {
        java {
            setSrcDirs(listOf("src/webMain/java"))
            include("dev/equo/swt/WebFlutterServer.java")
        }
        compileClasspath += sourceSets.main.get().output + sourceSets.main.get().compileClasspath
    }

    // Create source sets for all platform combinations
    oss.forEach { os ->
        create(os) {
            java {
                when {
                    os.startsWith("web") -> setSrcDirs(listOf("src/main/java", "src/webMain/java", "src/${os}/java"))
                    else -> setSrcDirs(listOf("src/main/java", "src/${os}/java"))
                }
                if (os.startsWith("web")) {
                    exclude("dev/equo/swt/ConfigDyn.java", "**/GraphicsUtilsSwt.java")
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

    // The web bench (BenchBridge in -Dbench.client=web mode) compiles and runs against the
    // production WebFlutterServer exposed by the webShared set above.
    test {
        compileClasspath += webShared.output
        runtimeClasspath += webShared.output
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
        val excludeTagsProp = System.getProperty("excludeTags")
        if (excludeTagsProp != null) excludeTags(*excludeTagsProp.split(",").toTypedArray())
    }
    testLogging {
        if (System.getProperty("quietTests") != null) {
            events = setOf(TestLogEvent.FAILED)
            showStandardStreams = false
        } else {
            events = setOf(TestLogEvent.PASSED, TestLogEvent.FAILED, TestLogEvent.SKIPPED, TestLogEvent.STANDARD_OUT, TestLogEvent.STANDARD_ERROR)
        }
    }
    dependsOn("${currentPlatform}ExtractNatives", "${currentPlatform}CopyFlutterBinaries")
    if (org.gradle.internal.os.OperatingSystem.current().isMacOsX)
        jvmArgs = listOf("-XstartOnFirstThread")
    systemProperty("swt.library.path", layout.buildDirectory.dir("natives/$currentPlatform").get().toString())
    if (System.getProperty("test.debug") != null)
        jvmArgs("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005")
}

// Config shared by both bench Test tasks (native `benchmark` + browser `webBenchmark`): the
// 'bench'-tagged test run, full logging, and always-rerun. Per-task specifics (deps, jvmArgs,
// system properties) are layered on by the caller.
fun Test.configureCommBench() {
    group = "verification"
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath
    useJUnitPlatform { includeTags("bench") }
    testLogging {
        events = setOf(TestLogEvent.PASSED, TestLogEvent.FAILED, TestLogEvent.SKIPPED, TestLogEvent.STANDARD_OUT, TestLogEvent.STANDARD_ERROR)
        showStandardStreams = true
    }
    outputs.upToDateWhen { false } // always rerun benchmarks
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
    commandLine = listOf(flutterExe(), "pub", "get")
}

tasks.register<Exec>("analyze") {
    group = "build"
    description = "Flutter analyze"
    workingDir = file("../flutter-lib")
    commandLine = listOf(flutterExe(), "analyze")
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
    commandLine = listOf(dartExe(), "run", "build_runner", "build", "--delete-conflicting-outputs")
}

data class WebPlatformMeta(
    val os: String,
    val arch: String,
    val isWeb: Boolean,
) {
    val desktopPlatform: String get() = "$os-$arch"
    val sourceSet: String get() = if (isWeb) "web${os.replaceFirstChar { it.titlecase() }}" else os
    val swtWs: String get() = getSwtWs(os)
    val swtOs: String get() = getSwtOs(os)
}

fun parsePlatform(platform: String): WebPlatformMeta {
    val isWeb = platform.startsWith("web")
    var parts = platform.split("-")
    if (isWeb) parts = parts.drop(1)
    return WebPlatformMeta(parts[0], parts[1], isWeb)
}

fun CopySpec.copyFlutterNatives(os: String, flutterArch: String) {
    when (os) {
        "macos" -> from("../flutter-lib/build/macos/Build/Products/Release/swtflutter.app") {
            into("swtflutter.app")
        }
        "linux" -> {
            from("../flutter-lib/build/linux/$flutterArch/release/runner") {
                include("libflutter_library.so")
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
    commandLine = listOf(flutterExe(), "build", "web")
}

val copyWebBinaries = tasks.register<Copy>("webCopyFlutterBinaries") {
    group = "build"
    description = "Copies Flutter binaries for web"
    if (System.getProperty("skipFlutterLib") == null)
        dependsOn(webFlutterLib)
    from("../flutter-lib/build/web")
    into(layout.buildDirectory.dir("natives/web"))
}

// Create tasks for each platform JAR
platforms.forEach { platform ->
    val info = parsePlatform(platform)

    if (!info.isWeb) {
        configurations.create("${platform}SwtImpl") {
            exclude(group = "org.eclipse.platform", module = "org.eclipse.swt")
        }

        dependencies {
            configurations["${platform}SwtImpl"]("org.eclipse.platform:org.eclipse.swt.${info.swtWs}.${info.swtOs}.${info.arch}:$swtVersion")
        }

        tasks.register<Exec>("${platform}FlutterLib") {
            group = "build"
            description = "Builds Flutter lib for $platform"
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
                    commandLine = listOf("bash", "-c", "./set-arch.sh $flutterArch && '${flutterExe()}' build macos")
                }
                else -> commandLine = listOf(flutterExe(), "build", info.os)
            }
        }

        tasks.register<Copy>("${platform}CopyFlutterBinaries") {
            group = "build"
            description = "Copies Flutter binaries for $platform"

            if ((currentPlatform == platform || info.os == "macos") && System.getProperty("skipFlutterLib") == null)
                dependsOn("${info.desktopPlatform}FlutterLib")
            if (info.os == "macos")
                mustRunAfter("macos-aarch64FlutterLib", "macos-x86_64FlutterLib")

            val flutterArch = if (info.arch == "aarch64") "arm64" else "x64"
            copyFlutterNatives(info.os, flutterArch)

            into(layout.buildDirectory.dir("natives/$platform"))
        }

        tasks.register<Copy>("${platform}ExtractNatives") {
            from(configurations["${platform}SwtImpl"].map { zipTree(it) })
            into(layout.buildDirectory.dir("natives/$platform"))
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
        from(layout.buildDirectory.dir("natives/${info.desktopPlatform}"))
        if (info.isWeb) {
            dependsOn(copyWebBinaries)
            from(layout.buildDirectory.dir("natives/web")) {
                into("web")
            }
        }

        // Add all dependencies to the JAR
        from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
        exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA", "META-INF/LICENSE*", "META-INF/NOTICE*", "OSGI-OPT/")

        manifest {
            attributes(
                "Fragment-Host" to provider { "org.eclipse.swt;bundle-version=\"[${swtVersionProvider.get().substring(0..6)},4.0.0)\"" },
                "Bundle-Name" to "SWT Evolve for ${info.swtOs} on ${info.arch}",
                "Bundle-Vendor" to "Equo Tech, Inc.",
                "Bundle-SymbolicName" to "org.eclipse.swt.${info.swtWs}.${info.swtOs}.${info.arch}; singleton:=true",
                "Bundle-Version" to swtVersionProvider,
                "Bundle-ManifestVersion" to 2,
                "Export-Package" to "org.eclipse.swt,org.eclipse.swt.accessibility,"+
                        "org.eclipse.swt.awt,org.eclipse.swt.browser,org.eclipse.swt.custom,"+
                        "org.eclipse.swt.dnd,org.eclipse.swt.events,org.eclipse.swt.graphics,"+
                        "org.eclipse.swt.layout,org.eclipse.swt.opengl,org.eclipse.swt.printing,"+
                        "org.eclipse.swt.program,org.eclipse.swt.widgets,org.eclipse.swt.internal; x-friends:=\"org.eclipse.ui\","+
                        "org.eclipse.swt.internal.image; x-internal:=true,org.eclipse.swt.internal.${info.swtWs}; x-friends:=\"org.eclipse.ui\"," +
                        "com.equo.chromium.swt",
                "Eclipse-PlatformFilter" to "(& (osgi.ws=${info.swtWs}) (osgi.os=${info.swtOs}) (osgi.arch=${info.arch}) )",
                "SWT-WS" to info.swtWs,
                "SWT-OS" to info.swtOs,
                "SWT-Arch" to info.arch,
                "Automatic-Module-Name" to "org.eclipse.swt.${info.swtWs}.${info.swtOs}.${info.arch}",
                "Evolve-Version" to (gradle.parent?.rootProject?.version ?: project.version),
            )
            if (info.isWeb)
                attributes("Import-Package" to "com.equo.chromium;resolution:=optional," +
                        "com.equo.chromium.utils;resolution:=optional," +
                        "com.equo.chromium.events;resolution:=optional")
            }
        dependsOn("${info.desktopPlatform}ExtractNatives")
        dependsOn("${info.desktopPlatform}CopyFlutterBinaries")
    }
}

tasks.register("buildAllPlatforms") {
    group = "build"
    description = "Builds JARs for all platforms"
    dependsOn(platforms.map { "${it}Jar" })
}

// Configure the default build task to include all platform JARs
tasks.build {
    dependsOn("buildAllPlatforms")
}
