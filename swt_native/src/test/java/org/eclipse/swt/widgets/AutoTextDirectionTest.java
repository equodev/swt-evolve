package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.internal.BidiUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * With SWT.AUTO_TEXT_DIRECTION, a control holding text takes its direction from the text's first
 * strong Bidi character, and answers its orientation when the text has none.
 */
@Tag("native-unit")
class AutoTextDirectionTest {

    private static final String HEBREW = "שלום";
    private static final String ARABIC = "مرحبا";
    private static final String LATIN = "hello";

    private Shell shell;

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
        shell = DartMocks.dartShell();
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    private static Control create(String kind, Composite parent) {
        switch (kind) {
            case "Label": return new Label(parent, SWT.NONE);
            case "Button": return new Button(parent, SWT.PUSH);
            case "Link": return new Link(parent, SWT.NONE);
            case "Group": return new Group(parent, SWT.NONE);
            case "Text": return new Text(parent, SWT.SINGLE);
            default: throw new IllegalArgumentException(kind);
        }
    }

    private static void setText(Control control, String text) {
        if (control instanceof Label) ((Label) control).setText(text);
        else if (control instanceof Button) ((Button) control).setText(text);
        else if (control instanceof Link) ((Link) control).setText(text);
        else if (control instanceof Group) ((Group) control).setText(text);
        else ((Text) control).setText(text);
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"Label", "Button", "Link", "Group", "Text"})
    @DisplayName("right-to-left text in a left-to-right control, AUTO set after the text")
    void rightToLeftTextAfter(String kind) {
        Control control = create(kind, shell);
        setText(control, HEBREW);
        control.setTextDirection(SWT.AUTO_TEXT_DIRECTION);

        assertThat(control.getTextDirection()).isEqualTo(SWT.RIGHT_TO_LEFT);
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"Label", "Button", "Link", "Group", "Text"})
    @DisplayName("the direction follows each new text when AUTO is set first")
    void followsTheText(String kind) {
        Control control = create(kind, shell);
        control.setTextDirection(SWT.AUTO_TEXT_DIRECTION);

        setText(control, ARABIC);
        assertThat(control.getTextDirection()).isEqualTo(SWT.RIGHT_TO_LEFT);
        setText(control, LATIN);
        assertThat(control.getTextDirection()).isEqualTo(SWT.LEFT_TO_RIGHT);
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"Label", "Button", "Link", "Group", "Text"})
    @DisplayName("latin text in a right-to-left control is left to right")
    void latinTextInARightToLeftControl(String kind) {
        Composite rtl = new Composite(shell, SWT.RIGHT_TO_LEFT);
        Control control = create(kind, rtl);
        setText(control, LATIN);
        control.setTextDirection(SWT.AUTO_TEXT_DIRECTION);

        assertThat(control.getOrientation()).isEqualTo(SWT.RIGHT_TO_LEFT);
        assertThat(control.getTextDirection()).isEqualTo(SWT.LEFT_TO_RIGHT);
    }

    @Test
    @DisplayName("the first strong character decides, not the later ones")
    void firstStrongCharacterDecides() {
        Label latinFirst = new Label(shell, SWT.NONE);
        latinFirst.setText("12 " + LATIN + " " + HEBREW);
        latinFirst.setTextDirection(SWT.AUTO_TEXT_DIRECTION);
        Label hebrewFirst = new Label(shell, SWT.NONE);
        hebrewFirst.setText("12 " + HEBREW + " " + LATIN);
        hebrewFirst.setTextDirection(SWT.AUTO_TEXT_DIRECTION);

        assertThat(latinFirst.getTextDirection()).isEqualTo(SWT.LEFT_TO_RIGHT);
        assertThat(hebrewFirst.getTextDirection()).isEqualTo(SWT.RIGHT_TO_LEFT);
    }

    @Test
    @DisplayName("a Link's direction comes from the text it shows, not from its markup")
    void linkMarkupIsNotText() {
        Link link = new Link(shell, SWT.NONE);
        link.setText("<a href=\"x\">" + HEBREW + "</a>");
        link.setTextDirection(SWT.AUTO_TEXT_DIRECTION);

        assertThat(link.getTextDirection()).isEqualTo(SWT.RIGHT_TO_LEFT);
    }

    @Test
    @DisplayName("text with no strong character answers the orientation")
    void noStrongCharacter() {
        Composite rtl = new Composite(shell, SWT.RIGHT_TO_LEFT);
        Label label = new Label(rtl, SWT.NONE);
        label.setText("12 - 34");
        label.setTextDirection(SWT.AUTO_TEXT_DIRECTION);

        assertThat(label.getTextDirection()).isEqualTo(SWT.RIGHT_TO_LEFT);
    }

    @Test
    @DisplayName("BidiUtil resolves a text as the controls do")
    void bidiUtilResolvesAsTheControls() {
        assertThat(BidiUtil.resolveTextDirection("12 " + HEBREW + " " + LATIN)).isEqualTo(SWT.RIGHT_TO_LEFT);
        assertThat(BidiUtil.resolveTextDirection(LATIN)).isEqualTo(SWT.LEFT_TO_RIGHT);
        assertThat(BidiUtil.resolveTextDirection("12 - 34")).isEqualTo(SWT.NONE);
        assertThat(BidiUtil.resolveTextDirection(null)).isEqualTo(SWT.NONE);
    }

    @Test
    @DisplayName("a separator Label and an arrow Button hold no text and answer the orientation")
    void separatorAndArrow() {
        Label separator = new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL);
        separator.setText(HEBREW);
        separator.setTextDirection(SWT.AUTO_TEXT_DIRECTION);
        Button arrow = new Button(shell, SWT.ARROW);
        arrow.setText(HEBREW);
        arrow.setTextDirection(SWT.AUTO_TEXT_DIRECTION);

        assertThat(separator.getTextDirection()).isEqualTo(SWT.LEFT_TO_RIGHT);
        assertThat(arrow.getTextDirection()).isEqualTo(SWT.LEFT_TO_RIGHT);
    }
}
