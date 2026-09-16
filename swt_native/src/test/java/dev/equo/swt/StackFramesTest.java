package dev.equo.swt;

import static org.assertj.core.api.Assertions.assertThat;

import dev.equo.swt.jdk9.StackWalkerFrames;
import org.junit.jupiter.api.Test;

/**
 * The two StackFrames implementations have to agree on what `skip` counts from, or Config's caller
 * checks answer differently depending on which one a fragment carries — and the Throwable one only
 * ships in the Java 8 fragments, where nothing else would notice.
 */
class StackFramesTest {

    private final StackFrames walker = new StackWalkerFrames();
    private final StackFrames throwable = new ThrowableFrames();

    /** at(0) is the caller of at(), i.e. this method, on both implementations. */
    @Test
    void skip_zero_is_the_caller() {
        assertThat(walker.at(0).getMethodName()).isEqualTo("skip_zero_is_the_caller");
        assertThat(throwable.at(0).getMethodName()).isEqualTo("skip_zero_is_the_caller");
    }

    @Test
    void the_two_implementations_report_the_same_frames() {
        for (int skip = 0; skip <= 3; skip++) {
            StackTraceElement fromWalker = oneLevelDown(walker, skip);
            StackTraceElement fromThrowable = oneLevelDown(throwable, skip);
            assertThat(fromThrowable.getClassName())
                    .describedAs("class name at skip=%d", skip)
                    .isEqualTo(fromWalker.getClassName());
            assertThat(fromThrowable.getMethodName())
                    .describedAs("method name at skip=%d", skip)
                    .isEqualTo(fromWalker.getMethodName());
        }
    }

    /** skip=1 from here has to name this test's own method, not the helper. */
    @Test
    void skip_counts_frames_above_the_caller() {
        assertThat(oneLevelDown(walker, 1).getMethodName()).isEqualTo("skip_counts_frames_above_the_caller");
        assertThat(oneLevelDown(throwable, 1).getMethodName()).isEqualTo("skip_counts_frames_above_the_caller");
    }

    private static StackTraceElement oneLevelDown(StackFrames frames, int skip) {
        return frames.at(skip);
    }

    @Test
    void anyMatch_finds_a_frame_by_class_and_method() {
        String self = StackFramesTest.class.getName();
        assertThat(walker.anyMatch(self, "anyMatch_finds_a_frame_by_class_and_method")).isTrue();
        assertThat(throwable.anyMatch(self, "anyMatch_finds_a_frame_by_class_and_method")).isTrue();
        // A null method name matches any method of the class.
        assertThat(walker.anyMatch(self, null)).isTrue();
        assertThat(throwable.anyMatch(self, null)).isTrue();
        assertThat(walker.anyMatch(self, "no_such_method")).isFalse();
        assertThat(throwable.anyMatch(self, "no_such_method")).isFalse();
    }

    /** Past the top of the stack both have to answer null rather than throwing. */
    @Test
    void a_skip_past_the_stack_top_is_null() {
        assertThat(walker.at(10_000)).isNull();
        assertThat(throwable.at(10_000)).isNull();
    }
}
