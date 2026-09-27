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
    private static final Map<Long, byte[]> encodedCache = new LinkedHashMap<Long, byte[]>(256, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Long, byte[]> eldest) {
            return size() > CACHE_CAPACITY;
        }
    };

    private static long cacheKey(ImageData img) {
        CRC32 crc = new CRC32();
        crc.update(img.data);
        // On an indexed image the pixel bytes are palette indices, so two different images can share
        // them and differ only in the palette - a re-paletted ImageData, as the IMAGE_GRAY transform
        // produces. The palette is part of the identity.
        org.eclipse.swt.graphics.PaletteData palette = img.palette;
        if (palette != null) {
            if (palette.isDirect) {
                updateInt(crc, palette.redMask);
                updateInt(crc, palette.greenMask);
                updateInt(crc, palette.blueMask);
            } else if (palette.colors != null) {
                for (org.eclipse.swt.graphics.RGB rgb : palette.colors) {
                    if (rgb == null) continue;
                    crc.update(rgb.red);
                    crc.update(rgb.green);
                    crc.update(rgb.blue);
                }
            }
        }
        updateInt(crc, img.transparentPixel);
        // Transparency is encoded too, so it must be part of the key.
        if (img.maskData != null) crc.update(img.maskData);
        if (img.alphaData != null) crc.update(img.alphaData);
        updateInt(crc, img.alpha);
        return (crc.getValue() << 24) ^ ((long) img.width << 12) ^ ((long) img.height << 1) ^ img.depth;
    }

    private static void updateInt(CRC32 crc, int value) {
        crc.update(value & 0xFF);
        crc.update((value >>> 8) & 0xFF);
        crc.update((value >>> 16) & 0xFF);
        crc.update((value >>> 24) & 0xFF);
    }

    /** The PNG encoder ignores {@code maskData} but keeps {@code alphaData}, so a mask is converted. */
    private static ImageData maskAsAlpha(ImageData img) {
        if (img.maskData == null || img.alphaData != null || img.alpha != -1) return img;
        ImageData mask = img.getTransparencyMask();
        if (mask == null) return img;
        byte[] alpha = new byte[img.width * img.height];
        for (int y = 0; y < img.height; y++) {
            for (int x = 0; x < img.width; x++) {
                alpha[y * img.width + x] = mask.getPixel(x, y) != 0 ? (byte) 0xFF : 0;
            }
        }
        ImageData out = (ImageData) img.clone();
        out.alphaData = alpha;
        out.maskData = null;
        return out;
    }

    /**
     * Scales sub-byte palette channels to full range as native SWT does; {@code PaletteData#getRGB},
     * which the encoder uses, only shifts them, so such images would come out darker.
     */
    private static ImageData widenNarrowChannels(ImageData img) {
        org.eclipse.swt.graphics.PaletteData palette = img.palette;
        if (palette == null || !palette.isDirect) return img;
        int redWidth = channelWidth(palette.redMask);
        int greenWidth = channelWidth(palette.greenMask);
        int blueWidth = channelWidth(palette.blueMask);
        if (redWidth >= 8 && greenWidth >= 8 && blueWidth >= 8) return img;

        ImageData wide = new ImageData(img.width, img.height, 24,
                new org.eclipse.swt.graphics.PaletteData(0xFF0000, 0x00FF00, 0x0000FF));
        // A row at a time: 16-bit 5-6-5 images can be large and this runs on the UI thread.
        int[] row = new int[img.width];
        for (int y = 0; y < img.height; y++) {
            img.getPixels(0, y, img.width, row, 0);
            for (int x = 0; x < img.width; x++) {
                row[x] = widen(row[x], palette);
            }
            wide.setPixels(0, y, img.width, row, 0);
        }
        wide.alpha = img.alpha;
        wide.alphaData = img.alphaData;
        if (img.transparentPixel != -1) {
            wide.transparentPixel = widen(img.transparentPixel, palette);
        }
        return wide;
    }

    private static int widen(int pixel, org.eclipse.swt.graphics.PaletteData palette) {
        return (channel(pixel, palette.redMask) << 16)
                | (channel(pixel, palette.greenMask) << 8)
                | channel(pixel, palette.blueMask);
    }

    /** One channel of {@code pixel}, scaled from its own width to 0..255. */
    private static int channel(int pixel, int mask) {
        int width = channelWidth(mask);
        if (width == 0) return 0;
        int value = (pixel & mask) >>> Integer.numberOfTrailingZeros(mask);
        int max = (1 << width) - 1;
        // Exactly bit replication for the widths that occur here (1, 2, 4, 5, 6).
        return (value * 255 + max / 2) / max;
    }

    private static int channelWidth(int mask) {
        return Integer.bitCount(mask);
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
            ldr.data = new ImageData[]{ widenNarrowChannels(maskAsAlpha(img)) };

            int fmt;
            switch (img.type) {
                case SWT.IMAGE_JPEG:
                case SWT.IMAGE_PNG:
                case SWT.IMAGE_GIF:
                case SWT.IMAGE_BMP:
                case SWT.IMAGE_ICO:
                    fmt = img.type;
                    break;
                default:
                    fmt = SWT.IMAGE_PNG;
            }

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
                if (target.type   == SWT.IMAGE_UNDEFINED) target.type = src.type;
                // encode() may widen the depth, so the layout fields must follow the adopted bytes.
                if (target.depth != src.depth) {
                    target.depth = src.depth;
                    target.palette = src.palette;
                    target.bytesPerLine = src.bytesPerLine;
                    target.scanlinePad = src.scanlinePad;
                }
                return;
            }
        } catch (Exception e) {
            System.err.println("decode error: " + e.getMessage());
        }
        target.data = encoded;
    }
}
