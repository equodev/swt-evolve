package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayDeque;
import java.util.Deque;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

/**
 * A GC opened on a control starts from that control's own colors, the way
 * {@code Control#internal_new_GC} sets them up. Owner-drawn controls rely on it: an
 * {@code AbstractHyperlink} never sets a background of its own, so a GC that started white painted a
 * white block over a dark workbench.
 */
@ExtendWith(Mocks.class)
class GCInheritsDrawableColorsTest {

    private static final RGB DARK_BACKGROUND = new RGB(47, 47, 47);

    private RecordingBridge bridge;
    private final Deque<Runnable> asyncQueue = new ArrayDeque<>();

    @BeforeAll
    static void useEquo() {
        Config.forceEquo();
    }

    @AfterAll
    static void reset() {
        Config.defaultToEclipse();
    }

    @BeforeEach
    void setUp() {
        bridge = new RecordingBridge();
        FlutterBridge.set(bridge);
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
        asyncQueue.clear();
    }

    private Canvas canvasWithColors() {
        Shell shell = Mocks.shell();
        Display display = shell.getDisplay();
        doAnswer(inv -> {
            asyncQueue.add(inv.getArgument(0));
            return null;
        }).when(display).asyncExec(any(Runnable.class));
        Canvas canvas = new Canvas(shell, SWT.NONE);
        canvas.setBounds(0, 0, 400, 400);
        canvas.setBackground(new Color(display, DARK_BACKGROUND));
        Runnable r;
        while ((r = asyncQueue.poll()) != null) {
            r.run();
        }
        bridge.comm.sent.clear();
        return canvas;
    }

    @Test
    @DisplayName("a GC on a control starts from the control's background, not white")
    void backgroundComesFromTheControl() {
        GC gc = new GC(canvasWithColors());
        try {
            assertThat(gc.getBackground().getRGB())
                    .as("a GC seeded with white paints an opaque light block over a dark control")
                    .isEqualTo(DARK_BACKGROUND);
        } finally {
            gc.dispose();
        }
    }

    @Test
    @DisplayName("a GC on an Image keeps the drawable-less white/black default")
    void imageGcKeepsTheDefault() {
        Shell shell = Mocks.shell();
        Image image = new Image(shell.getDisplay(), 64, 64);
        GC gc = new GC(image);
        try {
            assertThat(gc.getBackground().getRGB())
                    .as("no control to inherit from, so the SWT default stands")
                    .isEqualTo(new RGB(255, 255, 255));
            assertThat(gc.getForeground().getRGB()).isEqualTo(new RGB(0, 0, 0));
        } finally {
            gc.dispose();
            image.dispose();
        }
    }
}
