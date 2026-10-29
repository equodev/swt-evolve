// SWT releases older than 3.105.2, and the Eclipse bundles that go with them, were never published to
// Maven Central: they exist only in the release's p2 repository (`eclipseUrl`). For those releases
// every `org.eclipse.platform` dependency resolves from a Maven-layout copy of the p2 bundles it
// names, filled on demand under the Gradle user home, one directory per p2 repository so that two
// releases shipping the same bundle version with different qualifiers never mix.
//
// A Maven version maps to the p2 bundle of the same id whose version is that one plus a qualifier,
// and the `sources` classifier to the `<id>.source` bundle. The copied bundles carry no dependencies:
// the builds declare every Eclipse bundle they compile against.

import java.io.File
import java.net.URI
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.zip.ZipFile

val p2Group = "org.eclipse.platform"
val firstMavenCentralSwt = listOf(3, 105, 2)

fun versionParts(v: String) = v.split(".").map { it.takeWhile(Char::isDigit).toIntOrNull() ?: 0 }

fun predatesMavenCentral(swtVersion: String): Boolean {
    val parts = versionParts(swtVersion)
    for (i in firstMavenCentralSwt.indices) {
        val v = parts.getOrElse(i) { 0 }
        if (v != firstMavenCentralSwt[i]) return v < firstMavenCentralSwt[i]
    }
    return false
}

fun download(url: String, target: File): File {
    target.parentFile.mkdirs()
    val tmp = File.createTempFile(target.name, ".part", target.parentFile)
    try {
        URI(url).toURL().openStream().use { input -> tmp.outputStream().use { input.copyTo(it) } }
        Files.move(tmp.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
    } finally {
        tmp.delete()
    }
    return target
}

fun mirrorDir(eclipseUrl: String) = File(
    gradle.gradleUserHomeDir,
    "caches/eclipse-p2-maven/" + eclipseUrl.substringAfter("://").trimEnd('/').replace(Regex("[^A-Za-z0-9._-]"), "_"))

/** Bundle id to the versions the p2 repository at [eclipseUrl] holds. */
fun p2Bundles(eclipseUrl: String): Map<String, List<String>> {
    val index = File(mirrorDir(eclipseUrl), "artifacts.jar")
    if (!index.exists()) download("${eclipseUrl.trimEnd('/')}/artifacts.jar", index)
    val xml = ZipFile(index).use { zip ->
        val entry = zip.getEntry("artifacts.xml") ?: throw GradleException("$index holds no artifacts.xml")
        zip.getInputStream(entry).bufferedReader().readText()
    }
    return Regex("""<artifact\s+classifier=['"]osgi\.bundle['"]\s+id=['"]([^'"]+)['"]\s+version=['"]([^'"]+)['"]""")
        .findAll(xml)
        .groupBy({ it.groupValues[1] }, { it.groupValues[2] })
}

/** The full p2 version of bundle [id] at Maven version [version], or null when the repository lacks it. */
fun p2Version(bundles: Map<String, List<String>>, id: String, version: String): String? =
    bundles[id].orEmpty()
        .filter { it.startsWith("$version.") && !it.removePrefix("$version.").first().isDigit() }
        .maxOrNull()

fun mirror(eclipseUrl: String, bundles: Map<String, List<String>>, module: String, version: String, sources: Boolean) {
    val full = p2Version(bundles, module, version) ?: return
    val dir = File(mirrorDir(eclipseUrl), "${p2Group.replace('.', '/')}/$module/$version")
    val plugins = "${eclipseUrl.trimEnd('/')}/plugins"
    val jar = File(dir, "$module-$version.jar")
    if (!jar.exists()) download("$plugins/${module}_$full.jar", jar)
    val sourcesJar = File(dir, "$module-$version-sources.jar")
    if (sources && !sourcesJar.exists() && p2Version(bundles, "$module.source", version) == full)
        download("$plugins/$module.source_$full.jar", sourcesJar)
    val pom = File(dir, "$module-$version.pom")
    if (!pom.exists()) pom.writeText(
        "<project><modelVersion>4.0.0</modelVersion><groupId>$p2Group</groupId>" +
            "<artifactId>$module</artifactId><version>$version</version></project>\n")
}

val swtVersionFull = findProperty("swtVersionFull")?.toString()
val eclipseUrl = findProperty("eclipseUrl")?.toString()

if (swtVersionFull != null && eclipseUrl != null && predatesMavenCentral(swtVersionFull.substringBefore(".v"))) {
    val bundles by lazy { p2Bundles(eclipseUrl) }
    val repo = repositories.maven {
        name = "Eclipse p2 (Maven layout)"
        url = mirrorDir(eclipseUrl).toURI()
        content { includeGroup(p2Group) }
    }
    repositories.remove(repo)
    repositories.addFirst(repo)
    configurations.configureEach {
        withDependencies {
            filterIsInstance<ExternalModuleDependency>()
                .filter { it.group == p2Group && it.version != null }
                .forEach { dep ->
                    mirror(eclipseUrl, bundles, dep.name, dep.version!!, dep.artifacts.any { it.classifier == "sources" })
                }
        }
    }
}

