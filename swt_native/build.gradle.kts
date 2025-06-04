import org.gradle.kotlin.dsl.*

import org.gradle.kotlin.dsl.support.unzipTo

plugins {
    java
    `java-library`
}

repositories {
    mavenCentral()
}

val isWindows = org.gradle.internal.os.OperatingSystem.current().isWindows
val isLinux = org.gradle.internal.os.OperatingSystem.current().isLinux
val isMac = org.gradle.internal.os.OperatingSystem.current().isMacOsX
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

val swtVersion = "3.128.0"

val swtBundle = when {
    isWindows -> {
        when (arch) {
            "amd64", "x86_64" -> "org.eclipse.platform:org.eclipse.swt.win32.win32.x86_64"
            "aarch64" -> "org.eclipse.platform:org.eclipse.swt.win32.win32.aarch64"
            else -> throw GradleException("Unsupported Windows architecture: $arch")
        }
    }
    isMac -> {
        when (arch) {
            "amd64", "x86_64" -> "org.eclipse.platform:org.eclipse.swt.cocoa.macosx.x86_64"
            "aarch64" -> "org.eclipse.platform:org.eclipse.swt.cocoa.macosx.aarch64"
            else -> throw GradleException("Unsupported macOS architecture: $arch")
        }
    }
    isLinux -> {
        when (arch) {
            "amd64", "x86_64" -> "org.eclipse.platform:org.eclipse.swt.gtk.linux.x86_64"
            "aarch64" -> "org.eclipse.platform:org.eclipse.swt.gtk.linux.aarch64"
            else -> throw GradleException("Unsupported Linux architecture: $arch")
        }
    }
    else -> throw GradleException("Unsupported operating system")
}

//val swtImplementation by configurations.creating {
//    exclude(group = "org.eclipse.platform", module = "org.eclipse.swt")
//}
//
val swtSources by configurations.creating {
    exclude(group = "org.eclipse.platform", module = "org.eclipse.swt")
}

dependencies {
//    platforms.forEach { platform ->
//
//
//        swtImplementation
//    }
//    swtImplementation("$swtBundle:$swtVersion")
    swtSources("$swtBundle:$swtVersion:sources")
    implementation("dev.equo:com.equo.comm.ws.provider:3.1.0.202405302201") {
        exclude(group = "dev.equo", module = "com.equo.comm.common")
    }

    implementation("com.google.auto.value:auto-value-annotations:1.10.4")
    annotationProcessor("com.google.auto.value:auto-value:1.10.4")
    implementation("com.dslplatform:dsl-json:2.0.2")
    annotationProcessor("com.dslplatform:dsl-json:2.0.2")

    testImplementation("org.junit.jupiter:junit-jupiter:5.12.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.assertj:assertj-core:3.25.3")
    testImplementation("net.javacrumbs.json-unit:json-unit-assertj:4.1.1")
    testImplementation("org.mockito:mockito-core:5.18.0")
    testImplementation("org.instancio:instancio-junit:5.4.0")
}

