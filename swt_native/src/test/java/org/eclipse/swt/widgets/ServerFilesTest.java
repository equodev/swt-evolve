package org.eclipse.swt.widgets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("native-unit")
class ServerFilesTest {

    @TempDir
    Path tmp;

    @Test
    @DisplayName("a filter extension matches by its wildcards, any of its patterns, without case")
    void matches() {
        assertThat(ServerFiles.matches("a.txt", "*.txt")).isTrue();
        assertThat(ServerFiles.matches("A.TXT", "*.txt")).isTrue();
        assertThat(ServerFiles.matches("a.java", "*.txt;*.java")).isTrue();
        assertThat(ServerFiles.matches("a.java", "*.txt")).isFalse();
        assertThat(ServerFiles.matches("trace.ctf", "trace.c?f")).isTrue();
        assertThat(ServerFiles.matches("noext", "*.*")).isTrue();
        assertThat(ServerFiles.matches("noext", "*")).isTrue();
        assertThat(ServerFiles.matches("noext", null)).isTrue();
        assertThat(ServerFiles.matches("noext", "")).isTrue();
    }

    @Test
    @DisplayName("a listing names the subdirectories and the files the filter matches, sorted")
    void listing() throws IOException {
        Files.createDirectory(tmp.resolve("b"));
        Files.createDirectory(tmp.resolve("A"));
        Files.createFile(tmp.resolve("z.txt"));
        Files.createFile(tmp.resolve("x.TXT"));
        Files.createFile(tmp.resolve("y.java"));

        Map<String, Object> listing = ServerFiles.listing(tmp.toFile(), "*.txt", false);

        assertThat(listing.get("path")).isEqualTo(tmp.toFile().getPath());
        assertThat(listing.get("parent")).isEqualTo(tmp.toFile().getParent());
        assertThat(listing.get("separator")).isEqualTo(File.separator);
        assertThat(names(listing, "dirs")).containsExactly("A", "b");
        assertThat(names(listing, "files")).containsExactly("x.TXT", "z.txt");
        assertThat(names(listing, "roots")).isNotEmpty();
        assertThat(dev.equo.swt.comm.TaggedValues.isPlain(listing)).isTrue();
    }

    @Test
    @DisplayName("a listing leaves out hidden entries: dot-entries and those the file system marks hidden")
    void hiddenEntries() throws IOException {
        Files.createDirectory(tmp.resolve(".config"));
        Files.createDirectory(tmp.resolve("shown"));
        Files.createFile(tmp.resolve(".profile"));
        Files.createFile(tmp.resolve("trace.pcap"));
        Path marked = Files.createFile(tmp.resolve("marked.txt"));
        // Only Windows reads dos:hidden as hidden; Linux stores it as an extended attribute the OS ignores.
        if (System.getProperty("os.name").startsWith("Windows")
                && Files.getFileStore(marked).supportsFileAttributeView("dos")) {
            Files.setAttribute(marked, "dos:hidden", true);
        } else {
            Files.delete(marked);
        }

        Map<String, Object> listing = ServerFiles.listing(tmp.toFile(), null, false);

        assertThat(names(listing, "dirs")).containsExactly("shown");
        assertThat(names(listing, "files")).containsExactly("trace.pcap");
    }

    @Test
    @DisplayName("a directory chooser's listing has no files")
    void directoriesOnly() throws IOException {
        Files.createDirectory(tmp.resolve("d"));
        Files.createFile(tmp.resolve("f.txt"));

        Map<String, Object> listing = ServerFiles.listing(tmp.toFile(), null, true);

        assertThat(names(listing, "dirs")).containsExactly("d");
        assertThat(names(listing, "files")).isEmpty();
    }

    @Test
    @DisplayName("a navigate request with a name makes that folder and shows it")
    void newFolder() {
        char s = ServerFiles.SEPARATOR;

        Map<String, Object> listing = ServerFiles.navigate("0" + s + tmp + s + "made" + s, null, false);

        assertThat(tmp.resolve("made")).isDirectory();
        assertThat(listing.get("path")).isEqualTo(tmp.resolve("made").toFile().getPath());
        assertThat(listing.get("created")).isEqualTo(true);
        assertThat(ServerFiles.navigate("0" + s + tmp + s, null, false)).doesNotContainKey("created");
    }

    @Test
    @DisplayName("a New Folder that exists or isn't one plain name makes nothing and stays in the directory")
    void newFolderRefused() throws IOException {
        Files.createDirectory(tmp.resolve("taken"));
        char s = ServerFiles.SEPARATOR;

        for (String name : new String[] {"taken", "..", " ", "a/b"}) {
            Map<String, Object> listing = ServerFiles.navigate("0" + s + tmp + s + name + s, null, true);
            assertThat(listing.get("path")).as(name).isEqualTo(tmp.toFile().getPath());
            assertThat(listing.get("created")).as(name).isEqualTo(false);
        }
        assertThat(names(ServerFiles.listing(tmp.toFile(), null, true), "dirs")).containsExactly("taken");
    }

    @SuppressWarnings("unchecked")
    private static List<String> names(Map<String, Object> listing, String key) {
        return (List<String>) listing.get(key);
    }

    @Test
    @DisplayName("a chooser starts in the named directory, the one holding a named file, or home")
    void startDirectory() throws IOException {
        Path file = Files.createFile(tmp.resolve("f.txt"));
        File home = new File(System.getProperty("user.home")).getAbsoluteFile();

        assertThat(ServerFiles.startDirectory(tmp.toString())).isEqualTo(tmp.toFile().getAbsoluteFile());
        assertThat(ServerFiles.startDirectory(file.toString())).isEqualTo(tmp.toFile().getAbsoluteFile());
        assertThat(ServerFiles.startDirectory(tmp.resolve("gone/deeper").toString())).isEqualTo(tmp.toFile().getAbsoluteFile());
        assertThat(ServerFiles.startDirectory(null)).isEqualTo(home);
        assertThat(ServerFiles.startDirectory(" ")).isEqualTo(home);
    }

    @Test
    @DisplayName("an answer reads as filter index, directory and names; an empty one is a cancel")
    void choice() {
        String dir = tmp.toString();
        char s = ServerFiles.SEPARATOR;

        ServerFiles.Choice multi = ServerFiles.choice("1" + s + dir + s + "a.txt" + s + "b.txt" + s);
        assertThat(multi.filterIndex).isEqualTo(1);
        assertThat(multi.directory).isEqualTo(dir);
        assertThat(multi.names).containsExactly("a.txt", "b.txt");
        assertThat(multi.fullPath()).isEqualTo(new File(dir, "a.txt").getPath());

        ServerFiles.Choice directory = ServerFiles.choice("0" + s + dir + s);
        assertThat(directory.names).isEmpty();
        assertThat(directory.fullPath()).isEqualTo(dir);

        String typed = tmp.resolve("elsewhere/new.txt").toString();
        assertThat(ServerFiles.choice("0" + s + dir + s + typed + s).fullPath()).isEqualTo(typed);

        assertThat(ServerFiles.choice(null)).isNull();
        assertThat(ServerFiles.choice("")).isNull();
        assertThat(ServerFiles.choice("0")).isNull();
    }
}
