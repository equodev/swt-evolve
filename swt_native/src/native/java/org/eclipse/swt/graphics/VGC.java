package org.eclipse.swt.graphics;

import java.util.*;
import org.eclipse.swt.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;
import java.io.IOException;

@CompiledJson()
public class VGC extends VResource {

    protected VGC() {
    }

    protected VGC(DartGC impl) {
        super(impl);
    }

    @JsonAttribute(name = "XORMode")
    public boolean getXORMode() {
        return ((DartGC) impl).getXORMode();
    }

    public void setXORMode(boolean value) {
        ((DartGC) impl).XORMode = value;
    }

    @JsonAttribute(ignore = true)
    public boolean getAdvanced() {
        return ((DartGC) impl).getAdvanced();
    }

    public void setAdvanced(boolean value) {
        ((DartGC) impl).advanced = value;
    }

    public Integer getAlpha() {
        int value = ((DartGC) impl).getAlpha();
        return value == 255 ? null : value;
    }

    public void setAlpha(Integer value) {
        ((DartGC) impl).alpha = value == null ? 255 : value;
    }

    public int getAntialias() {
        return ((DartGC) impl).getAntialias();
    }

    public void setAntialias(int value) {
        ((DartGC) impl).antialias = value;
    }

    public Color getBackground() {
        return ((DartGC) impl).background;
    }

    public void setBackground(Color value) {
        ((DartGC) impl).background = value;
    }

    public Pattern getBackgroundPattern() {
        Pattern val = ((DartGC) impl).backgroundPattern;
        if (val != null && !(val.getImpl() instanceof DartPattern))
            return null;
        return val;
    }

    public void setBackgroundPattern(Pattern value) {
        ((DartGC) impl).backgroundPattern = value;
    }

    public Rectangle getClipping() {
        return ((DartGC) impl).clipping;
    }

    public void setClipping(Rectangle value) {
        ((DartGC) impl).clipping = value;
    }

    public PathData getClippingPath() {
        return ((DartGC) impl).clippingPath;
    }

    public void setClippingPath(PathData value) {
        ((DartGC) impl).clippingPath = value;
    }

    public int[] getClippingRects() {
        return ((DartGC) impl).clippingRects;
    }

    public void setClippingRects(int[] value) {
        ((DartGC) impl).clippingRects = value;
    }

    public Integer getFillRule() {
        int value = ((DartGC) impl).getFillRule();
        return value == 1 ? null : value;
    }

    public void setFillRule(Integer value) {
        ((DartGC) impl).fillRule = value == null ? 1 : value;
    }

    public Font getFont() {
        Font val = ((DartGC) impl).font;
        if (val != null && !(val.getImpl() instanceof DartFont))
            return null;
        return val;
    }

    public void setFont(Font value) {
        ((DartGC) impl).font = value;
    }

    public Color getForeground() {
        return ((DartGC) impl).foreground;
    }

    public void setForeground(Color value) {
        ((DartGC) impl).foreground = value;
    }

    public Pattern getForegroundPattern() {
        Pattern val = ((DartGC) impl).foregroundPattern;
        if (val != null && !(val.getImpl() instanceof DartPattern))
            return null;
        return val;
    }

    public void setForegroundPattern(Pattern value) {
        ((DartGC) impl).foregroundPattern = value;
    }

    public int getInterpolation() {
        return ((DartGC) impl).getInterpolation();
    }

    public void setInterpolation(int value) {
        ((DartGC) impl).interpolation = value;
    }

    @JsonAttribute(ignore = true)
    public LineAttributes getLineAttributes() {
        return ((DartGC) impl).lineAttributes;
    }

    public void setLineAttributes(LineAttributes value) {
        ((DartGC) impl).lineAttributes = value;
    }

    public Integer getLineCap() {
        int value = ((DartGC) impl).getLineCap();
        return value == 1 ? null : value;
    }

    public void setLineCap(Integer value) {
        ((DartGC) impl).lineCap = value == null ? 1 : value;
    }

    public int[] getLineDash() {
        return ((DartGC) impl).lineDash;
    }

    public void setLineDash(int[] value) {
        ((DartGC) impl).lineDash = value;
    }

    public Integer getLineJoin() {
        int value = ((DartGC) impl).getLineJoin();
        return value == 1 ? null : value;
    }

    public void setLineJoin(Integer value) {
        ((DartGC) impl).lineJoin = value == null ? 1 : value;
    }

    public Integer getLineStyle() {
        int value = ((DartGC) impl).getLineStyle();
        return value == 1 ? null : value;
    }

    public void setLineStyle(Integer value) {
        ((DartGC) impl).lineStyle = value == null ? 1 : value;
    }

    public int getLineWidth() {
        return ((DartGC) impl).getLineWidth();
    }

