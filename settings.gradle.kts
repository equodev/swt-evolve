buildscript {
    repositories {
        mavenCentral()
    }
}

rootProject.name = "swt-evolve"

include("swt_native")
// A build that includes this one can leave the examples out, when none of its tasks need them, by
// setting `swtEvolveExamples = false` in its Gradle object's extra properties.
val parentExtra = (gradle.parent as ExtensionAware?)?.extra
if (parentExtra?.has("swtEvolveExamples") != true || parentExtra["swtEvolveExamples"] != false)
    include("examples")
include("flutter-lib")