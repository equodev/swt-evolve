package org.eclipse.swt.widgets;

import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.Mocks.shell;

/**
 * A DateTime nobody has set a value on holds the moment it was created, as every native backend
 * does: win32's control starts at the system time, gtk's at {@code Calendar.getInstance()}, cocoa's
 * at {@code NSCalendarDate.calendarDate()}.
 */
class DateTimeStartsAtNowTest extends SerializeTestBase {

    @ParameterizedTest(name = "style {0}")
    @ValueSource(ints = {SWT.DATE | SWT.DROP_DOWN | SWT.MEDIUM, SWT.DATE | SWT.MEDIUM, SWT.CALENDAR})
    void anUntouchedDateIsToday(int style) {
        LocalDate before = LocalDate.now();
        DateTime w = new DateTime(shell(), style);
        LocalDate after = LocalDate.now();

        assertThat(LocalDate.of(w.getYear(), w.getMonth() + 1, w.getDay())).isBetween(before, after);
    }

    @Test
    void anUntouchedTimeIsNow() {
        LocalDateTime before = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        DateTime w = new DateTime(shell(), SWT.TIME);
        LocalDateTime after = LocalDateTime.now();

        LocalDateTime held = before.toLocalDate().atTime(w.getHours(), w.getMinutes(), w.getSeconds());
        if (held.isBefore(before))
            held = held.plusDays(1);
        assertThat(held).isBetween(before, after);
    }

    @Test
    void theClientIsSentToday() {
        LocalDate before = LocalDate.now();
        DateTime w = new DateTime(shell(), SWT.DATE | SWT.DROP_DOWN | SWT.MEDIUM);
        LocalDate after = LocalDate.now();

        String json = serialize(w);
        LocalDate sent = LocalDate.of(field(json, "year"), field(json, "month") + 1, field(json, "day"));
        assertThat(sent).isBetween(before, after);
    }

    private static int field(String json, String name) {
        Matcher m = Pattern.compile("\"" + name + "\":(\\d+)").matcher(json);
        assertThat(m.find()).as("%s in %s", name, json).isTrue();
        return Integer.parseInt(m.group(1));
    }
}
