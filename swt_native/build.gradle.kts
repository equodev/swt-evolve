import org.gradle.kotlin.dsl.*

plugins {
    java
    `java-library`
}

repositories {
    mavenCentral()
}

val arch = System.getProperty("os.arch")

val oss = listOf("windows", "linux", "macos")
val platforms = listOf(
    "windows-x86_64", "windows-aarch64",
    "linux-x86_64", "linux-aarch64",
    "macos-x86_64", "macos-aarch64"
)

val currentOs = when {
    org.gradle.internal.os.OperatingSystem.current().isWindows -> "windows"
    org.gradle.internal.os.OperatingSystem.current().isMacOsX -> "macos"
    else -> "linux"
}

val currentPlatform = "$currentOs-${if (arch.contains("aarch64") || arch.contains("arm")) "aarch64" else "x86_64"}"

val swtVersion = "3.128.0.v20241113-2009"

val swtVersionConfig by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

val swtVersionProvider = provider {
    val versionFiles = swtVersionConfig.files
    if (versionFiles.isNotEmpty() && versionFiles.first().exists()) versionFiles.first().readText().trim() else swtVersion
}

dependencies {
    if (gradle.parent != null)
        swtVersionConfig("dev.equo:eclipse_run")
    implementation(libs.equo.comm.ws) {
        exclude(group = "dev.equo", module = "com.equo.comm.common")
    }

    implementation(libs.auto.value.annotations)
    annotationProcessor(libs.auto.value.annotations)
    implementation(libs.dsl.json)
    annotationProcessor(libs.dsl.json)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testImplementation(libs.assertj)
    testImplementation(libs.json.unit.assertj)
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

    // Create source sets for all platform combinations
    oss.forEach { os ->
        create(os) {
            java {
                setSrcDirs(listOf(
                    "src/main/java",
                    "src/${os}/java"
                ))
            }
            annotationProcessorPath += sourceSets.main.get().annotationProcessorPath
            compileClasspath += sourceSets.main.get().compileClasspath
            runtimeClasspath += sourceSets.main.get().runtimeClasspath
        }
    }
}

// Configure Java compilation
tasks.compileJava {
    options.encoding = "UTF-8"
}

tasks.test {
    useJUnitPlatform()
    dependsOn("${currentPlatform}ExtractNatives")
//    if (org.gradle.internal.os.OperatingSystem.current().isMacOsX)
//        jvmArgs = listOf("-XstartOnFirstThread")
    systemProperty("swt.library.path", layout.buildDirectory.dir("natives/$currentPlatform").get().toString())
    if (System.getProperty("test.debug") != null)
        jvmArgs("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005")
}

tasks.jar {
    from(layout.buildDirectory.dir("natives/$currentPlatform"))

    // Add all dependencies to the JAR
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA", "OSGI-OPT/")

    dependsOn("${currentPlatform}ExtractNatives")
    dependsOn("${currentPlatform}CopyFlutterBinaries")
}

val dart = tasks.register<Exec>("dartRunner") {
    group = "build"
    description = "Generate dart files"
    workingDir = file("../flutter-lib")
    inputs.files(fileTree("../flutter-lib/lib/src") {
        include("gen/*.dart")
        include("swt/*.dart")
        exclude("**/*.g.dart")
    })
    outputs.files(fileTree("../flutter-lib/lib/src") { include("**/*.g.dart") })
    commandLine = listOf("dart", "run", "build_runner", "build", "--delete-conflicting-outputs")
}

