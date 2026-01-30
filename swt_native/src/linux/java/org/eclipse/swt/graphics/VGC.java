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

    @JsonAttribute(name = "XORMode")
    public boolean getXORMode() {
        return ((DartGC) impl).getXORMode();
    }

    public void setXORMode(boolean value) {
        ((DartGC) impl).XORMode = value;
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

    public Font getFont() {
        Font val = ((DartGC) impl).font;
        if (val != null && val.getImpl() instanceof SwtFont)
            return GraphicsUtils.copyFont(val);
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

    @CompiledJson()
    public static class VGCCopyAreaImageintint {

        public Image image;

        public int x;

        public int y;
    }

    @CompiledJson()
    public static class VGCCopyAreaintintintintintint {

        public int srcX;

        public int srcY;

        public int width;

        public int height;

        public int destX;

        public int destY;
    }

    @CompiledJson()
    public static class VGCCopyAreaintintintintintintboolean {

        public int srcX;

        public int srcY;

        public int width;

        public int height;

        public int destX;

        public int destY;

        public boolean paint;
    }

    @CompiledJson()
    public static class VGCDrawArcintintintintintint {

        public int x;

        public int y;

        public int width;

        public int height;

        public int startAngle;

        public int arcAngle;
    }

    @CompiledJson()
    public static class VGCDrawFocusintintintint {

        public int x;

        public int y;

        public int width;

        public int height;
    }

    @CompiledJson()
    public static class VGCDrawImageImageintint {

        public Image image;

        public int x;

        public int y;
    }

    @CompiledJson()
    public static class VGCDrawImageImageintintintintintintintint {

        public Image image;

        public int srcX;

        public int srcY;

        public int srcWidth;

        public int srcHeight;

        public int destX;

        public int destY;

        public int destWidth;

        public int destHeight;
    }

    @CompiledJson()
    public static class VGCDrawLineintintintint {

        public int x1;

        public int y1;

        public int x2;

        public int y2;
    }

    @CompiledJson()
    public static class VGCDrawOvalintintintint {

        public int x;

        public int y;

        public int width;

        public int height;
    }

    @CompiledJson()
    public static class VGCDrawPointintint {

        public int x;

        public int y;
    }

    @CompiledJson()
    public static class VGCDrawPolygonint {

        public int[] pointArray;
    }

    @CompiledJson()
    public static class VGCDrawPolylineint {

        public int[] pointArray;
    }

    @CompiledJson()
    public static class VGCDrawRectangleRectangle {

        public Rectangle rect;
    }

    @CompiledJson()
    public static class VGCDrawRectangleintintintint {

        public int x;

        public int y;

        public int width;

        public int height;
    }

    @CompiledJson()
    public static class VGCDrawRoundRectangleintintintintintint {

        public int x;

        public int y;

        public int width;

        public int height;

        public int arcWidth;

        public int arcHeight;
    }

    @CompiledJson()
    public static class VGCDrawStringStringintint {

        public String string;

        public int x;

        public int y;
    }

    @CompiledJson()
    public static class VGCDrawStringStringintintboolean {

        public String string;

        public int x;

        public int y;

        public boolean isTransparent;
    }

    @CompiledJson()
    public static class VGCDrawTextStringintint {

        public String string;

        public int x;

        public int y;
    }

    @CompiledJson()
    public static class VGCDrawTextStringintintboolean {

        public String string;

        public int x;

        public int y;

        public boolean isTransparent;
    }

    @CompiledJson()
    public static class VGCDrawTextStringintintint {

        public String string;

        public int x;

        public int y;

        public int flags;
    }

    @CompiledJson()
    public static class VGCFillArcintintintintintint {

        public int x;

        public int y;

        public int width;

        public int height;

        public int startAngle;

        public int arcAngle;
    }

    @CompiledJson()
    public static class VGCFillGradientRectangleintintintintboolean {

        public int x;

        public int y;

        public int width;

        public int height;

        public boolean vertical;
    }

    @CompiledJson()
    public static class VGCFillOvalintintintint {

        public int x;

        public int y;

        public int width;

        public int height;
    }

    @CompiledJson()
    public static class VGCFillPolygonint {

        public int[] pointArray;
    }

    @CompiledJson()
    public static class VGCFillRectangleRectangle {

        public Rectangle rect;
    }

    @CompiledJson()
    public static class VGCFillRectangleintintintint {

        public int x;

        public int y;

        public int width;

        public int height;
    }

    @CompiledJson()
    public static class VGCFillRoundRectangleintintintintintint {

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
