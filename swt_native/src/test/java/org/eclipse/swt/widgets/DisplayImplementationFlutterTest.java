package org.eclipse.swt.widgets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GCData;
import org.eclipse.swt.internal.cloudready.DefaultDisplayResolver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** What {@link DartDisplay} does when it is driven directly rather than through {@link Display}. */
@Tag("flutter-it")
class DisplayImplementationFlutterTest {

    private Display display;
    private DartDisplay impl;

    @BeforeEach
    void setUp() {
        display = newDisplay();
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

    private static Display newDisplay() {
        FlutterBridge.set(new RecordingBridge());
        try {
            return new Display();
        } finally {
            FlutterBridge.set(null);
        }
    }

    /** Waits until the timer the test scheduled is due, which is what makes sleep() return at once. */
    private void awaitDueTimer() throws InterruptedException {
        long deadline = System.currentTimeMillis() + 2000;
        while (impl.untilNextTimer(1) != 0 && System.currentTimeMillis() < deadline)
            Thread.sleep(1);
        assertThat(impl.untilNextTimer(1)).as("the 0 ms timer never became due").isZero();
    }

    private static <T> T inAnotherThread(java.util.function.Supplier<T> body) throws InterruptedException {
        AtomicReference<T> result = new AtomicReference<>();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Thread thread = new Thread(() -> {
            try {
                result.set(body.get());
            } catch (Throwable t) {
                failure.set(t);
            }
        });
        thread.start();
        thread.join();
        if (failure.get() != null)
            throw new AssertionError(failure.get());
        return result.get();
    }

    @Test
    void anImplementationBuiltWithoutItsApiCreatesOneOnceBoundToItself() {
        display.dispose();
        FlutterBridge.set(new RecordingBridge());
        DartDisplay fresh;
        try {
            fresh = new DartDisplay((Display) null);
        } finally {
            FlutterBridge.set(null);
        }
        display = fresh.getApi();
        assertThat(display.getImpl()).isSameAs(fresh);
        assertThat(fresh.getApi()).isSameAs(display);
        assertThat(Display.getCurrent()).isSameAs(display);
    }

    @Test
    void theProtectedHooksOfTheApiReachTheImplementation() {
        display.checkDevice();
        // init() starts the Display's bridge again; a stub keeps it from starting a server and a browser.
        FlutterBridge.set(new RecordingBridge());
        try {
            display.init();
        } finally {
            FlutterBridge.set(null);
        }
        assertThat(display.getSystemFont()).isNotNull();
        GCData data = new GCData();
        assertThat(display.internal_new_GC(data)).isZero();
        assertThat(data.device).isSameAs(display);
        assertThat(data.style & SWT.LEFT_TO_RIGHT).isNotZero();
        assertThat(data.font).isSameAs(display.getSystemFont());
        data.style = SWT.RIGHT_TO_LEFT;
        display.internal_new_GC(data);
        assertThat(data.style).isEqualTo(SWT.RIGHT_TO_LEFT);
        assertThat(display.internal_new_GC(null)).isZero();
        display.internal_dispose_GC(0, data);
        display.dispose();
        assertThatThrownBy(() -> display.internal_new_GC(new GCData())).isInstanceOf(SWTException.class);
        assertThatThrownBy(() -> display.internal_dispose_GC(0, null)).isInstanceOf(SWTException.class);
    }

    @Test
    void releasingAndDestroyingThroughTheApiTakesTheDisplayOut() {
        Shell shell = new Shell(display);
        display.release();
        display.destroy();
        assertThat(shell.isDisposed()).isTrue();
        assertThat(Display.getCurrent()).isNull();
        display = null;
    }

    @Test
    void createAgainReplacesTheSynchronizerAndKeepsTheDisplayCurrent() {
        Synchronizer before = display.getSynchronizer();
        display.create(null);
        assertThat(display.getSynchronizer()).isNotSameAs(before);
        assertThat(Display.getCurrent()).isSameAs(display);
    }

    @Test
    void theInternalKeysOfSetDataAreKeptLikeAnyOther() {
        Shell shell = new Shell(display);
        display.setData(DartDisplay.ADD_WIDGET_KEY, new Object[] {null, null});
        display.setData(DartDisplay.ADD_WIDGET_KEY, new Object[] {null, shell});
        display.setData(DartDisplay.SET_MODAL_DIALOG, new MessageBox(shell));
        display.setData(DartDisplay.SET_MODAL_DIALOG, null);
        display.setData(DartDisplay.LOCK_CURSOR, Boolean.FALSE);
        assertThat(impl._lockCursor()).isFalse();
        display.setData(DartDisplay.LOCK_CURSOR, Boolean.TRUE);
        assertThat(impl._lockCursor()).isTrue();
        display.setData(DartDisplay.RUN_AWT_INVOKE_LATER_KEY, Boolean.TRUE);
        assertThat(display.getData(DartDisplay.RUN_AWT_INVOKE_LATER_KEY)).isEqualTo(Boolean.TRUE);
        assertThat(impl._allowTimers()).isTrue();
        assertThat(impl._runAsyncMessages()).isTrue();
        display.setData(DartDisplay.RUN_AWT_INVOKE_LATER_KEY, null);
        assertThat(display.getData(DartDisplay.RUN_AWT_INVOKE_LATER_KEY)).isNull();
    }

    @Test
    void aSettingsChangeIsAnnouncedAndRedrawsTheShells() {
        Shell shell = new Shell(display);
        List<Event> events = new ArrayList<>();
        display.addListener(SWT.Settings, events::add);
        impl.runSettings = true;
        display.readAndDispatch();
        Tray tray = display.getSystemTray();
        impl.runSettings = true;
        display.readAndDispatch();
        new TrayItem(tray, SWT.NONE);
        impl.runSettings = true;
        display.readAndDispatch();
        assertThat(events).hasSize(3);
        assertThat(events.get(0).type).isEqualTo(SWT.Settings);
        assertThat(impl._runSettings()).isFalse();
        assertThat(shell.isDisposed()).isFalse();
        impl.initColors(true);
        assertThat(impl._colors()).isNotNull();
    }

    @Test
    void postedEventsRunInTheLoopUnlessTheirWidgetOrItemIsGone() {
        Shell shell = new Shell(display);
        Button button = new Button(shell, SWT.PUSH);
        Button gone = new Button(shell, SWT.PUSH);
        Button goneItem = new Button(shell, SWT.PUSH);
        List<Integer> delivered = new ArrayList<>();
        button.addListener(SWT.Selection, e -> delivered.add(e.detail));
        gone.addListener(SWT.Selection, e -> delivered.add(-1));
        DartWidget widget = (DartWidget) button.getImpl();
        for (int i = 1; i <= 5; i++) {
            Event event = new Event();
            event.detail = i;
            widget.sendEvent(SWT.Selection, event, false);
        }
        ((DartWidget) gone.getImpl()).sendEvent(SWT.Selection, new Event(), false);
        Event withItem = new Event();
        withItem.item = goneItem;
        withItem.detail = 99;
        widget.sendEvent(SWT.Selection, withItem, false);
        Event withLiveItem = new Event();
        withLiveItem.item = shell;
        withLiveItem.detail = 6;
        widget.sendEvent(SWT.Selection, withLiveItem, false);
        gone.dispose();
        goneItem.dispose();
        assertThat(delivered).isEmpty();
        while (display.readAndDispatch()) {
        }
        assertThat(delivered).containsExactly(1, 2, 3, 4, 5, 6);
    }

    @Test
    void anEventWithItsOwnTimeKeepsIt() {
        List<Event> events = new ArrayList<>();
        display.addListener(SWT.Close, events::add);
        Event event = new Event();
        event.time = 5;
        impl.sendEvent(SWT.Close, event);
        impl.sendEvent(SWT.Close, null);
        assertThat(events).hasSize(2);
        assertThat(events.get(0).time).isEqualTo(5);
        assertThat(events.get(1).time).isNotZero();
    }

    @Test
    void aSkinListenerThatDisposesTheDisplayEndsTheLoop() {
        display.addListener(SWT.Skin, e -> display.dispose());
        new Shell(display);
        assertThat(display.readAndDispatch()).isTrue();
        assertThat(display.isDisposed()).isTrue();
    }

    @Test
    void aDefaultButtonAndAControlGcReachTheDisplay() {
        FlutterBridge.set(new RecordingBridge());
        Shell shell = new Shell(display);
        Button button = new Button(shell, SWT.PUSH);
        shell.setDefaultButton(button);
        assertThat(shell.getDefaultButton()).isSameAs(button);
        button.internal_dispose_GC(0, null);
        assertThat(impl._contexts()).isNull();
    }

    @Test
    void aTimerThatDisposesTheDisplayEndsTheLoop() {
        display.timerExec(0, display::dispose);
        long deadline = System.currentTimeMillis() + 2000;
        while (!display.isDisposed() && System.currentTimeMillis() < deadline)
            display.readAndDispatch();
        assertThat(display.isDisposed()).isTrue();
    }

    // Both timers are due when the batch is collected, so the second is already in it when the first
    // takes it out: what the batch holds is only a snapshot of what was due.
    @Test
    void aTimerThatCancelsAnotherAlreadyDueOneKeepsItFromRunning() {
        List<String> ran = new ArrayList<>();
        Runnable second = () -> ran.add("second");
        display.timerExec(0, () -> {
            ran.add("first");
            display.timerExec(-1, second);
        });
        display.timerExec(0, second);
        while (display.readAndDispatch()) {
        }
        assertThat(ran).isEqualTo(List.of("first"));
    }

    @Test
    void aTimerThatPostponesAnotherAlreadyDueOneLeavesItForALaterRound() {
        List<String> ran = new ArrayList<>();
        Runnable second = () -> ran.add("second");
        display.timerExec(0, () -> {
            ran.add("first");
            display.timerExec(60_000, second);
        });
        display.timerExec(0, second);
        while (display.readAndDispatch()) {
        }
        assertThat(ran).isEqualTo(List.of("first"));
    }

    @Test
    void sleepReturnsAtOnceWithPendingWorkAndKeepsAnInterrupt() throws InterruptedException {
        display.asyncExec(() -> {});
        assertThat(display.sleep()).isTrue();
        while (display.readAndDispatch()) {
        }
        display.timerExec(0, () -> {});
        awaitDueTimer();
        assertThat(display.sleep()).isTrue();
        while (display.readAndDispatch()) {
        }
        Thread.currentThread().interrupt();
        try {
            assertThat(display.sleep()).isTrue();
            assertThat(Thread.currentThread().isInterrupted()).isTrue();
        } finally {
            Thread.interrupted();
        }
    }

    @Test
    void theSmallFontsPropertyUpstreamReadIsRead() {
        display.dispose();
        System.setProperty("org.eclipse.swt.internal.carbon.smallFonts", "true");
        try {
            display = newDisplay();
            assertThat(((DartDisplay) display.getImpl())._smallFonts()).isTrue();
        } finally {
            System.clearProperty("org.eclipse.swt.internal.carbon.smallFonts");
        }
    }

    /**
     * Win32 and GTK turn SWT.SHEET into a dialog centred on its parent and only Cocoa attaches a real
     * sheet to the parent window, so Evolve follows the two (swt-evolve 18dc7ebe3):
     * {@link DartDisplay#getSheetEnabled()} is constant false, whatever {@code org.eclipse.swt.sheet},
     * which the cocoa Display reads, says.
     */
    @Test
    void aSheetIsNeverEnabledWhateverTheSheetPropertySays() {
        System.setProperty("org.eclipse.swt.sheet", "false");
        try {
            assertThat(DartDisplay.getSheetEnabled()).isFalse();
            System.setProperty("org.eclipse.swt.sheet", "true");
            assertThat(DartDisplay.getSheetEnabled()).isFalse();
        } finally {
            System.clearProperty("org.eclipse.swt.sheet");
        }
        assertThat(DartDisplay.getSheetEnabled()).isFalse();
    }

    /**
     * The locale the client reports on ClientReady (DisplayBridge's ClientReady handler hands it to
     * {@link DartDisplay#applyClientLocale}, and DartSpinner formats its numbers in it): a tag the
     * client does not send leaves the display on the locale it already had.
     */
    @Test
    void theClientLocaleIsTakenFromTheTagTheClientReportsAndOnlyFromOne() {
        DartDisplay impl = (DartDisplay) display.getImpl();
        assertThat(impl.clientLocale).isEqualTo(Locale.ROOT);
        impl.applyClientLocale(null);
        assertThat(impl.clientLocale).isEqualTo(Locale.ROOT);
        impl.applyClientLocale("");
        assertThat(impl.clientLocale).isEqualTo(Locale.ROOT);
        impl.applyClientLocale("es-AR");
        assertThat(impl.clientLocale).isEqualTo(Locale.forLanguageTag("es-AR"));
        impl.applyClientLocale(null);
        assertThat(impl.clientLocale).isEqualTo(Locale.forLanguageTag("es-AR"));
    }

    @Test
    void aSessionResolverAnswersTheDefaultDisplay() {
        DefaultDisplayResolver.set(() -> display);
        assertThat(Display.getDefault()).isSameAs(display);
        List<Cursor> cursors = new ArrayList<>();
        cursors.add(display.getSystemCursor(SWT.CURSOR_ARROW));
        FlutterBridge.set(new RecordingBridge());
        Display other;
        try {
            DefaultDisplayResolver.set(() -> null);
            other = Display.getDefault();
        } finally {
            FlutterBridge.set(null);
        }
        assertThat(other).isNotSameAs(display);
        Cursor otherCursor = other.getSystemCursor(SWT.CURSOR_ARROW);
        other.dispose();
        assertThat(otherCursor.isDisposed()).as("a session's display leaves the shared cursors alone").isFalse();
    }

    @Test
    void displaysOnTwoThreadsKeepTheFirstAsDefault() throws InterruptedException {
        List<Boolean> seen = inAnotherThread(() -> {
            Display second = newDisplay();
            Display third = newDisplay();
            List<Boolean> answers = new ArrayList<>();
            answers.add(Display.getDefault() == display);
            answers.add(Display.getCurrent() == third);
            third.dispose();
            second.dispose();
            answers.add(Display.getCurrent() == null);
            return answers;
        });
        assertThat(seen).containsExactly(true, true, true);
        assertThat(Display.getDefault()).isSameAs(display);
    }

    @Test
    void aDisplayWhoseInitFailsIsTakenOutAgain() throws InterruptedException {
        display.dispose();
        assertThatThrownBy(DisplayImplementationFlutterTest::failingDisplay).isInstanceOf(IllegalStateException.class);
        assertThat(Display.getCurrent()).isNull();
        display = newDisplay();
        Boolean defaultKept = inAnotherThread(() -> {
            try {
                failingDisplay();
            } catch (IllegalStateException expected) {
            }
            return Display.findDisplay(Thread.currentThread()) == null;
        });
        assertThat(defaultKept).isTrue();
        assertThat(Display.getDefault()).isSameAs(display);
    }

    private static void failingDisplay() {
        FlutterBridge.set(new RecordingBridge());
        try {
            new DartDisplay((Display) null) {
                @Override
                public void init() {
                    throw new IllegalStateException("boot failure");
                }
            };
        } finally {
            FlutterBridge.set(null);
        }
    }

    @Test
    void theCaretTimerBlinksTheCurrentCaretUntilItStops() {
        FlutterBridge.set(new RecordingBridge());
        Shell shell = new Shell(display);
        Canvas canvas = new Canvas(shell, SWT.NONE);
        Caret caret = new Caret(canvas, SWT.NONE);
        DartCaret caretImpl = (DartCaret) caret.getImpl();
        impl.caretTimer.run();
        impl.setCurrentCaret(caret);
        assertThat(impl._currentCaret()).isSameAs(caret);
        impl.caretTimer.run();
        caretImpl.blinkRate = 0;
        impl.caretTimer.run();
        caretImpl.isVisible = false;
        impl.caretTimer.run();
        caretImpl.isVisible = true;
        caretImpl.isShowing = false;
        impl.caretTimer.run();
        caret.dispose();
        impl.currentCaret = caret;
        impl.caretTimer.run();
        impl.setCurrentCaret(null);
        assertThat(impl._currentCaret()).isNull();
    }

    @Test
    void aCaretThatCannotBlinkStopsTheTimer() {
        FlutterBridge.set(new RecordingBridge());
        Shell shell = new Shell(display);
        Canvas canvas = new Canvas(shell, SWT.NONE);
        Caret caret = new Caret(canvas, SWT.NONE);
        DartCaret caretImpl = (DartCaret) caret.getImpl();
        caretImpl.isVisible = true;
        caretImpl.isShowing = true;
        caretImpl.blinkRate = 500;
        impl.currentCaret = caret;
        canvas.setVisible(false);
        impl.caretTimer.run();
        assertThat(impl._currentCaret()).isIn(caret, null);
    }

    @Test
    void popupsAreShownOnceEachAndForgottenWhenRemoved() {
        Shell shell = new Shell(display);
        Menu[] menus = new Menu[6];
        for (int i = 0; i < menus.length; i++)
            menus[i] = new Menu(shell, SWT.POP_UP);
        impl.removePopup(menus[0]);
        for (Menu menu : menus)
            impl.addPopup(menu);
        impl.addPopup(menus[0]);
        assertThat(impl._popups()).contains(menus);
        menus[5].dispose();
        assertThat(impl.runPopups()).isTrue();
        assertThat(impl.runPopups()).isFalse();
        impl.removePopup(menus[1]);
        impl.removePopup(new Menu(shell, SWT.POP_UP));
        impl.addPopup(menus[1]);
        assertThat(impl.isPopupAlreadyShown(menus[1])).isFalse();
        menus[2].dispose();
        impl.markPopupShown(menus[1]);
        menus[4].dispose();
        impl.unmarkPopupShown(menus[3]);
        assertThat(impl.isPopupAlreadyShown(menus[3])).isFalse();
        assertThat(impl.isPopupAlreadyShown(menus[1])).isTrue();
    }

    @Test
    void modalShellsAreListedOnceAndClearedInAnyOrder() {
        Shell[] shells = new Shell[6];
        for (int i = 0; i < shells.length; i++)
            shells[i] = new Shell(display, SWT.APPLICATION_MODAL);
        Shell other = new Shell(display);
        impl.clearModal(other);
        for (Shell shell : shells)
            impl.setModalShell(shell);
        impl.setModalShell(shells[0]);
        assertThat(impl._modalShells()).contains(shells);
        impl.clearModal(other);
        impl.clearModal(shells[3]);
        assertThat(impl._modalShells()).doesNotContain(shells[3]);
        for (Shell shell : shells)
            impl.clearModal(shell);
        assertThat(impl._modalShells()).isNull();
        impl.setModalShell(shells[0]);
        impl.setModalShell(shells[1]);
        impl.clearModal(shells[1]);
        assertThat(impl._modalShells()).containsOnly(shells[0], null);
        impl.clearModal(shells[0]);
        for (int i = 0; i < 4; i++)
            impl.setModalShell(shells[i]);
        impl.clearModal(other);
        assertThat(impl._modalShells()).containsExactly(shells[0], shells[1], shells[2], shells[3]);
    }

    @Test
    void theFocusBookkeepingFollowsTheFocusControl() {
        Shell shell = new Shell(display);
        Button button = new Button(shell, SWT.PUSH);
        Button other = new Button(shell, SWT.PUSH);
        List<String> events = new ArrayList<>();
        button.addListener(SWT.FocusIn, e -> events.add("in"));
        button.addListener(SWT.FocusOut, e -> events.add("out"));
        impl.currentFocusControl = button;
        impl.ignoreFocusControl = button;
        impl.checkFocus();
        assertThat(events).isEmpty();
        impl.ignoreFocusControl = null;
        impl.focusControl = button;
        impl.currentFocusControl = other;
        impl.checkFocus();
        assertThat(events).containsExactly("in");
        impl.focusControl = null;
        impl.checkFocus();
        assertThat(events).containsExactly("in", "out");
    }

    @Test
    void anActiveShellIsAVisibleShellThatTakesTheFocus() {
        FlutterBridge.set(new RecordingBridge());
        Shell hidden = new Shell(display);
        Shell shown = new Shell(display);
        Shell noFocus = new Shell(display, SWT.NO_FOCUS);
        shown.setVisible(true);
        noFocus.setVisible(true);
        Shell gone = new Shell(display);
        gone.dispose();
        assertThat(impl.canBeActiveShell(null)).isFalse();
        assertThat(impl.canBeActiveShell(gone)).isFalse();
        assertThat(impl.canBeActiveShell(hidden)).isFalse();
        assertThat(impl.canBeActiveShell(noFocus)).isFalse();
        assertThat(impl.canBeActiveShell(shown)).isTrue();
        Button inShown = new Button(shown, SWT.PUSH);
        impl.focusControl = inShown;
        assertThat(display.getActiveShell()).isSameAs(shown);
        assertThat(display.getFocusControl()).isSameAs(inShown);
        Button inHidden = new Button(hidden, SWT.PUSH);
        impl.focusControl = inHidden;
        assertThat(display.getActiveShell()).isIn(shown, noFocus, null);
        inHidden.dispose();
        assertThat(display.getFocusControl()).isNotSameAs(inHidden);
    }

    @Test
    void shellsAndTooltipsAreListedOnce() {
        Shell shell = new Shell(display);
        impl.addShell(shell);
        assertThat(impl._shells()).containsExactly(shell);
        ToolTip tip = new ToolTip(shell, SWT.NONE);
        ToolTip gone = new ToolTip(shell, SWT.NONE);
        impl._addActiveTooltip(tip);
        impl._addActiveTooltip(tip);
        impl._addActiveTooltip(gone);
        impl._addActiveTooltip(null);
        gone.dispose();
        assertThat(impl._activeTooltips()).containsExactly(tip);
        impl._removeActiveTooltip(tip);
        assertThat(impl._activeTooltips()).isEmpty();
    }
}
