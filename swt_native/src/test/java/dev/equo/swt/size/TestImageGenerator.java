package dev.equo.swt.size;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Utility to generate test images in various formats for ImageSizeTest
 *
 * To compile and run:
 *   cd swt-evolve/swt_native
 *   javac -d build/test-classes src/test/java/dev/equo/swt/size/TestImageGenerator.java
 *   java -cp build/test-classes dev.equo.swt.size.TestImageGenerator
 */
public class TestImageGenerator {

    public static void main(String[] args) throws IOException {
        String resourcePath = "src/test/resources/images/";

        // Create images directory if it doesn't exist
        File imagesDir = new File(resourcePath);
        imagesDir.mkdirs();

        // Generate small (16x16) and medium (48x48) images in each format
        int[] sizes = {16, 48};
        String[] rasterFormats = {"png", "jpg", "gif", "bmp", "webp"};

        // Generate raster images (PNG, JPG, GIF, BMP, WEBP)
        for (int size : sizes) {
            for (String format : rasterFormats) {
                generateImage(resourcePath + size + "x" + size + "." + format, size, size, format);
            }
        }

        // Generate SVG images (vector format - text-based)
        for (int size : sizes) {
            generateSVG(resourcePath + size + "x" + size + ".svg", size, size);
        }

        System.out.println("Test images generated successfully!");
    }

    private static void generateImage(String filePath, int width, int height, String format) throws IOException {
        // Create a colored image with a pattern
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        // Fill with a gradient based on size
        g2d.setColor(new Color(100, 150, 200));
        g2d.fillRect(0, 0, width, height);

        // Add a border
        g2d.setColor(Color.BLACK);
        g2d.drawRect(0, 0, width - 1, height - 1);

        // Add some pattern in the middle
        g2d.setColor(Color.WHITE);
        int centerX = width / 2;
        int centerY = height / 2;
        int rectSize = Math.max(2, width / 4);
        g2d.fillRect(centerX - rectSize/2, centerY - rectSize/2, rectSize, rectSize);

        g2d.dispose();

        // Write the image
        File outputFile = new File(filePath);
        boolean written = ImageIO.write(image, format, outputFile);
        if (written) {
            System.out.println("Generated: " + filePath);
        } else {
            System.out.println("SKIPPED (no writer available): " + filePath + " - format '" + format + "' not supported by ImageIO");
        }
    }

    private static void generateSVG(String filePath, int width, int height) throws IOException {
        // Create SVG with similar pattern as raster images
        String svg = String.format("""
            <?xml version="1.0" encoding="UTF-8"?>
            <svg width="%d" height="%d" xmlns="http://www.w3.org/2000/svg">
              <!-- Background -->
              <rect x="0" y="0" width="%d" height="%d" fill="rgb(100,150,200)"/>
              <!-- Border -->
              <rect x="0" y="0" width="%d" height="%d" fill="none" stroke="black" stroke-width="1"/>
              <!-- Center pattern -->
              <rect x="%d" y="%d" width="%d" height="%d" fill="white"/>
            </svg>
            """,
            width, height,
            width, height,
            width, height,
            width / 2 - Math.max(2, width / 4) / 2,
            height / 2 - Math.max(2, height / 4) / 2,
            Math.max(2, width / 4),
            Math.max(2, height / 4)
        );

        File outputFile = new File(filePath);
        try (FileWriter writer = new FileWriter(outputFile)) {
            writer.write(svg);
        }
        System.out.println("Generated: " + filePath);
    }
}
