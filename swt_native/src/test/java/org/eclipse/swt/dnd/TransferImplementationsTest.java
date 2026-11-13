package org.eclipse.swt.dnd;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * What the transfer API never reaches in its implementations: an implementation made without an API, the value
 * the generator adds, and the implementation an application's own transfer gets.
 */
class TransferImplementationsTest {

    private static final List<Function<Void, ? extends DartByteArrayTransfer>> BUILT_IN = Arrays.asList(
            v -> new DartTextTransfer(null), v -> new DartRTFTransfer(null), v -> new DartHTMLTransfer(null),
            v -> new DartURLTransfer(null), v -> new DartFileTransfer(null), v -> new DartImageTransfer(null));

    /** A direct subclass whose types are TextTransfer's. */
    private static final class AliasOfText extends Transfer {
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
            return TextTransfer.getInstance().getTypeNames();
        }

        @Override
        protected void javaToNative(Object object, TransferData transferData) {
            TextTransfer.getInstance().javaToNative(object, transferData);
        }

        @Override
        protected Object nativeToJava(TransferData transferData) {
            return TextTransfer.getInstance().nativeToJava(transferData);
        }
    }

    @Test
    void aBuiltInImplementationWithoutAnApiMakesOne() {
        for (Function<Void, ? extends DartByteArrayTransfer> make : BUILT_IN) {
            DartByteArrayTransfer impl = make.apply(null);
            ByteArrayTransfer api = impl.getApi();
            assertThat(api).as(impl.getClass().getSimpleName()).isNotNull();
            assertThat(api.getImpl()).isSameAs(impl);
            assertThat(impl.getApi()).isSameAs(api);
            assertThat(impl.getValue()).isNotNull().isSameAs(impl.getValue());
            assertThat(api.getTypeIds()).isEqualTo(impl.getTypeIds());
        }
    }

    @Test
    void theApiOfABuiltInTransferAnswersItsTypeIds() {
        for (ByteArrayTransfer transfer : new ByteArrayTransfer[] {RTFTransfer.getInstance(), HTMLTransfer.getInstance(),
                URLTransfer.getInstance(), FileTransfer.getInstance(), ImageTransfer.getInstance()}) {
            int[] ids = transfer.getTypeIds();
            assertThat(ids).as(transfer.getClass().getSimpleName()).hasSameSizeAs(transfer.getTypeNames());
            for (int i = 0; i < ids.length; i++)
                assertThat(ids[i]).isEqualTo(Transfer.registerType(transfer.getTypeNames()[i]));
        }
    }

    @Test
    void anApplicationsDirectSubclassGetsAnImplementationThatAsksIt() {
        AliasOfText alias = new AliasOfText();
        ITransfer impl = alias.getImpl();
        assertThat(alias.getImpl()).isSameAs(impl);
        assertThat(((DartTransfer) impl).getApi()).isSameAs(alias);
        assertThat(((DartTransfer) impl).getValue()).isNotNull().isSameAs(((DartTransfer) impl).getValue());
        assertThat(impl.getTypeNames()).containsExactly("public.utf8-plain-text");
        assertThat(impl.getTypeIds()).isEqualTo(TextTransfer.getInstance().getTypeIds());
        TransferData type = impl.getSupportedTypes()[0];
        assertThat(impl.isSupportedType(type)).isTrue();
        impl.javaToNative("through the implementation", type);
        assertThat(impl.nativeToJava(type)).isEqualTo("through the implementation");
        assertThat(impl.validate(null)).isTrue();
    }

    @Test
    void anImplementationMadeWithoutAnApiHasNone() {
        DartTransfer impl = new DartTransfer(null) {
            @Override
            public TransferData[] getSupportedTypes() {
                return new TransferData[0];
            }

            @Override
            public boolean isSupportedType(TransferData transferData) {
                return false;
            }

            @Override
            public int[] getTypeIds() {
                return new int[0];
            }

            @Override
            public String[] getTypeNames() {
                return new String[0];
            }

            @Override
            public void javaToNative(Object object, TransferData transferData) {
            }

            @Override
            public Object nativeToJava(TransferData transferData) {
                return null;
            }
        };
        assertThat(impl.getApi()).isNull();
    }

    @Test
    void anApplicationsByteArrayTransferGetsAnImplementationThatAsksIt() {
        ByteArrayTransfer bytes = new ByteArrayTransfer() {
            final int id = registerType("org.eclipse.swt.test.impl");

            @Override
            protected int[] getTypeIds() {
                return new int[] {id};
            }

            @Override
            protected String[] getTypeNames() {
                return new String[] {"org.eclipse.swt.test.impl"};
            }
        };
        IByteArrayTransfer impl = bytes.getImpl();
        assertThat(impl.getTypeNames()).containsExactly("org.eclipse.swt.test.impl");
        assertThat(((DartByteArrayTransfer) impl).getApi()).isSameAs(bytes);
        assertThat(((DartByteArrayTransfer) impl).getValue()).isNotNull().isSameAs(((DartByteArrayTransfer) impl).getValue());
    }
}
