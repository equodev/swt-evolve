package org.eclipse.swt.dnd;

import org.eclipse.swt.SWTException;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The clipboards of the Flutter backends, the same on every OS: a datum is converted when it is read, only the
 * clipboard that wrote the data clears it, the selection is a clipboard of its own, and a clipboard offers the types
 * set and no others.
 */
class ClipboardHelperTest {

    private final Object writer = new Object(), other = new Object();
    private final ClipboardHelper boards = new ClipboardHelper();

    private String set(Object owner, Object datum, Transfer transfer, int clipboards) {
        return boards.setContents(owner, new Object[] {datum}, new Transfer[] {transfer}, clipboards);
    }

    private Object get(Transfer transfer) {
        return boards.getContents(transfer, DND.CLIPBOARD);
    }

    /** The type names the transfers offer, in order: they are platform specific, and differ between releases. */
    private static String[] names(Transfer... transfers) {
        return Arrays.stream(transfers).flatMap(t -> Arrays.stream(t.getTypeNames())).toArray(String[]::new);
    }

    private static boolean supports(Transfer transfer, TransferData[] types) {
        return Arrays.stream(types).anyMatch(transfer::isSupportedType);
    }

    private static ByteArrayTransfer bytesNamed(String name) {
        return new ByteArrayTransfer() {
            @Override
            protected int[] getTypeIds() {
                return new int[] {registerType(name)};
            }

            @Override
            protected String[] getTypeNames() {
                return new String[] {name};
            }
        };
    }

    @Test
    void whatIsSetIsRead() {
        boards.setContents(writer, new Object[] {"text", "<b>html</b>"},
                new Transfer[] {TextTransfer.getInstance(), HTMLTransfer.getInstance()}, DND.CLIPBOARD);
        assertThat(get(TextTransfer.getInstance())).isEqualTo("text");
        assertThat(get(HTMLTransfer.getInstance())).isEqualTo("<b>html</b>");
        assertThat(get(RTFTransfer.getInstance())).isNull();
    }

    @Test
    void aDatumIsConvertedWhenItIsRead() {
        String[] files = {"/a"};
        set(writer, files, FileTransfer.getInstance(), DND.CLIPBOARD);
        files[0] = "/b";
        assertThat((String[]) get(FileTransfer.getInstance())).containsExactly("/b");
    }

    @Test
    void aDatumTheTransferCannotConvertFailsWhenItIsRead() {
        Transfer bytes = bytesNamed("org.eclipse.swt.test.bytes");
        set(writer, "not bytes", bytes, DND.CLIPBOARD);
        assertThatThrownBy(() -> get(bytes)).isInstanceOfSatisfying(SWTException.class, e -> assertThat(e.code).isEqualTo(DND.ERROR_INVALID_DATA));
    }

    @Test
    void theTextOfAWriteIsWhatTheSystemClipboardHolds() {
        assertThat(boards.setContents(writer, new Object[] {"{\\rtf1 r}", "plain", "second"},
                new Transfer[] {RTFTransfer.getInstance(), TextTransfer.getInstance(), TextTransfer.getInstance()}, DND.CLIPBOARD))
                .isEqualTo("plain");
        assertThat(set(writer, "{\\rtf1 r}", RTFTransfer.getInstance(), DND.CLIPBOARD)).as("no text empties it").isEmpty();
        assertThat(set(writer, "selection", TextTransfer.getInstance(), DND.SELECTION_CLIPBOARD)).as("the selection is not it").isNull();
        assertThat(set(writer, "nowhere", TextTransfer.getInstance(), 0)).isNull();
    }

    @Test
    void theSelectionIsAClipboardOfItsOwn() {
        set(writer, "clipboard", TextTransfer.getInstance(), DND.CLIPBOARD);
        set(writer, "selection", TextTransfer.getInstance(), DND.SELECTION_CLIPBOARD);
        assertThat(get(TextTransfer.getInstance())).isEqualTo("clipboard");
        assertThat(boards.getContents(TextTransfer.getInstance(), DND.SELECTION_CLIPBOARD)).isEqualTo("selection");
        assertThat(boards.getContents(TextTransfer.getInstance(), DND.CLIPBOARD | DND.SELECTION_CLIPBOARD)).isEqualTo("clipboard");
        assertThat(boards.getAvailableTypes(DND.CLIPBOARD | DND.SELECTION_CLIPBOARD)).hasSize(2);
        assertThat(boards.getAvailableTypeNames()).as("the system clipboard's").containsExactly(names(TextTransfer.getInstance()));
        set(writer, "{\\rtf1 both}", RTFTransfer.getInstance(), DND.CLIPBOARD | DND.SELECTION_CLIPBOARD);
        assertThat(boards.getContents(RTFTransfer.getInstance(), DND.SELECTION_CLIPBOARD)).isEqualTo("{\\rtf1 both}");
        assertThat(boards.getContents(TextTransfer.getInstance(), DND.SELECTION_CLIPBOARD)).isNull();
    }