    public void setLineWidth(int value) {
        ((DartGC) impl).lineWidth = value;
    }

    @JsonAttribute(includeToMinimal = JsonAttribute.IncludePolicy.ALWAYS)
    public int getStyle() {
        return ((DartGC) impl).getStyle();
    }

    public void setStyle(int value) {
        ((DartGC) impl).style = value;
    }

    @JsonAttribute(ignore = true)
    public int getTextAntialias() {
        return ((DartGC) impl).getTextAntialias();
    }

    public void setTextAntialias(int value) {
        ((DartGC) impl).textAntialias = value;
    }

    public Transform getTransform() {
        Transform val = ((DartGC) impl).transform;
        if (val != null && !(val.getImpl() instanceof DartTransform))
            return null;
        return val;
    }

    public void setTransform(Transform value) {
        ((DartGC) impl).transform = value;
    }

    @CompiledJson(formats = CompiledJson.Format.ARRAY)
    public static class VGCCopyAreaImageintint {

        @JsonAttribute(index = 0)
        public Image image;

        @JsonAttribute(index = 1)
        public int x;

        @JsonAttribute(index = 2)
        public int y;
    }

    @CompiledJson(formats = CompiledJson.Format.ARRAY)
    public static class VGCCopyAreaintintintintintintboolean {

        @JsonAttribute(index = 0)
        public int srcX;

        @JsonAttribute(index = 1)
        public int srcY;

        @JsonAttribute(index = 2)
        public int width;

        @JsonAttribute(index = 3)
        public int height;

        @JsonAttribute(index = 4)
        public int destX;

        @JsonAttribute(index = 5)
        public int destY;

        @JsonAttribute(index = 6)
        public boolean paint;
    }

    @CompiledJson(formats = CompiledJson.Format.ARRAY)
    public static class VGCDrawArcintintintintintint {

        @JsonAttribute(index = 0)
        public int x;

        @JsonAttribute(index = 1)
        public int y;

        @JsonAttribute(index = 2)
        public int width;

        @JsonAttribute(index = 3)
        public int height;

        @JsonAttribute(index = 4)
        public int startAngle;

        @JsonAttribute(index = 5)
        public int arcAngle;
    }

    @CompiledJson(formats = CompiledJson.Format.ARRAY)
    public static class VGCDrawFocusintintintint {

        @JsonAttribute(index = 0)
        public int x;

        @JsonAttribute(index = 1)
        public int y;

        @JsonAttribute(index = 2)
        public int width;

        @JsonAttribute(index = 3)
        public int height;
    }

    @CompiledJson(formats = CompiledJson.Format.ARRAY)
    public static class VGCDrawImageImageintint {

        @JsonAttribute(index = 0)
        public Image image;

        @JsonAttribute(index = 1)
        public int x;

        @JsonAttribute(index = 2)
        public int y;
    }

    @CompiledJson(formats = CompiledJson.Format.ARRAY)
    public static class VGCDrawImageImageintintintint {

        @JsonAttribute(index = 0)
        public Image image;

        @JsonAttribute(index = 1)
        public int destX;

        @JsonAttribute(index = 2)
        public int destY;

        @JsonAttribute(index = 3)
        public int destWidth;

        @JsonAttribute(index = 4)
        public int destHeight;
    }

    @CompiledJson(formats = CompiledJson.Format.ARRAY)
    public static class VGCDrawImageImageintintintintintintintint {

        @JsonAttribute(index = 0)
        public Image image;

        @JsonAttribute(index = 1)
        public int srcX;

        @JsonAttribute(index = 2)
        public int srcY;

        @JsonAttribute(index = 3)
        public int srcWidth;

        @JsonAttribute(index = 4)
        public int srcHeight;

        @JsonAttribute(index = 5)
        public int destX;

        @JsonAttribute(index = 6)
        public int destY;

        @JsonAttribute(index = 7)
        public int destWidth;

        @JsonAttribute(index = 8)
        public int destHeight;
    }

    @CompiledJson(formats = CompiledJson.Format.ARRAY)
    public static class VGCDrawLineintintintint {

        @JsonAttribute(index = 0)
        public int x1;

        @JsonAttribute(index = 1)
        public int y1;

        @JsonAttribute(index = 2)
        public int x2;

        @JsonAttribute(index = 3)
        public int y2;
    }

    @CompiledJson(formats = CompiledJson.Format.ARRAY)
    public static class VGCDrawOvalintintintint {

        @JsonAttribute(index = 0)
        public int x;

        @JsonAttribute(index = 1)
        public int y;

        @JsonAttribute(index = 2)
        public int width;

        @JsonAttribute(index = 3)
        public int height;
    }

