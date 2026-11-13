package dev.equo.swt;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.UserPrincipal;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.jar.Manifest;

/**
 * Keeps the class-data-sharing archive lines in an installed product's launcher .ini: with them the JVM
 * writes the classes a start loaded into a file at its exit, and later starts map them from it.
 * <p>
 * The path has to be absolute, since the JVM resolves it against the directory the user started the
 * product from, and only the running product knows where it is installed. A wrong setup costs more than
 * the archive gains: a missing or unwritable directory makes the JVM exit with status 1 after every run
 * (which also turns a restart into a plain exit), and a JVM agent makes it refuse to start while the
 * archive does not exist yet. So the lines are written only when {@link #archiveFor} finds every
 * condition met; a start with an agent changes nothing, since the archive it may be relying on exists.
 * <p>
 * {@code -Ddev.equo.swt.classDataArchive=false} takes the lines and the archives out.
 */
public final class ClassDataArchive {

    public static final String PROPERTY = "dev.equo.swt.classDataArchive";
    static final String AUTO_CREATE = "-XX:+AutoCreateSharedArchive";
    static final String ARCHIVE_FILE = "-XX:SharedArchiveFile=";
    static final String QUIET = "-Xlog:cds*=error";
    static final String PREFIX = "org.eclipse.swt.evolve-";
    static final String SUFFIX = ".jsa";
    /** The first release with {@code -XX:+AutoCreateSharedArchive}; an older JVM refuses to start with it. */
    static final int MIN_JAVA = 19;

    private ClassDataArchive() {
    }

    /**
     * Brings the launcher .ini of this product in line, once per JVM (whatever number of Displays it
     * creates), off the calling thread; never throws.
     */
    public static void configureOnce() {
        Once.started();
    }

    private static final class Once {
        static {
            Thread thread = new Thread(ClassDataArchive::configure, "evolve-class-data-archive");
            thread.setDaemon(true);
            thread.start();
        }

        static void started() {
        }
    }

    static void configure() {
        try {
            configure(System.getProperty("eclipse.launcher"), System.getProperty("osgi.configuration.area"),
                    ManagementFactory.getRuntimeMXBean().getInputArguments(),
                    evolveVersion(ClassDataArchive.class.getClassLoader()),
                    System.getProperty("java.specification.version"), System.getProperty("java.vm.name"),
                    System.getProperty("user.name"), !"false".equalsIgnoreCase(System.getProperty(PROPERTY)));
        } catch (Throwable notConfigured) {
            // The archive only makes starts faster: nothing here may stop or slow down this one.
        }
    }

    /** {@link #configure()} for this start's launcher, configuration area, JVM arguments and JVM. */
    static void configure(String launcher, String configurationArea, List<String> jvmArgs, String evolveVersion,
            String javaVersion, String vmName, String user, boolean enabled) throws IOException {
        Path launcherIni = launcherIni(launcher);
        if (launcherIni == null || !Files.isRegularFile(launcherIni)) return;
        // The start succeeded, so whatever archive the .ini names exists: changing it could break the next.
        // Taking the lines out is always safe, so the switch acts on such a start too.
        if (enabled && agentIn(jvmArgs) != null) return;
        // A packaged .ini is often a link to a file elsewhere: the link stays, its target is the one edited.
        Path ini = launcherIni.toRealPath();
        String text = decodeUtf8(Files.readAllBytes(ini));
        // Not UTF-8 (a .ini in a system code page): rewriting it would damage the lines it does not own.
        if (text == null || productArchiveLine(text) != null) return;
        Path configuration = fileOf(configurationArea);
        // Root starts a shared install: its other users could not write the archive the lines would name.
        boolean shared = "root".equals(user);
        Path archive = enabled && !shared ? archiveFor(launcherIni, configuration, evolveVersion, javaVersion, vmName) : null;
        String updated = updatedIni(text, archive);
        boolean written = updated.equals(text) || (Files.isWritable(ini) && replace(ini, updated));
        // Only once the .ini names the archive kept: deleting the one it still names would cost every exit a dump.
        if (configuration != null && written) removeOtherArchives(configuration, archive);
    }

