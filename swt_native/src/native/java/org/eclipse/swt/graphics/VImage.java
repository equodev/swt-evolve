package org.eclipse.swt.graphics;

import java.io.*;
import java.util.*;
import java.util.function.*;
import org.eclipse.swt.*;

public class VImage extends VResource {

    protected VImage() {
    }

    protected VImage(DartImage impl) {
        super(impl);
    }

    public Color getBackground() {
        return ((DartImage) impl).background;
    }

    public void setBackground(Color value) {
        ((DartImage) impl).background = value;
    }

    public String getFilename() {
        return ((DartImage) impl).filename;
    }

    public void setFilename(String value) {
        ((DartImage) impl).filename = value;
    }

    public int getHeight() {
        return ((DartImage) impl)._wireHeight();
    }

    public void setHeight(int value) {
        ((DartImage) impl).height = value;
    }

    public ImageData getImageData() {
        return ((DartImage) impl)._imageDataForWire();
    }

    public void setImageData(ImageData value) {
        ((DartImage) impl).imageData = value;
    }

    public Long getRemoteRef() {
        return ((DartImage) impl).remoteRef;
    }

    public void setRemoteRef(Long value) {
        ((DartImage) impl).remoteRef = value;
    }

    public String getSvgContent() {
        return ((DartImage) impl).svgContent;
    }

    public void setSvgContent(String value) {
        ((DartImage) impl).svgContent = value;
    }

    public int getWidth() {
        return ((DartImage) impl)._wireWidth();
    }

    public void setWidth(int value) {
        ((DartImage) impl).width = value;
    }

    public static final int FILENAME = 2752;

    public static final int HEIGHT = 2753;

    public static final int IMAGE_DATA = 2754;

    public static final int REMOTE_REF = 2755;

    public static final int SVG_CONTENT = 2756;

    public static final int WIDTH = 2757;

    @Override
    public String wireName(int key) {
        switch(key) {
            case FILENAME:
                return "filename";
            case HEIGHT:
                return "height";
            case IMAGE_DATA:
                return "imageData";
            case REMOTE_REF:
                return "remoteRef";
            case SVG_CONTENT:
                return "svgContent";
            case WIDTH:
                return "width";
        }
        return super.wireName(key);
    }

    @Override
    public boolean writeWire(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 2752:
                w.write(getFilename());
                return true;
            case 2753:
                w.write((int) getHeight());
                return true;
            case 2754:
                w.object(getImageData());
                return true;
            case 2755:
                w.write(getRemoteRef());
                return true;
            case 2756:
                w.write(getSvgContent());
                return true;
            case 2757:
                w.write((int) getWidth());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 2752:
                {
                    String v_ = getFilename();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.isEmpty())
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(2752);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 2753:
                {
                    int v_ = getHeight();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(2753);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 2754:
                {
                    ImageData v_ = getImageData();
                    dev.equo.swt.WireObject o_ = v_;
                    if (o_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(2754);
                    w.object(o_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 2755:
                {
                    Long v_ = getRemoteRef();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(2755);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 2756:
                {
                    String v_ = getSvgContent();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.isEmpty())
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(2756);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 2757:
                {
                    int v_ = getWidth();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(2757);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VResource.allWireKeys_(), new int[] { 2752, 2753, 2754, 2755, 2756, 2757 });

    protected static int[] allWireKeys_() {
        return wireKeys_;
    }

    @Override
    public int[] wireKeys() {
        return wireKeys_;
    }

    @Override
    public boolean wireAlways(int key) {
        return super.wireAlways(key);
    }

    @Override
    public boolean wireKeepsEmpty(int key) {
        return super.wireKeepsEmpty(key);
    }

    @Override
    public int wireTypeId() {
        return 56;
    }

    @Override
    public void writeWireFields(dev.equo.swt.WidgetWire w) {
        super.writeWireFields(w);
        w.key(2752);
        w.write(getFilename());
        w.key(2753);
        w.write((int) getHeight());
        w.key(2754);
        w.object(getImageData());
        w.key(2755);
        w.write(getRemoteRef());
        w.key(2756);
        w.write(getSvgContent());
        w.key(2757);
        w.write((int) getWidth());
    }
}
