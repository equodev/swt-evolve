package org.eclipse.swt.widgets;

import dev.equo.swt.harness.WebDisplayHarness;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.text.DecimalFormatSymbols;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The browser reports its language when it connects, and a spinner writes its decimals the way that language does;
 * the browser writes them with the same separator, which it takes from that language itself.
 */
@Tag("flutter-it")
class SpinnerBrowserLocaleFlutterTest {

    private final WebDisplayHarness flutter = new WebDisplayHarness();

    @AfterEach
    void shutdown() {
        flutter.teardown();
    }

    @Test
    void aSpinnerWritesItsDecimalsInTheBrowsersLanguage() {
        Display display = flutter.boot();
        Locale browser = Locale.forLanguageTag(flutter.devTools().evaluate("navigator.language").getAsString());
        // The language comes with the client's ClientReady, which the Display applies as it gets to it.
        DartDisplay impl = (DartDisplay) display.getImpl();
        flutter.pumpUntil(() -> browser.equals(impl.clientLocale), 10_000);

        assertThat(impl.clientLocale).isEqualTo(browser);

        Shell shell = new Shell(display);
        Spinner spinner = new Spinner(shell, SWT.NONE);
        spinner.setValues(123, 0, 1000, 2, 1, 10);
        shell.open();
        flutter.settle();
        String separator = String.valueOf(DecimalFormatSymbols.getInstance(browser).getDecimalSeparator());

        assertThat(spinner.getText()).isEqualTo("1" + separator + "23");
        String browsers = flutter.devTools().evaluate("(1.5).toLocaleString(navigator.language)").getAsString();
        assertThat(browsers).isEqualTo("1" + separator + "5");
    }
}
