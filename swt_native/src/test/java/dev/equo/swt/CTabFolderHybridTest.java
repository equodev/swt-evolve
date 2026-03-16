package dev.equo.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.DartCTabFolder;
import org.eclipse.swt.widgets.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.MockedConstruction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.Mocks.*;
import static org.mockito.Mockito.*;

class CTabFolderHybridTest {

    @RegisterExtension
    Mocks mock = Mocks.noNativeBridge();

    @BeforeEach
    void config() {
        Config.useEquo(CTabFolder.class);
        Config.useEquo(Button.class);
    }

    @AfterEach
    void reset() {
        System.clearProperty(Config.PROPERTY_PREFIX+"CTabFolder");
        System.clearProperty(Config.PROPERTY_PREFIX+"Button");
    }

    @Nested
    class CTabFolderBridge {

        @Test
        void bridge_should_be_created_for_swt_parent() {
            CTabFolder tabs = new CTabFolder(swtShell(), SWT.NONE);

            DartCTabFolder impl = (DartCTabFolder) tabs.getImpl();
            assertThat(impl.getBridge().forWidget())
                    .isNotNull()
                    .isEqualTo(impl);
        }

        @Test
        void bridge_should_not_be_created_for_dart_parent() {
            Composite parent = composite();
            FlutterBridge parentBridge = mockBridge(parent);

            CTabFolder tabs = new CTabFolder(parent, SWT.NONE);

            DartCTabFolder impl = (DartCTabFolder) tabs.getImpl();
            assertThat(impl.getBridge().forWidget())
                    .isNotEqualTo(impl);
            assertThat(impl.getBridge()).isNotNull()
                    .isEqualTo(parentBridge);
        }

        @Test
        void bridge_should_not_be_created_for_nested() {
            CTabFolder tabs = new CTabFolder(swtShell(), SWT.NONE);
            CTabFolder nested = new CTabFolder(tabs, SWT.NONE);

            DartCTabFolder impl = (DartCTabFolder) nested.getImpl();
            assertThat(impl.getBridge().forWidget())
                    .isNotEqualTo(impl)
                    .isNotNull()
                    .isEqualTo(tabs.getImpl());
        }

        @Test
        void bridge_should_be_created_for_deep_nested_in_swt() {
            CTabFolder tabs = new CTabFolder(swtShell(), SWT.NONE);
            Composite body = swtComposite(tabs);
            Shell shell = tabs.getShell();
            when(body.getShell()).thenReturn(shell);
            CTabFolder nested = new CTabFolder(body, SWT.NONE);

            DartCTabFolder impl = (DartCTabFolder) nested.getImpl();
            assertThat(impl.getBridge().forWidget())
                    .isEqualTo(impl);
        }

        @Test
        void bridge_should_be_created_for_nested_in_swt_folder() {
            Config.useEclipse(CTabFolder.class);
            Shell swtShell = swtShell();
            CTabFolder tabs = swtCTabFolder(swtShell);
            when(tabs.getShell()).thenReturn(swtShell);
            Config.useEquo(CTabFolder.class);
            CTabFolder nested = new CTabFolder(tabs, SWT.NONE);

            DartCTabFolder impl = (DartCTabFolder) nested.getImpl();
            assertThat(impl.getBridge().forWidget())
                    .isEqualTo(impl);
        }
    }

    private static FlutterBridge mockBridge(Composite parent) {
        FlutterBridge parentBridge = mock(SwtFlutterBridge.class);
        DartComposite impl = (DartComposite) parent.getImpl();
        when(parentBridge.forWidget()).thenReturn(impl);
        when(impl.getBridge()).thenReturn(parentBridge);
        return parentBridge;
    }

    @Nested
    class BodyBridge {

        @Test
        void bridge_should_not_be_created_for_equo_body() {
            CTabFolder tabs = new CTabFolder(swtShell(), SWT.NONE);
            Button b = new Button(tabs, SWT.PUSH);

            DartButton impl = (DartButton) b.getImpl();
            assertThat(impl.getBridge().forWidget())
                    .isNotEqualTo(impl)
                    .isNotNull()
                    .isEqualTo(tabs.getImpl());
        }
    }

    @Nested
    class SetBounds {

        @Test
        void setBounds_should_be_full_height_for_no_tabs() {
            CTabFolder tabs = new CTabFolder(swtShell(), SWT.NONE);

            tabs.setBounds(10, 10, 200, 300);

            verifySetBounds(mock.bridge, 10, 10, 200, 300, 0, 0, 200, 300);
        }

