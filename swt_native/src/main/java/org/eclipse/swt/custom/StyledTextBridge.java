package org.eclipse.swt.custom;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.runtime.Settings;
import dev.equo.swt.FlutterBridge;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class StyledTextBridge {

    public static class RangeModifyPayload {
        public List<Map<String, Object>> styleRanges;

        public RangeModifyPayload(List<Map<String, Object>> styleRanges) {
            this.styleRanges = styleRanges;
        }
    }

    public static class LineInfoModifyPayload {
        public Map<String, Object> lineProperties;

        public LineInfoModifyPayload(Map<String, Object> lineProperties) {
            this.lineProperties = lineProperties;
        }
    }

    public static void hookevents(DartWidget widget) {
        final DslJson<Object> jsonProcessor = new DslJson<>(Settings.withRuntime().allowArrayFormat(true));

        FlutterBridge.onPayload(widget, "RangeModify", payload -> {
            widget.getDisplay().asyncExec(() -> {
                StyledTextRenderer renderer = ((DartStyledText) widget)._renderer();
                DartStyledTextRenderer rendererImpl = (DartStyledTextRenderer) renderer.getImpl();

                // Deserialize JSON
                List<Map<String, Object>> styleRanges;
                if (payload instanceof String) {
                    try {
                        String jsonString = (String) payload;
                        ByteArrayInputStream inputStream = new ByteArrayInputStream(jsonString.getBytes());
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> deserializedList = (List<Map<String, Object>>) jsonProcessor.deserialize(List.class, inputStream);
                        styleRanges = deserializedList;
                    } catch (IOException e) {
                        System.err.println("Error deserializing RangeModify JSON: " + e.getMessage());
                        e.printStackTrace();
                        styleRanges = new ArrayList<>();
                    }
                } else {
                    System.err.println("Unexpected payload type for RangeModify: " + payload.getClass());
                    styleRanges = new ArrayList<>();
                }

                StyledTextBridge.RangeModifyPayload modifyPayload =
                        new StyledTextBridge.RangeModifyPayload(styleRanges);

                StyledTextBridge.setStyleRangesJava(rendererImpl, modifyPayload);
            });
        });
        FlutterBridge.onPayload(widget, "LineInfoModify", payload -> {
            widget.getDisplay().asyncExec(() -> {
                StyledTextRenderer renderer = ((DartStyledText) widget)._renderer();
                DartStyledTextRenderer rendererImpl = (DartStyledTextRenderer) renderer.getImpl();

                // Deserialize JSON
                Map<String, Object> lineProperties;
                if (payload instanceof String) {
                    try {
                        String jsonString = (String) payload;
                        ByteArrayInputStream inputStream = new ByteArrayInputStream(jsonString.getBytes());
                        @SuppressWarnings("unchecked")
                        Map<String, Object> deserializedMap = (Map<String, Object>) jsonProcessor.deserialize(Map.class, inputStream);
                        lineProperties = deserializedMap;
                    } catch (IOException e) {
                        System.err.println("Error deserializing LineInfoModify JSON: " + e.getMessage());
                        e.printStackTrace();
                        lineProperties = new HashMap<>();
                    }
                } else {
                    System.err.println("Unexpected payload type for LineInfoModify: " + payload.getClass());
                    lineProperties = new HashMap<>();
                }

                StyledTextBridge.LineInfoModifyPayload modifyPayload =
                        new StyledTextBridge.LineInfoModifyPayload(lineProperties);

                StyledTextBridge.setLineInfoJava(rendererImpl, modifyPayload);
            });
        });
    }
    public static void applyStyleToChars(StyledCharacter[] chars, StyleRange style, int textLength) {
        if (style != null) {
            int start = Math.max(0, style.start);
            int end = Math.min(style.start + style.length, textLength);

            for (int i = start; i < end; i++) {
                chars[i].applyStyle(style);
            }
        }
    }

    private static Map<String, Object> createTextPayload(String text, int x, int y) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("text", text);
        payload.put("x", x);
        payload.put("y", y);
        return payload;
    }

    public static void drawStyledText(DartStyledText widget,
                                      int x, int y, Caret caret, long styledTextId, FlutterClient client){

        String text = widget._text();
        StyledTextRenderer renderer = widget._renderer();
        DartStyledTextRenderer rendererImpl = (DartStyledTextRenderer) renderer.getImpl();

        // gets StyleRanges from the renderer
        StyleRange[] styles = rendererImpl._styles();

        // gets LineInfo from the renderer
        DartStyledTextRenderer.LineInfo[] lineInfo = rendererImpl.lines;

        // creates the array of chars that will contain the ranges info
        StyledCharacter[] styledChars = new StyledCharacter[text.length()];
        for (int i = 0; i < text.length(); i++) {
            styledChars[i] = new StyledCharacter(text.charAt(i));
        }

        // apply ranges info to chars
        if (styles != null){
            for (StyleRange style : styles) {
                applyStyleToChars(styledChars, style, text.length());
            }
        }

        if (text == null || styledChars == null || text.length() != styledChars.length) return;

        Map<String, Object> payload = createTextPayload(text, x, y);

        if (styledTextId >= 0) {
            payload.put("styledTextId", styledTextId);
        }

        DartCaret dartCaret = (DartCaret) caret.getImpl();

        if (dartCaret != null) {
            Map<String, Object> caretInfo = new HashMap<>();
            caretInfo.put("visible", dartCaret.getVisible());
            // caretInfo.put("blinkRate", caret.getBlinkRate());
            caretInfo.put("offset", widget.getCaretOffset());

            caretInfo.put("width", 1);
            caretInfo.put("height", 16);

            payload.put("caretInfo", caretInfo);
        }

        ArrayList<Map<String, Object>> styleRanges = new ArrayList<>();
        Map<String, Object> lineProperties = new HashMap<>();

        int start = 0;

        while (start < styledChars.length) {
            StyledCharacter base = styledChars[start];
            int end = start + 1;

            while (end < styledChars.length && sameCharacterStyle(base, styledChars[end])) {
                end++;
            }

            Map<String, Object> range = new HashMap<>();
            range.put("startIndex", start);
            range.put("endIndex", end);

            if (base.foreground != null) {
                range.put("foreground", colorToMap(base.foreground));
            }
            if (base.background != null) {
                range.put("background", colorToMap(base.background));
            }
            if (base.font != null) {
                FontData[] fd = base.font.getFontData();
                if (fd.length > 0) {
                    range.put("fontSize", fd[0].getHeight());
                    range.put("fontName", fd[0].getName());
                    range.put("fontStyle", fd[0].getStyle());
                }
            } else {
                if (base.fontStyle != SWT.NORMAL) {
                    range.put("fontStyle", base.fontStyle);
                }
            }
            range.put("borderStyle", base.borderStyle);
            if (base.borderColor != null) {
                range.put("borderColor", colorToMap(base.borderColor));
            }

            range.put("underline", base.underline);
            if (base.underline) {
                range.put("underlineStyle", base.underlineStyle);
            }

            range.put("strikeout", base.strikeout);
            range.put("rise", base.rise);

            styleRanges.add(range);
            start = end;
        }

        lineProperties = convertLineInfoToMap(lineInfo);

        payload.put("styleRanges", styleRanges);
        payload.put("lineProperties", lineProperties);

        System.out.println("send: DrawStyledText: " + payload);
        client.getComm().send("DrawStyledText", payload);

    }

    public static Map<String, Object> convertLineInfoToMap(DartStyledTextRenderer.LineInfo[] lineInfo) {
        Map<String, Object> lineProperties = new HashMap<>();

        if (lineInfo == null) {
            return lineProperties;
        }

        for (int i = 0; i < lineInfo.length; i++) {
            DartStyledTextRenderer.LineInfo info = lineInfo[i];

            if (info != null) {
                Map<String, Object> properties = new HashMap<>();
                boolean hasProperties = false;

                if ((info.flags & (1 << 1)) != 0) {
                    properties.put("alignment", info.alignment);
                    hasProperties = true;
                }

                if ((info.flags & (1 << 2)) != 0) {
                    properties.put("indent", info.indent);
                    hasProperties = true;
                }

                if ((info.flags & (1 << 3)) != 0) {
                    properties.put("justify", info.justify);
                    hasProperties = true;
                }

                if (hasProperties) {
                    lineProperties.put(String.valueOf(i), properties);
                }
            }
        }

        return lineProperties;
    }


    private static Map<String, Integer> colorToMap(Color c) {
        Map<String, Integer> map = new HashMap<>();
        map.put("red", c.getRed());
        map.put("green", c.getGreen());
        map.put("blue", c.getBlue());
        map.put("alpha", 255);
        return map;
    }

    private static boolean sameCharacterStyle(StyledCharacter a, StyledCharacter b) {
        return equals(a.foreground, b.foreground)
                && equals(a.background, b.background)
                && a.fontStyle == b.fontStyle
                && equals(a.font, b.font)
                && a.borderStyle == b.borderStyle
                && equals(a.borderColor, b.borderColor)
                && a.underline == b.underline
                && a.underlineStyle == b.underlineStyle
                && a.strikeout == b.strikeout
                && a.rise == b.rise;
    }

    private static boolean equals(Object a, Object b) {
        return (a == null && b == null) || (a != null && a.equals(b));
    }

    public static void setStyleRangesJava(DartStyledTextRenderer rendererImpl, RangeModifyPayload payload) {
        System.out.println("setStyleRangesJava");

        if (payload == null || payload.styleRanges == null) {
            rendererImpl.setStyleRanges(null, null);
            return;
        }

        List<Map<String, Object>> styleRanges = payload.styleRanges;

        if (styleRanges.isEmpty()) {
            rendererImpl.setStyleRanges(null, null);
            return;
        }

        List<Integer> rangePositions = new ArrayList<>();
        List<StyleRange> stylesList = new ArrayList<>();

        for (Map<String, Object> range : styleRanges) {
            try {
                int startIndex = ((Number) range.get("startIndex")).intValue();
                int endIndex = ((Number) range.get("endIndex")).intValue();

                if (startIndex < 0 || startIndex >= endIndex) {
                    continue;
                }

                int length = endIndex - startIndex;

                StyleRange style = new StyleRange();
                style.start = startIndex;
                style.length = length;

                if (range.containsKey("foreground")) {
                    Map<String, Object> fg = (Map<String, Object>) range.get("foreground");
                    int red = ((Number) fg.get("red")).intValue();
                    int green = ((Number) fg.get("green")).intValue();
                    int blue = ((Number) fg.get("blue")).intValue();
                    style.foreground = new Color(red, green, blue);
                }

                if (range.containsKey("background")) {
                    Map<String, Object> bg = (Map<String, Object>) range.get("background");
                    int red = ((Number) bg.get("red")).intValue();
                    int green = ((Number) bg.get("green")).intValue();
                    int blue = ((Number) bg.get("blue")).intValue();
                    style.background = new Color(red, green, blue);
                }

                if (range.containsKey("fontStyle")) {
                    style.fontStyle = ((Number) range.get("fontStyle")).intValue();
                }

                if (range.containsKey("fontSize") || range.containsKey("fontName")) {
                    int fontSize = range.containsKey("fontSize") ?
                            ((Number) range.get("fontSize")).intValue() : 14;

                    String fontName = range.containsKey("fontName") ?
                            (String) range.get("fontName") : "Segoe UI";

                    style.font = new Font(Display.getDefault(), fontName, fontSize, style.fontStyle);
                }

                if (range.containsKey("underline")) {
                    style.underline = (Boolean) range.get("underline");

                    if (range.containsKey("underlineStyle")) {
                        style.underlineStyle = ((Number) range.get("underlineStyle")).intValue();
                    }
                }

                if (range.containsKey("strikeout")) {
                    style.strikeout = (Boolean) range.get("strikeout");
                }

                if (range.containsKey("rise")) {
                    style.rise = ((Number) range.get("rise")).intValue();
                }

                if (range.containsKey("borderStyle")) {
                    style.borderStyle = ((Number) range.get("borderStyle")).intValue();

                    if (range.containsKey("borderColor")) {
                        Map<String, Object> bc = (Map<String, Object>) range.get("borderColor");
                        int red = ((Number) bc.get("red")).intValue();
                        int green = ((Number) bc.get("green")).intValue();
                        int blue = ((Number) bc.get("blue")).intValue();
                        style.borderColor = new Color(red, green, blue);
                    }
                }

                rangePositions.add(startIndex);
                rangePositions.add(endIndex);
                stylesList.add(style);

            } catch (Exception e) {
                System.err.println("Error processing style range: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // Convert to arrays
        int[] ranges = rangePositions.stream().mapToInt(i -> i).toArray();
        StyleRange[] styles = stylesList.toArray(new StyleRange[0]);

        rendererImpl.setStyleRanges(ranges, styles);
        System.out.println("ranges seteados" + Arrays.toString(rendererImpl._styles()));
    }

    public static void setLineInfoJava(DartStyledTextRenderer rendererImpl, LineInfoModifyPayload payload) {
        if (payload == null || payload.lineProperties == null) {
            System.err.println("Invalid payload for setLineInfoJava: null payload or lineProperties");
            return;
        }
        
        Map<String, Object> lineProps = payload.lineProperties;
        
        if (lineProps == null) return;

        lineProps.forEach((lineIndexStr, propsObj) -> {
            try {
                int lineIndex = Integer.parseInt(lineIndexStr);
                if (propsObj instanceof Map) {
                    Map<String, Object> props = (Map<String, Object>) propsObj;

                    if (props.containsKey("alignment")) {
                        int alignment = ((Number) props.get("alignment")).intValue();
                        rendererImpl.setLineAlignment(lineIndex, 1, alignment);
                    }

                    if (props.containsKey("indent")) {
                        int indent = ((Number) props.get("indent")).intValue();
                        rendererImpl.setLineIndent(lineIndex, 1, indent);
                    }

                    if (props.containsKey("justify")) {
                        boolean justify = (Boolean) props.get("justify");
                        rendererImpl.setLineJustify(lineIndex, 1, justify);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error processing line properties: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}
