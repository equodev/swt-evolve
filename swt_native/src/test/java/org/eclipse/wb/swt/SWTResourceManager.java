package org.eclipse.wb.swt;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * The image cache WindowBuilder generates into applications: it decodes an image from a stream, so
 * the Image is built from pixels alone, and keeps it under the resource's path.
 */
public class SWTResourceManager {

    private static final Map<String, Image> m_imageMap = new HashMap<>();

    protected static Image getImage(InputStream stream) {
        ImageData data = new ImageData(stream);
        return data.transparentPixel > 0
                ? new Image(Display.getCurrent(), data, data.getTransparencyMask())
                : new Image(Display.getCurrent(), data);
    }

    public static Image getImage(String path, byte[] bytes) {
        Image image = m_imageMap.get(path);
        if (image == null) {
            image = getImage(new ByteArrayInputStream(bytes));
            m_imageMap.put(path, image);
        }
        return image;
    }

    public static void disposeImages() {
        m_imageMap.values().forEach(Image::dispose);
        m_imageMap.clear();
    }
}