    /**
     * The archive the .ini should name, or null for none: a HotSpot JVM of release 19 or later, and a
     * configuration area inside the install that this user can write, as can the .ini itself.
     */
    static Path archiveFor(Path ini, Path configuration, String evolveVersion, String javaVersion, String vmName) {
        if (evolveVersion == null || configuration == null) return null;
        if (javaFeature(javaVersion) < MIN_JAVA) return null;
        if (!isHotSpot(vmName)) return null;
        Path install = ini.toAbsolutePath().getParent();
        Path area = configuration.toAbsolutePath().normalize();
        // A configuration area outside the install is per user (a shared install): one .ini cannot name it.
        if (!area.startsWith(install)) return null;
        if (!Files.isDirectory(area) || !Files.isWritable(area) || !Files.isWritable(ini)) return null;
        if (!ownedByThisUser(area)) return null;
        Path archive = area.resolve(PREFIX + evolveVersion + SUFFIX);
        // The JVM splits this option's value on the path separator (a base and a top archive).
        return archive.toString().contains(File.pathSeparator) ? null : archive;
    }

    /**
     * Whether {@code dir} belongs to the user running this JVM. An install another user (an administrator)
     * owns is shared: its other users could not write the archive the lines would name.
     */
    static boolean ownedByThisUser(Path dir) {
        try {
            Path probe = Files.createTempFile(dir, ".evolve-owner", ".tmp");
            try {
                UserPrincipal me = Files.getOwner(probe);
                return me.equals(Files.getOwner(dir));
            } finally {
                Files.deleteIfExists(probe);
            }
        } catch (IOException | UnsupportedOperationException | SecurityException e) {
            return false;
        }
    }

    /** HotSpot, as OpenJDK builds and Oracle's name their VM; OpenJ9 and others have no such option. */
    static boolean isHotSpot(String vmName) {
        return vmName != null && !vmName.contains("J9")
                && (vmName.contains("HotSpot") || vmName.contains("OpenJDK") || vmName.contains("Server VM"));
    }

