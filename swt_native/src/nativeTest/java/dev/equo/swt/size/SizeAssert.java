package dev.equo.swt.size;

import dev.equo.swt.harness.FlutterHarness;
import dev.equo.swt.harness.StallTolerantDeadline;
import org.eclipse.swt.graphics.Point;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

public class SizeAssert {

    private static final double TEXT_TOLERANCE_PERCENT = 12/100.0; // 12%
    private static final double TOLERANCE_PERCENT = 5/100.0; // 5%
    /**
     * Seconds of <em>running</em> time a measurement round-trip is given before the test gives up.
     * The loop exits the moment the result lands, so a generous number costs a passing run nothing.
     *
     * <p>Raising it is not what makes this wait reliable, and three raises did not: a round-trip is
     * either tens of milliseconds or never, with no tail in between to grow into. The real cause is
     * the process freezing wholesale, which {@link StallTolerantDeadline} discounts.
     *
     * <p>Override with {@code -Dsize.wait.seconds} to debug a genuinely stuck measurement locally.
     */
    public static final int WAIT = Integer.getInteger("size.wait.seconds", 20);
    protected TestInfo info;

    @BeforeEach
    void info(TestInfo info) {
        this.info = info;
    }

    static <R> R assertCompletes(FlutterHarness bridge, CompletableFuture<R> result) {
        StallTolerantDeadline deadline = new StallTolerantDeadline(WAIT * 1000L);
        while (!result.isDone() && deadline.hasTimeLeft()) {
            bridge.pumpClient();
        }
        assertThat(result).as("measurement did not complete within %ss — the round-trip timed out, "
                + "which says nothing about the size being right%s", WAIT, deadline.stallSuffix()).isDone();
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
