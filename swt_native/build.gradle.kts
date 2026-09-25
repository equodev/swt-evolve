import java.util.zip.ZipFile
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.kotlin.dsl.*
import java.io.File

plugins {
    java
    `java-library`
    jacoco
}

val eclipseUrl: String by project
val draw2dVersion: String by project

repositories {
    mavenCentral()
    mavenLocal()
    // draw2d, for the tests that cover how a FigureCanvas is painted. Scoped to that one module so
    // nothing else can resolve through the Eclipse update site. Same declaration examples/ uses.
    ivy {
        url = uri("${project.findProperty("draw2dUrl")?.toString() ?: eclipseUrl}/plugins")
        name = "Eclipse Plugins"
        patternLayout { artifact("[organisation].[artifact]_[revision].[ext]") }
        metadataSources { artifact() }
        content { includeModule("org.eclipse", "draw2d") }
    }
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
    // Test-only: the paint path used to exclude a FigureCanvas via an isInstance() check, so the
    // real type has to be resolvable for a test to tell that exclusion from its absence.
    testImplementation("org.eclipse:draw2d:$draw2dVersion")

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

// Classes that need a JDK newer than Java 8 and have no Java 8 equivalent to fall back on:
// dev/equo/swt/awt is built on jdk.swing.interop (added in 9) and dev/equo/swt/jdk9 on StackWalker.
// Everything reaches them through dev.equo.swt.Jdk9, which resolves them reflectively, so a fragment
// built for an older SWT release can leave them out and still work -- SWT_AWT falls back to native
// reparenting, Config to a Throwable-based stack read. Excluding them is what lets the rest of the
// tree compile at --release 8, where those packages are not visible at all.
//
// javafx/embed/swt (FXCanvas) is the same case: it opens the host's javafx.graphics module to this
// bundle through java.lang.Module, which does not exist before 9, and the whole feature embeds modern
// JavaFX (21 needs Java 17+), so it can never run on a pre-9 SWT release. It is left out of those
// fragments entirely -- there is nothing to fall back to, and nothing that could use it there.
val jdk9OnlySources = listOf("**/dev/equo/swt/awt/**", "**/dev/equo/swt/jdk9/**", "**/javafx/embed/swt/**")

// The oldest JDK the SWT release being built for supports, from gradle/versions/{ver}.properties.
val targetJavaRelease = (project.findProperty("minJavaVersion") as String?)?.toIntOrNull()?.takeIf { it < 21 }
val dropJdk9Sources = targetJavaRelease != null && targetJavaRelease < 9

// The desktop surface paints text with the host's own font stack, which is what the embedded
// backend's table for the same OS was measured on. Each native<OS> tree gets that table as
// GenDesktopFontMetrics, beside the shared GenFontMetrics the web surface measures with (see
// FontMetricsUtil.metrics).
val desktopFontMetrics = oss.filter { it.startsWith("native") }.associateWith { os ->
    tasks.register<Copy>("${os}DesktopFontMetrics") {
        from("src/embed${os.removePrefix("native")}/java/dev/equo/swt/GenFontMetrics.java") {
            into("dev/equo/swt")
            rename { "GenDesktopFontMetrics.java" }
            filter { it.replace("GenFontMetrics", "GenDesktopFontMetrics") }
        }
        into(layout.buildDirectory.dir("generated/desktopFontMetrics/$os"))
    }
}

sourceSets {
    main {
        java {
            setSrcDirs(listOf(
                "src/main/java",
                "src/native/java",
                "src/native${currentOs.replaceFirstChar { it.titlecase() }}/java"
            ))
            srcDir(desktopFontMetrics.getValue("native${currentOs.replaceFirstChar { it.titlecase() }}"))
            exclude(nativeFlutterExcludes)
            if (dropJdk9Sources) exclude(jdk9OnlySources)
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
            include("dev/equo/swt/WebFlutterServer.java", "dev/equo/swt/WebFontSubstitutions.java",
                    "dev/equo/swt/HeadlessChrome.java")
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
                desktopFontMetrics[os]?.let { srcDir(it) }
                if (dropJdk9Sources) exclude(jdk9OnlySources)
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

    val nativeBackend = getByName("native${currentOs.replaceFirstChar { it.titlecase() }}")

    // `test` is the whole-tree-Flutter suite: the backend that ships, so it is what `./gradlew test`
    // and `build` run. Compiled against src/main + src/native + src/native<currentOs>; everything in
    // it runs, tags only say what to leave out.
    test {
        java {
            setSrcDirs(listOf("src/test/java"))
            // Mac*NativeTest: assert against Cocoa itself, so they only compile where the per-OS
            // backend carries org.eclipse.swt.internal.cocoa. An @EnabledOnOs would skip the run
            // but the source still has to compile on every runner.
            if (currentOs != "macos") exclude("**/Mac*NativeTest.java")
            // ImageDataAtSizeProvider and the at-size machinery it drives only exist from 3.132
            // (absent in 3.131), so the test covering that path cannot compile against older SWT.
            val swtMinorNative = swtVersion.split(".").getOrNull(1)?.toIntOrNull() ?: Int.MAX_VALUE
            if (swtMinorNative < 132) exclude("org/eclipse/swt/graphics/ImageAtSizeProviderNativeTest.java")
            // ImageGcDrawer (and the Image constructor taking it) only exist from 3.129, so the
            // tests covering that constructor cannot compile against older SWT.
            if (swtMinorNative < 129) {
                exclude("org/eclipse/swt/graphics/ImageGcDrawerFailureTest.java")
                exclude("org/eclipse/swt/graphics/ImageGcDrawerTrafficTest.java")
            }
            // The committed WorkbenchTree bench fixture is generated against the DEFAULT SWT and
            // bakes in APIs newer than the oldest supported versions (e.g.
            // CTabFolder.setSelectionBarThickness, added in 3.121). The bench suite is tagged
            // 'bench' and never runs in a version job — it only needs to compile — so below that
            // baseline it is dropped rather than made to compile.
            if (swtMinorNative < 121) exclude("dev/equo/swt/bench/**")
            // org.eclipse.swt.layout.BorderLayout was added in 3.119; the test that subclasses it
            // cannot compile against older baselines. Layout subclassing itself stays covered by
            // LayoutSubclassTest, which does not touch BorderLayout.
            if (swtMinorNative < 119) exclude("org/eclipse/swt/widgets/BorderLayoutSubclassTest.java")
        }
        // src/test/resources is this source set's own by convention; the per-OS blocks above add the
        // .css/.png/SWTMessages that sit beside the generated Java.
        compileClasspath += nativeBackend.output + nativeBackend.compileClasspath
        runtimeClasspath += output + nativeBackend.output + nativeBackend.runtimeClasspath
    }

    // What is left of the embedded suite: the tests whose subject is Dart and Swt widgets coexisting
    // in one Display, which only the embed<OS> backend can host. Retired with that backend; kept
    // runnable locally in the meantime.
    val embedBackend = getByName("embed${currentOs.replaceFirstChar { it.titlecase() }}")
    create("embedTest") {
        java { setSrcDirs(listOf("src/embedTest/java")) }
        resources { srcDirs("src/test/resources") }
        compileClasspath += embedBackend.output + webShared.output
        runtimeClasspath += output + embedBackend.output + webShared.output
    }
}

// embedTest reuses the test dependencies (JUnit, AssertJ, Mockito, Gson, …) and annotation processor.
configurations["embedTestImplementation"].extendsFrom(configurations["testImplementation"])
configurations["embedTestRuntimeOnly"].extendsFrom(configurations["testRuntimeOnly"])
configurations["embedTestAnnotationProcessor"].extendsFrom(configurations["testAnnotationProcessor"])

run {
    val jfaceVersion: String by project
    val coreCommandsVersion: String by project
    val equinoxCommonVersion: String by project
    dependencies {
        "testImplementation"("org.eclipse.platform:org.eclipse.jface:$jfaceVersion") {
            exclude(group = "org.eclipse.platform", module = "org.eclipse.swt")
        }
        "testImplementation"("org.eclipse.platform:org.eclipse.core.commands:$coreCommandsVersion")
        "testImplementation"("org.eclipse.platform:org.eclipse.equinox.common:$equinoxCommonVersion")

        // FXCanvasSwtThreadDispatchNativeTest drives the FXCanvas SWT-thread dispatch against a
        // live JavaFX toolkit. It needs JavaFX + headless Monocle on the test classpath (unnamed
        // module), exactly as a product ships the FX jars alongside.
        val javafxVersion = libs.versions.javafx.get()
        val javafxClassifier = when (currentOs) {
            "macos" -> if (getSwtArch(arch) == "aarch64") "mac-aarch64" else "mac"
            "windows" -> "win"
            else -> if (getSwtArch(arch) == "aarch64") "linux-aarch64" else "linux"
        }
        "testImplementation"("org.openjfx:javafx-base:$javafxVersion:$javafxClassifier")
        "testImplementation"("org.openjfx:javafx-graphics:$javafxVersion:$javafxClassifier")
        "testImplementation"("org.openjfx:javafx-controls:$javafxVersion:$javafxClassifier")
        "testRuntimeOnly"("org.testfx:openjfx-monocle:${libs.versions.openjfx.monocle.get()}") {
            isTransitive = false
        }
    }
}

val chromiumMode = System.getProperty("mode.chromium", "false") == "true"
if (chromiumMode) {
    dependencies {
        "testRuntimeOnly"(libs.equo.chromium)
        "testRuntimeOnly"("com.equo:com.equo.chromium.cef.${getSwtWs(currentOs)}.${getSwtOs(currentOs)}.${getSwtArch(arch)}:${libs.versions.equo.chromium.cef.get()}")
    }
}

// Configure Java compilation
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
    // Compile AT the level this jar has to run on rather than compiling at 21 and rewriting the
    // bytecode afterwards. The generator copies each SWT release's sources at that release's own
    // level and src/main is written to the lowest one, so there is nothing left to downgrade.
    //
    // Two ways of saying it, because javac refuses --add-exports on a system module together with
    // --release, at ANY level. Below 9 the Swing bridge that needs those exports is excluded anyway
    // (jdk.swing.interop does not exist there), so --release applies and brings its API check with
    // it. From 9 up the bridge ships, so the level is set with -source/-target and the API check is
    // left to checkJavaApiLevel, which excludes the bridge and can therefore use --release.
    when {
        targetJavaRelease == null -> {}
        dropJdk9Sources -> options.release.set(targetJavaRelease)
        else -> {
            sourceCompatibility = targetJavaRelease.toString()
            targetCompatibility = targetJavaRelease.toString()
            // -source/-target compiles against THIS JDK's API, so javac warns it cannot verify the
            // result runs on the older one. checkJavaApiLevel is what actually verifies it.
            options.compilerArgs.add("-Xlint:-options")
        }
    }
    // The Evolve SWT_AWT bridge (dev.equo.swt.awt.*) hosts Swing off-screen through
    // sun.swing.JLightweightFrame / LightweightContent — the same internal contract
    // JavaFX's SwingNode uses. These packages are not exported by default.
    // jdk.swing.interop.DispatcherWrapper: lets EvolveSwingHost install its own AWT/Swing
    // dispatcher on java.awt.EventQueue, routed through the SWT Display's own pump, instead of
    // leaving whatever JavaFX/host bridge code installs last (which routes through a JavaFX
    // event loop DartDisplay never pumps, so posted tasks never run). The runtime JVM this
    // actually executes under also needs the matching --add-exports on its own command line
    // (the deployment's own -vmargs), independent of this compile-time one.
    // Only meaningful from 9 on: below that there are no modules to export from, javac rejects the
    // flag outright, and the Swing bridge that needs them is excluded from the build anyway.
    if (!dropJdk9Sources) options.compilerArgs.addAll(listOf(
        "--add-exports", "java.desktop/sun.swing=ALL-UNNAMED",
        "--add-exports", "java.desktop/sun.awt=ALL-UNNAMED",
        "--add-exports", "jdk.unsupported.desktop/jdk.swing.interop=ALL-UNNAMED"
    ))
}

// Guards the Java level of everything we hand to an older SWT release. The generator copies each
// release's sources at that release's own level, and src/main is shared across all of them, so the
// only way a Java 9+ construct reaches a fragment built for SWT 3.114 (minJavaVersion=8) is from our
// own code. Compiling at --release 8 is what proves it: unlike -source/-target it checks the API
// surface too, so Map.ofEntries or String.isBlank fail here instead of at a customer's runtime.
//
// dev/equo/swt/awt (and the SWT_AWT that calls into it) is excluded: the Swing bridge is built on
// sun.swing / jdk.swing.interop, which --release hides because they are not documented API. That
// pair is the one thing still standing between this check and dropping the bytecode downgrader --
// either the bridge leaves the fragments built for pre-9 releases, or it keeps them on -source 8
// with no API check.
// Only has something to say on a build targeting an older release; on the default (21) it skips.
val checkJavaApiLevel by tasks.registering(JavaCompile::class) {
    group = "verification"
    description = "Compiles the embedded backend at the target release's minJavaVersion, to catch newer-Java API leaking into an older SWT release"
    onlyIf {
        if (targetJavaRelease == null) logger.lifecycle("No minJavaVersion below 21 - nothing to check.")
        targetJavaRelease != null
    }
    val backend = sourceSets.getByName("embed${currentOs.replaceFirstChar { it.titlecase() }}")
    source = backend.java.asFileTree.matching { exclude(jdk9OnlySources) }
    classpath = backend.compileClasspath
    destinationDirectory.set(layout.buildDirectory.dir("java-api-check"))
    options.encoding = "UTF-8"
    options.release.set(targetJavaRelease ?: 21)
    // -Xlint:-options silences the "source value 8 is obsolete" pair; -proc:none keeps the dsl-json
    // annotation processor out (it targets 21 and has nothing to say about the Java level).
    options.compilerArgs.addAll(listOf("-Xlint:-options", "-proc:none"))
    // The withType<JavaCompile> block below adds --add-exports for the Swing bridge; javac refuses
    // it together with target 8, and this task excludes the bridge anyway.
    doFirst {
        options.compilerArgs.removeAll { it == "--add-exports" || it.endsWith("=ALL-UNNAMED") }
    }
}

// The files the generator does not own: src/main is entirely hand-written, and these names are
// hand-written islands inside the generated trees. Mirrors the exclusion list in swtgenerator's
// generatedJavaTree - HandWrittenSourcesInSyncTest fails the build if the two ever drift.
val handWrittenInGeneratedTrees = listOf(
    "**/*Bridge*.java",
    "**/WebFlutterServer.java",
    "**/ProxyHandler.java",
    "**/HeadlessChrome.java",
    "**/VDisplay.java",
    "**/VShellPopups.java",
    "**/GenFontMetrics.java",
    "**/WidgetSpy.java",
    "**/MacApplicationMenu.java",
    "**/MacMenuBar.java",
)

// The oldest JDK any enabled release runs on. Hand-written code is shared by every one of them, so
// this is the level it has to fit inside, whatever version the build happens to be targeting.
val javaFloor = 8

// checkJavaApiLevel needs the generated tree to BE the old release's, so it can only run in the
// release pipeline, after that version was generated. This one needs nothing generated: it compiles
// the hand-written files alone, with the already-built backend on the classpath. javac reads those
// Java 21 class files happily while holding the sources it is given to release 8, so the check runs
// on any branch, against whatever version is checked out, in seconds.
//
// That is the point. The shared sources reach every fragment, including the Java 8 ones, and
// nothing on an MR compiled them at that level: the normal pipeline builds at 21 and the
// old-version ladder is a child pipeline gated on release. Newer-Java syntax kept reaching main and
// surfacing days later, in a release job.
fun registerJava8Check(name: String, backendName: String, extraDirs: List<String>, flutterBackend: Boolean) =
    tasks.register<JavaCompile>(name) {
        group = "verification"
        description = "Compiles the hand-written sources of $backendName at Java $javaFloor"
        val backend = sourceSets.getByName(backendName)
        dependsOn(backend.classesTaskName)
        source = files(
            fileTree("src/main/java") { include("**/*.java") },
            *extraDirs.map { d ->
                fileTree("src/$d/java") { include(handWrittenInGeneratedTrees) }
            }.toTypedArray()
        ).asFileTree.matching {
            exclude(jdk9OnlySources)
            // ConfigDyn and GraphicsUtilsSwt reach for Dyn*/Swt* classes that only the embedded
            // backend generates, which is why its source sets drop them; mirror that here.
            if (flutterBackend) exclude(nativeFlutterExcludes)
        }
        // The generated classes this code calls into come from the classpath, already compiled.
        classpath = backend.output + backend.compileClasspath
        destinationDirectory.set(layout.buildDirectory.dir("java8-check/$name"))
        options.encoding = "UTF-8"
        options.release.set(javaFloor)
        options.compilerArgs.addAll(listOf("-Xlint:-options", "-proc:none"))
        // javac rejects --add-exports together with --release at any level; the sources that need
        // those exports are the jdk9-only ones this task excludes.
        doFirst { options.compilerArgs.removeAll { it == "--add-exports" || it.endsWith("=ALL-UNNAMED") } }
    }

private val osSuffix = currentOs.replaceFirstChar { it.titlecase() }

// One per backend: the same file name appears in several trees (DisplayBridgePlatform in each
// native<OS> and in web, SwtEmbeddedBridge and WidgetSpy in each embed<OS>), so one merged compile
// would collide on duplicate classes.
val checkJava8Native = registerJava8Check(
    "checkJava8Native", "native$osSuffix", listOf("native", "native$osSuffix"), flutterBackend = true)
val checkJava8Embed = registerJava8Check(
    "checkJava8Embed", "embed$osSuffix", listOf("embed$osSuffix"), flutterBackend = false)
val checkJava8Web = registerJava8Check(
    "checkJava8Web", "web", listOf("native", "web"), flutterBackend = true)

val checkJava8Sources by tasks.registering {
    group = "verification"
    description = "Checks every hand-written source that ships to a Java $javaFloor release"
    dependsOn(checkJava8Native, checkJava8Embed, checkJava8Web)
}

// checkJavaApiLevel proves the SOURCE stays inside the release's API. This proves the ARTIFACT:
// --release / -target are supposed to stamp the class-file version, but until something reads the
// jar back nobody has confirmed they did, and a shaded dependency can carry a newer class in behind
// them. Runs in swt_build, per version, once the jars exist.
val ourPackages = listOf("org/eclipse/swt/", "dev/equo/swt/")

val verifyJarJavaLevel by tasks.registering {
    group = "verification"
    description = "Reads the built jars back and checks every class against the target release's class-file version"
    // The jars are the subject, so this can never be considered up to date on their behalf.
    outputs.upToDateWhen { false }
    val libsDir = layout.buildDirectory.dir("libs")
    val release = targetJavaRelease ?: 21
    val expected = release + 44 // Java 8 -> 52, 11 -> 55, 17 -> 61, 21 -> 65
    doLast {
        val jars = libsDir.get().asFile
            .listFiles { f: File -> f.name.startsWith("swt_evolve-") && f.name.endsWith(".jar") }
            ?.sortedBy { it.name }.orEmpty()
        if (jars.isEmpty())
            throw GradleException("No swt_evolve-*.jar under ${libsDir.get().asFile} - the jars must be built first, " +
                    "or this check passes while verifying nothing.")

        val problems = mutableListOf<String>()
        jars.forEach { jar ->
            var checked = 0
            ZipFile(jar).use { zip ->
                for (entry in zip.entries()) {
                    // A multi-release overlay is invisible to the older JVM by design, so its
                    // class-file version says nothing about what that JVM will load.
                    if (!entry.name.endsWith(".class") || entry.name.startsWith("META-INF/versions/")) continue
                    val major = zip.getInputStream(entry).use { ins ->
                        val head = ByteArray(8)
                        var read = 0
                        while (read < head.size) {
                            val n = ins.read(head, read, head.size - read)
                            if (n < 0) break
                            read += n
                        }
                        if (read < head.size) -1
                        else ((head[6].toInt() and 0xFF) shl 8) or (head[7].toInt() and 0xFF)
                    }
                    if (major < 0) {
                        problems += "${jar.name}: ${entry.name} is truncated"
                        continue
                    }
                    // Anything above the target simply will not load there - bundled dependencies included.
                    if (major > expected) problems += "${jar.name}: ${entry.name} is major $major, above $expected"
                    // Our own classes must sit exactly on it: a lower one would mean the compile
                    // targeted something other than what this version declares.
                    if (ourPackages.any { entry.name.startsWith(it) }) {
                        checked++
                        if (major != expected) problems += "${jar.name}: ${entry.name} is major $major, expected $expected"
                    }
                }
            }
            if (checked == 0) problems += "${jar.name}: holds no ${ourPackages.joinToString(" or ")} classes"
            else logger.lifecycle("${jar.name}: $checked classes at major $expected (Java $release)")
        }
        if (problems.isNotEmpty()) {
            val shown = problems.take(20).joinToString("\n  ")
            val rest = if (problems.size > 20) "\n  ... and ${problems.size - 20} more" else ""
            throw GradleException("Class-file version check failed for Java $release:\n  $shown$rest")
        }
    }
}

// Coverage is only consumed from the native suite (merged into swt_eclipse_tests' combined report).
// Instrumenting the embedded one is unnecessary and perturbs its shared static state (Config
// routing), causing order-dependent failures — keep JaCoCo off everywhere except `test`.
tasks.withType<Test>().configureEach {
    extensions.configure(org.gradle.testing.jacoco.plugins.JacocoTaskExtension::class) {
        isEnabled = name == "test"
    }
}

/**
 * The tags a run skips: the ones whose tests are known to be *broken*, plus whatever the caller
 * added with -DexcludeTags=a,b. -DrunBroken=true opts the broken ones back in, to work on them.
 *
 * Broken-tag defaults belong to the task, not to each caller's command line, or only that one
 * caller gets them and a local run disagrees with the pipeline. A tag excluded because the *runner*
 * cannot do the work stays on the invocation instead — 'metal' needs a GPU-backed Flutter engine,
 * absent from the CI VMs but present on a developer machine, so it is not defaulted here.
 */
fun excludedTags(vararg brokenTags: String): Array<String> {
    val broken = if (System.getProperty("runBroken") == "true") emptyList() else brokenTags.asList()
    val requested = System.getProperty("excludeTags")
        ?.split(",")?.map(String::trim)?.filter(String::isNotEmpty) ?: emptyList()
    return (broken + requested).toTypedArray()
}

tasks.register<Test>("embedTest") {
    group = "verification"
    description = "Runs what is left of the embedded suite: the tests whose subject is Dart and Swt " +
            "widgets coexisting in one Display. Not run by CI; kept for local runs while that " +
            "backend is still shipped."
    testClassesDirs = sourceSets["embedTest"].output.classesDirs
    classpath = sourceSets["embedTest"].runtimeClasspath
    useJUnitPlatform {
        // Bench tests are slow + write artifacts; always excluded from the default test run.
        excludeTags("bench")
        // Flutter integration tests need a Flutter web build + Chrome; not run by default.
        excludeTags("flutter-it")
        excludeTags(*excludedTags())
    }
    configureTestLogging()
    dependsOn("${currentPlatform}ExtractNatives", "${currentPlatform}CopyFlutterBinaries")
    if (org.gradle.internal.os.OperatingSystem.current().isMacOsX)
        jvmArgs = listOf("-XstartOnFirstThread")
    // Runtime counterpart of the compile-time exports above -- EvolveDispatcherWrapperTest loads
    // EvolveSwingHost's dispatcher classes, which extend/reference these restricted packages.
    jvmArgs("--add-exports", "java.desktop/sun.swing=ALL-UNNAMED",
            "--add-exports", "java.desktop/sun.awt=ALL-UNNAMED",
            "--add-exports", "jdk.unsupported.desktop/jdk.swing.interop=ALL-UNNAMED")
    systemProperty("harness.client", "native")
    // Lets a run choose how state is delivered, so the two modes can be compared against each
    // other on the same suite. Read once at class-load, so it has to reach the test JVM itself
    // rather than only the Gradle one.
    System.getProperty("equo.swt.diff")?.let { systemProperty("equo.swt.diff", it) }
    systemProperty("swt.library.path", layout.buildDirectory.dir("natives/$currentPlatform").get().toString())
    if (System.getProperty("test.debug") != null)
        jvmArgs("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005")
}

tasks.test {
    group = "verification"
    description = "Runs the tests that need the whole-tree-Flutter (native/web) Java backend — every " +
            "widget, including Display/Shell, is Dart-backed, no Swt* classes involved. That is the " +
            "Flutter integration tests (tagged 'flutter-it', defaulting to the WEB render client, " +
            "headless Chrome + CanvasKit; pass -Dharness.client=native for the desktop Flutter engine) " +
            "plus the renderer-free unit tests of native-only classes (tagged 'native-unit')."
    // One JVM holds the whole suite plus a headless browser and its comm; Gradle's 512m default
    // dies mid-run with OutOfMemoryError, blamed on whatever allocated next. Allocation rate, not
    // retention: a mixed collection returns this suite to 12-15M, and the peak simply tracks the
    // ceiling given (974M of 1024M, 1423M of 2048M). The number buys the collector headroom, so it
    // is headroom that can be traded away: -DtestHeap=1g is what lets several forks share a
    // container, and the suite is green on it.
    maxHeapSize = System.getProperty("testHeap") ?: "2g"
    // Each fork is its own JVM with its own browser on an ephemeral port, so the shared statics
    // this suite is sensitive to (the mock display registry, FlutterBridge's dirty set) cannot
    // cross between them. Off by default all the same, because how many fit depends on the machine:
    // the callers that know their own headroom (preMerge, the CI job) pass -DtestForks.
    // Capped by the cores actually visible to this JVM: a caller asking for more than the machine
    // (or the container's quota) has just adds JVMs that wait for each other.
    maxParallelForks = (System.getProperty("testForks")?.toIntOrNull() ?: 1)
        .coerceIn(1, Runtime.getRuntime().availableProcessors())
    if (chromiumMode)
        classpath = sourceSets["native${currentOs.replaceFirstChar { it.titlecase() }}"].output + sourceSets["test"].runtimeClasspath
    useJUnitPlatform {
        // Everything in the source set runs; tags only say what to leave out. An includeTags list
        // here meant a class had to be tagged to run at all, and one that was not was compiled and
        // then silently skipped.
        excludeTags("bench")
        // 'web-known-broken': the widget-size suite the web/CanvasKit client cannot measure
        // correctly yet — part of it needs the native image decoder this backend deliberately does
        // not ship, the rest is real Java-vs-Flutter measurement drift. Tracked on its own; until
        // it is fixed no run wants it.
        excludeTags(*excludedTags("web-known-broken"))
    }
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
    // Needed on macOS for the desktop Flutter engine (-Dharness.client=native) and for chromium
    // mode; harmless (unused) for the default headless-browser web client.
    if (org.gradle.internal.os.OperatingSystem.current().isMacOsX)
        jvmArgs = listOf("-XstartOnFirstThread")
    // Runtime counterpart of the compile-time exports above -- EvolveDispatcherWrapperTest loads
    // EvolveSwingHost's dispatcher classes, which extend/reference these restricted packages.
    jvmArgs("--add-exports", "java.desktop/sun.swing=ALL-UNNAMED",
            "--add-exports", "java.desktop/sun.awt=ALL-UNNAMED",
            "--add-exports", "jdk.unsupported.desktop/jdk.swing.interop=ALL-UNNAMED")
    // equo.swt.diff: lets this suite run against either way of delivering state, so the whole
    // end-to-end path can be compared between them rather than only the Java side.
    // fontsize.seed: FontSizeTest fuzzes over random sizes and texts and prints the seed it used,
    // so a red run can be repeated with -Dfontsize.seed=<the printed value>. Read in the test JVM,
    // so it has to be forwarded rather than left on the Gradle one.
    forwardSystemProperties("harness.client", "harness.web.headless", "harness.web.console", "harness.readyTimeoutMs", "harness.queryTimeoutMs", "harness.holdMs", "equo.swt.browser", "dev.equo.swt.mode", "harness.bootAttempts", "harness.bootAttemptMs", "harness.web.failBoots", "equo.swt.diff", "fontsize.seed")
}

// Config shared by both bench Test tasks (native `benchmark` + browser `webBenchmark`): the
// 'bench'-tagged test run, full logging, and always-rerun. Per-task specifics (deps, jvmArgs,
// system properties) are layered on by the caller.
// It measures the comm protocol against the whole-tree-Flutter backend, in desk and web mode both,
// so it runs from the `test` source set.
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

/**
 * What the Dart-side checks read. Neither `flutter analyze` nor `flutter test` produces a file, so
 * without this they re-run on every invocation — together about 55s that a `preMerge` changing only
 * Java was paying for nothing. The stamp gives Gradle an output to compare so an unchanged Dart tree
 * skips both.
 */
fun Task.dartSourcesAsInputs() {
    // dartRunner writes the .g.dart/.tailor.dart files into lib/, so anything reading lib/ runs
    // after it. These checks always did; declaring the inputs is what made Gradle notice.
    dependsOn("dartRunner")
    inputs.dir("../flutter-lib/lib").withPathSensitivity(PathSensitivity.RELATIVE)
    inputs.dir("../flutter-lib/test").withPathSensitivity(PathSensitivity.RELATIVE)
    inputs.file("../flutter-lib/pubspec.yaml")
    inputs.file("../flutter-lib/analysis_options.yaml")
    outputs.file(layout.buildDirectory.file("stamps/$name.stamp"))
}

fun Task.stampOnSuccess() = doLast {
    val stamp = layout.buildDirectory.file("stamps/$name.stamp").get().asFile
    stamp.parentFile.mkdirs()
    stamp.writeText(System.currentTimeMillis().toString())
}

tasks.register<Exec>("analyze") {
    group = "build"
    description = "Flutter analyze"
    workingDir = file("../flutter-lib")
    commandLine = flutterCmd() + listOf("analyze")
    dartSourcesAsInputs()
    stampOnSuccess()
}

tasks.register<Exec>("flutterTest") {
    group = "verification"
    description = "Runs the Dart unit tests in flutter-lib (analyze answers whether the Dart side " +
            "is sound; this answers whether it behaves)."
    workingDir = file("../flutter-lib")
    dependsOn(pub)
    // `flutter test` sizes its pool at cores/2 and, on Linux, reads the node's cores rather than
    // the container's quota, which oversubscribes a shared runner badly. availableProcessors() is
    // the number that respects a cgroup quota, so it is both the safe one in a container and the
    // whole machine on a workstation.
    val jobs = System.getProperty("flutterTestJobs") ?: System.getenv("FLUTTER_TEST_JOBS")
        ?: Runtime.getRuntime().availableProcessors().toString()
    // 'bench' is the Dart side of the tag the Java suite already excludes: measurements that print
    // rather than assert. -DrunDartBench=true runs them.
    val benchTags = if (System.getProperty("runDartBench") == "true") emptyList()
                    else listOf("--exclude-tags=bench")
    commandLine = flutterCmd() + listOf("test", "--concurrency=$jobs") + benchTags
    dartSourcesAsInputs()
    stampOnSuccess()
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

// Liberation fonts substitute system fonts for CanvasKit's web renderer, which can't reach
// OS-installed fonts. Desktop builds never trigger the substitution (WebFontSubstitutions is
// only loaded by WebFlutterServer), so they'd carry the ~9MB of TTFs for nothing.
val libertyFontExclude = "**/flutter_assets/assets/fonts/Liberation*.ttf"

// Dart debug/JIT build: `-PdartDebug` builds the desktop Flutter frameworks in --debug (JIT) mode
// instead of the default --release (AOT), so the embedded engine exposes a Dart VM Service that
// DTD/MCP tooling (and flutter_driver) can attach to. Release is unchanged and remains the default.
// (One flag across render modes — web interprets it as `flutter run`; see examples/build.gradle.kts.)
// See docs/design/flutter-dtd-introspection.md (Phases 1 & 2).
val dartDebug = project.hasProperty("dartDebug")

fun CopySpec.copyFlutterNatives(os: String, flutterArch: String, debug: Boolean) {
    // Flutter emits build outputs under a mode-named directory: macOS/Windows capitalize
    // (Products/Debug, runner/Debug), Linux lower-cases (build/linux/<arch>/debug).
    val modeCap = if (debug) "Debug" else "Release"
    val modeLower = if (debug) "debug" else "release"
    when (os) {
        "macos" -> from("../flutter-lib/build/macos/Build/Products/$modeCap/swtflutter.app") {
            into("swtflutter.app")
            exclude(libertyFontExclude)
        }
        "linux" -> {
            from("../flutter-lib/build/linux/$flutterArch/$modeLower/runner") {
                include("libflutter_bridge.so")
                into("runner")
            }
            from("../flutter-lib/build/linux/$flutterArch/$modeLower/bundle/lib") {
                include("*.so")
                into("bundle/lib")
            }
            from("../flutter-lib/build/linux/$flutterArch/$modeLower/bundle/data") {
                include("icudtl.dat", "flutter_assets/**")
                exclude(libertyFontExclude)
                into("bundle/data")
            }
        }
        "windows" -> from("../flutter-lib/build/windows/$flutterArch/runner/$modeCap/") {
            include("*.dll", "data/")
            exclude(libertyFontExclude)
            into("runner")
        }
    }
}

// Single shared Flutter web build — all web platform JARs depend on this one task.
// Test semantics are NOT a build-time concern: the semantics tree is toggled purely at runtime via
// -Ddev.equo.swt.web.enableTestSemantics=true on the embedding app's JVM (see WebFlutterServer and
// main.dart's getEnableTestSemantics), so one bundle serves both production and E2E — no separate build.
val webFlutterLib = tasks.register<Exec>("webFlutterLib") {
    group = "build"
    description = "Builds Flutter web app (shared by all web platform JARs)"
    dependsOn(dart, pub)
    workingDir = file("../flutter-lib")
    inputs.dir("../flutter-lib/lib")
    inputs.dir("../flutter-lib/web")
    inputs.dir("../flutter-lib/assets")
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
val fxImportPackges = "javafx.application;resolution:=optional," +
        "javafx.scene;resolution:=optional," +
        "javafx.scene.image;resolution:=optional," +
        "com.sun.javafx.cursor;resolution:=optional," +
        "com.sun.javafx.embed;resolution:=optional," +
        "com.sun.javafx.stage;resolution:=optional"
fun fragmentHostHeader() =
    provider { "org.eclipse.swt;bundle-version=\"[${swtVersionProvider.get().substringBefore(".v")},4.0.0)\"" }
fun evolveVersion(): Any = gradle.parent?.rootProject?.version ?: project.version
fun hostVersion(suffix: String) = swtVersionProvider.map {
    "${it.substringBefore(".v")}.v${evolveVersion().toString().replace('.', '_')}-$suffix"
}
// -PexcludeFxCanvas: build the fragment WITHOUT our javafx.embed.swt.FXCanvas — neither the
// classes nor the package export — so a host that already provides one keeps using its own.
//
// Why that can be preferable: e(fx)clipse loads its FXCanvas inside the JavaFX ModuleLayer,
// where com.sun.javafx.* is reachable, so it works where ours cannot (see the FXCanvas issue).
// In hosts that route their AWT event processor through FXCanvas, ours failing does not just
// blank a canvas — it costs them single-UI-thread dispatch, and everything built on that
// assumption breaks. Deferring to the host's FXCanvas keeps that arrangement intact; JavaFX
// content still reaches the screen because upstream FXCanvas paints through the SWT Canvas's
// GC, which Evolve pipes to Flutter — what is given up is *our* render path, not the content.
//
// Off by default: with no host-provided FXCanvas, ours is the only one there is.
val excludeFxCanvas = project.hasProperty("excludeFxCanvas")

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
        + (if (excludeFxCanvas) emptyList() else listOf("javafx.embed.swt"))
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
            inputs.dir("../flutter-lib/assets")
            inputs.files(fileTree("../flutter-lib/${info.os}") {
                exclude("**/ephemeral/**", "Pods/**", "**/*.bak")
            })
            outputs.dir("../flutter-lib/build/${info.os}")
            // Toggling -PdartDebug must invalidate this task even though its output dir is unchanged
            // (Flutter writes debug/release into sibling subdirs of the same build/<os> tree).
            inputs.property("dartDebug", dartDebug)
            val modeFlag = if (dartDebug) listOf("--debug") else emptyList()
            when (info.os) {
                "macos" -> {
                    val flutterArch = if (info.arch == "aarch64") "arm64" else "x86_64"
                    val extra = if (dartDebug) " --debug" else ""
                    commandLine = listOf("bash", "-c", "./set-arch.sh $flutterArch && ${flutterCmd().joinToString(" ")} build macos$extra")
                }
                else -> commandLine = flutterCmd() + listOf("build", info.os) + modeFlag
            }
            // macOS aarch64 + x86_64 both build into flutter's single build/macos dir (set-arch.sh
            // switches the arch in place), so a second build overwrites the first arch's app. Serialize
            // the two so each arch's app is copied out before the next arch's build clobbers build/macos:
            // aarch64FlutterLib -> aarch64CopyFlutterBinaries -> x86_64FlutterLib -> x86_64CopyFlutterBinaries.
            if (info.desktopPlatform == "macos-x86_64")
                mustRunAfter("macos-aarch64CopyFlutterBinaries")
        }

        tasks.register<Copy>("${info.desktopPlatform}CopyFlutterBinaries") {
            group = "build"
            description = "Copies Flutter binaries for ${info.desktopPlatform}"

            if ((currentPlatform == info.desktopPlatform || info.os == "macos") && System.getProperty("skipFlutterLib") == null)
                dependsOn("${info.desktopPlatform}FlutterLib")
            // NOTE: the copy runs right after ITS OWN FlutterLib (dependsOn above). It must NOT wait for
            // the other arch's FlutterLib — that build overwrites the shared build/macos dir and would
            // make this copy pick up the wrong arch. The x86_64 build is instead ordered to run after
            // this (aarch64) copy completes; see macos-x86_64FlutterLib's mustRunAfter above.

            val flutterArch = if (info.arch == "aarch64") "arm64" else "x64"
            copyFlutterNatives(info.os, flutterArch, dartDebug)

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
        // Must drop the classes as well as the export (see excludeFxCanvas): leaving them in a
        // bundle that no longer exports the package would let nothing load them anyway, and
        // keeping the export without classes resolves the host's import against an empty package.
        if (excludeFxCanvas) exclude("javafx/embed/swt/**")

        val bsn = if (info.isWeb) "dev.equo.swt_evolve.web"
                  else "org.eclipse.swt.${info.swtWs}.${info.swtOs}.${info.arch}"
        manifest {
            attributes(
                "Fragment-Host" to fragmentHostHeader(),
                "Bundle-Name" to (if (info.isWeb) "SWT Evolve for Web" else "SWT Evolve for ${info.swtOs} on ${info.arch}"),
                "Bundle-Vendor" to bundleVendor,
                "Bundle-SymbolicName" to "$bsn; singleton:=true",
                // Published (p2/mvn) version: distinct host version per variant (`-embed`/`-hyb`). The
                // eclipse_run in-place swap re-stamps this to the target app's SWT version at swap time
                // (reading the jar it replaces), so the build no longer needs the app's version here.
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
                attributes("Import-Package" to "$chromiumImportPackage,$fxImportPackges")
            else
                attributes("Import-Package" to fxImportPackges)
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
