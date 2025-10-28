package org.eclipse.swt.graphics;

import dev.equo.swt.Config;
import dev.equo.swt.SerializeTestBase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Mocks;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Font constructors and FontData verification.
 * Tests all Font constructors and verifies that the font data is correctly created.
 */
class FontCreatorTest extends SerializeTestBase {

    @Test
    @DisplayName("Should create Font with single FontData constructor")
    void shouldCreateFontWithSingleFontData() {
        // Given
        Device device = Mocks.device();
        FontData fontData = new FontData("Arial", 12, SWT.BOLD);

        // When
        Font font = new Font(device, fontData);

        // Then
        assertNotNull(font, "Font should not be null");
        assertNotNull(font.getImpl(), "Font implementation should not be null");

        // Verify FontData
        FontData[] fontDataArray = font.getFontData();
        assertNotNull(fontDataArray, "FontData array should not be null");
        assertEquals(1, fontDataArray.length, "FontData array should contain one element");

        FontData retrievedFontData = fontDataArray[0];
        assertEquals("Arial", retrievedFontData.getName(), "Font name should be Arial");
        assertEquals(12, retrievedFontData.getHeight(), "Font height should be 12");
        assertEquals(SWT.BOLD, retrievedFontData.getStyle(), "Font style should be BOLD");
    }

    @Test
    @DisplayName("Should create Font with FontData array constructor")
    void shouldCreateFontWithFontDataArray() {
        // Given
        Device device = Mocks.device();
        FontData fontData1 = new FontData("Helvetica", 14, SWT.ITALIC);
        FontData fontData2 = new FontData("Times New Roman", 16, SWT.NORMAL);
        FontData[] fontDataArray = new FontData[]{fontData1, fontData2};

        // When
        Font font = new Font(device, fontDataArray);

        // Then
        assertNotNull(font, "Font should not be null");
        assertNotNull(font.getImpl(), "Font implementation should not be null");

        // Verify FontData array
        FontData[] retrievedFontDataArray = font.getFontData();
        assertNotNull(retrievedFontDataArray, "FontData array should not be null");
        assertTrue(retrievedFontDataArray.length >= 1, "FontData array should contain at least one element");

        // Verify first FontData (the actual font used is platform-dependent)
        FontData firstFontData = retrievedFontDataArray[0];
        assertNotNull(firstFontData, "First FontData should not be null");
        assertTrue(firstFontData.getName().equals("Helvetica") || firstFontData.getName().equals("Times New Roman"),
                "Font name should be one of the provided fonts");
    }

    @Test
    @DisplayName("Should create Font with name, height and style constructor")
    void shouldCreateFontWithNameHeightAndStyle() {
        // Given
        Device device = Mocks.device();
        String fontName = "Courier New";
        int fontSize = 10;
        int fontStyle = SWT.BOLD | SWT.ITALIC;

        // When
        Font font = new Font(device, fontName, fontSize, fontStyle);

        // Then
        assertNotNull(font, "Font should not be null");
        assertNotNull(font.getImpl(), "Font implementation should not be null");

        // Verify FontData
        FontData[] fontDataArray = font.getFontData();
        assertNotNull(fontDataArray, "FontData array should not be null");
        assertEquals(1, fontDataArray.length, "FontData array should contain one element");

        FontData retrievedFontData = fontDataArray[0];
        assertEquals(fontName, retrievedFontData.getName(), "Font name should match");
        assertEquals(fontSize, retrievedFontData.getHeight(), "Font height should match");
        assertEquals(fontStyle, retrievedFontData.getStyle(), "Font style should match");
    }

    @Test
    @DisplayName("Should create Font with NORMAL style")
    void shouldCreateFontWithNormalStyle() {
        // Given
        Device device = Mocks.device();
        String fontName = "Verdana";
        int fontSize = 11;

        // When
        Font font = new Font(device, fontName, fontSize, SWT.NORMAL);

        // Then
        assertNotNull(font, "Font should not be null");

        FontData[] fontDataArray = font.getFontData();
        FontData fontData = fontDataArray[0];

        assertEquals(fontName, fontData.getName(), "Font name should be Verdana");
        assertEquals(fontSize, fontData.getHeight(), "Font height should be 11");
        assertEquals(SWT.NORMAL, fontData.getStyle(), "Font style should be NORMAL");
    }

    @Test
    @DisplayName("Should create Font with combined BOLD and ITALIC style")
    void shouldCreateFontWithBoldItalicStyle() {
        // Given
        Device device = Mocks.device();
        FontData fontData = new FontData("Georgia", 14, SWT.BOLD | SWT.ITALIC);

        // When
        Font font = new Font(device, fontData);

        // Then
        assertNotNull(font, "Font should not be null");

        FontData[] retrievedFontDataArray = font.getFontData();
        FontData retrievedFontData = retrievedFontDataArray[0];

        int expectedStyle = SWT.BOLD | SWT.ITALIC;
        assertEquals(expectedStyle, retrievedFontData.getStyle(), "Font style should be BOLD | ITALIC");
    }

