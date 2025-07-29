plugins {
    id("java")
    id("application")
}

group = "dev.equo"
version = "0.1.0-SNAPSHOT"

dependencies {
    implementation(project(":swt_native"))
}