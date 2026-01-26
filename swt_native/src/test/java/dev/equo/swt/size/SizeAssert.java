package dev.equo.swt.size;

import org.eclipse.swt.graphics.Point;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

public class SizeAssert {

    private static final double TEXT_TOLERANCE_PERCENT = 12/100.0; // 12%
    private static final double TOLERANCE_PERCENT = 5/100.0; // 5%
    public static final int WAIT = 5;
    protected TestInfo info;

    @BeforeEach
    void info(TestInfo info) {
        this.info = info;
    }

    static <R> R assertCompletes(CompletableFuture<R> result) {
        long timeout = System.currentTimeMillis() + (WAIT * 1000);
        while (!result.isDone() && System.currentTimeMillis() < timeout) {
            // Pump messages on main thread while waiting for result
            GenericSizeBridge.pumpMessages(1);
        }
        assertThat(result).as("future did not compete in %s", WAIT).isDone();
        try {
            return result.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected Consumer<Measure> similarSize(Measure expected) {
        return m -> {
            Point flutter = expected.widget;
            Point java = m.widget;

            double xDiff = Math.abs(flutter.x - java.x);
            double yDiff = Math.abs(flutter.y - java.y);

            double xTolerance = Math.max(1, Math.ceil(flutter.x * TOLERANCE_PERCENT)+1);
            double yTolerance = Math.max(1, Math.ceil(flutter.y * TOLERANCE_PERCENT)+1);

            String message = String.format(
                    "%s: Flutter(%d, %d) vs Java(%d, %d)",
                    info.getDisplayName(), flutter.x, flutter.y, java.x, java.y
            );

            assertThat(xDiff)
                    .as("%s - X diff (%.2f) exceeds tolerance (%.2f)", message, xDiff, xTolerance)
                    .isLessThanOrEqualTo(xTolerance);
            assertThat(yDiff)
                    .as("%s - Y diff (%.2f) exceeds tolerance (%.2f)", message, yDiff, yTolerance)
                    .isLessThanOrEqualTo(yTolerance);

            System.out.println("PASS WID size: " + message);
        };
    }

    public Consumer<Measure> similarTextSize(Measure expected) {
        return p -> similarTextSize(expected.text).accept(p.text);
    }

    public Consumer<Measure> similarImageSize(Measure expected) {
        return p -> assertThat(p.image).isEqualTo(expected.image);
    }

    public Consumer<PointD> similarTextSize(PointD flutter) {
        return java -> {
            String message;
            if (java == null && flutter == null) {
                message = String.format("%s: No text in Flutter and Java", info.getDisplayName());
            } else if (flutter == null && java.x() == 0) {
                message = String.format("%s: No text in Flutter, empty text in Java", info.getDisplayName());
            } else {
                assertThat(java).as("Java text size is null but Flutter is %s", flutter).isNotNull();
                assertThat(flutter).as("Flutter text size is null but Java is %s", java).isNotNull();
                double xDiff = (long) Math.abs(flutter.x() - java.x());
                double yDiff = (long) Math.abs(flutter.y() - java.y());

                double xTolerance = Math.max(2, Math.ceil(flutter.x() * TEXT_TOLERANCE_PERCENT) + 1);
                double yTolerance = Math.max(2, Math.ceil(flutter.y() * TEXT_TOLERANCE_PERCENT) + 1);

                message = String.format(
                        "%s: Flutter(%.2f, %.2f) vs Java(%.2f, %.2f)",
                        info.getDisplayName(), flutter.x(), flutter.y(), java.x(), java.y()
                );

                assertThat(xDiff)
                        .as("%s - X diff (%.2f) exceeds tolerance (%.2f)", message, xDiff, xTolerance)
                        .isLessThanOrEqualTo(xTolerance);
                assertThat(yDiff)
                        .as("%s - Y diff (%.2f) exceeds tolerance (%.2f)", message, yDiff, yTolerance)
                        .isLessThanOrEqualTo(yTolerance);
            }
            System.out.println("PASS TXT size: " + message);
        };
    }

}
