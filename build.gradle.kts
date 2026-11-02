group = "dev.equo"
version = "0.2.0-SNAPSHOT"

if (gradle.parent != null) {
    val parentRoot = gradle.parent!!.rootProject
    val versionProps = listOf(
        "swtVersionFull", "eclipseUrl", "draw2dVersion",
        "jfaceVersion", "coreCommandsVersion", "equinoxCommonVersion",
        "jfaceTextVersion", "eclipseTextVersion"
    )
    allprojects {
        versionProps.forEach { key ->
            val value = parentRoot.findProperty(key)?.toString()
            if (value != null) {
                project.extra[key] = value
            }
        }
    }
}

allprojects {
    repositories {
        mavenCentral()
        maven {
            url = uri("https://dl.equo.dev/sdk/mvn/release")
            content {
                excludeGroup("org.eclipse")
                excludeGroup("com.equo")
            }
        }
        maven {
            url = uri(providers.gradleProperty("chromiumRepoUrl").getOrElse("https://dl.equo.dev/chromium-swt-ee/marketplace/mvn"))
            content {
                includeGroup("com.equo")
            }
        }
    }
}

// SWT releases older than 3.105.2 are not on Maven Central. A build that includes this one and
// builds such a release hands over, as `eclipseP2Script` in its Gradle object's extra properties, the
// script that resolves them from the release's p2 repository; it goes to the projects that need SWT.
((gradle.parent as ExtensionAware?)?.extra?.properties?.get("eclipseP2Script") as File?)?.let { script ->
    listOf(":swt_native", ":examples").mapNotNull { findProject(it) }.forEach { it.apply(from = script) }
}

dependencies {
}

tasks.wrapper {
    gradleVersion = "9.8.0"
    distributionType = Wrapper.DistributionType.ALL
}
