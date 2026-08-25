package dev.equo.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public final class ImageDataCodec {

    public static byte[] encode(ImageData img) {
        if (img.data == null) return null;

        try {
            ImageLoader ldr = new ImageLoader();
            ldr.data = new ImageData[]{ img };

            int fmt = switch (img.type) {
                case SWT.IMAGE_JPEG,
                     SWT.IMAGE_PNG,
                     SWT.IMAGE_GIF,
                     SWT.IMAGE_BMP,
                     SWT.IMAGE_ICO  -> img.type;
                default -> SWT.IMAGE_PNG;
            };

            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                ldr.save(out, fmt);
                return out.toByteArray();
            }
        } catch (Exception e) {
            System.err.println("encode error: " + e.getMessage());
            return img.data;
        }
    }

    public static void decode(ImageData target, byte[] encoded) {
        if (encoded == null) { target.data = null; return; }

        try (ByteArrayInputStream in = new ByteArrayInputStream(encoded)) {
            ImageData[] arr = new ImageLoader().load(in);
            if (arr.length > 0) {
                ImageData src = arr[0];

                target.data   = src.data;
                if (target.width  == 0) target.width  = src.width;
                if (target.height == 0) target.height = src.height;
                if (target.depth  == 0) target.depth  = src.depth;
                if (target.type   == SWT.IMAGE_UNDEFINED) target.type = src.type;
                return;
            }
        } catch (Exception e) {
            System.err.println("decode error: " + e.getMessage());
        }
        target.data = encoded;
    }
}
