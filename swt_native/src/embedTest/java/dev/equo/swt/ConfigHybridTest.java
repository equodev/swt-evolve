package dev.equo.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.widgets.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The part of {@link Config}'s Dart-or-native choice that needs a native widget: a native parent,
 * and ids shared by native and Dart widgets. Only the embedded backend hosts native widgets; the
 * rest of Config's choice is covered by {@code ConfigTest} in the main suite.
 */
class ConfigHybridTest {

    private static Config.Impl initialDefault;

    @BeforeAll
    static void keep_the_process_default() {
        initialDefault = Config.defaultImpl;
    }

    @AfterAll
    static void restore_the_process_default() {
        Config.defaultImpl = initialDefault;
    }

    @BeforeEach
    void restore_config_before() {
        restoreConfig();
    }

    @AfterEach
    void restore_config_after() {
        restoreConfig();
    }

    private static void restoreConfig() {
        Config.defaultImpl = Config.Impl.eclipse;
        Config.reset();
    }

    @Nested
    class WithParent {
        @Test
        void should_default_to_swt_for_widget() {
            assertThat(Config.isEquo(CTabFolder.class, Mocks.shell())).isFalse();
        }

    }

    @Nested
    @DisabledOnOs(OS.LINUX)
    @Tag("metal")
    class SwtAndDartMatchesWithTracker {

        private IdWidgetTracker tracker;
        private Display display;

        @BeforeEach
        void tracker() {
            FlutterLibraryLoader.initialize();
            display = new Display();
            tracker = new IdWidgetTracker();
            tracker.startTracking();
        }

        @AfterEach
        void stop() {
            tracker.stopTracking();
            display.dispose();
        }

        @ParameterizedTest
        @ValueSource(classes = {Optional.class, Composite.class})
        void composite_should_match_ids(Class<?> useEquo) {
            Config.useEquo(useEquo);
            Shell s = new Shell(display);

            assertThat(Config.getId(Composite.class, s)).isEqualTo("/Shell/-1/Composite/1");

            Composite c1 = new Composite(s, SWT.NONE);
            assertThat(c1.getImpl()).isInstanceOf((useEquo == Composite.class) ? DartComposite.class : SwtComposite.class);
            assertThat(tracker.getWidgetId(c1)).as("tracker id !=").isEqualTo("/Shell/-1/Composite/1");
            assertThat(Config.getId(Composite.class, s)).isEqualTo("/Shell/-1/Composite/2");
            assertThat(Config.getId(Composite.class, c1)).isEqualTo("/Shell/-1/Composite/1/Composite/1");
        }

        @ParameterizedTest
        @ValueSource(classes = {Optional.class, Composite.class, CTabFolder.class})
        void composite_in_ctabfolder_should_match_ids(Class<?> useEquo) {
            Config.useEclipse(CTabFolder.class);
            Config.useEquo(useEquo);
            Shell s = new Shell(display);
            assertThat(Config.getId(CTabFolder.class, s)).isEqualTo("/Shell/-1/CTabFolder/1");
            CTabFolder c1 = new CTabFolder(s, SWT.NONE);
            assertThat(c1.getImpl()).isInstanceOf((useEquo == CTabFolder.class) ? DartCTabFolder.class : SwtCTabFolder.class);

            assertThat(tracker.getWidgetId(c1)).as("tracker id !=").isEqualTo("/Shell/-1/CTabFolder/1");
            assertThat(Config.getId(CTabFolder.class, s)).isEqualTo("/Shell/-1/CTabFolder/2");
            assertThat(Config.getId(Composite.class, c1)).isEqualTo("/Shell/-1/CTabFolder/1/Composite/1");

            Composite c2 = new Composite(c1, SWT.NONE);
            assertThat(c2.getImpl()).isInstanceOf((useEquo == Composite.class) ? DartComposite.class : SwtComposite.class);

            assertThat(tracker.getWidgetId(c2)).as("tracker id !=").isEqualTo("/Shell/-1/CTabFolder/1/Composite/1");
            assertThat(Config.getId(Composite.class, c1)).isEqualTo("/Shell/-1/CTabFolder/1/Composite/2");
        }
    }
}
