package org.eclipse.swt.widgets;

import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@code toString()} starts with the name of the SWT class, as upstream's {@code getName()} gives it
 * ("Button {OK}"). The string is built by the implementation, whose own class is {@code DartButton};
 * applications and Eclipse put these strings in logs and messages, and some match on them.
 */
@Tag("native-unit")
class ToStringNamesApiClassTest {

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
    }

    @AfterEach
    void tearDown() {
        FlutterBridge.set(null);
    }

    @Test
    @DisplayName("a widget's toString() names its SWT class, not the implementation's")
    void widgetsNameTheirApiClass() {
        Shell shell = DartMocks.dartShell();
        Button button = new Button(shell, SWT.PUSH);
        button.setText("OK");
        Label label = new Label(shell, SWT.NONE);
        label.setText("Name");
        Composite composite = new Composite(shell, SWT.NONE);

        assertThat(button.toString()).isEqualTo("Button {OK}");
        assertThat(label.toString()).isEqualTo("Label {Name}");
        assertThat(composite.toString()).startsWith("Composite {");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("org.eclipse.swt.widgets.DartControlFactories#withOwnComputeSize")
    void everyControlNamesItsSwtClass(String name, Function<Shell, Control> factory) {
        Control control = factory.apply(DartMocks.dartShell());

        assertThat(control.toString()).as(name).startsWith(control.getClass().getSimpleName() + " {");
    }

    @Test
    @DisplayName("a layout's toString() names its SWT class, not the implementation's")
    void layoutsNameTheirApiClass() {
        assertThat(new GridLayout().toString()).startsWith("GridLayout {");
        assertThat(new FillLayout().toString()).startsWith("FillLayout {");
        assertThat(new RowLayout().toString()).startsWith("RowLayout {");
        assertThat(new FormLayout().toString()).startsWith("FormLayout {");
        assertThat(new StackLayout().toString()).startsWith("StackLayout {");
    }
}