    /** {@code bytes} as UTF-8 text, or null when they are not valid UTF-8. */
    static String decodeUtf8(byte[] bytes) {
        try {
            return StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(bytes)).toString();
        } catch (CharacterCodingException e) {
            return null;
        }
    }

    /** The .ini with the archive lines naming {@code archive} after -vmargs, or without them when null. */
    static String updatedIni(String text, Path archive) {
        String lineBreak = text.contains("\r\n") ? "\r\n" : "\n";
        List<String> lines = new ArrayList<>();
        for (String line : text.split("\r?\n", -1)) {
            String trimmed = line.trim();
            if (trimmed.equals(AUTO_CREATE) || trimmed.equals(QUIET) || isOurArchiveLine(trimmed)) continue;
            lines.add(line);
        }
        boolean removed = lines.size() != text.split("\r?\n", -1).length;
        if (archive == null && !removed) return text;
        while (!lines.isEmpty() && lines.get(lines.size() - 1).isEmpty()) lines.remove(lines.size() - 1);
        if (archive != null) {
            boolean vmargs = false;
            for (String line : lines) vmargs |= line.trim().equals("-vmargs");
            if (!vmargs) lines.add("-vmargs");
            lines.add(AUTO_CREATE);
            lines.add(ARCHIVE_FILE + archive);
            lines.add(QUIET);
        }
        return String.join(lineBreak, lines) + lineBreak;
    }

    private static boolean isOurArchiveLine(String line) {
        return line.startsWith(ARCHIVE_FILE)
                && new File(line.substring(ARCHIVE_FILE.length())).getName().startsWith(PREFIX);
    }

    /** A line that sets up a class-data archive of the product's own, or null: those .ini files are left alone. */
    static String productArchiveLine(String text) {
        for (String line : text.split("\r?\n")) {
            String trimmed = line.trim();
            if ((trimmed.startsWith(ARCHIVE_FILE) && !isOurArchiveLine(trimmed))
                    || trimmed.startsWith("-XX:ArchiveClassesAtExit") || trimmed.startsWith("-Xshare:")) {
                return trimmed;
            }
        }
        return null;
    }

    /** The first JVM agent among {@code jvmArgs}, or null. */
    static String agentIn(List<String> jvmArgs) {
        for (String arg : jvmArgs) {
            if (arg.startsWith("-javaagent:") || arg.startsWith("-agentlib:") || arg.startsWith("-agentpath:")) {
                return arg;
            }
        }
        return null;
    }

    /** The launcher's .ini: the one named for its executable, else the eclipse.ini beside it. */
    static Path launcherIni(String launcher) {
        if (launcher == null || launcher.isEmpty()) return null;
        Path exe = Paths.get(launcher).toAbsolutePath();
        Path dir = exe.getParent();
        if (dir == null) return null;
        String name = exe.getFileName().toString();
        String stem = name.toLowerCase(Locale.ROOT).endsWith(".exe") ? name.substring(0, name.length() - 4) : name;
        Path named = dir.resolve(stem + ".ini");
        return Files.isRegularFile(named) ? named : dir.resolve("eclipse.ini");
    }

    /** The feature release of a {@code java.specification.version}: 8 for "1.8", 21 for "21". */
    static int javaFeature(String version) {
        if (version == null) return 0;
        String[] parts = version.split("\\.");
        try {
            int first = Integer.parseInt(parts[0]);
            return first == 1 && parts.length > 1 ? Integer.parseInt(parts[1]) : first;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * A file: URL or a plain path as a path, or null. Equinox writes its areas as file: URLs that are not
     * always encoded ({@code file:/C:/My Apps/C#/configuration/}), so an invalid URI is read as a path.
     */
    static Path fileOf(String location) {
        if (location == null || location.isEmpty()) return null;
        if (!location.startsWith("file:")) return pathOrNull(location);
        try {
            return Paths.get(new URI(location.replace(" ", "%20")));
        } catch (URISyntaxException | IllegalArgumentException e) {
            String path = location.substring("file:".length()).replaceFirst("^/+(?=[A-Za-z]:)", "");
            return pathOrNull(path);
        }
    }

    private static Path pathOrNull(String path) {
        try {
            return Paths.get(path);
        } catch (InvalidPathException e) {
            return null;
        }
    }

    /**
     * The Evolve-Version of the manifest {@code loader} sees it in, or null. Read as a resource, since the
     * fragment is a jar, or a directory once p2 installs it, and in OSGi its class loader is the host's.
     */
    static String evolveVersion(ClassLoader loader) {
        try {
            Enumeration<URL> manifests = loader.getResources("META-INF/MANIFEST.MF");
            while (manifests.hasMoreElements()) {
                try (InputStream in = manifests.nextElement().openStream()) {
                    String version = new Manifest(in).getMainAttributes().getValue("Evolve-Version");
                    if (version != null) return version;
                }
            }
        } catch (IOException | SecurityException e) {
            // No version: no archive.
        }
        return null;
    }

    /** Replaces {@code ini} with {@code text} in one step, keeping its permissions; false when it could not. */
    private static boolean replace(Path ini, String text) throws IOException {
        Path temp = Files.createTempFile(ini.getParent(), ini.getFileName().toString(), ".tmp");
        try {
            Files.write(temp, text.getBytes(StandardCharsets.UTF_8));
            // A temporary file is readable by its owner only; the .ini may be read by other users.
            if (Files.getFileStore(ini).supportsFileAttributeView(PosixFileAttributeView.class)) {
                Files.setPosixFilePermissions(temp, Files.getPosixFilePermissions(ini));
            }
            Files.move(temp, ini, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            return true;
        } catch (IOException | UnsupportedOperationException e) {
            return false;
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    /** Deletes the Evolve archives in {@code dir} other than {@code keep} (all of them when it is null). */
    static void removeOtherArchives(Path dir, Path keep) throws IOException {
        if (!Files.isDirectory(dir)) return;
        List<Path> archives = new ArrayList<>();
        try (DirectoryStream<Path> entries = Files.newDirectoryStream(dir, PREFIX + "*" + SUFFIX)) {
            for (Path entry : entries) archives.add(entry);
        }
        for (Path archive : archives) {
            if (keep != null && archive.getFileName().equals(keep.getFileName())) continue;
            try {
                Files.deleteIfExists(archive);
            } catch (IOException mappedElsewhere) {
                // Windows refuses while another running JVM maps it: a later start deletes it.
            }
        }
    }
}
