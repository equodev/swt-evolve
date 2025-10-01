group = "dev.equo"
version = "0.2.0-SNAPSHOT"

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
