package org.eclipse.wb.swt;

import org.eclipse.swt.graphics.Image;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * WindowBuilder's plugin-image cache: it decodes the image through {@link SWTResourceManager} and
 * keeps it in a map of its own, under the resource's URL.
 */
public class ResourceManager extends SWTResourceManager {

    private static final Map<String, Image> m_URLImageMap = new HashMap<>();

    public static Image getPluginImage(String url, byte[] bytes) {
        Image image = m_URLImageMap.get(url);
        if (image == null) {
            image = getImage(new ByteArrayInputStream(bytes));
            m_URLImageMap.put(url, image);
        }
        return image;
    }

    public static void disposeImages() {
        SWTResourceManager.disposeImages();
        m_URLImageMap.values().forEach(Image::dispose);
        m_URLImageMap.clear();
    }
}
