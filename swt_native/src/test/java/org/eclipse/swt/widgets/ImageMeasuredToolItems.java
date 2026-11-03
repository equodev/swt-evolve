package org.eclipse.swt.widgets;

import dev.equo.swt.Config;
import dev.equo.swt.ConfigFlags;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Turns {@code bigger_icon_size} off for each test, so a ToolItem is measured from its image as
 * native SWT measures it.
 */
public class ImageMeasuredToolItems implements BeforeEachCallback, AfterEachCallback {

    private ConfigFlags flags;
    private boolean biggerIconSize;

    @Override
    public void beforeEach(ExtensionContext context) {
        flags = Config.getConfigFlags();
        biggerIconSize = flags.bigger_icon_size;
        flags.bigger_icon_size = false;
    }

    @Override
    public void afterEach(ExtensionContext context) {
        flags.bigger_icon_size = biggerIconSize;
        Config.setConfigFlags(flags);
    }
}
