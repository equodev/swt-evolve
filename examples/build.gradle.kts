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
    mavenLocal()
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
        implementation("dev.equo:swt-evolve:+:$currentPlatform")

    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.21")
    // JFace dependencies
    implementation("org.eclipse.platform:org.eclipse.jface:3.33.0") {
        exclude(group = "org.eclipse.platform", module = "org.eclipse.swt")
    }
    implementation("org.eclipse.platform:org.eclipse.core.commands:3.12.0")
    implementation("org.eclipse.platform:org.eclipse.equinox.common:3.19.0")
    implementation("org.eclipse.platform:org.eclipse.jface.text:3.25.0") {
        exclude(group = "org.eclipse.platform", module = "org.eclipse.swt")
    }
    implementation("org.eclipse.platform:org.eclipse.text:3.14.0")
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