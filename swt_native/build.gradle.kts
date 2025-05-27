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
}


sourceSets {
    main {
        java {
            // Include the shared sources and current OS-specific sources for IDE
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
        val sourcesDir = file("src/$os/java")
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

        // Add manifest attributes if needed
        manifest {
            attributes(
                "Implementation-Title" to project.name,
                "Implementation-Version" to project.version,
                "Target-Platform" to platform
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