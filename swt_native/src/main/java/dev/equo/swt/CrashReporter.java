package dev.equo.swt;

import org.eclipse.swt.widgets.Display;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class CrashReporter {

    private static volatile boolean handling = false;
    private static volatile List<File> pendingNativeCrashes;
    private static volatile boolean nativeCrashesChecked = false;
    private static final long CRASH_FILE_MAX_AGE_MS = 24 * 60 * 60 * 1000;
    private static Path getCleanShutdownMarker() {
        String suffix = "";
        String installArea = System.getProperty("osgi.install.area");
        if (installArea != null && !installArea.isBlank()) {
            suffix = "_" + Integer.toHexString(installArea.hashCode());
        }
        return Path.of(System.getProperty("user.home"), ".equo", ".clean_shutdown" + suffix);
    }

    static void init() {
        if (Boolean.getBoolean("dev.equo.swt.crashReport.disabled")) return;
        String url = System.getProperty("dev.equo.swt.crashReportUrl");
        if (url == null || url.isBlank()) return;
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> handleCrash(throwable));
        Thread initThread = new Thread(() -> {
            EclipseErrorHook.tryInstall();
            boolean cleanShutdown = deleteCleanShutdownMarker();
            if (!cleanShutdown) {
                checkNativeCrashFiles();
            }
        }, "crash-reporter-init");
        initThread.setDaemon(true);
        initThread.start();
    }

    private static void checkNativeCrashFiles() {
        try {
            List<File> found = scanForNativeCrashFiles();
            if (!found.isEmpty()) {
                pendingNativeCrashes = found;
            }
        } catch (Throwable t) {
            System.err.println("[CrashReporter] Failed to scan for native crash files: " + t);
        }
    }

    private static boolean deleteCleanShutdownMarker() {
        try {
            return Files.deleteIfExists(getCleanShutdownMarker());
        } catch (IOException e) {
            return false;
        }
    }

    public static void writeCleanShutdownMarker() {
        if (Boolean.getBoolean("dev.equo.swt.crashReport.disabled")) return;
        String url = System.getProperty("dev.equo.swt.crashReportUrl");
        if (url == null || url.isBlank()) return;
        try {
            Path marker = getCleanShutdownMarker();
            Files.createDirectories(marker.getParent());
            Files.createFile(marker);
        } catch (IOException e) {
            // silently skip
        }
    }

    public static void handleCrash(Throwable throwable) {
        doHandle(throwable, true);
    }

    public static void handleError(Throwable throwable) {
        doHandle(throwable, false);
    }

    private static void doHandle(Throwable throwable, boolean fatal) {
        if (Boolean.getBoolean("dev.equo.swt.crashReport.disabled")) return;
        if (handling) return;
        handling = true;
        try {
            File crashLog = writeCrashLog(throwable);
            File eclipseLog = findEclipseLog();

            Config.forceEclipse();

            Display display = Display.getCurrent();
            if (display == null) display = Display.getDefault();

            if (display == null || display.isDisposed()) {
                printFallback(throwable, crashLog);
                if (fatal) System.exit(1);
                return;
            }

            final Display d = display;
            final File log = crashLog;
            final File elog = eclipseLog;

            if (Thread.currentThread() == d.getThread()) {
                showDialog(d, log, elog);
            } else {
                d.syncExec(() -> showDialog(d, log, elog));
            }
        } catch (Throwable t) {
            System.err.println("[CrashReporter] Failed to show crash dialog: " + t);
            throwable.printStackTrace();
        } finally {
            if (!fatal) handling = false;
        }
        if (fatal) System.exit(1);
    }

    private static void showDialog(Display display, File crashLog, File eclipseLog) {
        try {
            new CrashDialog(display, crashLog, eclipseLog).open();
        } catch (Throwable t) {
            printFallback(null, crashLog);
        }
    }

    private static void printFallback(Throwable throwable, File crashLog) {
        System.err.println("[CrashReporter] The application crashed unexpectedly.");
        if (crashLog != null) {
            System.err.println("[CrashReporter] A crash log has been saved to: " + crashLog.getAbsolutePath());
        }
        System.err.println("[CrashReporter] Please send the crash log to support@equo.dev so we can investigate.");
        if (throwable != null) {
            throwable.printStackTrace();
        }
    }

    static File writeCrashLog(Throwable throwable) {
        try {
            Path dir = Path.of(System.getProperty("user.home"), ".equo");
            Files.createDirectories(dir);

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
            File logFile = dir.resolve("crash-" + timestamp + ".log").toFile();

            try (PrintWriter pw = new PrintWriter(new FileWriter(logFile, StandardCharsets.UTF_8))) {
                pw.println("=== Equo SWT Crash Report ===");
                pw.println("Timestamp: " + LocalDateTime.now());
                pw.println();

                pw.println("=== System Info ===");
                pw.println("os.name: " + System.getProperty("os.name"));
                pw.println("os.version: " + System.getProperty("os.version"));
                pw.println("os.arch: " + System.getProperty("os.arch"));
                pw.println("java.version: " + System.getProperty("java.version"));
                pw.println("java.vendor: " + System.getProperty("java.vendor"));
                pw.println();

                pw.println(Config.asString());
                pw.println();

                pw.println("=== Exception ===");
                pw.println("Thread: " + Thread.currentThread().getName());
                throwable.printStackTrace(pw);
                pw.println();

                pw.println("=== All Thread Dumps ===");
                for (var entry : Thread.getAllStackTraces().entrySet()) {
                    pw.println("Thread: " + entry.getKey().getName() + " (state=" + entry.getKey().getState() + ")");
                    for (StackTraceElement ste : entry.getValue()) {
                        pw.println("    at " + ste);
                    }
                    pw.println();
                }
            }
            return logFile;
        } catch (IOException e) {
            System.err.println("[CrashReporter] Failed to write crash log: " + e);
            return null;
        }
    }

    static File findEclipseLog() {
        try {
            String workspaceLocation = System.getProperty("osgi.instance.area");
            if (workspaceLocation == null) return null;

            if (workspaceLocation.startsWith("file:")) {
                workspaceLocation = workspaceLocation.substring(5);
            }

            File logFile = new File(workspaceLocation, ".metadata/.log");
            return logFile.exists() ? logFile : null;
        } catch (Exception e) {
            return null;
        }
    }

    static boolean sendReport(String description, String email, File crashLog, File eclipseLog) {
        String url = System.getProperty("dev.equo.swt.crashReportUrl");
        if (url == null || url.isBlank()) return false;

        String boundary = "----EquoCrashReport" + System.currentTimeMillis();

        try {
            HttpURLConnection conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            conn.setConnectTimeout(10_000);
            conn.setReadTimeout(15_000);

            try (OutputStream os = conn.getOutputStream()) {
                String summary = summarize(description, crashLog);
                String subject = "[User] " + summary;
                writeField(os, boundary, "subject", subject);
                writeField(os, boundary, "body", description);
                writeField(os, boundary, "email", email != null ? email : "");

                if (crashLog != null && crashLog.exists()) {
                    writeFile(os, boundary, "crash_log", crashLog);
                }
                if (eclipseLog != null && eclipseLog.exists()) {
                    writeFile(os, boundary, "eclipse_log", eclipseLog);
                }

                os.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();
            return responseCode >= 200 && responseCode < 300;
        } catch (Exception e) {
            System.err.println("[CrashReporter] Failed to send report: " + e);
            return false;
        }
    }

    private static void writeField(OutputStream os, String boundary, String name, String value) throws IOException {
        String field = "--" + boundary + "\r\n" +
                "Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n" +
                value + "\r\n";
        os.write(field.getBytes(StandardCharsets.UTF_8));
    }

    private static void writeFile(OutputStream os, String boundary, String name, File file) throws IOException {
        String header = "--" + boundary + "\r\n" +
                "Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + file.getName() + "\"\r\n" +
                "Content-Type: text/plain\r\n\r\n";
        os.write(header.getBytes(StandardCharsets.UTF_8));
        Files.copy(file.toPath(), os);
        os.write("\r\n".getBytes(StandardCharsets.UTF_8));
    }

    private static String summarize(String description, File crashLog) {
        if (description != null && !description.isBlank()) {
            return summarizeDescription(description);
        }
        if (crashLog != null && crashLog.exists()) {
            return summarizeFromCrashLog(crashLog);
        }
        return "Crash report";
    }

    private static String summarizeDescription(String description) {
        String trimmed = description.strip();
        // Take the first sentence: split on sentence-ending punctuation or newline
        String firstSentence = trimmed.split("[.!?\\n]")[0].strip();
        if (firstSentence.isEmpty()) {
            firstSentence = trimmed.lines().findFirst().orElse(trimmed).strip();
        }
        if (firstSentence.length() <= 100) return firstSentence;
        return firstSentence.substring(0, 97) + "...";
    }

    private static String summarizeFromCrashLog(File crashLog) {
        try (BufferedReader reader = new BufferedReader(new FileReader(crashLog, StandardCharsets.UTF_8))) {
            String firstLine = reader.readLine();
            // Detect hs_err format: first line starts with '#' and contains "fatal error"
            if (firstLine != null && firstLine.startsWith("#") && firstLine.toLowerCase().contains("fatal error")) {
                return summarizeHsErrLog(reader, firstLine);
            }

            boolean inExceptionSection = false;
            // Re-check from first line context
            if (firstLine != null && firstLine.startsWith("=== Exception ===")) {
                inExceptionSection = true;
            }
            String line = firstLine;
            // If first line wasn't the exception section header, keep reading
            if (!inExceptionSection) {
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("=== Exception ===")) {
                        inExceptionSection = true;
                        break;
                    }
                }
            }
            if (inExceptionSection) {
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("Thread:")) continue;
                    String trimmed = line.strip();
                    if (!trimmed.isEmpty()) {
                        if (trimmed.length() <= 120) return trimmed;
                        return trimmed.substring(0, 117) + "...";
                    }
                }
            }
        } catch (IOException e) {
            // fall through
        }
        return "Crash report";
    }

    private static String summarizeHsErrLog(BufferedReader reader, String firstLine) throws IOException {
        // Extract signal info from hs_err log
        // Look for lines like "# SIGSEGV (0xb) at pc=..." or "# SIGBUS ..."
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("#") && (line.contains("SIG") || line.contains("EXCEPTION_"))) {
                String signal = line.substring(1).strip();
                if (signal.length() > 100) signal = signal.substring(0, 97) + "...";
                return "Native crash: " + signal;
            }
            // Stop after the header section (lines not starting with #)
            if (!line.startsWith("#")) break;
        }
        return "Native crash: " + firstLine.substring(1).strip();
    }

    // --- Native crash file detection ---

    static List<File> scanForNativeCrashFiles() {
        Set<Path> scannedDirs = new HashSet<>();
        List<File> found = new ArrayList<>();
        long cutoff = System.currentTimeMillis() - CRASH_FILE_MAX_AGE_MS;

        // 1. Working directory
        scanDirectory(Path.of(System.getProperty("user.dir")), cutoff, scannedDirs, found);

        // 2. Temp directory
        scanDirectory(Path.of(System.getProperty("java.io.tmpdir")), cutoff, scannedDirs, found);

        // 3. /tmp — JVM fallback location for hs_err files (java.io.tmpdir may differ, e.g. macOS)
        scanDirectory(Path.of("/tmp"), cutoff, scannedDirs, found);

        // 4. Directory from -XX:ErrorFile JVM arg
        String customPath = getCustomErrorFilePath();
        if (customPath != null) {
            Path parent = Path.of(customPath).getParent();
            if (parent != null) {
                scanDirectory(parent, cutoff, scannedDirs, found);
            }
        }

        // 5. Directory from Eclipse .ini file
        String iniPath = getErrorFileFromIni();
        if (iniPath != null) {
            Path parent = Path.of(iniPath).getParent();
            if (parent != null) {
                scanDirectory(parent, cutoff, scannedDirs, found);
            }
        }

        // Sort by last modified, most recent first
        found.sort((a, b) -> Long.compare(b.lastModified(), a.lastModified()));
        return found;
    }

    private static void scanDirectory(Path dir, long cutoff, Set<Path> scannedDirs, List<File> found) {
        try {
            Path normalized = dir.toRealPath();
            if (!scannedDirs.add(normalized)) return;
            if (!Files.isDirectory(normalized)) return;

            try (DirectoryStream<Path> stream = Files.newDirectoryStream(normalized, "hs_err_pid*.log")) {
                for (Path path : stream) {
                    File file = path.toFile();
                    if (file.isFile() && file.lastModified() >= cutoff) {
                        found.add(file);
                    }
                }
            }
        } catch (Exception e) {
            // Permission errors or other issues — silently skip
        }
    }

    public static void checkPendingNativeCrashesIfNeeded() {
        if (nativeCrashesChecked) return;
        if (pendingNativeCrashes == null) return;
        nativeCrashesChecked = true;

        List<File> crashes = pendingNativeCrashes;
        pendingNativeCrashes = null;

        Display display = Display.getCurrent();
        if (display == null || display.isDisposed()) return;

        display.asyncExec(() -> showNativeCrashDialog(display, crashes));
    }

    private static void showNativeCrashDialog(Display display, List<File> crashFiles) {
        if (crashFiles.isEmpty()) return;

        // Show only the most recent file to avoid annoyance
        File mostRecent = crashFiles.get(0);

        try {
            Config.forceEclipse();
            File eclipseLog = findEclipseLog();
            new CrashDialog(display, mostRecent, eclipseLog, true).open();
        } catch (Throwable t) {
            System.err.println("[CrashReporter] Failed to show native crash dialog: " + t);
        } finally {
            // Move all found crash files to ~/.equo/
            for (File file : crashFiles) {
                moveToEquoDir(file);
            }
        }
    }

    private static void moveToEquoDir(File file) {
        try {
            Path equoDir = Path.of(System.getProperty("user.home"), ".equo");
            Files.createDirectories(equoDir);

            Path target = equoDir.resolve(file.getName());
            if (Files.exists(target)) {
                // Add timestamp suffix if name conflicts
                String name = file.getName();
                int dot = name.lastIndexOf('.');
                String base = dot >= 0 ? name.substring(0, dot) : name;
                String ext = dot >= 0 ? name.substring(dot) : "";
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
                target = equoDir.resolve(base + "-" + timestamp + ext);
            }

            try {
                Files.move(file.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                // Falls back to deleting on move failure (e.g. across drives on Windows)
                try { Files.deleteIfExists(file.toPath()); } catch (IOException ignored) {}
            }
        } catch (IOException e) {
            System.err.println("[CrashReporter] Failed to move crash file: " + e);
        }
    }

    private static String getCustomErrorFilePath() {
        try {
            List<String> args = ManagementFactory.getRuntimeMXBean().getInputArguments();
            for (String arg : args) {
                if (arg.startsWith("-XX:ErrorFile=")) {
                    return arg.substring("-XX:ErrorFile=".length());
                }
            }
        } catch (Exception e) {
            // silently skip
        }
        return null;
    }

    private static String getErrorFileFromIni() {
        try {
            String homeLocation = System.getProperty("eclipse.home.location");
            if (homeLocation == null) return null;

            if (homeLocation.startsWith("file:")) {
                homeLocation = homeLocation.substring(5);
            }

            Path homeDir = Path.of(homeLocation);
            // Find .ini file — typically named after the launcher executable
            File[] iniFiles;
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(homeDir, "*.ini")) {
                List<File> files = new ArrayList<>();
                for (Path p : stream) files.add(p.toFile());
                iniFiles = files.toArray(new File[0]);
            }

            if (iniFiles.length == 0) return null;

            // Parse the first .ini file found
            File iniFile = iniFiles[0];
            boolean afterVmargs = false;
            try (BufferedReader reader = new BufferedReader(new FileReader(iniFile, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.strip();
                    if ("-vmargs".equals(line)) {
                        afterVmargs = true;
                        continue;
                    }
                    if (afterVmargs && line.startsWith("-XX:ErrorFile=")) {
                        return line.substring("-XX:ErrorFile=".length());
                    }
                }
            }
        } catch (Exception e) {
            // silently skip
        }
        return null;
    }
}
