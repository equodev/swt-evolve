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

// A build that includes this one can extend it: the script it hands over as `extensionScript` in its
// Gradle object's extra properties is applied to this root project.
((gradle.parent as ExtensionAware?)?.extra?.properties?.get("extensionScript") as File?)?.let { apply(from = it) }

dependencies {
}

tasks.wrapper {
    gradleVersion = "9.8.0"
    distributionType = Wrapper.DistributionType.ALL
}