        @Test
        void setBounds_should_be_full_height_for_dart_tab() {
            CTabFolder tabs = new CTabFolder(swtShell(), SWT.NONE);

            Control body1 = new Button(tabs, SWT.PUSH);
            assertThat(body1.getImpl()).isInstanceOf(DartButton.class);
            new CTabItem(tabs, SWT.NONE).setControl(body1);

            Control body2 = swtComposite(tabs);
            assertThat(body2.getImpl()).isInstanceOf(SwtComposite.class);
            new CTabItem(tabs, SWT.NONE).setControl(body2);

            tabs.setSelection(0);
            tabs.setBounds(10, 10, 200, 300);

            verifySetBounds(mock.bridge, 10, 10, 200, 300, 0, 0, 200, 300);
        }

        @Test
        void setBounds_should_be_tabs_height_for_swt_tab() {
            CTabFolder tabs = new CTabFolder(swtShell(), SWT.NONE);

            Control body1 = new Button(tabs, SWT.PUSH);
            assertThat(body1.getImpl()).isInstanceOf(DartButton.class);
            new CTabItem(tabs, SWT.NONE).setControl(body1);

            Control body2 = swtComposite(tabs);
            assertThat(body2.getImpl()).isInstanceOf(SwtComposite.class);
            new CTabItem(tabs, SWT.NONE).setControl(body2);

            tabs.setSelection(1);
            tabs.setBounds(10, 10, 200, 300);

            verifySetBounds(mock.bridge, 10, 10, 200, 300, 0, 0, 200, 32);
        }

        @Test
        void setBounds_should_be_full_height_switching_from_swt_to_dart_tab() {
            CTabFolder tabs = new CTabFolder(swtShell(), SWT.NONE);

            Control body1 = new Button(tabs, SWT.PUSH);
            assertThat(body1.getImpl()).isInstanceOf(DartButton.class);
            new CTabItem(tabs, SWT.NONE).setControl(body1);

            Control body2 = swtComposite(tabs);
            assertThat(body2.getImpl()).isInstanceOf(SwtComposite.class);
            new CTabItem(tabs, SWT.NONE).setControl(body2);

            tabs.setSelection(1);
            tabs.setBounds(10, 10, 200, 300);
            verifySetBounds(mock.bridge, 10, 10, 200, 300, 0, 0, 200, 32);
            tabs.setSelection(0);
            verifySetBounds(mock.bridge, 10, 10, 200, 300, 0, 0, 200, 300);
        }

        @Test
        void setBounds_should_be_tabs_height_switching_from_dart_to_swt_tab() {
            CTabFolder tabs = new CTabFolder(swtShell(), SWT.NONE);

            Control body1 = new Button(tabs, SWT.PUSH);
            assertThat(body1.getImpl()).isInstanceOf(DartButton.class);
            new CTabItem(tabs, SWT.NONE).setControl(body1);

            Control body2 = swtComposite(tabs);
            assertThat(body2.getImpl()).isInstanceOf(SwtComposite.class);
            new CTabItem(tabs, SWT.NONE).setControl(body2);

            tabs.setSelection(0);
            tabs.setBounds(10, 10, 200, 300);
            verifySetBounds(mock.bridge, 10, 10, 200, 300, 0, 0, 200, 300);
            tabs.setSelection(1);
            verifySetBounds(mock.bridge, 10, 10, 200, 300, 0, 0, 200, 32);
        }

        @Test
        void setBounds_should_not_be_called_for_nested() {
            CTabFolder tabs = new CTabFolder(swtShell(), SWT.NONE);
            CTabFolder nested = new CTabFolder(tabs, SWT.NONE);

            nested.setBounds(10, 10, 200, 300);

            verifyNoSetBounds(mock.bridge);
        }
    }

    @Test
    void body_should_be_swt_for_parent_swt() {
        Shell shell = swtShell();
        Composite parent = swtComposite(shell);
        when(parent.getShell()).thenReturn(shell);
        when(parent.getImpl()._backgroundMode()).thenReturn(1);
        assertThat(parent.getImpl()).isInstanceOf(SwtComposite.class);

        CTabFolder nested = new CTabFolder(parent, SWT.NONE);
        assertThat(nested.getImpl()).isInstanceOf(DartCTabFolder.class);

        try (MockedConstruction<SwtComposite> ignored = mockConstruction(SwtComposite.class)) {
            Composite composite = new Composite(nested, SWT.NONE);

            assertThat(composite.getImpl()).isInstanceOf(SwtComposite.class);
        }
    }

    @Test
    void body_should_be_dart_for_deep_nested_in_dart() {
        Composite body = composite();
        when(body.getImpl()._backgroundMode()).thenReturn(1);
        mockBridge(body);
        assertThat(body.getImpl()).isInstanceOf(DartComposite.class);

        CTabFolder nested = new CTabFolder(body, SWT.NONE);
        assertThat(nested.getImpl()).isInstanceOf(DartCTabFolder.class);

        Composite composite = new Composite(nested, SWT.NONE);

        assertThat(composite.getImpl()).isInstanceOf(DartComposite.class);
    }

}
