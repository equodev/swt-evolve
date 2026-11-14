import java.nio.file.Files
import java.nio.file.Path
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.kotlin.dsl.*
import java.io.File
import net.ltgt.gradle.errorprone.errorprone
import javax.inject.Inject
import org.gradle.process.ExecOperations
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.workers.WorkerExecutor

plugins {
    java
    `java-library`
    `java-test-fixtures`
    jacoco
    alias(libs.plugins.errorprone)
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

val oss = listOf("nativeWindows", "nativeLinux", "nativeMacos")
val platforms = listOf(
    "windows-x86_64", "windows-aarch64",
    "linux-x86_64", "linux-aarch64",
    "macos-x86_64", "macos-aarch64",
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
    if (!file("../flutter-lib/.fvmrc").exists()) null
    // Both spellings on Windows: chocolatey's package installs fvm.exe, and looking only for
    // fvm.bat silently falls through to whatever global Flutter is on PATH — which is how the
    // Windows VM kept building against the version it was provisioned with instead of .fvmrc's.
    else if (isWindowsOs) findOnPath("fvm.bat") ?: findOnPath("fvm.exe")
    else findOnPath("fvm")
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

dependencies {
    implementation(libs.java.websocket)
    compileOnly(libs.equo.chromium)

    // The harness and mocks the tests share, published so a build outside this one can run tests
    // against the same Display.
    testFixturesImplementation(platform(libs.junit.bom))
    testFixturesImplementation(libs.junit.jupiter)
    testFixturesImplementation(libs.assertj)
    testFixturesImplementation(libs.mockito.core)
    testFixturesImplementation(libs.instancio.junit)
    testFixturesImplementation(libs.lwjgl)
    testFixturesImplementation(libs.lwjgl.opengl)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.assertj)
    // Gson, for the JSON tests read from outside: Chrome DevTools Protocol messages, the case file the
    // canonical form shares with Dart, and the result files measurement tests write for tools.
    testImplementation(libs.gson)
    testImplementation(libs.mockito.core)
    testImplementation(libs.instancio.junit)
    // Test-only: the paint path used to exclude a FigureCanvas via an isInstance() check, so the
    // real type has to be resolvable for a test to tell that exclusion from its absence.
    testImplementation("org.eclipse:draw2d:$draw2dVersion")
    // GL calls for the GLCanvas tests, which draw through the context the canvas makes current.
    testImplementation(libs.lwjgl)
    testImplementation(libs.lwjgl.opengl)
    val lwjglNatives = "natives-" + when (currentOs) {
        "macos" -> "macos"
        "windows" -> "windows"
        else -> "linux"
    } + if (getSwtArch(arch) == "aarch64") "-arm64" else ""
    testRuntimeOnly(variantOf(libs.lwjgl) { classifier(lwjglNatives) })
    testRuntimeOnly(variantOf(libs.lwjgl.opengl) { classifier(lwjglNatives) })

    // JavaFX — required to COMPILE the FXCanvas embedded-scene bridge
    // (javafx.embed.swt + com.sun.javafx.embed internals). compileOnly so the FX
    // jars + native libs are NOT flattened into the Evolve platform jars (that
    // breaks JavaFX's NativeLibLoader); the FX runtime is provided as separate
    // jars on the application classpath instead. JavaFX is on the classpath
    // (unnamed module) so com.sun.* embed packages need no --add-exports.
    run {
        val javafxVersion = libs.versions.javafx.get()
        val fxArch = getSwtArch(arch)
        // OpenJFX 21 publishes no linux-aarch64 (nor any win-aarch64) jars; the classifier only changes
        // the bundled natives, so the x64 jar compiles the same API on those hosts.
        val javafxClassifier = when (currentOs) {
            "macos" -> if (fxArch == "aarch64") "mac-aarch64" else "mac"
            "windows" -> "win"
            else -> "linux"
        }
        compileOnly("org.openjfx:javafx-base:$javafxVersion:$javafxClassifier")
        compileOnly("org.openjfx:javafx-graphics:$javafxVersion:$javafxClassifier")
        compileOnly("org.openjfx:javafx-controls:$javafxVersion:$javafxClassifier")
    }
}

// The desktop surface paints text with the host's own font stack, which each OS's table in
// src/desktopFontMetrics was measured on. Each native<OS> tree gets that table as
// GenDesktopFontMetrics, beside the shared GenFontMetrics the web surface measures with (see
// FontMetricsUtil.metrics).
val desktopFontMetrics = oss.filter { it.startsWith("native") }.associateWith { os ->
    tasks.register<Copy>("${os}DesktopFontMetrics") {
        from("src/desktopFontMetrics/${os.removePrefix("native").lowercase()}/GenFontMetrics.java") {
            into("dev/equo/swt")
            rename { "GenDesktopFontMetrics.java" }
            filter { it.replace("GenFontMetrics", "GenDesktopFontMetrics") }
        }
        into(layout.buildDirectory.dir("generated/desktopFontMetrics/$os"))
    }
}

// What every backend source set shares besides its sources.
fun backendSetup(set: SourceSet) = set.apply {
    // The wire's channel, type and property tables: without them in the jar Java cannot read
    // a numbered channel header, so nothing the client sends reaches it.
    resources { srcDir("src/main/resources") }
    annotationProcessorPath += sourceSets.main.get().annotationProcessorPath
    compileClasspath += sourceSets.main.get().compileClasspath
    runtimeClasspath += sourceSets.main.get().runtimeClasspath
}

// A build that includes this one adds a whole-tree-Flutter backend of its own: src/main and
// src/native, plus [dirs].
extra["registerFlutterBackend"] = { name: String, dirs: List<Any> ->
    sourceSets.create(name) {
        java {
            setSrcDirs(listOf("src/main/java", "src/native/java") + dirs)
        }
        backendSetup(this)
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
        }
    }

    // Create source sets for all platform combinations
    oss.forEach { os ->
        create(os) {
            java {
                setSrcDirs(listOf("src/main/java", "src/native/java", "src/${os}/java"))
                desktopFontMetrics[os]?.let { srcDir(it) }
            }
            backendSetup(this)

            test {
                resources {
                    srcDirs("src/${os}/java")
                    include("**/*.css", "**/*.png", "**/*.bmp", "**/*.gif", "**/*.svg", "**/*.jpg", "**/SWTMessages*.properties", "**/SWTMessages.properties")
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
            // Mac*NativeTest and their MacChildJvm helper: assert against Cocoa itself, so they only
            // compile where the per-OS backend carries org.eclipse.swt.internal.cocoa. An
            // @EnabledOnOs would skip the run but the source still has to compile on every runner.
            if (currentOs != "macos") exclude("**/Mac*NativeTest.java", "**/MacChildJvm.java")
            // The GLCanvas tests need a working OpenGL driver, not yet provisioned on the Linux and
            // Windows CI runners; locally (and through :cross) they run on every OS.
            if (currentOs != "macos" && System.getenv("CI") != null) exclude("org/eclipse/swt/opengl/GLCanvas*Test.java")
            // Win32*NativeTest: the classes they cover only exist in the win32 backend.
            if (currentOs != "windows") exclude("**/Win32*NativeTest.java")
        }
        // src/test/resources is this source set's own by convention; the per-OS blocks above add the
        // .css/.png/SWTMessages that sit beside the generated Java.
        compileClasspath += nativeBackend.output + nativeBackend.compileClasspath
        runtimeClasspath += output + nativeBackend.output + nativeBackend.runtimeClasspath
    }
}

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
            else -> "linux"
        }
        "testImplementation"("org.openjfx:javafx-base:$javafxVersion:$javafxClassifier")
        "testImplementation"("org.openjfx:javafx-graphics:$javafxVersion:$javafxClassifier")
        "testImplementation"("org.openjfx:javafx-controls:$javafxVersion:$javafxClassifier")
        "testRuntimeOnly"("org.testfx:openjfx-monocle:${libs.versions.openjfx.monocle.get()}") {
            isTransitive = false
        }
    }
}