sourceSets {
    main {
        java {
            // Include the shared sources and current OS-specific sources for IDE
            if (currentOs != "macos") // temp exclude src/main from linux
                setSrcDirs(listOf(
                    "src/${currentOs}/java"
                ))
            else
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
                if (os != "macos") // temp exclude src/main from linux
                    setSrcDirs(listOf(
                        "src/${os}/java"
                    ))
                else
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
//// Configure the main source set
//java {
//    sourceSets {
//        create("macos") {
//            java {
//                srcDir("src/macos/java")
//            }
//        }
////        main {
////            java {
////                srcDir("src/main/java")
////            }
////        }
//    }
//}


tasks.test {
    useJUnitPlatform()
//    if (org.gradle.internal.os.OperatingSystem.current().isMacOsX)
//        jvmArgs = listOf("-XstartOnFirstThread")
}

//val extractNatives by tasks.registering {
//    dependsOn(configurations["swtImplementation"])
//
//    doLast {
//        val nativesDir = layout.buildDirectory.file("natives").get().getAsFile()
//        nativesDir.mkdirs()
//
//        configurations["swtImplementation"].files.forEach { jar ->
//            ZipFile(jar).use { zip ->
//                zip.entries().asSequence()
//                    .filter { it.name.matches(".+\\.(so|dll|dylib|jnilib)$".toRegex()) }
//                    .forEach { entry ->
//                        zip.getInputStream(entry).use { input ->
//                            val outputFile = file("${nativesDir}/${entry.name}")
//                            outputFile.parentFile.mkdirs()
//                            outputFile.outputStream().use { output ->
//                                input.copyTo(output)
//                            }
//                            if (!isWindows) {
//                                outputFile.setExecutable(true)
//                            }
//                        }
//                    }
//            }
//        }
//    }
//}

val os = "macos"

val extractSources by tasks.registering {
    dependsOn(configurations["swtSources"])

    doLast {
        val sourcesDir = file("build/swt/$os")
        sourcesDir.mkdirs()

        configurations["swtSources"].files.forEach { jar ->
            unzipTo(sourcesDir, jar)
        }

//        configurations["swtSources"].files.forEach { jar ->
//            ZipFile(jar).use { zip ->
//                zip.entries().asSequence()
//                    .filter { !it.isDirectory }
//                    .forEach { entry ->
//                        zip.getInputStream(entry).use { input ->
//                            val outputFile = file("${sourcesDir}/${entry.name}")
//                            outputFile.parentFile.mkdirs()
//                            outputFile.outputStream().use { output ->
//                                input.copyTo(output)
//                            }
//                        }
//                    }
//            }
//        }
    }
}

tasks.compileJava {
//    dependsOn(extractSources)
//    options.setIncremental(false) // dsl-json processor seems to get crazy
}

//tasks.jar {
////    dependsOn(extractNatives)
//
//    val sourceSets = project.extensions.getByType<SourceSetContainer>()
//    from(sourceSets.getByName("main").java.srcDirs) {
//        include("**/*.css")
//    }
//
//    from(layout.buildDirectory.file("natives")) {
//        into(".")
//    }
//
//    // Add all dependencies to the JAR
//    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
//
//    // Exclude META-INF signatures to avoid signature validation errors
//    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
//
//    manifest {
//        attributes(
//        )
//    }
//
//    // Avoid duplicate files in the JAR
//    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
//}

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
        configurations["${platform}SwtImpl"]("org.eclipse.platform:org.eclipse.swt.$swtWs.$swtOs.${osArch[1]}:$swtVersion")
    }

    tasks.register<Exec>("${platform}FlutterLib") {
        group = "build"
        description = "Builds Flutter lib for $platform"
        workingDir = file("../flutter-lib")

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

    tasks.register<Copy>("${platform}ExtractNatives") {
        from(configurations["${platform}SwtImpl"].map { zipTree(it) })
        into(layout.buildDirectory.dir("natives/$platform"))
        include("*.so", "*.dll", "*.dylib", "*.jnilib")
    }

    tasks.register<Jar>("${platform}Jar") {
        group = "build"
        description = "Assembles a jar archive for $platform"
        archiveBaseName.set("${project.name}-$platform")
        from(sourceSets[osArch[0]].output)
        from(layout.buildDirectory.dir("natives/$platform"))

        // Add all dependencies to the JAR
        from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
        exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA", "OSGI-OPT/")

        // Add manifest attributes if needed
        manifest {
            attributes(
                "Fragment-Host" to "org.eclipse.swt;bundle-version=\"[3.128.0,4.0.0)\"",
                "Bundle-Name" to "Equo SWT for ${osArch[0]} on ${osArch[1]}",
                "Bundle-Vendor" to "Equo Tech, Inc.",
                "Bundle-SymbolicName" to "org.eclipse.swt.$swtWs.$swtOs.${osArch[1]}; singleton:=true",
                "Bundle-Version" to "3.128.0.v20241113-2009",
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
    }
}

// Task to build all platform JARs
tasks.register("buildAllPlatforms") {
    group = "build"
    description = "Builds JARs for all platforms"
    dependsOn(platforms.map { "${it}Jar" })
}

// Configure the default build task to include all platform JARs
tasks.build {
    dependsOn("buildAllPlatforms")
}