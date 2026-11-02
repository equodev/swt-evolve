package org.eclipse.swt.widgets;

import dev.equo.swt.SerializeTestBase;
import dev.equo.swt.harness.WireValues;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.Mocks.shell;

/**
 * A DateTime nobody has set a value on holds the moment it was created, as every native backend
 * does: win32's control starts at the system time, gtk's at {@code Calendar.getInstance()}, cocoa's
 * at {@code NSCalendarDate.calendarDate()}. A CALENDAR holds today at 00:00:00, as on win32 and gtk,
 * which keep a calendar's time in fields of their own that start at zero.
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
    void anUntouchedCalendarIsTodayAtMidnight() {
        LocalDate before = LocalDate.now();
        DateTime w = new DateTime(shell(), SWT.CALENDAR);
        LocalDate after = LocalDate.now();

        assertThat(LocalDate.of(w.getYear(), w.getMonth() + 1, w.getDay())).isBetween(before, after);
        assertThat(LocalTime.of(w.getHours(), w.getMinutes(), w.getSeconds())).isEqualTo(LocalTime.MIDNIGHT);
    }

    @Test
    void theClientIsSentToday() {
        LocalDate before = LocalDate.now();
        DateTime w = new DateTime(shell(), SWT.DATE | SWT.DROP_DOWN | SWT.MEDIUM);
        LocalDate after = LocalDate.now();

        Map<String, Object> state = serialize(w);
        LocalDate sent = LocalDate.of(field(state, "year"), field(state, "month") + 1, field(state, "day"));
        assertThat(sent).isBetween(before, after);
    }

    private static int field(Map<String, Object> state, String name) {
        List<Map<String, Object>> holders = WireValues.find(state, m -> m.get(name) instanceof Number);
        assertThat(holders).as("%s in %s", name, state).isNotEmpty();
        return ((Number) holders.get(0).get(name)).intValue();
    }
}
