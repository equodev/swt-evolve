package dev.equo.swt;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Manages the extraction and loading of native Flutter libraries.
 * Libraries are extracted to a subdirectory within the user's home directory.
 */
public class FlutterLibraryLoader {

    private static final String SEPARATOR = File.separator;
    private static final String USER_HOME = System.getProperty("user.home");

    // OS and Architecture Constants
    private static final String OS_LINUX = "linux";
    private static final String OS_MACOSX = "macosx";
    private static final String OS_WINDOWS_PREFIX = "Win";
    private static final String ARCH_AMD64 = "amd64";
    private static final String ARCH_X86_64 = "x86_64";

    // Directory and File Name Constants
    private static final String EQUO_BASE_DIR_NAME = ".equo";
    private static final String LIB_SUB_DIR_NAME = "lib";
    private static final String SWT_DIR_NAME = "swt";
    private static final String MACOS_LIB_NAME = "libFlutterBridge.dylib";
    public static final String CONTENTS = "Contents";
    public static final String SWTFLUTTER_APP = "swtflutter.app";
    public static final String SWTFLUTTER_APP_CONTENTS = "macos/Build/Products/Release/" + SWTFLUTTER_APP + SEPARATOR+ CONTENTS;
    private static final String LINUX_RUNNER_DIR_NAME = "runner";
    private static final String LINUX_BUNDLE_DIR_NAME = "bundle";
    private static final String LINUX_LIB_NAME = "libflutter_library.so";
    public static final String LINUX_X_64_RELEASE = "linux/x64/release";

    // Permissions
    private static final String EXECUTE_PERMISSION = "755";
    private static final String EQUO_LIB_PATH_SUFFIX =
            EQUO_BASE_DIR_NAME + SEPARATOR + SWT_DIR_NAME + SEPARATOR + LIB_SUB_DIR_NAME + SEPARATOR + getOS() + SEPARATOR + getArch();

    /**
     * Initializes the Flutter library loader.
     * This method is synchronized and ensures that extraction and loading
     * occur only once.
     *
     * @throws LibraryLoaderException if initialization fails.
     */
    public static void initialize() {
        try {
            extractAndLoadFlutterLibraries();
        } catch (Exception e) {
            throw new LibraryLoaderException("Failed to initialize Flutter libraries", e);
        }
    }

    /**
     * Determines the operating system type.
     *
     * @return A string identifier for the OS (e.g., "linux", "macosx", "win32").
     */
    private static String getOS() {
        String osName = System.getProperty("os.name");
        if (osName.equalsIgnoreCase(OS_LINUX)) return OS_LINUX;
        if (osName.equalsIgnoreCase("Mac OS X")) return OS_MACOSX; // Common variation
        if (osName.startsWith(OS_WINDOWS_PREFIX)) return "win32"; // Standardize to "win32"
        return osName.toLowerCase().replaceAll("\\s+", "");
    }

    /**
     * Determines the system architecture.
     *
     * @return A string identifier for the architecture (e.g., "x86_64").
     */
    private static String getArch() {
        String osArch = System.getProperty("os.arch");
        if (osArch.equalsIgnoreCase(ARCH_AMD64)) return ARCH_X86_64;
        return osArch;
    }

    /**
     * Determines if we're running in development mode.
     * Uses multiple heuristics to detect development environment.
     *
     * @return true if in development mode, false if in production
     */
    private static boolean isDevelopmentMode() {
        return findFlutterBuildDirectory() != null;
    }

