plugins {
    id("java")
    id("application")
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://gitlab.com/api/v4/projects/72079350/packages/maven")
        name = "SWT Evolve DEV"
    }
}

val currentOs = when {
    org.gradle.internal.os.OperatingSystem.current().isWindows -> "windows"
    org.gradle.internal.os.OperatingSystem.current().isMacOsX -> "macos"
    else -> "linux"
}
val arch = System.getProperty("os.arch")
val currentPlatform = "$currentOs-${if (arch.contains("aarch64") || arch.contains("arm")) "aarch64" else "x86_64"}"

tasks.compileJava {
    options.encoding = "UTF-8"
}

dependencies {
    if (gradle.parent != null)
        implementation(project(":swt_native"))
    else
        implementation("dev.equo:swt-evolve:0.2.0:$currentPlatform")
}

tasks.register<JavaExec>("runExample") {
    group = "examples"
    description = "Run an example class. Usage: ./gradlew :examples:runExample -PmainClass=dev.equo.StyledTextSnippet"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set(project.findProperty("mainClass")?.toString() ?: "dev.equo.StyledTextSnippet3")

    if (System.getProperty("os.name").lowercase().contains("mac")) {
        jvmArgs("-XstartOnFirstThread")
    }
}