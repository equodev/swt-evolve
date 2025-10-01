package org.eclipse.swt.graphics;

import java.util.*;
import org.eclipse.swt.*;
import com.dslplatform.json.*;
import dev.equo.swt.Serializer;

@CompiledJson()
public class VGC extends VResource {

    protected VGC() {
    }

    protected VGC(DartGC impl) {
        super(impl);
    }

    public boolean getAdvanced() {
        return ((DartGC) impl).getAdvanced();
    }

    public void setAdvanced(boolean value) {
        ((DartGC) impl).advanced = value;
    }

    public int getAlpha() {
        return ((DartGC) impl).getAlpha();
    }

    public void setAlpha(int value) {
        ((DartGC) impl).alpha = value;
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

    @JsonAttribute(ignore = true)
    public Pattern getBackgroundPattern() {
        return ((DartGC) impl).backgroundPattern;
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

    public int getFillRule() {
        return ((DartGC) impl).getFillRule();
    }

    public void setFillRule(int value) {
        ((DartGC) impl).fillRule = value;
    }

    @JsonAttribute(ignore = true)
    public Font getFont() {
        return ((DartGC) impl).font;
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

    @JsonAttribute(ignore = true)
    public Pattern getForegroundPattern() {
        return ((DartGC) impl).foregroundPattern;
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

    public int getLineCap() {
        return ((DartGC) impl).getLineCap();
    }

    public void setLineCap(int value) {
        ((DartGC) impl).lineCap = value;
    }

    public int[] getLineDash() {
        return ((DartGC) impl).lineDash;
    }

    public void setLineDash(int[] value) {
        ((DartGC) impl).lineDash = value;
    }

    public int getLineJoin() {
        return ((DartGC) impl).getLineJoin();
    }

    public void setLineJoin(int value) {
        ((DartGC) impl).lineJoin = value;
    }

    public int getLineStyle() {
        return ((DartGC) impl).getLineStyle();
    }

    public void setLineStyle(int value) {
        ((DartGC) impl).lineStyle = value;
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

    public int getTextAntialias() {
        return ((DartGC) impl).getTextAntialias();
    }

    public void setTextAntialias(int value) {
        ((DartGC) impl).textAntialias = value;
    }

    @JsonAttribute(ignore = true)
    public Transform getTransform() {
        return ((DartGC) impl).transform;
    }

    public void setTransform(Transform value) {
        ((DartGC) impl).transform = value;
    }

    public boolean getXORMode() {
        return ((DartGC) impl).getXORMode();
    }

    public void setXORMode(boolean value) {
        ((DartGC) impl).xORMode = value;
    }

    @CompiledJson()
    public static class VGCCopyArea {

        public int srcX;

        public int srcY;

        public int width;

        public int height;

        public int destX;

        public int destY;

        public boolean paint;
    }

    @CompiledJson()
    public static class VGCCopyAreaImage {

        public Image image;

        public int x;

        public int y;
    }

    @CompiledJson()
    public static class VGCDrawArc {

        public int x;

        public int y;

        public int width;

        public int height;

        public int startAngle;

        public int arcAngle;
    }

    @CompiledJson()
    public static class VGCDrawFocus {

        public int x;

        public int y;

        public int width;

        public int height;
    }

    @CompiledJson()
    public static class VGCDrawImage {

        public Image srcImage;

        public int srcX;

        public int srcY;

        public int srcWidth;

        public int srcHeight;

        public int destX;

        public int destY;

        public int destWidth;

        public int destHeight;

        public boolean simple;
    }

    @CompiledJson()
    public static class VGCDrawLine {

        public int x1;

        public int y1;

        public int x2;

        public int y2;
    }

    @CompiledJson()
    public static class VGCDrawOval {

        public int x;

        public int y;

        public int width;

        public int height;
    }

    @CompiledJson()
    public static class VGCDrawPoint {

        public int x;

        public int y;
    }

    @CompiledJson()
    public static class VGCDrawPolygon {

        public int[] pointArray;
    }

    @CompiledJson()
    public static class VGCDrawPolyline {

        public int[] pointArray;
    }

    @CompiledJson()
    public static class VGCDrawRectangle {

        public int x;

        public int y;

        public int width;

        public int height;
    }

    @CompiledJson()
    public static class VGCDrawRoundRectangle {

        public int x;

        public int y;

        public int width;

        public int height;

        public int arcWidth;

        public int arcHeight;
    }

    @CompiledJson()
    public static class VGCDrawText {

        public String string;

        public int x;

        public int y;

        public int flags;
    }

    @CompiledJson()
    public static class VGCFillArc {

        public int x;

        public int y;

        public int width;

        public int height;

        public int startAngle;

        public int arcAngle;
    }

    @CompiledJson()
    public static class VGCFillGradientRectangle {

        public int x;

        public int y;

        public int width;

        public int height;

        public boolean vertical;
    }

    @CompiledJson()
    public static class VGCFillOval {

        public int x;

        public int y;

        public int width;

        public int height;
    }

    @CompiledJson()
    public static class VGCFillPolygon {

        public int[] pointArray;
    }

    @CompiledJson()
    public static class VGCFillRectangle {

        public int x;

        public int y;

        public int width;

        public int height;
    }

    @CompiledJson()
    public static class VGCFillRoundRectangle {

        public int x;

        public int y;

        public int width;

        public int height;

        public int arcWidth;

        public int arcHeight;
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

        public static GC read(JsonReader<?> reader) {
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
