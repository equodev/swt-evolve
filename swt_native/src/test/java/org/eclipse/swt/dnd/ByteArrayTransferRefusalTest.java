package org.eclipse.swt.dnd;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.eclipse.swt.SWTException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** ByteArrayTransfer.javaToNative refuses with ERROR_INVALID_DATA what its javadoc says it refuses. */
class ByteArrayTransferRefusalTest {

    private static final String NAME = "org.eclipse.swt.test.refusal";

    private static final ByteArrayTransfer BYTES = new ByteArrayTransfer() {
        final int id = registerType(NAME);

        @Override
        protected int[] getTypeIds() {
            return new int[] {id};
        }

        @Override
        protected String[] getTypeNames() {
            return new String[] {NAME};
        }
    };

    private static void assertInvalidData(ThrowingCallable conversion) {
        assertThatThrownBy(conversion).isInstanceOfSatisfying(SWTException.class,
                e -> assertThat(e.code).isEqualTo(DND.ERROR_INVALID_DATA));
    }

    @Test
    void dataThatIsNotBytesIsRefusedForItsOwnType() {
        TransferData type = BYTES.getSupportedTypes()[0];
        assertInvalidData(() -> BYTES.javaToNative(null, type));
        assertInvalidData(() -> BYTES.javaToNative("text", type));
    }

    @Test
    void emptyBytesAreRefused() {
        assertInvalidData(() -> BYTES.javaToNative(new byte[0], BYTES.getSupportedTypes()[0]));
    }

    @Test
    void bytesForATypeItDoesNotSupportAreRefused() {
        assertInvalidData(() -> BYTES.javaToNative(new byte[] {1}, TextTransfer.getInstance().getSupportedTypes()[0]));
    }

    /** GTK and Cocoa write to the TransferData before refusing anything: no type is a NullPointerException. */
    @Test
    void bytesForNoTypeThrowANullPointerException() {
        assertThatThrownBy(() -> BYTES.javaToNative(new byte[] {1}, null)).isInstanceOf(NullPointerException.class);
        assertInvalidData(() -> BYTES.javaToNative(null, null));
    }

    @Test
    void bytesForItsOwnTypeAreConverted() {
        TransferData type = BYTES.getSupportedTypes()[0];
        BYTES.javaToNative(new byte[] {1, 2}, type);
        assertThat((byte[]) BYTES.nativeToJava(type)).containsExactly(1, 2);
    }

    /** A native form without bytes, as a subclass may write one, reads as no data, as on Cocoa. */
    @Test
    void anEmptyNativeFormReadsNull() {
        TransferData type = BYTES.getSupportedTypes()[0];
        type.data = new byte[0];
        assertThat(BYTES.nativeToJava(type)).isNull();
    }
}
