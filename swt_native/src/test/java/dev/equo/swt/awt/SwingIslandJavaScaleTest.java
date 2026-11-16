package dev.equo.swt.awt;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** The JVM's scale an island's mirror lays Swing out at, read as the JDK reads it. */
class SwingIslandJavaScaleTest {

    @Test
    @DisplayName("every form the JDK reads names the same scale")
    void forms() {
        assertThat(SwingIslandHost.javaScale("true", "1")).isEqualTo(1.0);
        assertThat(SwingIslandHost.javaScale("true", "1.5")).isEqualTo(1.5);
        assertThat(SwingIslandHost.javaScale("true", "1.25x")).isEqualTo(1.25);
        assertThat(SwingIslandHost.javaScale("true", "100%")).isEqualTo(1.0);
        assertThat(SwingIslandHost.javaScale("true", "150%")).isEqualTo(1.5);
        assertThat(SwingIslandHost.javaScale("true", "144dpi")).isEqualTo(1.5);
    }

    @Test
    @DisplayName("unset or unreadable leaves the monitor's scale, and a disabled scale is 1")
    void noScale() {
        assertThat(SwingIslandHost.javaScale("true", null)).isEqualTo(0.0);
        assertThat(SwingIslandHost.javaScale("true", "big")).isEqualTo(0.0);
        assertThat(SwingIslandHost.javaScale("true", "0")).isEqualTo(0.0);
        assertThat(SwingIslandHost.javaScale("false", "150%")).isEqualTo(1.0);
    }

    @Test
    @DisplayName("what the JDK's graphics environment says it took is what is read off it")
    void takenScale() {
        // The three answers the JDK's own two calls give, from a stand-in with their names: the
        // class the JVM really has is unreachable here (see below), so what this pins is the pair
        // of names and the arithmetic over them, not the JDK's behaviour.
        assertThat(SwingIslandHost.takenScale(Took150.class.getName())).isEqualTo(1.5);
        assertThat(SwingIslandHost.takenScale(ScalingOff.class.getName())).isEqualTo(1.0);
        assertThat(SwingIslandHost.takenScale(TakesTheMonitors.class.getName()))
                .as("no scale of its own: the monitor's, which the mirror reads as 0")
                .isEqualTo(0.0);
    }

    @Test
    @DisplayName("an unreachable graphics environment names no scale, and the properties answer")
    void thisJvm() {
        assertThat(SwingIslandHost.takenScale("sun.java2d.NoSuchEnvironment")).isNull();
        assertThat(SwingIslandHost.takenScale(SwingIslandHost.JDK_GRAPHICS_ENVIRONMENT))
                .as("this suite runs on a plain JDK, which exports sun.java2d to no unnamed module, "
                        + "so the JDK's own reading can only be reproduced under the engine's "
                        + "launch flags; the properties it read are what is left here")
                .isNull();
        assertThat(SwingIslandHost.javaScale()).isEqualTo(
                SwingIslandHost.javaScale(System.getProperty("sun.java2d.uiScale.enabled", "true"),
                        System.getProperty("sun.java2d.uiScale")));
    }

    /** A JVM that took 1.5, as {@code SunGraphicsEnvironment} reports one. */
    public static class Took150 {
        public static boolean isUIScaleEnabled() {
            return true;
        }

        public static double getDebugScale() {
            return 1.5;
        }
    }

    /** {@code -Dsun.java2d.uiScale.enabled=false}: Swing draws at 1 whatever the monitor says. */
    public static class ScalingOff {
        public static boolean isUIScaleEnabled() {
            return false;
        }

        public static double getDebugScale() {
            throw new AssertionError("not asked once scaling is off");
        }
    }

    /** Scaling on with no scale set: the JDK takes each monitor's, and reports -1 here. */
    public static class TakesTheMonitors {
        public static boolean isUIScaleEnabled() {
            return true;
        }

        public static double getDebugScale() {
            return -1;
        }
    }
}