    /**
     * Creates the specified directory if it does not already exist.
     *
     * @param directory The directory to create.
     * @throws IOException if the directory cannot be created.
     */
    private static void ensureDirectoryExists(File directory) throws IOException {
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                throw new IOException("Failed to create directory: " + directory.getAbsolutePath());
            }
        }
    }

    /**
     * Main method to orchestrate library extraction and loading based on OS.
     *
     * @throws LibraryLoaderException if extraction or loading fails.
     */
    private static void extractAndLoadFlutterLibraries() {
        String os = getOS();
        File equoLibDir = new File(USER_HOME, EQUO_LIB_PATH_SUFFIX);

        try {
            ensureDirectoryExists(equoLibDir);
            // Check if we're running from JAR or development environment
            boolean isDevelopmentMode = isDevelopmentMode();
            System.out.println("Running in development: " + isDevelopmentMode);

            if (OS_MACOSX.equals(os)) {
                extractAndLoadMacOSLibraries(equoLibDir, isDevelopmentMode);
            } else if (OS_LINUX.equals(os)) {
                extractAndLoadLinuxLibraries(equoLibDir, isDevelopmentMode);
            } else {
                throw new UnsupportedOperationException("Unsupported OS: " + os + ". Equo SWT currently supports macOS and Linux.");
            }
        } catch (IOException e) {
            throw new LibraryLoaderException("IO failure during Flutter library extraction", e);
        } catch (UnsupportedOperationException e) {
            throw new LibraryLoaderException(e.getMessage(), e);
        } catch (Exception e) {
            throw new LibraryLoaderException("Failed to extract and load Flutter libraries", e);
        }
    }

    /**
     * Extracts and loads libraries specific to macOS.
     *
     * @param targetDir         The directory where libraries should be extracted.
     * @param isDevelopmentMode true if is development env, false otherwise
     * @throws IOException            if extraction fails.
     * @throws LibraryLoaderException if loading fails.
     */
    private static void extractAndLoadMacOSLibraries(File targetDir, boolean isDevelopmentMode) throws IOException {
        if (!isDevelopmentMode) {
            extractDirectoryFromJar(SWTFLUTTER_APP, targetDir);
            File libFile = new File(targetDir, SWTFLUTTER_APP + SEPARATOR + CONTENTS + SEPARATOR + MACOS_LIB_NAME);
            loadLibrary(libFile.getAbsolutePath());
        } else {
            loadOSLibraries(SWTFLUTTER_APP_CONTENTS, MACOS_LIB_NAME);
        }
    }

    /**
     * Extracts and loads libraries specific to Linux.
     *
     * @param targetDir         The directory where libraries should be extracted.
     * @param isDevelopmentMode true if is development env, false otherwise
     * @throws IOException            if extraction fails.
     * @throws LibraryLoaderException if loading fails.
     */
    private static void extractAndLoadLinuxLibraries(File targetDir, boolean isDevelopmentMode) throws IOException {
        if (!isDevelopmentMode) {
            extractDirectoryFromJar(LINUX_RUNNER_DIR_NAME, targetDir);
            extractDirectoryFromJar(LINUX_BUNDLE_DIR_NAME, targetDir);

            File libFile = new File(targetDir, LINUX_RUNNER_DIR_NAME + SEPARATOR + LINUX_LIB_NAME);
            if (libFile.exists()) {
                setExecutablePermission(libFile.getAbsolutePath());
                loadLibrary(libFile.getAbsolutePath());
            } else {
                throw new LibraryLoaderException("Essential Linux library not found after extraction: " + libFile.getAbsolutePath());
            }
        } else {
            loadOSLibraries(LINUX_X_64_RELEASE, LINUX_RUNNER_DIR_NAME + SEPARATOR + LINUX_LIB_NAME);
        }
    }

    /**
     * Loads macOS libraries directly from file system (development mode).
     *
     * @throws IOException if library loading fails.
     */

    private static void loadOSLibraries(String libDirectoryPath, String libName) throws IOException {
        System.out.println("Development mode: loading Flutter libraries directly from build directory");
        // Find the Flutter build directory
        File flutterBuildDir = findFlutterBuildDirectory();
        if (flutterBuildDir == null) {
            throw new IOException("Flutter build directory not found. Please build the Flutter app first.");
        }

        File releaseDir = findReleaseDirectory(flutterBuildDir, libDirectoryPath);
        File libFile = new File(releaseDir, libName);
        if (libFile.exists()) {
            setExecutablePermission(libFile.getAbsolutePath());
            loadLibrary(libFile.getAbsolutePath());
        } else {
            throw new IOException("Essential library not found at: " + libFile.getAbsolutePath());
        }
    }

    /**
     * Finds the Flutter build directory by looking for it relative to the current working directory.
     *
     * @return The Flutter build directory or null if not found.
     */
    private static File findFlutterBuildDirectory() {
        // Try common locations relative to current working directory
        String[] possiblePaths = {
                "flutter-lib/build",
                "../flutter-lib/build",
                "../../flutter-lib/build"
        };

        for (String path : possiblePaths) {
            File buildDir = new File(path);
            if (buildDir.exists() && buildDir.isDirectory()) {
                System.out.println("Found Flutter build directory at: " + buildDir.getAbsolutePath());
                return buildDir;
            }
        }
        return null;
    }

    /**
     * Finds and validates the release directory for the current OS.
     *
     * @param flutterBuildDir The Flutter build directory.
     * @param relativeReleasePath The relative path to the release directory.
     * @return The validated release directory.
     * @throws IOException if the release directory is not found.
     */
    private static File findReleaseDirectory(File flutterBuildDir, String relativeReleasePath) throws IOException {
        File releaseDir = new File(flutterBuildDir, relativeReleasePath);
        if (!releaseDir.exists()) {
            throw new IOException("Flutter Release build not found at: " + releaseDir.getAbsolutePath());
        }
        return releaseDir;
    }

    /**
     * Extracts a directory and its contents from the JAR file to a target directory.
     *
     * @param directoryPathInJar The path of the directory within the JAR (e.g., "frameworks/App.framework").
     * @param targetBaseDir      The base directory into which the JAR directory's contents will be extracted.
     *                           The directory structure from the JAR will be preserved under this base.
     * @throws IOException if an I/O error occurs during extraction or if the code source is not a JAR.
     */
    private static void extractDirectoryFromJar(String directoryPathInJar, File targetBaseDir) throws IOException {
        URL classUrl = FlutterLibraryLoader.class.getProtectionDomain().getCodeSource().getLocation();
        if (classUrl == null) {
            throw new IOException("Cannot determine JAR location to extract directory: " + directoryPathInJar);
        }

        File jarFileSource;
        try {
            jarFileSource = Paths.get(classUrl.toURI()).toFile();
        } catch (URISyntaxException e) {
            throw new IOException("Invalid JAR URL syntax: " + classUrl, e);
        }

        System.out.println("Extracting directory '" + directoryPathInJar + "' from JAR " + jarFileSource.getAbsolutePath() + " to " + targetBaseDir.getAbsolutePath());

        try (JarFile jarFile = new JarFile(jarFileSource)) {
            Enumeration<JarEntry> entries = jarFile.entries();
            String normalizedDirectoryPathInJar = directoryPathInJar.endsWith("/") ? directoryPathInJar : directoryPathInJar + "/";

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();

                if (entryName.startsWith(normalizedDirectoryPathInJar)) {
                    // Remove the leading directoryPathInJar to get the relative path
                    String relativePath = entryName.substring(normalizedDirectoryPathInJar.length());
                    if (relativePath.isEmpty() && !entry.isDirectory()) {
                        if (!entryName.equals(directoryPathInJar) && !entryName.equals(normalizedDirectoryPathInJar.substring(0, normalizedDirectoryPathInJar.length() - 1))) {
                        }
                    }

                    File targetFile = new File(targetBaseDir, entryName);

                    if (entry.isDirectory()) {
                        ensureDirectoryExists(targetFile);
                    } else {
                        ensureDirectoryExists(targetFile.getParentFile());
                        try (InputStream inputStream = jarFile.getInputStream(entry)) {
                            Files.copy(inputStream, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                            System.out.println("Extracted file " + entryName + " to " + targetFile.getAbsolutePath());
                        } catch (IOException e) {
                            System.out.println("Failed to extract file " + entryName + " from JAR: " + e.getMessage() + " - Exception: " + e);
                            // throw new IOException("Failed to extract file " + entryName + " from JAR", e);
                        }
                    }
                }
            }
            System.out.println("Successfully extracted directory structure for '" + directoryPathInJar + "'");
        } catch (IOException e) {
            throw new IOException("Failed to extract directory " + directoryPathInJar + " from JAR: " + jarFileSource.getAbsolutePath(), e);
        }
    }


    /**
     * Sets execute permission on the given file path.
     * No-op on Windows.
     *
     * @param path The file path.
     */
    private static void setExecutablePermission(String path) {
        if (OS_WINDOWS_PREFIX.equals(getOS())) {
            return;
        }
        File file = new File(path);
        if (!file.exists()) {
            return;
        }

        // First, try Java's built-in method (Java 7+)
        if (file.canExecute() || (file.setExecutable(true, false) && file.canExecute())) {
            return;
        }

        // Fallback to chmod command for systems where setExecutable might not work as expected
        // (e.g. for owner-only vs all users, or if more specific permissions are needed)
        try {
            Process chmodProcess = Runtime.getRuntime().exec(new String[]{"chmod", EXECUTE_PERMISSION, path});
            int exitCode = chmodProcess.waitFor();
            if (exitCode == 0 && file.canExecute()) {
                System.out.println("Execute permission set via chmod for: " + path);
            } else {
                System.out.println("chmod command for " + path + " exited with code " + exitCode + " or file is still not executable. Output: " + new String(chmodProcess.getInputStream().readAllBytes()) + ", Error: " + new String(chmodProcess.getErrorStream().readAllBytes()));
                // As a last resort, try again with a simpler setExecutable if chmod failed
                if (!file.canExecute() && file.setExecutable(true)) {
                    System.out.println("Execute permission set via second attempt of setExecutable(true) for: " + path);
                } else if (!file.canExecute()) {
                    System.out.println("Failed to set execute permission for: " + path);
                }
            }
        } catch (IOException | InterruptedException e) {
            // Last attempt with Java's method if chmod fails
            if (!file.canExecute() && file.setExecutable(true)) {
                System.out.println("Execute permission set via fallback setExecutable(true) for: " + path);
            } else if (!file.canExecute()) {
                System.out.println("All attempts to set execute permission for " + path + " failed. Exception: " + e);
            }
        }
    }

    /**
     * Loads a native library from the given absolute path.
     *
     * @param absoluteLibPath The absolute path to the native library.
     * @throws LibraryLoaderException if loading the library fails.
     */
    private static void loadLibrary(String absoluteLibPath) {
        try {
            System.load(absoluteLibPath);
            System.out.println("Successfully loaded library: " + absoluteLibPath);
        } catch (UnsatisfiedLinkError e) {
            String errorMessage = String.format("Failed to load library %s. Architecture: %s. OS: %s. Error: %s",
                    absoluteLibPath, System.getProperty("os.arch"), System.getProperty("os.name"), e.getMessage());
            File libFile = new File(absoluteLibPath);
            if (System.getProperty("java.vendor").toLowerCase().contains("ibm") && System.getProperty("os.name").toLowerCase().contains("aix")) {
                System.out.println("On AIX with IBM JDK, ensure LIBPATH is correctly set or that the library dependencies are met.");
            }
            throw new LibraryLoaderException(errorMessage, e);
        } catch (Throwable e) {
            String errorMessage = String.format("An unexpected error occurred while loading library %s: %s", absoluteLibPath, e.getMessage());
            throw new LibraryLoaderException(errorMessage, e);
        }
    }

    /**
     * Custom exception for library loading and extraction failures.
     */
    static class LibraryLoaderException extends RuntimeException {
        public LibraryLoaderException(String message) {
            super(message);
        }

        public LibraryLoaderException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
