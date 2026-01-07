package dev.equo.swt;

import dev.equo.swt.size.PointD;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;

import java.io.InputStream;

public class ImageMetricUtil {
    public static PointD getImageSize(InputStream inputStream) {
        ImageLoader loader = new ImageLoader();
        ImageData[] imageDataArray = loader.load(inputStream);
        if (imageDataArray != null && imageDataArray.length > 0) {
            ImageData imageData = imageDataArray[0];
            return new PointD(imageData.width, imageData.height);
        }
        return new PointD(0, 0);
    }

    public static PointD getImageSize(ImageData imageData) {
        return new PointD(imageData.width, imageData.height);
    }

    public static PointD getImageSize(Image image) {
        if (image == null) return null;
        return getImageSize(image.getImageData());
    }
}
