package org.eclipse.swt.graphics;

import dev.equo.swt.FontMetricsUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DartMocks;
import org.eclipse.swt.widgets.Display;
import org.junit.jupiter.api.AfterEach;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** A TextLayout drawn on a GC that records what it is told to draw. */
abstract class TextLayoutDrawTestBase {

    /** One drawString: the text, where, and with which font and foreground. */
    record Run(String text, int x, int y, Font font, Color foreground) {
    }

    record Fill(int x, int y, int width, int height, Color background) {
    }

    final Display display = DartMocks.dartDisplay();
    final List<Run> runs = new ArrayList<>();
    final List<Fill> fills = new ArrayList<>();
    final List<int[]> lines = new ArrayList<>();
    final List<TextLayout> layouts = new ArrayList<>();
    final Color black = new Color(display, 0, 0, 0);
    final Color white = new Color(display, 255, 255, 255);

    @AfterEach
    void dispose() {
        layouts.forEach(TextLayout::dispose);
    }

    Font font(int size, int style) {
        return new DartFont(display, "System", size, style, null).getApi();
    }

    TextLayout layout(String text) {
        TextLayout layout = new TextLayout(display);
        layouts.add(layout);
        layout.setFont(font(12, SWT.NORMAL));
        layout.setText(text);
        return layout;
    }

    /** A GC that records what is drawn on it. */
    GC gc() {
        GC gc = mock(GC.class);
        Font[] font = {font(12, SWT.NORMAL)};
        Color[] foreground = {black}, background = {white};
        when(gc.getFont()).thenAnswer(i -> font[0]);
        when(gc.getForeground()).thenAnswer(i -> foreground[0]);
        when(gc.getBackground()).thenAnswer(i -> background[0]);
        when(gc.getLineStyle()).thenReturn(SWT.LINE_SOLID);
        doAnswer(i -> font[0] = i.getArgument(0)).when(gc).setFont(any());
        doAnswer(i -> foreground[0] = i.getArgument(0)).when(gc).setForeground(any());
        doAnswer(i -> background[0] = i.getArgument(0)).when(gc).setBackground(any());
        doAnswer(i -> runs.add(new Run(i.getArgument(0), i.getArgument(1), i.getArgument(2), font[0], foreground[0])))
                .when(gc).drawString(anyString(), anyInt(), anyInt(), anyBoolean());
        doAnswer(i -> fills.add(new Fill(i.getArgument(0), i.getArgument(1), i.getArgument(2), i.getArgument(3), background[0])))
                .when(gc).fillRectangle(anyInt(), anyInt(), anyInt(), anyInt());
        doAnswer(i -> lines.add(new int[] {i.getArgument(0), i.getArgument(1), i.getArgument(2), i.getArgument(3)}))
                .when(gc).drawLine(anyInt(), anyInt(), anyInt(), anyInt());
        return gc;
    }

    static double measured(String text, Font font) {
        return FontMetricsUtil.getFontSize(text, font).x() * FontMetricsUtil.dpiScale();
    }

    Run run(String text) {
        return runs.stream().filter(r -> r.text.equals(text)).findFirst()
                .orElseThrow(() -> new AssertionError(text + " not drawn: " + texts()));
    }

    List<String> texts() {
        return runs.stream().map(Run::text).collect(Collectors.toList());
    }
}
