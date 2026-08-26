package dev.equo.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.CRC32;

public final class ImageDataCodec {

    /**
     * getImageData() hands back a fresh ImageData (and pixel array) on every call, so a static
     * icon reappearing in an unrelated widget's dirty-flush (e.g. every item of a large Tree, on
     * every refresh) would otherwise re-run PngEncoder/Deflater from scratch each time. That cost,
     * multiplied across a tree with hundreds of icons, is enough to stall the UI thread for whole
     * seconds — long enough for the OS to flag the process as not responding while a modal (like a
     * native FileDialog) is trying to open. Cache the encoded bytes by pixel content so an
     * unchanged icon is encoded once. Bounded + access-ordered so a long session cycling through
     * many distinct images doesn't grow this without limit.
     */
    private static final int CACHE_CAPACITY = 4000;
    private static final Map<Long, byte[]> encodedCache = new LinkedHashMap<>(256, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Long, byte[]> eldest) {
            return size() > CACHE_CAPACITY;
        }
    };

    private static long cacheKey(ImageData img) {
        CRC32 crc = new CRC32();
        crc.update(img.data);
        return (crc.getValue() << 24) ^ ((long) img.width << 12) ^ ((long) img.height << 1) ^ img.depth;
    }

    public static byte[] encode(ImageData img) {
        if (img.data == null) return null;

        long key = cacheKey(img);
        synchronized (encodedCache) {
            byte[] cached = encodedCache.get(key);
            if (cached != null) return cached;
        }

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

            byte[] bytes;
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                ldr.save(out, fmt);
                bytes = out.toByteArray();
            }
            synchronized (encodedCache) {
                encodedCache.put(key, bytes);
            }
            return bytes;
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
