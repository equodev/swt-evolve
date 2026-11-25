package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.ConfigFlags;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.AnnotationRulerColumn;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.LineNumberRulerColumn;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A text editor's annotation column fills itself with its own background. An e4 CSS theme can give
 * that column a colour the rest of the ruler does not have (its anonymous Canvas subclass misses the
 * theme's Canvas rules), which shows as a strip beside the line numbers. In a dark theme the column
 * takes the ruler's ground; in a light one it keeps what the application set, as native SWT does.
 */
@Tag("flutter-it")
class RulerColumnDarkGroundFlutterTest {

    private static final RGB RULER = new RGB(30, 31, 34);
    private static final RGB COLUMN = new RGB(47, 47, 47);

    private Display display;
    private ConfigFlags savedFlags;

    @BeforeAll
    static void useEquo() {
        Config.forceEquo();
    }

    @AfterAll
    static void reset() {
        Config.defaultToEclipse();
    }

    @AfterEach
    void tearDown() {
        if (display != null && !display.isDisposed()) display.dispose();
        FlutterBridge.set(null);
        Config.setConfigFlags(savedFlags);
    }

    @Test
    void inADarkTheme_theAnnotationColumnFillsWithTheRulersGround() {
        AnnotationRulerColumn column = editorRuler("dark");

        assertThat(column.getControl().getBackground().getRGB()).isEqualTo(RULER);
    }

    @Test
    void inALightTheme_theAnnotationColumnKeepsTheColourTheApplicationSet() {
        AnnotationRulerColumn column = editorRuler("light");

        assertThat(column.getControl().getBackground().getRGB()).isEqualTo(COLUMN);
    }

    /** An editor whose ruler and annotation column are coloured the way an e4 CSS theme colours them. */
    private AnnotationRulerColumn editorRuler(String theme) {
        savedFlags = Config.getConfigFlags();
        ConfigFlags flags = new ConfigFlags();
        flags.force_theme = theme;
        Config.setConfigFlags(flags);
        FlutterBridge.set(new RecordingBridge());
        display = new Display();

        Shell shell = new Shell(display);
        CompositeRuler ruler = new CompositeRuler();
        AnnotationRulerColumn column = new AnnotationRulerColumn(new AnnotationModel(), 16);
        ruler.addDecorator(0, column);
        ruler.addDecorator(1, new LineNumberRulerColumn());
        SourceViewer viewer = new SourceViewer(shell, ruler, SWT.MULTI);
        viewer.setDocument(new Document("first\nsecond"), new AnnotationModel());

        ruler.getControl().setBackground(new Color(display, RULER));
        column.getControl().setBackground(new Color(display, COLUMN));
        return column;
    }
}