    @Test
    void theAvailableTypesAreTheTypeNamesInOrder() {
        boards.setContents(writer, new Object[] {"https://www.eclipse.org", "text"},
                new Transfer[] {URLTransfer.getInstance(), TextTransfer.getInstance()}, DND.CLIPBOARD);
        assertThat(boards.getAvailableTypeNames()).containsExactly(names(URLTransfer.getInstance(), TextTransfer.getInstance()));
        TransferData[] types = boards.getAvailableTypes(DND.CLIPBOARD);
        assertThat(types).hasSize(names(URLTransfer.getInstance(), TextTransfer.getInstance()).length);
        assertThat(supports(URLTransfer.getInstance(), types)).isTrue();
        assertThat(supports(TextTransfer.getInstance(), types)).isTrue();
        assertThat(supports(FileTransfer.getInstance(), types)).isFalse();
        assertThat(boards.getAvailableTypes(0)).isEmpty();
    }

    @Test
    void onlyTheWriterClears() {
        set(writer, "text", TextTransfer.getInstance(), DND.CLIPBOARD);
        assertThat(boards.clearContents(other, DND.CLIPBOARD)).isNull();
        assertThat(get(TextTransfer.getInstance())).isEqualTo("text");
        assertThat(boards.clearContents(writer, DND.CLIPBOARD)).isEmpty();
        assertThat(get(TextTransfer.getInstance())).isNull();
        assertThat(boards.clearContents(writer, DND.CLIPBOARD)).as("nothing to clear").isNull();
    }

    @Test
    void clearingTheSelectionLeavesTheClipboard() {
        set(writer, "both", TextTransfer.getInstance(), DND.CLIPBOARD | DND.SELECTION_CLIPBOARD);
        assertThat(boards.clearContents(writer, DND.SELECTION_CLIPBOARD)).isNull();
        assertThat(boards.getContents(TextTransfer.getInstance(), DND.SELECTION_CLIPBOARD)).isNull();
        assertThat(get(TextTransfer.getInstance())).isEqualTo("both");
    }

    @Test
    void textAnotherApplicationCopiedReplacesTheClipboard() {
        boards.setContents(writer, new Object[] {"mine", "{\\rtf1 mine}"},
                new Transfer[] {TextTransfer.getInstance(), RTFTransfer.getInstance()}, DND.CLIPBOARD);
        boards.importSystemText("theirs");
        assertThat(get(TextTransfer.getInstance())).isEqualTo("theirs");
        assertThat(get(RTFTransfer.getInstance())).isNull();
        assertThat(boards.clearContents(writer, DND.CLIPBOARD)).as("this clipboard did not write it").isNull();
        assertThat(get(TextTransfer.getInstance())).isEqualTo("theirs");
    }

    @Test
    void textThisProcessWroteOrNoTextChangesNothing() {
        set(writer, "mine", TextTransfer.getInstance(), DND.CLIPBOARD);
        set(writer, new byte[] {1}, bytesNamed("org.eclipse.swt.test.more"), DND.SELECTION_CLIPBOARD);
        boards.importSystemText("mine");
        boards.importSystemText(null);
        boards.importSystemText("");
        assertThat(boards.getAvailableTypeNames()).containsExactly(names(TextTransfer.getInstance()));
        assertThat(get(TextTransfer.getInstance())).isEqualTo("mine");
        boards.importSystemText("theirs");
        boards.importSystemText("theirs");
        assertThat(get(TextTransfer.getInstance())).isEqualTo("theirs");
    }

    /** A transfer of the text type, converted by TextTransfer, listed after a type nobody set. */
    private static final Transfer ALIAS_OF_TEXT = new Transfer() {
        @Override
        public TransferData[] getSupportedTypes() {
            return TextTransfer.getInstance().getSupportedTypes();
        }

        @Override
        public boolean isSupportedType(TransferData transferData) {
            return TextTransfer.getInstance().isSupportedType(transferData);
        }

        @Override
        protected int[] getTypeIds() {
            return TextTransfer.getInstance().getTypeIds();
        }

        @Override
        protected String[] getTypeNames() {
            return new String[] {"org.eclipse.swt.test.none", "public.utf8-plain-text"};
        }

        @Override
        protected void javaToNative(Object object, TransferData transferData) {
            TextTransfer.getInstance().javaToNative(object, transferData);
        }

        @Override
        protected Object nativeToJava(TransferData transferData) {
            return TextTransfer.getInstance().nativeToJava(transferData);
        }
    };

    @Test
    void anotherTransferWithTheSameTypeReadsTheDatum() {
        set(writer, "abc", TextTransfer.getInstance(), DND.CLIPBOARD);
        assertThat(get(ALIAS_OF_TEXT)).isEqualTo("abc");
    }

    @Test
    void noTypeIsDerivedFromAnother() {
        set(writer, "{\\rtf1\\ansi first\\par {\\b second}}", RTFTransfer.getInstance(), DND.CLIPBOARD);
        assertThat(get(TextTransfer.getInstance())).isNull();
        assertThat(boards.getAvailableTypeNames()).containsExactly(names(RTFTransfer.getInstance()));
        set(writer, new String[] {"/tmp/one two/", "/tmp/three"}, FileTransfer.getInstance(), DND.CLIPBOARD);
        assertThat(get(URLTransfer.getInstance())).isNull();
        assertThat(boards.getAvailableTypeNames()).containsExactly(names(FileTransfer.getInstance()));
        set(writer, "file://localhost/tmp/a b", URLTransfer.getInstance(), DND.CLIPBOARD);
        assertThat(get(FileTransfer.getInstance())).isNull();
        assertThat(boards.getAvailableTypeNames()).containsExactly(names(URLTransfer.getInstance()));
    }
}