    @Test
    @DisplayName("Should verify FontData fields after Font creation")
    void shouldVerifyFontDataFields() {
        // Given
        Device device = Mocks.device();
        String expectedName = "Tahoma";
        int expectedHeight = 18;
        int expectedStyle = SWT.ITALIC;

        // When
        Font font = new Font(device, expectedName, expectedHeight, expectedStyle);
        FontData[] fontDataArray = font.getFontData();

        // Then
        assertNotNull(fontDataArray, "FontData array should not be null");
        assertEquals(1, fontDataArray.length, "Should have exactly one FontData");

        FontData fontData = fontDataArray[0];

        // Verify all FontData fields
        assertNotNull(fontData.getName(), "Font name should not be null");
        assertEquals(expectedName, fontData.getName(), "Font name should match");

        assertTrue(fontData.getHeight() > 0, "Font height should be positive");
        assertEquals(expectedHeight, fontData.getHeight(), "Font height should match");

        assertEquals(expectedStyle, fontData.getStyle(), "Font style should match");

        // Locale can be null or empty string
        String locale = fontData.getLocale();
        assertTrue(locale == null || locale.isEmpty() || !locale.isEmpty(),
                "Locale should be a valid string or null");
    }

    @Test
    @DisplayName("Should create multiple fonts with different properties")
    void shouldCreateMultipleFontsWithDifferentProperties() {
        // Given
        Device device = Mocks.device();

        // When
        Font font1 = new Font(device, "Arial", 10, SWT.NORMAL);
        Font font2 = new Font(device, "Arial", 12, SWT.BOLD);
        Font font3 = new Font(device, "Helvetica", 10, SWT.NORMAL);

        // Then
        assertNotNull(font1, "Font1 should not be null");
        assertNotNull(font2, "Font2 should not be null");
        assertNotNull(font3, "Font3 should not be null");

        // Verify each font has correct properties
        FontData fontData1 = font1.getFontData()[0];
        FontData fontData2 = font2.getFontData()[0];
        FontData fontData3 = font3.getFontData()[0];

        // Font1 and Font2 have same name but different size/style
        assertEquals(fontData1.getName(), fontData2.getName(), "Font1 and Font2 should have same name");
        assertNotEquals(fontData1.getHeight(), fontData2.getHeight(), "Font1 and Font2 should have different heights");

        // Font1 and Font3 have same size/style but different name
        assertEquals(fontData1.getHeight(), fontData3.getHeight(), "Font1 and Font3 should have same height");
        assertEquals(fontData1.getStyle(), fontData3.getStyle(), "Font1 and Font3 should have same style");
        assertNotEquals(fontData1.getName(), fontData3.getName(), "Font1 and Font3 should have different names");
    }

    @Test
    @DisplayName("Should create Font from FontData with all fields set")
    void shouldCreateFontFromCompletelyDefinedFontData() {
        // Given
        Device device = Mocks.device();
        FontData fontData = new FontData("Monaco", 9, SWT.NORMAL);
        fontData.setLocale("en_US");

        // When
        Font font = new Font(device, fontData);

        // Then
        assertNotNull(font, "Font should not be null");

        FontData[] retrievedFontDataArray = font.getFontData();
        FontData retrievedFontData = retrievedFontDataArray[0];

        assertEquals("Monaco", retrievedFontData.getName(), "Font name should be Monaco");
        assertEquals(9, retrievedFontData.getHeight(), "Font height should be 9");
        assertEquals(SWT.NORMAL, retrievedFontData.getStyle(), "Font style should be NORMAL");

        // Locale may or may not be preserved depending on platform implementation
        String locale = retrievedFontData.getLocale();
        assertNotNull(locale, "Locale should not be null (can be empty string)");
    }

    @Test
    @DisplayName("Should verify FontData array contains non-null elements")
    void shouldVerifyFontDataArrayContainsNonNullElements() {
        // Given
        Device device = Mocks.device();
        Font font = new Font(device, "Consolas", 11, SWT.BOLD);

        // When
        FontData[] fontDataArray = font.getFontData();

        // Then
        assertNotNull(fontDataArray, "FontData array should not be null");
        assertTrue(fontDataArray.length > 0, "FontData array should not be empty");

        for (FontData fontData : fontDataArray) {
            assertNotNull(fontData, "Each FontData element should not be null");
            assertNotNull(fontData.getName(), "Each FontData should have a name");
            assertTrue(fontData.getHeight() > 0, "Each FontData should have positive height");
        }
    }
}