dependencies { errorprone(libs.errorprone.core) }

// Error Prone on the one compile holding all our code once: every native backend shares main/ and
// native/, Windows adds our only per-OS file. Upstream copies are skipped; a Dart* finding is fixed
// in the generator. -PerrorProne=false turns it off where CI runs it as a job of its own.
val errorProne = findProperty("errorProne")?.toString() != "false"
tasks.withType<JavaCompile>().configureEach {
    options.errorprone {
        enabled.set(errorProne && name == "compileNativeWindowsJava")
        disableAllWarnings.set(true)
        // Checks off or only warnings by default that found real defects here, or guard against
        // common ones, as errors.
        error(
            "StringCaseLocaleUsage", "DefaultLocale", "ObjectEqualsForPrimitives",
            "JavaTimeDefaultTimeZone", "UndefinedEquals", "CollectionUndefinedEquality",
            "ToStringReturnsNull", "InterruptedExceptionSwallowed",
            "FallThrough", "MissingCasesInEnumSwitch", "ModifiedButNotUsed",
        )
        excludedPaths.set("(.*/src/native[A-Za-z]*/java/(?!dev/equo/)(?!.*/Dart[^/]*\\.java$)|.*/build/generated/).*")
    }
    // That compile is warning-free; keep it so.
    if (errorProne && name == "compileNativeWindowsJava") options.compilerArgs.add("-Werror")
}

