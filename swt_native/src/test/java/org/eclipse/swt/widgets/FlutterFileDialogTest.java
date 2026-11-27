package org.eclipse.swt.widgets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.eclipse.swt.SWT;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import dev.equo.swt.WireTable;

/**
 * What FileDialog and DirectoryDialog answer when the Flutter chooser draws the server's file system:
 * the server path the user picked, with the getters set from the choice, and null for a cancel.
 */
@Tag("native-unit")
class FlutterFileDialogTest extends DialogBridgeTestBase {

    @TempDir
    Path dir;

    /** The listing Java sent back for the chooser's directory request. */
    private Object sentListing;

    private static String answer(int filterIndex, String directory, String... names) {
        StringBuilder b = new StringBuilder().append(filterIndex).append(ServerFiles.SEPARATOR)
                .append(directory).append(ServerFiles.SEPARATOR);
        for (String name : names) b.append(name).append(ServerFiles.SEPARATOR);
        return b.toString();
    }

    /**
     * A client that asks once for {@code browse} with {@code filterIndex}, then closes the chooser with
     * {@code closePayload}.
     */
    private Shell shellChoosing(int filterIndex, String browse, String closePayload) {
        return shellAsking(answer(filterIndex, browse), closePayload);
    }

    /** A client that sends {@code navigateRequest} once, then closes the chooser with {@code closePayload}. */
    private Shell shellAsking(String navigateRequest, String closePayload) {
        Shell shell = DartMocks.dartShell();
        DisplayBridge bridge = mock(DisplayBridge.class);
        doAnswer(invocation -> {
            Consumer<String> handler = invocation.getArgument(3);
            handler.accept(closePayload);
            return null;
        }).when(bridge).onChannel(anyInt(), anyLong(), eq(WireTable.CHANNEL_CLOSE), any());
        doAnswer(invocation -> {
            Consumer<String> handler = invocation.getArgument(3);
            handler.accept(navigateRequest);
            return null;
        }).when(bridge).onChannel(anyInt(), anyLong(), eq(WireTable.CHANNEL_NAVIGATE_REQUEST), any());
        doAnswer(invocation -> {
            sentListing = invocation.getArgument(3);
            return null;
        }).when(bridge).sendOnChannel(anyInt(), anyLong(), eq(WireTable.CHANNEL_NAVIGATE), any());
        installBridge(shell.getDisplay(), bridge);
        return shell;
    }

    private void someFiles() throws IOException {
        Files.createFile(dir.resolve("a.txt"));
        Files.createFile(dir.resolve("b.txt"));
        Files.createFile(dir.resolve("c.log"));
        Files.createDirectory(dir.resolve("sub"));
    }

    @Test
    void openAnswersTheServerPathPickedAndSetsTheGetters() throws IOException {
        someFiles();
        String directory = dir.toString();
        Shell shell = shellChoosing(1, directory, answer(1, directory, "a.txt", "b.txt"));
        DartFileDialog dialog = new DartFileDialog(shell, SWT.OPEN | SWT.MULTI, null);
        dialog.setFilterExtensions(new String[] {"*.*", "*.txt"});

        assertThat(dialog.open()).isEqualTo(new File(directory, "a.txt").getPath());
        assertThat(dialog.getFileNames()).containsExactly("a.txt", "b.txt");
        assertThat(dialog.getFileName()).isEqualTo("a.txt");
        assertThat(dialog.getFilterPath()).isEqualTo(directory);
        assertThat(dialog.getFilterIndex()).isEqualTo(1);
    }

    @Test
    void aChoiceWithNoFilterAndNoNameKeepsTheFilterIndexAndFileName() throws IOException {
        someFiles();
        String directory = dir.toString();
        Shell shell = shellChoosing(-1, directory, answer(-1, directory));
        DartFileDialog dialog = new DartFileDialog(shell, SWT.OPEN, null);
        dialog.setFilterIndex(2);
        dialog.setFileName("start.txt");

        assertThat(dialog.open()).isEqualTo(directory);
        assertThat(dialog.getFilterIndex()).isEqualTo(2);
        assertThat(dialog.getFileName()).isEqualTo("start.txt");
        assertThat(dialog.getFileNames()).isEmpty();
        assertThat(dialog.getFilterPath()).isEqualTo(directory);
    }

    @Test
    void theChooserIsListedTheDirectoryItAsksForThroughTheFilterItUses() throws IOException {
        someFiles();
        String directory = dir.toString();
        Shell shell = shellChoosing(1, directory, "");
        DartFileDialog dialog = new DartFileDialog(shell, SWT.OPEN, null);
        dialog.setFilterExtensions(new String[] {"*.*", "*.txt"});

        dialog.open();

        assertThat(sentListing).isInstanceOf(Map.class);
        Map<?, ?> listing = (Map<?, ?>) sentListing;
        assertThat(listing.get("path")).isEqualTo(directory);
        assertThat(listing.get("files")).isEqualTo(List.of("a.txt", "b.txt"));
        assertThat(listing.get("dirs")).isEqualTo(List.of("sub"));
    }

    @Test
    void aCancelAnswersNullAndLeavesTheState() {
        Shell shell = shellChoosing(0, dir.toString(), "");
        DartFileDialog dialog = new DartFileDialog(shell, SWT.SAVE, null);
        dialog.setFileName("start.txt");
        dialog.setFilterPath("/start");

        assertThat(dialog.open()).isNull();
        assertThat(dialog.getFileName()).isEqualTo("start.txt");
        assertThat(dialog.getFilterPath()).isEqualTo("/start");
    }

    @Test
    void aDirectoryDialogAnswersTheDirectoryPickedAsItsFilterPath() throws IOException {
        someFiles();
        String directory = dir.resolve("sub").toString();
        Shell shell = shellChoosing(-1, dir.toString(), answer(-1, directory));
        DartDirectoryDialog dialog = new DartDirectoryDialog(shell, SWT.NONE, null);

        assertThat(dialog.open()).isEqualTo(directory);
        assertThat(dialog.getFilterPath()).isEqualTo(directory);
        Map<?, ?> listing = (Map<?, ?>) sentListing;
        assertThat(listing.get("files")).isEqualTo(List.of());
        assertThat(listing.get("dirs")).isEqualTo(List.of("sub"));
    }

    @Test
    void newFolderMakesTheFolderOnTheServerAndTheChooserIsListedIt() throws IOException {
        someFiles();
        String made = dir.resolve("made").toString();
        Shell shell = shellAsking(answer(0, dir.toString(), "made"), answer(0, made));
        DartDirectoryDialog dialog = new DartDirectoryDialog(shell, SWT.NONE, null);

        assertThat(dialog.open()).isEqualTo(made);
        assertThat(dir.resolve("made")).isDirectory();
        Map<?, ?> listing = (Map<?, ?>) sentListing;
        assertThat(listing.get("path")).isEqualTo(made);
        assertThat(listing.get("created")).isEqualTo(true);
    }

    @Test
    void aCancelledDirectoryDialogAnswersNull() {
        Shell shell = shellChoosing(-1, dir.toString(), "");
        DartDirectoryDialog dialog = new DartDirectoryDialog(shell, SWT.NONE, null);
        dialog.setFilterPath("/start");

        assertThat(dialog.open()).isNull();
        assertThat(dialog.getFilterPath()).isEqualTo("/start");
    }
}
