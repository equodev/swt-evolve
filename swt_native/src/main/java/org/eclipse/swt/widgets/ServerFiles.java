package org.eclipse.swt.widgets;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The file system a FileDialog or DirectoryDialog drawn in the browser browses: the one of the JVM
 * running the application, since that is where the path open() answers is used.
 */
final class ServerFiles {

    /** Separates the lines of a chooser's answer: a file name cannot hold it on any OS. */
    static final char SEPARATOR = '\0';

    private ServerFiles() {
    }

    /**
     * True when {@code name} matches one of the {@code ;}-separated patterns of a filter extension, as
     * {@code *.txt;*.java}. Only {@code *} and {@code ?} are wildcards, compared without case; no filter
     * or an empty one matches every name.
     */
    static boolean matches(String name, String filterExtension) {
        if (filterExtension == null || filterExtension.trim().isEmpty()) return true;
        String lower = name.toLowerCase(Locale.ROOT);
        for (String pattern : filterExtension.split(";")) {
            String p = pattern.trim().toLowerCase(Locale.ROOT);
            if (p.isEmpty()) continue;
            if (p.equals("*.*") || glob(lower, 0, p, 0)) return true;
        }
        return false;
    }

    private static boolean glob(String s, int si, String p, int pi) {
        while (pi < p.length()) {
            char c = p.charAt(pi);
            if (c == '*') {
                for (int k = si; k <= s.length(); k++) {
                    if (glob(s, k, p, pi + 1)) return true;
                }
                return false;
            }
            if (si >= s.length() || (c != '?' && c != s.charAt(si))) return false;
            si++;
            pi++;
        }
        return si == s.length();
    }

    /**
     * The directory a chooser shows first: {@code requested} when it names one, else the user's home
     * directory.
     */
    static File startDirectory(String requested) {
        if (requested != null && !requested.trim().isEmpty()) {
            File dir = new File(requested).getAbsoluteFile();
            while (dir != null && !dir.isDirectory()) dir = dir.getParentFile();
            if (dir != null) return dir;
        }
        return new File(System.getProperty("user.home")).getAbsoluteFile();
    }

    /**
     * What the chooser shows of {@code dir}: its path, its parent ({@code ""} at a root), the roots
     * of the file system, its subdirectories and, unless {@code directoriesOnly}, the files matching
     * {@code filterExtension}, each list sorted without case. Hidden entries are left out, as the
     * platform panels do. An unreadable directory lists nothing.
     */
    static Map<String, Object> listing(File dir, String filterExtension, boolean directoriesOnly) {
        List<String> dirs = new ArrayList<>();
        List<String> files = new ArrayList<>();
        File[] children = dir.listFiles();
        if (children != null) {
            for (File child : children) {
                if (hidden(child)) continue;
                if (child.isDirectory()) dirs.add(child.getName());
                else if (!directoriesOnly && matches(child.getName(), filterExtension)) files.add(child.getName());
            }
        }
        dirs.sort(String.CASE_INSENSITIVE_ORDER);
        files.sort(String.CASE_INSENSITIVE_ORDER);
        List<String> roots = new ArrayList<>();
        File[] listed = File.listRoots();
        if (listed != null) {
            for (File root : listed) roots.add(root.getPath());
        }
        File parent = dir.getParentFile();
        Map<String, Object> listing = new LinkedHashMap<>();
        listing.put("path", dir.getPath());
        listing.put("parent", parent == null ? "" : parent.getPath());
        listing.put("separator", File.separator);
        listing.put("roots", roots);
        listing.put("dirs", dirs);
        listing.put("files", files);
        return listing;
    }

    /**
     * The listing a chooser asks for while it is open. Its request has the answer's shape with no names:
     * the filter index, whose extension filters the files, and the directory to show, resolved as
     * {@link #startDirectory} resolves it. A request with a name is the chooser's New Folder: the folder
     * is made in that directory and shown; when it can't be, the directory is shown with
     * {@code created} false.
     */
    static Map<String, Object> navigate(String request, String[] filterExtensions, boolean directoriesOnly) {
        Choice asked = choice(request);
        String directory = asked == null ? null : asked.directory;
        int index = asked == null ? -1 : asked.filterIndex;
        String extension = filterExtensions != null && index >= 0 && index < filterExtensions.length
                ? filterExtensions[index] : null;
        File dir = startDirectory(directory);
        if (asked == null || asked.names.length == 0) return listing(dir, extension, directoriesOnly);
        File created = newFolder(dir, asked.names[0]);
        Map<String, Object> listing = listing(created == null ? dir : created, extension, directoriesOnly);
        listing.put("created", created != null);
        return listing;
    }

    /** A dot-entry, or one the file system marks hidden (the hidden attribute on Windows). */
    static boolean hidden(File file) {
        return file.getName().startsWith(".") || file.isHidden();
    }

    /** Makes the folder {@code name} in {@code dir}; null when the name isn't one plain name or it can't be made. */
    static File newFolder(File dir, String name) {
        String trimmed = name == null ? "" : name.trim();
        if (trimmed.isEmpty() || trimmed.equals(".") || trimmed.equals("..")
                || trimmed.indexOf('/') >= 0 || trimmed.indexOf(File.separatorChar) >= 0) return null;
        File folder = new File(dir, trimmed);
        return folder.mkdir() ? folder : null;
    }

    /**
     * What the user chose, read from the chooser's answer: the filter index, the directory, then the
     * chosen names, each ended by {@link #SEPARATOR}. Null or empty is a cancel.
     */
    static Choice choice(String answer) {
        if (answer == null || answer.isEmpty()) return null;
        List<String> parts = new ArrayList<>(Arrays.asList(answer.split(String.valueOf(SEPARATOR), -1)));
        if (!parts.isEmpty() && parts.get(parts.size() - 1).isEmpty()) parts.remove(parts.size() - 1);
        if (parts.size() < 2) return null;
        int filterIndex;
        try {
            filterIndex = Integer.parseInt(parts.get(0).trim());
        } catch (NumberFormatException e) {
            filterIndex = -1;
        }
        return new Choice(filterIndex, parts.get(1), parts.subList(2, parts.size()).toArray(new String[0]));
    }

    /** A chooser's answer: the directory it ended in and the names picked there. */
    static final class Choice {
        final int filterIndex;
        final String directory;
        final String[] names;

        Choice(int filterIndex, String directory, String[] names) {
            this.filterIndex = filterIndex;
            this.directory = directory;
            this.names = names;
        }

        /** The first name's path; a typed name that is already absolute stands as it is. */
        String fullPath() {
            if (names.length == 0) return directory;
            File named = new File(names[0]);
            return named.isAbsolute() ? named.getPath() : new File(directory, names[0]).getPath();
        }
    }
}