    @CompiledJson(formats = CompiledJson.Format.ARRAY)
    public static class VGCDrawPathPath {

        @JsonAttribute(index = 0)
        public Path path;
    }

    @CompiledJson(formats = CompiledJson.Format.ARRAY)
    public static class VGCDrawPointintint {

        @JsonAttribute(index = 0)
        public int x;

        @JsonAttribute(index = 1)
        public int y;
    }

    @CompiledJson(formats = CompiledJson.Format.ARRAY)
    public static class VGCDrawPolygonint {

        @JsonAttribute(index = 0)
        public int[] pointArray;
    }

    @CompiledJson(formats = CompiledJson.Format.ARRAY)
    public static class VGCDrawPolylineint {

        @JsonAttribute(index = 0)
        public int[] pointArray;
    }

    @CompiledJson(formats = CompiledJson.Format.ARRAY)
    public static class VGCDrawRectangleintintintint {

        @JsonAttribute(index = 0)
        public int x;

        @JsonAttribute(index = 1)
        public int y;

        @JsonAttribute(index = 2)
        public int width;

        @JsonAttribute(index = 3)
        public int height;
    }

    @CompiledJson(formats = CompiledJson.Format.ARRAY)
    public static class VGCDrawRoundRectangleintintintintintint {

        @JsonAttribute(index = 0)
        public int x;

        @JsonAttribute(index = 1)
        public int y;

        @JsonAttribute(index = 2)
        public int width;

        @JsonAttribute(index = 3)
        public int height;

        @JsonAttribute(index = 4)
        public int arcWidth;

        @JsonAttribute(index = 5)
        public int arcHeight;
    }

    @CompiledJson(formats = CompiledJson.Format.ARRAY)
    public static class VGCDrawTextStringintintint {

        @JsonAttribute(index = 0)
        public String string;

        @JsonAttribute(index = 1)
        public int x;

        @JsonAttribute(index = 2)
        public int y;

        @JsonAttribute(index = 3)
        public int flags;
    }

    @CompiledJson(formats = CompiledJson.Format.ARRAY)
    public static class VGCFillArcintintintintintint {

        @JsonAttribute(index = 0)
        public int x;

        @JsonAttribute(index = 1)
        public int y;

        @JsonAttribute(index = 2)
        public int width;

        @JsonAttribute(index = 3)
        public int height;

        @JsonAttribute(index = 4)
        public int startAngle;

        @JsonAttribute(index = 5)
        public int arcAngle;
    }

    @CompiledJson(formats = CompiledJson.Format.ARRAY)
    public static class VGCFillGradientRectangleintintintintboolean {

        @JsonAttribute(index = 0)
        public int x;

        @JsonAttribute(index = 1)
        public int y;

        @JsonAttribute(index = 2)
        public int width;

        @JsonAttribute(index = 3)
        public int height;

        @JsonAttribute(index = 4)
        public boolean vertical;
    }

    @CompiledJson(formats = CompiledJson.Format.ARRAY)
    public static class VGCFillOvalintintintint {

        @JsonAttribute(index = 0)
        public int x;

        @JsonAttribute(index = 1)
        public int y;

        @JsonAttribute(index = 2)
        public int width;

        @JsonAttribute(index = 3)
        public int height;
    }

    @CompiledJson(formats = CompiledJson.Format.ARRAY)
    public static class VGCFillPathPath {

        @JsonAttribute(index = 0)
        public Path path;
    }

    @CompiledJson(formats = CompiledJson.Format.ARRAY)
    public static class VGCFillPolygonint {

        @JsonAttribute(index = 0)
        public int[] pointArray;
    }

    @CompiledJson(formats = CompiledJson.Format.ARRAY)
    public static class VGCFillRectangleintintintint {

        @JsonAttribute(index = 0)
        public int x;

        @JsonAttribute(index = 1)
        public int y;

        @JsonAttribute(index = 2)
        public int width;

        @JsonAttribute(index = 3)
        public int height;
    }

    @CompiledJson(formats = CompiledJson.Format.ARRAY)
    public static class VGCFillRoundRectangleintintintintintint {

        @JsonAttribute(index = 0)
        public int x;

        @JsonAttribute(index = 1)
        public int y;

        @JsonAttribute(index = 2)
        public int width;

        @JsonAttribute(index = 3)
        public int height;

        @JsonAttribute(index = 4)
        public int arcWidth;

        @JsonAttribute(index = 5)
        public int arcHeight;
    }

    public Path getClippingText() {
        return ((DartGC) impl).wireClippingText();
    }

    public void setClippingText(Path value) {
    }

    public float getLineDashOffset() {
        return ((DartGC) impl).wireLineDashOffset();
    }

    public void setLineDashOffset(float value) {
    }

