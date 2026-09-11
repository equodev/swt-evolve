package dev.equo.swt;

import dev.equo.swt.size.PointD;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Rectangle;

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
        // An Image whose imageData never got populated returns null here rather than throwing.
        // Zero is the right answer: the *Sizes formulas all read a zero image size as "this
        // image contributes nothing to the layout", and Flutter draws nothing for it either.
        if (imageData == null) return new PointD(0, 0);
        return new PointD(imageData.width, imageData.height);
    }

    public static PointD getImageSize(Image image) {
        if (image == null || image.isDisposed()) return new PointD(0, 0);
        // getBounds(), not getImageData(): the size is metadata the Image already holds, while
        // getImageData() pulls the whole pixel buffer back from Flutter. Layout calls this on
        // every pass, so reading it through the pixels would make every measure a round trip.
        Rectangle bounds = image.getBounds();
        if (bounds == null || bounds.width < 0 || bounds.height < 0) return new PointD(0, 0);
        return new PointD(bounds.width, bounds.height);
    }
}