// Configure Java compilation
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
    // The Evolve SWT_AWT bridge (dev.equo.swt.awt.*) hosts Swing off-screen through
    // sun.swing.JLightweightFrame / LightweightContent — the same internal contract
    // JavaFX's SwingNode uses. These packages are not exported by default.
    // sun.awt.FwDispatcher: lets EvolveSwingHost install its own AWT/Swing dispatcher on
    // java.awt.EventQueue, routed through the SWT Display's own pump, instead of leaving whatever
    // JavaFX/host bridge code installs last (which routes through a JavaFX event loop DartDisplay
    // never pumps, so posted tasks never run). At runtime the bridge exports both packages to
    // itself (EvolveSwingHost.openSwingInternals), or takes the matching --add-exports from the
    // deployment's own -vmargs.
    options.compilerArgs.addAll(listOf(
        "--add-exports", "java.desktop/sun.swing=ALL-UNNAMED",
        "--add-exports", "java.desktop/sun.awt=ALL-UNNAMED"
    ))
}

// Coverage is only consumed from the native suite (merged into swt_eclipse_tests' combined report),
// so JaCoCo stays off everywhere except `test`.
tasks.withType<Test>().configureEach {
    extensions.configure(org.gradle.testing.jacoco.plugins.JacocoTaskExtension::class) {
        isEnabled = name == "test"
    }
    // Tests check what goes out on the wire through FlutterBridge.setSendObserver and WireStats,
    // which only a JVM started with these watches.
    systemProperty("dev.equo.swt.observeSends", "true")
    systemProperty("dev.equo.swt.wireStats", "true")
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

tasks.test {
    group = "verification"
    description = "Runs the tests that need no renderer, against the whole-tree-Flutter Java backend " +
            "(src/main + src/native + src/native<currentOs>)."
    maxHeapSize = System.getProperty("testHeap") ?: "2g"
    // Capped by the cores actually visible to this JVM: asking for more only adds JVMs that wait.
    maxParallelForks = (System.getProperty("testForks")?.toIntOrNull() ?: 1)
        .coerceIn(1, Runtime.getRuntime().availableProcessors())
    // @Nested classes run through their outer class; listing them too deals them to forks twice.
    include("**/*Test.class", "**/*Tests.class")
    exclude("**/*$*.class")
    useJUnitPlatform {
        // 'transport-copies': the WebSocket library copies every frame it sends and queues the
        // copies without bound, so senders that outrun the client grow the heap; red until the
        // transport writes from buffers it does not have to copy.
        excludeTags(*excludedTags("transport-copies"))
    }
    configureTestLogging()
    systemProperty("dev.equo.swt.loadLibrary", "false")
    // An exception in a Display runnable must fail the test that caused it; the crash dialog's
    // modal loop would instead block the run until the task times out.
    systemProperty("dev.equo.swt.crashReport.disabled", "true")
    systemProperty("dev.equo.swt.mode", "web")
    // A test that builds a real web Display by mistake would open the developer's own browser on a
    // localhost page that dies with the run; any such Display gets headless Chrome instead.
    systemProperty("dev.equo.swt.web.headless", "true")
    if (org.gradle.internal.os.OperatingSystem.current().isMacOsX)
        jvmArgs = listOf("-XstartOnFirstThread")
    // Runtime counterpart of the compile-time exports above -- EvolveDispatcherWrapperTest loads
    // EvolveSwingHost's dispatcher classes, which extend/reference these restricted packages.
    jvmArgs("--add-exports", "java.desktop/sun.swing=ALL-UNNAMED",
            "--add-exports", "java.desktop/sun.awt=ALL-UNNAMED")
    forwardSystemProperties("equo.swt.diff", "equo.swt.binaryWire", "dev.equo.swt.debug")
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

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(layout.buildDirectory.dir("natives/$currentPlatform"))

    // Add all dependencies to the JAR
    from(configurations.runtimeClasspath.map { cp -> cp.map { if (it.isDirectory) it else zipTree(it) } })
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

/** Runs one command and, once it succeeds, writes the stamp its task declares as output. */
abstract class StampedCommand : WorkAction<StampedCommand.Params> {
    interface Params : WorkParameters {
        val command: ListProperty<String>
        val workingDir: DirectoryProperty
        val stamp: RegularFileProperty
    }

    @get:Inject abstract val execOperations: ExecOperations

    override fun execute() {
        execOperations.exec {
            commandLine(parameters.command.get())
            workingDir(parameters.workingDir.get().asFile)
        }
        val stamp = parameters.stamp.get().asFile
        stamp.parentFile.mkdirs()
        stamp.writeText(System.currentTimeMillis().toString())
    }
}

/**
 * A command run as Worker API work rather than as an Exec action. Gradle runs one task of a project
 * at a time, but while a task's submitted work is in flight it may start the project's other tasks:
 * `flutter test` then runs beside the Java suite instead of ahead of it.
 */
abstract class BackgroundCommand @Inject constructor(private val workers: WorkerExecutor) : DefaultTask() {
    @get:Input abstract val command: ListProperty<String>
    @get:Internal abstract val workingDir: DirectoryProperty

    @TaskAction
    fun run() {
        val stamp = outputs.files.singleFile
        workers.noIsolation().submit(StampedCommand::class.java) {
            command.set(this@BackgroundCommand.command)
            workingDir.set(this@BackgroundCommand.workingDir)
            this.stamp.set(stamp)
        }
    }
}

tasks.register<BackgroundCommand>("analyze") {
    group = "build"
    description = "Flutter analyze"
    workingDir.set(file("../flutter-lib"))
    command.set(flutterCmd() + listOf("analyze"))
    dartSourcesAsInputs()
}

// `flutter test` sizes its pool at cores/2 and, on Linux, reads the node's cores rather than the
// container's quota, which oversubscribes a shared runner badly. availableProcessors() is the number
// that respects a cgroup quota, so it is both the safe one in a container and the whole machine on a
// workstation.
val flutterTestJobs = (System.getProperty("flutterTestJobs") ?: System.getenv("FLUTTER_TEST_JOBS"))?.toIntOrNull()
    ?: Runtime.getRuntime().availableProcessors()
// Twice the pool, so a bundle that runs long does not leave the rest of the pool idle at the end.
val flutterTestBundleCount = flutterTestJobs * 2

// `flutter test` compiles its files one at a time, so a run of one file per test isolate is as long
// as that queue. The bundles (tool/test_bundles.dart, as CI runs them) put the files into a few
// isolates instead; files with @Tags or @TestOn run on their own.
val flutterTestBundles = tasks.register<Exec>("flutterTestBundles") {
    group = "verification"
    description = "Writes flutter-lib/test/bundle_*_test.dart, the files flutterTest runs"
    workingDir = file("../flutter-lib")
    dependsOn(pub)
    commandLine = dartCmd() + listOf("tool/test_bundles.dart", flutterTestBundleCount.toString())
}

tasks.register<BackgroundCommand>("flutterTest") {
    group = "verification"
    description = "Runs the Dart unit tests in flutter-lib (analyze answers whether the Dart side " +
            "is sound; this answers whether it behaves)."
    workingDir.set(file("../flutter-lib"))
    dependsOn(pub, flutterTestBundles)
    // -DredrawRecord=true prints each redraw test's measured costs as a budget table instead of
    // checking them (test/redraw/support/budget.dart).
    val redrawRecord = if (System.getProperty("redrawRecord") == "true") listOf("--dart-define=REDRAW_RECORD=true")
                       else emptyList()
    // -DflutterTests=<path>[,<path>] runs only those test files or directories, outside the bundles.
    val only = System.getProperty("flutterTests")?.split(',')?.filter { it.isNotBlank() } ?: emptyList()
    val standalone = file("../flutter-lib/build/standalone_tests.txt")
    val bundles = (0 until flutterTestBundleCount).map { "test/bundle_${it}_of_${flutterTestBundleCount}_test.dart" }
    command.set(provider {
        flutterCmd() + listOf("test", "--concurrency=$flutterTestJobs") + redrawRecord +
            (only.ifEmpty { bundles + standalone.readLines().filter { it.isNotBlank() } })
    })
    dartSourcesAsInputs()
    // Re-recordings print what they find, so they run every time.
    if (redrawRecord.isNotEmpty()) outputs.upToDateWhen { false }
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

data class PlatformMeta(
    val os: String,
    val arch: String,
) {
    val desktopPlatform: String get() = "$os-$arch"
    val sourceSet: String get() = "native" + os.replaceFirstChar { it.titlecase() }
    val swtWs: String get() = getSwtWs(os)
    val swtOs: String get() = getSwtOs(os)
}

fun parsePlatform(platform: String): PlatformMeta {
    val parts = platform.split("-")
    return PlatformMeta(parts[0], parts[1])
}

// Liberation fonts substitute system fonts for CanvasKit's web renderer, which can't reach
// OS-installed fonts. Desktop builds never trigger the substitution (WebFontSubstitutions is
// only loaded by WebFlutterServer), so they'd carry the ~9MB of TTFs for nothing.
val libertyFontExclude = "**/flutter_assets/assets/fonts/Liberation*.ttf"

// Dart debug/JIT build: `-PdartDebug` builds the desktop Flutter frameworks in --debug (JIT) mode
// instead of the default --release (AOT), so the embedded engine exposes a Dart VM Service that
// DTD/MCP tooling (and flutter_driver) can attach to. Release is unchanged and remains the default.
// See docs/design/flutter-dtd-introspection.md (Phases 1 & 2).
val dartDebug = project.hasProperty("dartDebug")

// `-PdartProfile` builds them in --profile: AOT like release, with the timeline kept, so a measurement
// can tell build, layout and paint apart at release speed.
val dartProfile = !dartDebug && project.hasProperty("dartProfile")
val dartMode = if (dartDebug) "debug" else if (dartProfile) "profile" else "release"

// The Flutter app the desktop jars carry: flutter-lib, unless an including build names another
// (`evolveFlutterApp`) built on it as a package.
val flutterApp: File = ((gradle.parent as ExtensionAware?)?.extra?.properties?.get("evolveFlutterApp") as File?)
    ?: file("../flutter-lib")

fun CopySpec.copyFlutterNatives(os: String, flutterArch: String, mode: String) {
    // Flutter emits build outputs under a mode-named directory: macOS/Windows capitalize
    // (Products/Debug, runner/Debug), Linux lower-cases (build/linux/<arch>/debug).
    val modeCap = mode.replaceFirstChar { it.uppercase() }
    val modeLower = mode
    when (os) {
        "macos" -> {
            val app = flutterApp.resolve("build/macos/Build/Products/$modeCap/swtflutter.app")
            from(app) {
                into("swtflutter.app")
                exclude(libertyFontExclude)
                // Each file once, under its real path; writeMacAppLinks lists the links to recreate.
                exclude { underSymlink(app, it.file) }
            }
        }
        "linux" -> {
            from(flutterApp.resolve("build/linux/$flutterArch/$modeLower/runner")) {
                include("libflutter_bridge.so")
                into("runner")
            }
            from(flutterApp.resolve("build/linux/$flutterArch/$modeLower/bundle/lib")) {
                include("*.so")
                into("bundle/lib")
            }
            from(flutterApp.resolve("build/linux/$flutterArch/$modeLower/bundle/data")) {
                include("icudtl.dat", "flutter_assets/**")
                exclude(libertyFontExclude)
                into("bundle/data")
            }
        }
        "windows" -> from(flutterApp.resolve("build/windows/$flutterArch/runner/$modeCap/")) {
            include("*.dll", "data/")
            exclude(libertyFontExclude)
            into("runner")
        }
    }
}

fun underSymlink(root: File, file: File): Boolean {
    val rootPath = root.toPath()
    var path: Path? = file.toPath()
    while (path != null && path != rootPath && path.startsWith(rootPath)) {
        if (Files.isSymbolicLink(path)) return true
        path = path.parent
    }
    return false
}

// Flutter's macOS frameworks link X.framework/X, X.framework/Resources and Versions/Current into
// Versions/A. A jar cannot hold a symlink (a JarFile reads one as a file holding its target), so the
// fragment ships each file once and lists the links: swtflutter.app.links for FlutterLibraryLoader,
// which creates them after extracting, and META-INF/p2.inf for p2, which creates them when it
// installs the fragment as a directory.
fun writeMacAppLinks(app: File, nativesDir: File) {
    val root = app.toPath()
    val linkPaths: List<Path> = Files.walk(root).use { paths ->
        paths.filter { Files.isSymbolicLink(it) }.sorted().iterator().asSequence().toList()
    }
    val links: List<Pair<String, String>> = linkPaths.map { link ->
        "swtflutter.app/" + root.relativize(link).joinToString("/") to
            Files.readSymbolicLink(link).joinToString("/")
    }
    if (links.isEmpty()) throw GradleException("No framework symlinks found in $app")
    File(nativesDir, "swtflutter.app.links").writeText(links.joinToString("") { (link, target) -> "$link\t$target\n" })
    // Not force: `ln -sf` onto an existing link to a directory would create the link inside it.
    File(nativesDir, "META-INF/p2.inf").apply { parentFile.mkdirs() }.writeText(
        "instructions.install = " + links.joinToString("") { (link, target) ->
            "ln(targetDir:@artifact,linkTarget:$target,linkName:$link);"
        } + "\n")
}

val bundleVendor = "Equo Tech, Inc."
val chromiumImportPackage = "com.equo.chromium;resolution:=optional," +
        "com.equo.chromium.utils;resolution:=optional," +
        "com.equo.chromium.events;resolution:=optional"
val fxImportPackges = "javafx.application;resolution:=optional," +
        "javafx.geometry;resolution:=optional," +
        "javafx.scene;resolution:=optional," +
        "javafx.scene.image;resolution:=optional," +
        "javafx.scene.transform;resolution:=optional," +
        "com.sun.javafx.cursor;resolution:=optional," +
        "com.sun.javafx.embed;resolution:=optional," +
        "com.sun.javafx.stage;resolution:=optional"
// The fragments attach to the one SWT minor release their generated sources were written from.
fun fragmentHostHeader(): String {
    val (major, minor) = swtVersion.split(".").map { it.toInt() }
    return "org.eclipse.swt;bundle-version=\"[$major.$minor,$major.${minor + 1})\""
}
fun evolveVersion(): Any = gradle.parent?.rootProject?.version ?: project.version
fun hostVersion(suffix: String) = provider {
    "$swtVersion.v${evolveVersion().toString().replace('.', '_')}-$suffix"
}

// AppCDS for an installed macOS product: p2.inf adds these lines to its .ini. Relative because the
// launcher starts the JVM in Contents/MacOS; the directory must be writable, or the JVM exits with 1.
// AutoCreateSharedArchive needs Java 19; the file is per Evolve version (the launcher jar it checks stays).
val macosCdsJvmArgs = listOf(
    "-XX:+AutoCreateSharedArchive",
    "-XX:SharedArchiveFile=../Eclipse/configuration/org.eclipse.swt.evolve-${evolveVersion()}.jsa",
    // The dump's per-class "Skipping" warnings (signed jars, hidden classes) go to stdout otherwise.
    "-Xlog:cds*=error",
)
// The oldest JDK the SWT release being built for supports, which a build for another release passes.
val minJavaRelease = (project.findProperty("minJavaVersion") as String?)?.toIntOrNull()
val shipsMacosCdsArchive = (minJavaRelease ?: 21) >= 19
fun isMacosHybrid(info: PlatformMeta) = info.os == "macos"
// One p2.inf per macOS hybrid fragment: the framework links writeMacAppLinks lists, then these lines.
fun registerMacosFragmentP2Inf(platform: String) = tasks.register("${platform}FragmentP2Inf") {
    val linksP2Inf = layout.buildDirectory.file("natives/$platform/META-INF/p2.inf")
    val out = layout.buildDirectory.dir("fragment-p2inf/$platform")
    dependsOn("${platform}ExtractNatives", "${platform}CopyFlutterBinaries")
    inputs.files(linksP2Inf).optional()
    inputs.property("jvmArgs", if (shipsMacosCdsArchive) macosCdsJvmArgs else emptyList())
    outputs.dir(out)
    doLast {
        val links = linksP2Inf.get().asFile
        if (!links.isFile && links.resolveSibling("../swtflutter.app").normalize().exists())
            throw GradleException("$links missing: the fragment would install without its framework links")
        val lines = links.takeIf { it.isFile }?.readLines()?.filter { it.isNotBlank() }.orEmpty() +
            if (!shipsMacosCdsArchive) emptyList() else listOf(
                "instructions.configure=" + macosCdsJvmArgs.joinToString(";") { "addJvmArg(jvmArg:$it)" },
                "instructions.unconfigure=" + macosCdsJvmArgs.joinToString(";") { "removeJvmArg(jvmArg:$it)" })
        val file = out.get().file("META-INF/p2.inf").asFile
        delete(file)
        if (lines.isNotEmpty()) file.apply { parentFile.mkdirs() }.writeText(lines.joinToString("\n", postfix = "\n"))
    }
}
platforms.map { parsePlatform(it) }.filter { isMacosHybrid(it) }
    .forEach { registerMacosFragmentP2Inf(it.desktopPlatform) }
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
val hostExportPackage = swtExportPackage(null) +
    ",org.eclipse.swt.internal.cloudready,dev.equo.swt.spi"

platforms.forEach { platform ->
    val info = parsePlatform(platform)

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
        workingDir = flutterApp
        inputs.dir("../flutter-lib/lib")
        inputs.dir("../flutter-lib/assets")
        if (flutterApp != file("../flutter-lib")) inputs.dir(flutterApp.resolve("lib"))
        inputs.files(fileTree(flutterApp.resolve(info.os)) {
            exclude("**/ephemeral/**", "Pods/**", "**/*.bak")
        })
        // A plugin bump changes only the lock, and the plugin's native code is compiled into this build.
        inputs.file(flutterApp.resolve("pubspec.lock"))
        outputs.dir(flutterApp.resolve("build/${info.os}"))
        // Toggling -PdartDebug/-PdartProfile must invalidate this task even though its output dir is unchanged
        // (Flutter writes debug/release into sibling subdirs of the same build/<os> tree).
        inputs.property("dartMode", dartMode)
        val modeFlag = if (dartMode == "release") emptyList() else listOf("--$dartMode")
        when (info.os) {
            "macos" -> {
                val flutterArch = if (info.arch == "aarch64") "arm64" else "x86_64"
                val extra = if (dartMode == "release") "" else " --$dartMode"
                commandLine = listOf("bash", "-c", "${file("../flutter-lib/set-arch.sh")} $flutterArch && ${flutterCmd().joinToString(" ")} build macos$extra")
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
        copyFlutterNatives(info.os, flutterArch, dartMode)

        val nativesDir = layout.buildDirectory.dir("natives/${info.desktopPlatform}")
        into(nativesDir)
        if (info.os == "macos") {
            val app = flutterApp.resolve("build/macos/Build/Products/${dartMode.replaceFirstChar { it.uppercase() }}/swtflutter.app")
            // A copy never deletes: an earlier build's app would keep the files this one leaves out.
            doFirst { delete(nativesDir.get().dir("swtflutter.app")) }
            doLast { writeMacAppLinks(app, nativesDir.get().asFile) }
        }
    }

    tasks.register<Copy>("${info.desktopPlatform}ExtractNatives") {
        // Resolved when the task runs: a task another build depends on is created without this
        // build's lock, and resolving there is refused.
        from(provider { configurations["${info.desktopPlatform}SwtImpl"].map { zipTree(it) } })
        into(layout.buildDirectory.dir("natives/${info.desktopPlatform}"))
        include("*.so", "*.dll", "*.dylib", "*.jnilib", "**/*.css", "**/SWTMessages*.properties", "**/SWTMessages.properties")
        includeEmptyDirs = false
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
        if (isMacosHybrid(info)) {
            from(layout.buildDirectory.dir("natives/${info.desktopPlatform}")) { exclude("META-INF/p2.inf") }
            from(tasks.named("${info.desktopPlatform}FragmentP2Inf"))
        } else
            from(layout.buildDirectory.dir("natives/${info.desktopPlatform}"))

        // Add all dependencies to the JAR
        from(configurations.runtimeClasspath.map { cp -> cp.map { if (it.isDirectory) it else zipTree(it) } })
        exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA", "META-INF/LICENSE*", "META-INF/NOTICE*", "OSGI-OPT/")
        // Must drop the classes as well as the export (see excludeFxCanvas): leaving them in a
        // bundle that no longer exports the package would let nothing load them anyway, and
        // keeping the export without classes resolves the host's import against an empty package.
        if (excludeFxCanvas) exclude("javafx/embed/swt/**")

        val bsn = "org.eclipse.swt.${info.swtWs}.${info.swtOs}.${info.arch}"
        manifest {
            attributes(
                "Fragment-Host" to fragmentHostHeader(),
                "Bundle-Name" to "SWT Evolve for ${info.swtOs} on ${info.arch}",
                "Bundle-Vendor" to bundleVendor,
                "Bundle-SymbolicName" to "$bsn; singleton:=true",
                // Published (p2/mvn) version: the host version's `-hyb` suffix.
                "Bundle-Version" to hostVersion("hyb"),
                "Bundle-ManifestVersion" to 2,
                "Export-Package" to swtExportPackage(info.swtWs),
                "SWT-WS" to info.swtWs,
                "Automatic-Module-Name" to bsn,
                "Evolve-Version" to evolveVersion(),
                "Eclipse-PlatformFilter" to "(& (osgi.ws=${info.swtWs}) (osgi.os=${info.swtOs}) (osgi.arch=${info.arch}) )",
                "SWT-OS" to info.swtOs,
                "SWT-Arch" to info.arch,
                // Installed as a directory, so FlutterLibraryLoader loads the Flutter natives in place.
                "Eclipse-BundleShape" to "dir",
            )
            attributes("Import-Package" to "$chromiumImportPackage,$fxImportPackges")
        }
        dependsOn("${info.desktopPlatform}ExtractNatives")
        dependsOn("${info.desktopPlatform}CopyFlutterBinaries")
    }
}

fun genHostP2Inf(taskName: String, outFile: Provider<RegularFile>, suffix: String, frags: () -> List<PlatformMeta>) =
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
    platforms.map { parsePlatform(it) }
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
            "Export-Package" to hostExportPackage,
            "Eclipse-ExtensibleAPI" to "true",
            "Automatic-Module-Name" to "org.eclipse.swt",
            "Evolve-Version" to evolveVersion(),
        )
    }
}
registerHostJar("swtHostHybridJar", "org.eclipse.swt-hybrid", files(hybridHostP2Inf).builtBy(genHybridHostP2Inf), hostVersion("hyb"))

tasks.register("buildAllPlatforms") {
    group = "build"
    description = "Builds JARs for all platforms"
    dependsOn(platforms.map { "${it}Jar" })
}

// Configure the default build task to include all platform JARs
tasks.build {
    dependsOn("buildAllPlatforms", "swtHostHybridJar")
}
