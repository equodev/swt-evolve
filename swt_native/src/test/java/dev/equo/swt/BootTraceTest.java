package dev.equo.swt;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class BootTraceTest {

    private final BootTrace.Trace trace = new BootTrace.Trace();

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> steps() {
        return new Gson().fromJson(trace.toJson(), List.class);
    }

    private List<Object> names() {
        return steps().stream().map(s -> s.get("name")).collect(java.util.stream.Collectors.toList());
    }

    @Test
    void stepsStartedInsideASpanAreItsChildren() {
        try (BootTrace.Span outer = trace.span("outer")) {
            try (BootTrace.Span inner = trace.span("inner")) {
                trace.mark("point", false);
            }
        }
        trace.mark("after", false);

        assertThat(steps()).extracting(s -> s.get("name"), s -> ((Number) s.get("depth")).intValue())
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("outer", 0),
                        org.assertj.core.groups.Tuple.tuple("inner", 1),
                        org.assertj.core.groups.Tuple.tuple("point", 2),
                        org.assertj.core.groups.Tuple.tuple("after", 0));
    }

    @Test
    void aSpanLeftByAnExceptionStillEnds() {
        try (BootTrace.Span span = trace.span("throws")) {
            throw new IllegalStateException();
        } catch (IllegalStateException expected) {
        }
        trace.mark("next", false);

        Map<String, Object> thrown = steps().get(0);
        assertThat(((Number) thrown.get("endUs")).longValue()).isGreaterThanOrEqualTo(((Number) thrown.get("startUs")).longValue());
        assertThat(((Number) steps().get(1).get("depth")).intValue()).isZero();
    }

    @Test
    void onceStepsAreRecordedOnlyTheFirstTime() {
        trace.mark("connected", true);
        trace.mark("connected", true);
        trace.endOnce("http /index.html", BootTrace.nowMicros());
        trace.endOnce("http /index.html", BootTrace.nowMicros());

        assertThat(names()).containsExactly("connected", "http /index.html");
    }

    @Test
    void markOnceFromManyThreadsRecordsOneStep() throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(8);
        CountDownLatch go = new CountDownLatch(1);
        for (int i = 0; i < 64; i++) {
            pool.execute(() -> {
                try {
                    go.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                trace.mark("comm.clientConnected", true);
            });
        }
        go.countDown();
        pool.shutdown();
        assertThat(pool.awaitTermination(10, TimeUnit.SECONDS)).isTrue();

        assertThat(names()).containsExactly("comm.clientConnected");
    }

    @Test
    void nothingIsRecordedOnceFrozen() {
        trace.mark("boot", false);
        trace.freeze();

        assertThat(trace.span("second display")).isNull();
        trace.mark("clientReady Shell/2", false);
        trace.mark("comm.bound", true);
        trace.endOnce("http /other", BootTrace.nowMicros());

        assertThat(names()).containsExactly("boot");
    }

    @Test
    void namesAreEscapedInTheJson() {
        trace.mark("http /a\"b\\c\nd", false);

        assertThat(names()).containsExactly("http /a\"b\\c\nd");
    }
}
