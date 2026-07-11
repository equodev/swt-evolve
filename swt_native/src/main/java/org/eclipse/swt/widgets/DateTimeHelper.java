package org.eclipse.swt.widgets;

/**
 * Date validity helpers for the Dart {@code DateTime} implementation. SWT's setDay/setMonth/setYear
 * are ignored when the resulting (year, month, day) triple would be an invalid calendar date
 * (e.g. Nov 30 → setMonth(FEB) is a no-op because February has no 30th). Months are 0-based (0..11).
 */
public final class DateTimeHelper {
    private DateTimeHelper() {}

    public static boolean isLeapYear(int year) {
        return (year % 4 == 0 && year % 100 != 0) || year % 400 == 0;
    }

    public static int daysInMonth(int year, int month) {
        switch (month) {
            case 1:
                return isLeapYear(year) ? 29 : 28;
            case 3:
            case 5:
            case 8:
            case 10:
                return 30;
            default:
                return 31;
        }
    }

    public static boolean isValidDate(int year, int month, int day) {
        return month >= 0 && month <= 11 && day >= 1 && day <= daysInMonth(year, month);
    }
}
