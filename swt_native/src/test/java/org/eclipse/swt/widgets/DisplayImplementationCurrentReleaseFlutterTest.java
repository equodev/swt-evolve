package org.eclipse.swt.widgets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.internal.DPIUtil;
import org.eclipse.swt.internal.cloudready.DefaultDisplayResolver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * What {@link DartDisplay} holds, how it takes the client's zoom and how its dispose survives a failing
 * release, as of the current SWT release: the fields it reports, {@code DPIUtil.getNativeDeviceZoom()}
 * and the exceptions dispose collects (from 3.117) differ in older releases.
 */
@Tag("flutter-it")
class DisplayImplementationCurrentReleaseFlutterTest {

    private Display display;
    private DartDisplay impl;

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
        try {
            display = new Display();
        } finally {
            FlutterBridge.set(null);
        }
        impl = (DartDisplay) display.getImpl();
    }

    @AfterEach
    void tearDown() {
        DefaultDisplayResolver.set(null);
        if (display != null && !display.isDisposed())
            display.dispose();
        Display current = Display.getCurrent();
        if (current != null && !current.isDisposed())
            current.dispose();
        FlutterBridge.set(null);
    }

    @Test
    void theImplementationReportsWhatItHolds() {
        Shell shell = new Shell(display);
        display.setData("key", "value");
        display.setData("data");
        Object[] state = {
                impl._eventQueue(), impl._eventTable(), impl._filterTable(), impl._disposing(), impl._sendEventCount(),
                impl._rotation(), impl._magnification(), impl._gestureActive(), impl._touchCounter(),
                impl._primaryIdentifier(), impl._touchSources(), impl._synchronizer(), impl._runtimeExceptionHandler(),
                impl._errorHandler(), impl._thread(), impl._allowTimers(), impl._runAsyncMessages(), impl._contexts(),
                impl._currentCaret(), impl._sendEvent(), impl._clickCountButton(), impl._clickCount(), impl._blinkTime(),
                impl._currentControl(), impl._trackingControl(), impl._tooltipControl(), impl._ignoreFocusControl(),
                impl._tooltipTarget(), impl._smallFonts(), impl._useNativeItemHeight(), impl._modalShells(),
                impl._modalDialog(), impl._menuBar(), impl._menus(), impl._popups(), impl._escAsAcceleratorPresent(),
                impl._isEmbedded(), impl._focusControl(), impl._currentFocusControl(), impl._focusEvent(),
                impl._poolCount(), impl._loopCount(), impl._screenID(), impl._lockCursor(), impl._currentCombo(),
                impl._disposeList(), impl._layoutDeferred(), impl._layoutDeferredCount(), impl._tray(),
                impl._currentTrayItem(), impl._trayItemMenu(), impl._appMenuBar(), impl._appMenu(), impl._taskBar(),
                impl._errorImage(), impl._infoImage(), impl._warningImage(), impl._cursors(), impl._colors(),
                impl._alternateSelectedControlTextColor(), impl._selectedControlTextColor(), impl._skinList(),
                impl._skinCount(), impl._runSettings(), impl._data(), impl._keys(), impl._values(), impl._caretTimer(),
                impl._cursorLocation(), impl._cursorSizes(), impl._darkThemePreferred(), impl._iconSizes(),
                impl._monitors(), impl._shells(), impl._activeTooltips(),
        };
        assertThat(state).hasSize(75);
        assertThat(impl._thread()).isSameAs(Thread.currentThread());
        assertThat(impl._shells()).containsExactly(shell);
        assertThat(impl._keys()).containsExactly("key");
        assertThat(impl._values()).containsExactly("value");
        assertThat(impl._data()).isEqualTo("data");
        assertThat(impl._blinkTime()).isEqualTo(560);
        EventTable table = new EventTable();
        impl._eventTable(table);
        assertThat(impl._eventTable()).isSameAs(table);
    }

    @Test
    void theClientZoomIsTakenOnlyWhenItChanges() {
        int nativeZoom = DPIUtil.getNativeDeviceZoom();
        try {
            impl.applyClientDeviceZoom(0);
            assertThat(impl.getDeviceZoom()).isEqualTo(100);
            impl.applyClientDeviceZoom(150);
            assertThat(impl.getDeviceZoom()).isEqualTo(150);
            impl.applyClientDeviceZoom(150);
            assertThat(impl.getDeviceZoom()).isEqualTo(150);
            DPIUtil.setDeviceZoom(200);
            impl.applyClientDeviceZoom(150);
            assertThat(DPIUtil.getNativeDeviceZoom()).isEqualTo(150);
        } finally {
            impl.applyClientDeviceZoom(nativeZoom > 0 ? nativeZoom : 100);
            DPIUtil.setDeviceZoom(nativeZoom > 0 ? nativeZoom : 100);
        }
    }

    @Test
    void theApplicationMenusAreHandedOutAndGoWithTheDisplay() {
        Shell shell = new Shell(display);
        Menu appMenu = new Menu(display);
        Menu appMenuBar = new Menu(display);
        impl.appMenu = appMenu;
        impl.appMenuBar = appMenuBar;
        assertThat(display.getSystemMenu()).isSameAs(appMenu);
        impl.setMenuBar(null);
        assertThat(impl._menuBar()).isSameAs(appMenuBar);
        impl.setMenuBar(appMenuBar);
        Menu bar = new Menu(shell, SWT.BAR);
        impl.setMenuBar(bar);
        assertThat(impl._menuBar()).isSameAs(bar);
        IllegalStateException failure = new IllegalStateException("application menu");
        appMenu.addListener(SWT.Dispose, e -> {
            throw failure;
        });
        appMenuBar.addListener(SWT.Dispose, e -> {
            throw new IllegalStateException("application menu bar");
        });
        assertThatThrownBy(display::dispose).isSameAs(failure);
        assertThat(display.isDisposed()).isTrue();
        assertThat(appMenu.isDisposed()).isTrue();
        assertThat(appMenuBar.isDisposed()).isTrue();
    }
}
