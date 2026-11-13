package dev.equo.swt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;

/**
 * A control a build adds to the main toolbar's optional controls row, which the Flutter side draws
 * at the bar's trailing end, found through {@link ServiceLoader}. The bar reserves the row's room
 * while any control in it shows.
 */
public interface MainToolbarControl {

    /** Whether the control shows under {@code flags}, as the Flutter side decides it. */
    boolean shown(ConfigFlags flags);

    /** Whether any control this build adds shows under {@code flags}. */
    static boolean anyShown(ConfigFlags flags) {
        for (MainToolbarControl control : Found.CONTROLS) {
            if (control.shown(flags)) return true;
        }
        return false;
    }

    final class Found {
        private Found() {
        }

        static final List<MainToolbarControl> CONTROLS = load();

        private static List<MainToolbarControl> load() {
            List<MainToolbarControl> controls = new ArrayList<>();
            ServiceLoader.load(MainToolbarControl.class, MainToolbarControl.class.getClassLoader())
                    .forEach(controls::add);
            return Collections.unmodifiableList(controls);
        }
    }
}
