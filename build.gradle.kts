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
            }
        }
    }
}

dependencies {
}

tasks.wrapper {
    gradleVersion = "8.7"
    distributionType = Wrapper.DistributionType.ALL
}
