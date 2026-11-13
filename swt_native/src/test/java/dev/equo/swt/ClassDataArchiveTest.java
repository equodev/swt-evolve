package dev.equo.swt;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class ClassDataArchiveTest {

    private static final String VERSION = "0.345.0";
    private static final String HOTSPOT = "OpenJDK 64-Bit Server VM";
    private static final String INI = "-startup\nplugins/org.eclipse.equinox.launcher.jar\n-vmargs\n-Xms256m\n";
    private static final List<String> NO_AGENT = Collections.singletonList("-Xms256m");
    /** Whoever runs the tests (CI runs them as root): a user who is not root, as most who start a product. */
    private static final String USER = "user";

    /** An installed product: its launcher, the .ini named for it and its configuration area. */
    private static Path product(Path root, String ini) throws IOException {
        Files.createDirectories(root.resolve("configuration"));
        Files.write(root.resolve("eclipse"), new byte[0]);
        Files.write(root.resolve("eclipse.ini"), ini.getBytes(StandardCharsets.UTF_8));
        return root;
    }

    private static String start(Path root, List<String> jvmArgs, String java, String vm, boolean enabled) throws IOException {
        ClassDataArchive.configure(root.resolve("eclipse").toString(), root.resolve("configuration").toUri().toString(),
                jvmArgs, VERSION, java, vm, USER, enabled);
        return new String(Files.readAllBytes(root.resolve("eclipse.ini")), StandardCharsets.UTF_8);
    }

    private static String start(Path root) throws IOException {
        return start(root, NO_AGENT, "21", HOTSPOT, true);
    }

    private static String linesFor(Path archive) {
        return "-XX:+AutoCreateSharedArchive\n-XX:SharedArchiveFile=" + archive + "\n-Xlog:cds*=error\n";
    }

    @Test
    void aStartWritesTheLinesWithTheAbsolutePathOfItsConfigurationArea(@TempDir Path dir) throws IOException {
        Path root = product(dir.resolve("app"), INI);
        Path archive = root.resolve("configuration").resolve("org.eclipse.swt.evolve-0.345.0.jsa").toAbsolutePath();

        assertThat(start(root)).isEqualTo(INI + linesFor(archive));
        assertThat(start(root)).as("a second start changes nothing").isEqualTo(INI + linesFor(archive));
    }

    @Test
    void aMovedProductNamesItsNewPlace(@TempDir Path dir) throws IOException {
        Path old = dir.resolve("old").resolve("configuration").resolve("org.eclipse.swt.evolve-0.345.0.jsa");
        Path root = product(dir.resolve("app"), INI + linesFor(old));

        assertThat(start(root)).isEqualTo(INI + linesFor(
                root.resolve("configuration").resolve("org.eclipse.swt.evolve-0.345.0.jsa").toAbsolutePath()));
    }

    @Test
    void anUpdateNamesANewArchiveAndDeletesThePreviousOne(@TempDir Path dir) throws IOException {
        Path root = product(dir.resolve("app"), INI);
        Path configuration = root.resolve("configuration");
        Path previous = Files.write(configuration.resolve("org.eclipse.swt.evolve-0.340.0.jsa"), new byte[1]);
        Path current = Files.write(configuration.resolve("org.eclipse.swt.evolve-0.345.0.jsa"), new byte[1]);
        Path unrelated = Files.write(configuration.resolve("other.jsa"), new byte[1]);

        start(root);

        assertThat(previous).doesNotExist();
        assertThat(current).exists();
        assertThat(unrelated).exists();
    }

    @Test
    void aStartWithAnAgentChangesNothing(@TempDir Path dir) throws IOException {
        for (String agent : Arrays.asList("-javaagent:/opt/agent.jar", "-agentlib:jdwp=transport=dt_socket",
                "-agentpath:/opt/libagent.so")) {
            Path root = product(dir.resolve(agent.substring(1, 8)), INI);
            assertThat(start(root, Arrays.asList("-Xms256m", agent), "21", HOTSPOT, true)).as(agent).isEqualTo(INI);
        }
    }

    @Test
    void noLinesForAJvmThatCannotWriteTheArchive(@TempDir Path dir) throws IOException {
        Path old = dir.resolve("app").resolve("configuration").resolve("org.eclipse.swt.evolve-0.340.0.jsa");
        Path root = product(dir.resolve("app"), INI + linesFor(old));
        assertThat(start(root, NO_AGENT, "17", HOTSPOT, true)).isEqualTo(INI);
        assertThat(start(root, NO_AGENT, "1.8", HOTSPOT, true)).isEqualTo(INI);
        assertThat(start(root, NO_AGENT, "21", "Eclipse OpenJ9 VM", true)).isEqualTo(INI);
    }

    @Test
    void turnedOffTakesTheLinesAndTheArchiveOut(@TempDir Path dir) throws IOException {
        Path root = product(dir.resolve("app"), INI);
        start(root);
        Path archive = Files.write(root.resolve("configuration").resolve("org.eclipse.swt.evolve-0.345.0.jsa"), new byte[1]);

        assertThat(start(root, NO_AGENT, "21", HOTSPOT, false)).isEqualTo(INI);
        assertThat(archive).doesNotExist();
    }

    @Test
    void aProductsOwnArchiveIsLeftAlone(@TempDir Path dir) throws IOException {
        for (String own : Arrays.asList("-XX:SharedArchiveFile=/opt/app/app.jsa", "-XX:ArchiveClassesAtExit=app.jsa",
                "-Xshare:off")) {
            Path root = product(dir.resolve(Integer.toString(own.hashCode())), INI + own + "\n");
            assertThat(start(root)).as(own).isEqualTo(INI + own + "\n");
        }
    }

    @Test
    void noLinesForAConfigurationAreaOutsideTheInstall(@TempDir Path dir) throws IOException {
        Path root = product(dir.resolve("app"), INI);
        Path perUser = Files.createDirectories(dir.resolve("home").resolve(".app").resolve("configuration"));
        ClassDataArchive.configure(root.resolve("eclipse").toString(), perUser.toUri().toString(), NO_AGENT, VERSION,
                "21", HOTSPOT, USER, true);
        assertThat(new String(Files.readAllBytes(root.resolve("eclipse.ini")), StandardCharsets.UTF_8)).isEqualTo(INI);
    }

    @Test
    void noLinesWhenTheConfigurationAreaIsNotWritable(@TempDir Path dir) throws IOException {
        Path root = product(dir.resolve("app"), INI);
        Path configuration = root.resolve("configuration");
        assumeTrue(configuration.toFile().setWritable(false) && !Files.isWritable(configuration),
                "this file system lets the user write a read-only directory (Windows, root)");
        try {
            assertThat(start(root)).isEqualTo(INI);
        } finally {
            configuration.toFile().setWritable(true);
        }
    }

    @Test
    void noLinesWithoutAKnownEvolveVersion(@TempDir Path dir) throws IOException {
        Path root = product(dir.resolve("app"), INI);
        ClassDataArchive.configure(root.resolve("eclipse").toString(), root.resolve("configuration").toUri().toString(),
                NO_AGENT, null, "21", HOTSPOT, USER, true);
        assertThat(new String(Files.readAllBytes(root.resolve("eclipse.ini")), StandardCharsets.UTF_8)).isEqualTo(INI);
    }

    @Test
    void theIniKeepsItsLineBreaksAndGetsAVmargs(@TempDir Path dir) {
        Path archive = dir.resolve("a.jsa");
        assertThat(ClassDataArchive.updatedIni("-startup\r\nx.jar\r\n", archive)).isEqualTo(
                "-startup\r\nx.jar\r\n-vmargs\r\n-XX:+AutoCreateSharedArchive\r\n-XX:SharedArchiveFile=" + archive
                        + "\r\n-Xlog:cds*=error\r\n");
    }

    @Test
    void theIniIsNamedForTheLauncher(@TempDir Path dir) throws IOException {
        Files.write(dir.resolve("myapp.ini"), new byte[0]);
        assertThat(ClassDataArchive.launcherIni(dir.resolve("myapp.exe").toString())).isEqualTo(dir.resolve("myapp.ini"));
        assertThat(ClassDataArchive.launcherIni(dir.resolve("myapp").toString())).isEqualTo(dir.resolve("myapp.ini"));
        assertThat(ClassDataArchive.launcherIni(dir.resolve("other").toString())).isEqualTo(dir.resolve("eclipse.ini"));
        assertThat(ClassDataArchive.launcherIni(null)).isNull();
    }

    @Test
    void notStartedByALauncherDoesNothing(@TempDir Path dir) throws IOException {
        Path root = product(dir.resolve("app"), INI);
        Path archive = Files.write(root.resolve("configuration").resolve("org.eclipse.swt.evolve-0.340.0.jsa"), new byte[1]);
        ClassDataArchive.configure(null, root.resolve("configuration").toUri().toString(), NO_AGENT, VERSION, "21",
                HOTSPOT, USER, true);
        assertThat(new String(Files.readAllBytes(root.resolve("eclipse.ini")), StandardCharsets.UTF_8)).isEqualTo(INI);
        assertThat(archive).exists();
    }

    @Test
    void turnedOffActsOnAStartWithAnAgentToo(@TempDir Path dir) throws IOException {
        Path root = product(dir.resolve("app"), INI);
        start(root);
        assertThat(start(root, Arrays.asList("-javaagent:/opt/agent.jar"), "21", HOTSPOT, false)).isEqualTo(INI);
    }

    @Test
    void rootGetsNoLines(@TempDir Path dir) throws IOException {
        Path root = product(dir.resolve("app"), INI);
        ClassDataArchive.configure(root.resolve("eclipse").toString(), root.resolve("configuration").toUri().toString(),
                NO_AGENT, VERSION, "21", HOTSPOT, "root", true);
        assertThat(new String(Files.readAllBytes(root.resolve("eclipse.ini")), StandardCharsets.UTF_8)).isEqualTo(INI);
    }

    @Test
    void aConfigurationAreaOfAnotherUserGetsNoLines(@TempDir Path dir) throws IOException {
        Path area = Files.createDirectories(dir.resolve("configuration"));
        assertThat(ClassDataArchive.ownedByThisUser(area)).as("a directory this user made").isTrue();
        assertThat(ClassDataArchive.ownedByThisUser(dir.resolve("missing"))).isFalse();
        Path system = java.nio.file.Paths.get(System.getProperty("java.home"));
        assumeTrue(!Files.isWritable(system), "the JDK here belongs to this user");
        assertThat(ClassDataArchive.ownedByThisUser(system)).isFalse();
    }

    @Test
    void theVersionIsReadFromTheFragmentAsAJarAndAsADirectory(@TempDir Path dir) throws IOException {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        manifest.getMainAttributes().putValue("Evolve-Version", VERSION);
        Path jar = dir.resolve("fragment.jar");
        try (OutputStream out = Files.newOutputStream(jar); JarOutputStream ignored = new JarOutputStream(out, manifest)) {
            // the manifest is the whole jar
        }
        Path unpacked = Files.createDirectories(dir.resolve("fragment").resolve("META-INF"));
        try (OutputStream out = Files.newOutputStream(unpacked.resolve("MANIFEST.MF"))) {
            manifest.write(out);
        }
        for (Path location : Arrays.asList(jar, dir.resolve("fragment"))) {
            try (URLClassLoader loader = new URLClassLoader(new URL[] { location.toUri().toURL() }, null)) {
                assertThat(ClassDataArchive.evolveVersion(loader)).as(location.toString()).isEqualTo(VERSION);
            }
        }
        try (URLClassLoader none = new URLClassLoader(new URL[0], null)) {
            assertThat(ClassDataArchive.evolveVersion(none)).isNull();
        }
    }

    @Test
    void aConfigurationAreaUrlIsReadEncodedOrNot(@TempDir Path dir) throws IOException {
        Path area = Files.createDirectories(dir.resolve("My Apps").resolve("C#").resolve("configuration"));
        assertThat(ClassDataArchive.fileOf(area.toUri().toString())).isEqualTo(area);
        assertThat(ClassDataArchive.fileOf("file:" + area + "/")).isEqualTo(area);
        assertThat(ClassDataArchive.fileOf(null)).isNull();
    }

    @Test
    void anIniThatIsNotUtf8IsLeftAlone(@TempDir Path dir) throws IOException {
        Path root = product(dir.resolve("app"), INI);
        byte[] cp1252 = (INI + "-Duser.name=Jos\u00e9\n").getBytes(StandardCharsets.ISO_8859_1);
        Files.write(root.resolve("eclipse.ini"), cp1252);
        start(root);
        assertThat(Files.readAllBytes(root.resolve("eclipse.ini"))).isEqualTo(cp1252);
    }

    @Test
    void theIniKeepsItsPermissionsAndALinkStaysALink(@TempDir Path dir) throws IOException {
        Path root = product(dir.resolve("app"), INI);
        Path ini = root.resolve("eclipse.ini");
        assumeTrue(Files.getFileStore(ini).supportsFileAttributeView("posix"), "no POSIX permissions here");
        Path target = dir.resolve("etc").resolve("eclipse.ini");
        Files.createDirectories(target.getParent());
        Files.move(ini, target);
        Files.createSymbolicLink(ini, target);
        Set<PosixFilePermission> shared = PosixFilePermissions.fromString("rw-r--r--");
        Files.setPosixFilePermissions(target, shared);

        start(root);

        assertThat(Files.isSymbolicLink(ini)).isTrue();
        assertThat(new String(Files.readAllBytes(target), StandardCharsets.UTF_8)).contains("-XX:SharedArchiveFile=");
        assertThat(Files.getPosixFilePermissions(target)).isEqualTo(shared);
    }

    @Test
    void theArchiveTheIniStillNamesIsKeptWhenTheIniCannotBeRewritten(@TempDir Path dir) throws IOException {
        Path root = product(dir.resolve("app"), INI);
        start(root);
        Path ini = root.resolve("eclipse.ini");
        Path archive = Files.write(root.resolve("configuration").resolve("org.eclipse.swt.evolve-0.345.0.jsa"), new byte[1]);
        assumeTrue(ini.toFile().setWritable(false) && !Files.isWritable(ini), "the user can write a read-only file here");
        try {
            start(root, NO_AGENT, "21", HOTSPOT, false);
            assertThat(archive).exists();
        } finally {
            ini.toFile().setWritable(true);
        }
    }

    @Test
    void hotSpotIsRecognisedByTheNamesItsBuildsUse() {
        assertThat(ClassDataArchive.isHotSpot("OpenJDK 64-Bit Server VM")).isTrue();
        assertThat(ClassDataArchive.isHotSpot("Java HotSpot(TM) 64-Bit Server VM")).isTrue();
        assertThat(ClassDataArchive.isHotSpot("Eclipse OpenJ9 VM")).isFalse();
        assertThat(ClassDataArchive.isHotSpot(null)).isFalse();
    }

    @Test
    void javaFeature() {
        assertThat(ClassDataArchive.javaFeature("21")).isEqualTo(21);
        assertThat(ClassDataArchive.javaFeature("1.8")).isEqualTo(8);
        assertThat(ClassDataArchive.javaFeature("x")).isZero();
        assertThat(ClassDataArchive.javaFeature(null)).isZero();
    }
}
