package org.eclipse.swt.widgets;

import dev.equo.swt.harness.WebDisplayHarness;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** The browser reports its language when it connects, and a spinner writes its decimals the way that language does. */
@Tag("flutter-it")
class SpinnerBrowserLocaleFlutterTest {

    private final WebDisplayHarness flutter = new WebDisplayHarness();

    @AfterEach
    void shutdown() {
        flutter.teardown();
    }

    @Test
    @SuppressWarnings("unchecked")
    void aSpinnerWritesItsDecimalsInTheBrowsersLanguage() {
        Display display = flutter.boot();
        flutter.settle();
        Locale browser = Locale.forLanguageTag(flutter.devTools().evaluate("navigator.language").getAsString());

        assertThat(((DartDisplay) display.getImpl()).clientLocale).isEqualTo(browser);

        Shell shell = new Shell(display);
        Spinner spinner = new Spinner(shell, SWT.NONE);
        spinner.setValues(123, 0, 1000, 2, 1, 10);
        shell.open();
        flutter.settle();
        String separator = String.valueOf(DecimalFormatSymbols.getInstance(browser).getDecimalSeparator());

        assertThat(spinner.getText()).isEqualTo("1" + separator + "23");
        Map<String, Object> state = (Map<String, Object>) flutter.queryState(spinner).get("state");
        assertThat(state.get("decimalSeparator")).isEqualTo(separator);
    }
}