    public float getBufferScale() {
        return ((DartGC) impl).wireBufferScale();
    }

    public void setBufferScale(float value) {
    }

    public static final String XORMODE = "XORMode";

    public static final String ALPHA = "alpha";

    public static final String ANTIALIAS = "antialias";

    public static final String BACKGROUND = "background";

    public static final String BACKGROUND_PATTERN = "backgroundPattern";

    public static final String BUFFER_SCALE = "bufferScale";

    public static final String CLIPPING = "clipping";

    public static final String CLIPPING_PATH = "clippingPath";

    public static final String CLIPPING_RECTS = "clippingRects";

    public static final String CLIPPING_TEXT = "clippingText";

    public static final String FILL_RULE = "fillRule";

    public static final String FONT = "font";

    public static final String FOREGROUND = "foreground";

    public static final String FOREGROUND_PATTERN = "foregroundPattern";

    public static final String INTERPOLATION = "interpolation";

    public static final String LINE_CAP = "lineCap";

    public static final String LINE_DASH = "lineDash";

    public static final String LINE_DASH_OFFSET = "lineDashOffset";

    public static final String LINE_JOIN = "lineJoin";

    public static final String LINE_STYLE = "lineStyle";

    public static final String LINE_WIDTH = "lineWidth";

    public static final String STYLE = "style";

    public static final String TRANSFORM = "transform";

    @Override
    protected void writeProperty(JsonWriter writer, String key) {
        switch(key) {
            case "XORMode":
                Serializer.writeKeyValue(writer, "XORMode", getXORMode());
                return;
            case "alpha":
                Serializer.writeKeyValue(writer, "alpha", getAlpha());
                return;
            case "antialias":
                Serializer.writeKeyValue(writer, "antialias", getAntialias());
                return;
            case "background":
                Serializer.writeKeyValue(writer, "background", getBackground());
                return;
            case "backgroundPattern":
                Serializer.writeKeyValue(writer, "backgroundPattern", getBackgroundPattern());
                return;
            case "clipping":
                Serializer.writeKeyValue(writer, "clipping", getClipping());
                return;
            case "clippingPath":
                Serializer.writeKeyValue(writer, "clippingPath", getClippingPath());
                return;
            case "clippingRects":
                Serializer.writeKeyValue(writer, "clippingRects", getClippingRects());
                return;
            case "fillRule":
                Serializer.writeKeyValue(writer, "fillRule", getFillRule());
                return;
            case "font":
                Serializer.writeKeyValue(writer, "font", getFont());
                return;
            case "foreground":
                Serializer.writeKeyValue(writer, "foreground", getForeground());
                return;
            case "foregroundPattern":
                Serializer.writeKeyValue(writer, "foregroundPattern", getForegroundPattern());
                return;
            case "interpolation":
                Serializer.writeKeyValue(writer, "interpolation", getInterpolation());
                return;
            case "lineCap":
                Serializer.writeKeyValue(writer, "lineCap", getLineCap());
                return;
            case "lineDash":
                Serializer.writeKeyValue(writer, "lineDash", getLineDash());
                return;
            case "lineJoin":
                Serializer.writeKeyValue(writer, "lineJoin", getLineJoin());
                return;
            case "lineStyle":
                Serializer.writeKeyValue(writer, "lineStyle", getLineStyle());
                return;
            case "lineWidth":
                Serializer.writeKeyValue(writer, "lineWidth", getLineWidth());
                return;
            case "style":
                Serializer.writeKeyValue(writer, "style", getStyle());
                return;
            case "transform":
                Serializer.writeKeyValue(writer, "transform", getTransform());
                return;
            case "clippingText":
                Serializer.writeKeyValue(writer, "clippingText", getClippingText());
                return;
            case "lineDashOffset":
                Serializer.writeKeyValue(writer, "lineDashOffset", getLineDashOffset());
                return;
            case "bufferScale":
                Serializer.writeKeyValue(writer, "bufferScale", getBufferScale());
                return;
        }
        super.writeProperty(writer, key);
    }

    @JsonConverter(target = GC.class)
    public static class GCJson implements Configuration {

        @Override
        public void configure(DslJson json) {
            json.registerWriter(DartGC.class, (JsonWriter.WriteObject<DartGC>) (writer, impl) -> {
                Serializer.writeResourceWithId(json, writer, impl);
            });
            json.registerReader(DartGC.class, (JsonReader.ReadObject<DartGC>) reader -> {
                return null;
            });
        }

        public static GC read(JsonReader<?> reader) throws IOException {
            return null;
        }

        public static void write(JsonWriter writer, GC api) {
            if (api == null)
                writer.writeNull();
            else
                writer.serializeObject(api.getImpl());
        }
    }
}
