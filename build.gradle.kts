group = "dev.equo"
version = "0.2.0-SNAPSHOT"

allprojects {
    repositories {
        mavenCentral()
        maven(url = "https://dl.equo.dev/sdk/mvn/release")
    }
}

dependencies {
}

tasks.wrapper {
    gradleVersion = "8.7"
    distributionType = Wrapper.DistributionType.ALL
}