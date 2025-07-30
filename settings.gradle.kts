buildscript {
    repositories {
        mavenCentral()
    }
}

rootProject.name = "swt-evolve"

include("swt_native")
include("examples")
include("flutter-lib")