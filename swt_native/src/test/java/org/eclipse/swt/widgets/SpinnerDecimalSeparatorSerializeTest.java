package org.eclipse.swt.widgets;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.DecimalFormatSymbols;
import java.util.Locale;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.Mocks.shell;

/**
 * The text of a spinner with digits writes its decimal places with the separator of the host's locale, as every
 * native spinner does, and the client is told which separator that is so that it writes and reads the same one.
 */
class SpinnerDecimalSeparatorSerializeTest extends SerializeTestBase {

    private Locale format;

    @BeforeEach
    void rememberLocale() {
        format = Locale.getDefault(Locale.Category.FORMAT);
    }

    @AfterEach
    void restoreLocale() {
        Locale.setDefault(Locale.Category.FORMAT, format);
    }

    private Spinner spinner(Locale locale) {
        Locale.setDefault(Locale.Category.FORMAT, locale);
        Spinner spinner = new Spinner(shell(), SWT.NONE);
        spinner.setMaximum(100000);
        spinner.setDigits(2);
        spinner.setSelection(123);
        return spinner;
    }

    @Test
    void theTextUsesTheSeparatorOfTheDefaultLocale() {
        assertThat(spinner(Locale.US).getText()).isEqualTo("1.23");
        assertThat(spinner(Locale.GERMANY).getText()).isEqualTo("1,23");
        assertThat(spinner(Locale.FRANCE).getText()).isEqualTo("1,23");
        assertThat(spinner(Locale.forLanguageTag("de-CH")).getText()).isEqualTo("1.23");
    }

    @Test
    void everyPlaceIsWrittenWithTheSeparator() {
        Spinner spinner = spinner(Locale.GERMANY);
        spinner.setMinimum(-100000);

        spinner.setSelection(5);
        assertThat(spinner.getText()).isEqualTo("0,05");
        spinner.setSelection(-123);
        assertThat(spinner.getText()).isEqualTo("-1,23");
        spinner.setDigits(4);
        spinner.setSelection(123);
        assertThat(spinner.getText()).isEqualTo("0,0123");
    }

    @Test
    void aSeparatorThatIsNotAPointIsUsedAsTheLocaleGivesIt() {
        Locale arabic = Locale.forLanguageTag("ar-EG");
        String separator = String.valueOf(DecimalFormatSymbols.getInstance(arabic).getDecimalSeparator());

        assertThat(spinner(arabic).getText()).isEqualTo("1" + separator + "23");
    }

    @Test
    void theClientIsToldTheSeparator() {
        assertThatJson(serialize(spinner(Locale.GERMANY))).node("decimalSeparator").isEqualTo(",");
        assertThatJson(serialize(spinner(Locale.US))).node("decimalSeparator").isEqualTo(".");
    }

    @Test
    void aSpinnerWithoutDigitsHasNoSeparatorInItsText() {
        Locale.setDefault(Locale.Category.FORMAT, Locale.GERMANY);
        Spinner spinner = new Spinner(shell(), SWT.NONE);
        spinner.setSelection(42);

        assertThat(spinner.getText()).isEqualTo("42");
    }

    @Test
    void theNaturalWidthFollowsTheTextOfTheLocale() {
        Spinner german = spinner(Locale.GERMANY);
        Spinner us = spinner(Locale.US);

        assertThat(german.computeSize(SWT.DEFAULT, SWT.DEFAULT).x).isEqualTo(us.computeSize(SWT.DEFAULT, SWT.DEFAULT).x);
    }
}
