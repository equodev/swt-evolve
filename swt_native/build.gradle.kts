import org.gradle.api.tasks.Copy
import org.gradle.kotlin.dsl.*
import java.util.zip.ZipFile

import org.gradle.api.tasks.SourceSetContainer

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

// Configure the main source set
java {
    sourceSets {
        main {
            java {
                srcDir("src/main/java")
            }
        }
    }
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

val swtImplementation by configurations.creating {
    exclude(group = "org.eclipse.platform", module = "org.eclipse.swt")
}

val swtSources by configurations.creating {
    exclude(group = "org.eclipse.platform", module = "org.eclipse.swt")
}

dependencies {
    swtImplementation("$swtBundle:$swtVersion")
    swtSources("$swtBundle:$swtVersion:sources")
    implementation("dev.equo:com.equo.comm.ws.provider:3.1.0.202405302201") {
        exclude(group = "dev.equo", module = "com.equo.comm.common")
    }

    implementation("com.google.auto.value:auto-value-annotations:1.10.4")
    annotationProcessor("com.google.auto.value:auto-value:1.10.4")
}

val extractNatives by tasks.registering {
    dependsOn(configurations["swtImplementation"])
    
    doLast {
        val nativesDir = layout.buildDirectory.file("natives").get().getAsFile()
        nativesDir.mkdirs()
        
        configurations["swtImplementation"].files.forEach { jar ->
            ZipFile(jar).use { zip ->
                zip.entries().asSequence()
                    .filter { it.name.matches(".+\\.(so|dll|dylib|jnilib)$".toRegex()) }
                    .forEach { entry ->
                        zip.getInputStream(entry).use { input ->
                            val outputFile = file("${nativesDir}/${entry.name}")
                            outputFile.parentFile.mkdirs()
                            outputFile.outputStream().use { output ->
                                input.copyTo(output)
                            }
                            if (!isWindows) {
                                outputFile.setExecutable(true)
                            }
                        }
                    }
            }
        }
    }
}

val extractSources by tasks.registering {
    dependsOn(configurations["swtSources"])
    
    doLast {
        val sourcesDir = file("src/main/java") 
        sourcesDir.mkdirs()
        
        configurations["swtSources"].files.forEach { jar ->
            ZipFile(jar).use { zip ->
                zip.entries().asSequence()
                    .filter { !it.isDirectory }
                    .forEach { entry ->
                        zip.getInputStream(entry).use { input ->
                            val outputFile = file("${sourcesDir}/${entry.name}")
                            outputFile.parentFile.mkdirs()
                            outputFile.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }
                    }
            }
        }
    }
}

tasks.jar {
    dependsOn(extractNatives, extractSources)

    val sourceSets = project.extensions.getByType<SourceSetContainer>()
    from(sourceSets.getByName("main").java.srcDirs) {
        include("**/*.css")
    }
    
    from(layout.buildDirectory.file("natives")) {
        into(".")
    }
    
    // Add all dependencies to the JAR
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    
    // Exclude META-INF signatures to avoid signature validation errors
    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
    
    manifest {
        attributes(
        )
    }
    
    // Avoid duplicate files in the JAR
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