// Create tasks for each platform JAR
platforms.forEach { platform ->
    val osArch = platform.split("-")
    val swtWs = when (osArch[0]) {
        "macos" -> "cocoa"
        "windows" -> "win32"
        else -> "gtk"
    }
    val swtOs = when (osArch[0]) {
        "macos" -> "macosx"
        "windows" -> "win32"
        else -> "linux"
    }

    configurations.create("${platform}SwtImpl") {
        exclude(group = "org.eclipse.platform", module = "org.eclipse.swt")
    }

    dependencies {
        configurations["${platform}SwtImpl"]("org.eclipse.platform:org.eclipse.swt.$swtWs.$swtOs.${osArch[1]}:${swtVersion.substringBefore(".v")}")
    }

    tasks.register<Exec>("${platform}FlutterLib") {
        group = "build"
        description = "Builds Flutter lib for $platform"
        dependsOn(dart)
        workingDir = file("../flutter-lib")
        inputs.dir("../flutter-lib/lib")
        inputs.dir("../flutter-lib/${osArch[0]}")
        outputs.dir("../flutter-lib/build/${osArch[0]}")
        when (osArch[0]) {
            "macos" -> {
                val arch = when (osArch[1]) {
                    "x86_64" -> "x86_64"
                    "aarch64" -> "arm64"
                    else -> throw GradleException("Unsupported macOS architecture: ${osArch[1]}")
                }
                commandLine = listOf("bash", "-c", "./set-arch.sh $arch && flutter build macos")
            }
            else -> {
                commandLine = listOf("flutter", "build", osArch[0])
            }
        }
    }

    tasks.register<Copy>("${platform}CopyFlutterBinaries") {
        group = "build"
        description = "Copies Flutter binaries for $platform"
        if (currentPlatform == platform && System.getProperty("skipFlutterLib") == null)
            dependsOn("${platform}FlutterLib")

        val flutterArch = if (osArch[1] == "aarch64") "arm64" else "x64"
        when (osArch[0]) {
            "macos" -> {
                from("../flutter-lib/build/macos/Build/Products/Release/swtflutter.app") {
                    into("swtflutter.app")
                }
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
            "windows" -> {
                from("../flutter-lib/build/windows/$flutterArch/runner/Release/") {
                    include("*.dll", "data/")
                    into("runner")
                }
            }
        }

        into(layout.buildDirectory.dir("natives/$platform"))
    }

    tasks.register<Copy>("${platform}ExtractNatives") {
        from(configurations["${platform}SwtImpl"].map { zipTree(it) })
        into(layout.buildDirectory.dir("natives/$platform"))
        include("*.so", "*.dll", "*.dylib", "*.jnilib", "**/*.css")
        includeEmptyDirs = false
    }

    tasks.register<Jar>("${platform}Jar") {
        group = "build"
        description = "Assembles a jar archive for $platform"
        archiveBaseName.set("swt_evolve-$platform")
        from(sourceSets[osArch[0]].output)
        from(layout.buildDirectory.dir("natives/$platform"))

        // Add all dependencies to the JAR
        from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
        exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA", "OSGI-OPT/")

        manifest {
            attributes(
                "Fragment-Host" to provider { "org.eclipse.swt;bundle-version=\"[${swtVersionProvider.get().substring(0..6)},4.0.0)\"" },
                "Bundle-Name" to "SWT Evolve for ${osArch[0]} on ${osArch[1]}",
                "Bundle-Vendor" to "Equo Tech, Inc.",
                "Bundle-SymbolicName" to "org.eclipse.swt.$swtWs.$swtOs.${osArch[1]}; singleton:=true",
                "Bundle-Version" to swtVersionProvider,
                "Bundle-ManifestVersion" to 2,
                "Export-Package" to "org.eclipse.swt,org.eclipse.swt.accessibility,"+
                        "org.eclipse.swt.awt,org.eclipse.swt.browser,org.eclipse.swt.custom,"+
                        "org.eclipse.swt.dnd,org.eclipse.swt.events,org.eclipse.swt.graphics,"+
                        "org.eclipse.swt.layout,org.eclipse.swt.opengl,org.eclipse.swt.printing,"+
                        "org.eclipse.swt.program,org.eclipse.swt.widgets,org.eclipse.swt.internal; x-friends:=\"org.eclipse.ui\","+
                        "org.eclipse.swt.internal.image; x-internal:=true,org.eclipse.swt.internal.$swtWs; x-friends:=\"org.eclipse.ui\"",
                "Eclipse-PlatformFilter" to "(& (osgi.ws=$swtWs) (osgi.os=$swtOs) (osgi.arch=${osArch[1]}) )",
                "SWT-WS" to swtWs,
                "SWT-OS" to swtOs,
                "SWT-Arch" to osArch[1],
                "Automatic-Module-Name" to "org.eclipse.swt.$swtWs.$swtOs.${osArch[1]}",
            )
        }
        dependsOn("${platform}ExtractNatives")
        dependsOn("${platform}CopyFlutterBinaries")
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
