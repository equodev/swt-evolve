package org.eclipse.swt.graphics;

import dev.equo.swt.Config;
import org.eclipse.swt.widgets.Display;

public class ImageUtils {

    public static Image copyImage(Display display, Image image) {
        if (image == null) { return null; }
        if (image.getImpl() instanceof SwtImage si) {
            Image newImage = new Image(display, image.getImageData());
            if (!Config.getConfigFlags().image_disable_icons_replacement && si.filename != null && newImage.getImpl() instanceof DartImage di) {
                di.filename = si.filename;
            }
            return newImage;
        } else {
            return image;
        }
    }

    static String getFilename(String path) {
        String fileName = java.nio.file.Paths.get(path).getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex > 0) ? fileName.substring(0, dotIndex) : fileName;
    }
}
