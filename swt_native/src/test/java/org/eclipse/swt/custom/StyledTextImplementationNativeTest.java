package org.eclipse.swt.custom;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DartMocks;
import org.eclipse.swt.widgets.Display;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** What {@link DartStyledText} does when it is driven directly rather than through {@link StyledText}. */
@Tag("native-unit")
class StyledTextImplementationNativeTest {

    private Display display;

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
        display = DartMocks.dartDisplay();
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    @Test
    @DisplayName("an implementation built without its API object creates one, once, bound to itself")
    void implementationWithoutApiCreatesOne() {
        DartStyledText impl = new DartStyledText(DartMocks.dartShell(display), SWT.MULTI, null);

        StyledText api = impl.getApi();

        assertThat(api).isNotNull();
        assertThat(api.getImpl()).isSameAs(impl);
        assertThat(impl.getApi()).isSameAs(api);
    }

    @Test
    @DisplayName("a renderer built without its API object creates one, once, bound to itself")
    void rendererWithoutApiCreatesOne() {
        StyledText text = new StyledText(DartMocks.dartShell(display), SWT.MULTI);
        DartStyledTextRenderer impl = new DartStyledTextRenderer(display, text, null);

        StyledTextRenderer api = impl.getApi();

        assertThat(api).isNotNull();
        assertThat(api.getImpl()).isSameAs(impl);
        assertThat(impl.getApi()).isSameAs(api);
    }

    @Test
    @DisplayName("every accessor of the implementation answers the field it is named after")
    void accessorsAnswerTheState() throws Exception {
        StyledText text = new StyledText(DartMocks.dartShell(display), SWT.MULTI | SWT.V_SCROLL);
        DartStyledText impl = (DartStyledText) text.getImpl();
        text.setText("one\ntwo\nthree");
        text.setTabs(6);
        text.setWordWrap(true);
        text.setEditable(false);
        text.setSelection(2, 5);
        text.setIndent(3);
        text.setJustify(true);
        text.setLineSpacing(2);
        text.setTextLimit(50);

        List<String> wrong = new ArrayList<>();
        int checked = 0;
        for (Method extra : DartStyledText.class.getDeclaredMethods()) {
            if (extra.getName().startsWith("_") && extra.getParameterCount() == 0) extra.invoke(impl);
        }
        for (Method accessor : ImplStyledText.class.getDeclaredMethods()) {
            if (!accessor.getName().startsWith("_") || accessor.getParameterCount() != 0) continue;
            Object answered = accessor.invoke(impl);
            Field field = field(DartStyledText.class, accessor.getName().substring(1));
            if (field == null) continue;
            field.setAccessible(true);
            checked++;
            Object held = field.get(impl);
            if (!java.util.Objects.deepEquals(answered, held)
                    && !(answered != null && held != null && answered.getClass().isArray() && held.getClass().isArray()))
                wrong.add(accessor.getName() + ": " + answered + " vs " + held);
        }

        assertThat(wrong).isEmpty();
        assertThat(checked).isGreaterThan(50);
        assertThat(impl._tabLength()).isEqualTo(6);
        assertThat(impl._wordWrap()).isTrue();
        assertThat(impl._editable()).isFalse();
        assertThat(impl._indent()).isEqualTo(3);
        assertThat(impl._justify()).isTrue();
        assertThat(impl._lineSpacing()).isEqualTo(2);
        assertThat(impl._textLimit()).isEqualTo(50);
    }

    private static Field field(Class<?> type, String name) {
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            try {
                return c.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                // look in the superclass
            }
        }
        return null;
    }

    @Test
    @DisplayName("every accessor of the renderer implementation answers without failing")
    void rendererAccessorsAnswer() throws Exception {
        StyledText text = new StyledText(DartMocks.dartShell(display), SWT.MULTI);
        text.setText("one\ntwo");
        DartStyledTextRenderer renderer = (DartStyledTextRenderer) ((DartStyledText) text.getImpl()).renderer.getImpl();

        int called = 0;
        for (Method accessor : DartStyledTextRenderer.class.getDeclaredMethods()) {
            if (!accessor.getName().startsWith("_") || accessor.getParameterCount() != 0) continue;
            accessor.invoke(renderer);
            called++;
        }

        assertThat(called).isGreaterThan(20);
        assertThat(renderer._lineCount()).isEqualTo(2);
        assertThat(renderer._styledText()).isSameAs(text);
    }

    @Test
    @DisplayName("a font whose letters are all as wide as each other makes the renderer fixed pitch")
    void aFontWithOneWidthForEveryLetterIsFixedPitch() {
        StyledText text = new StyledText(DartMocks.dartShell(display), SWT.MULTI);
        DartStyledTextRenderer renderer = (DartStyledTextRenderer) ((DartStyledText) text.getImpl()).renderer.getImpl();
        org.eclipse.swt.graphics.Font mono = new org.eclipse.swt.graphics.Font(display, "Courier New", 12, SWT.NORMAL);
        org.eclipse.swt.graphics.Font proportional = new org.eclipse.swt.graphics.Font(display, "Arial", 12, SWT.NORMAL);

        text.setFont(mono);
        boolean monoPitch = renderer.fixedPitch;
        text.setFont(proportional);
        boolean proportionalPitch = renderer.fixedPitch;

        assertThat(monoPitch).isTrue();
        assertThat(proportionalPitch).isFalse();
        assertThat(StyledTextHelper.isFixedPitch(null)).isFalse();
        mono.dispose();
        assertThat(StyledTextHelper.isFixedPitch(mono)).isFalse();
    }
}
