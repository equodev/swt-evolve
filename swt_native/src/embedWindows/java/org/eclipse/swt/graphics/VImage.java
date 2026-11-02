package org.eclipse.swt.graphics;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.function.*;
import java.util.stream.*;
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;

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
    }

    public static final int FILENAME = 334;

    public static final int HEIGHT = 335;

    public static final int IMAGE_DATA = 336;

    public static final int REMOTE_REF = 337;

    public static final int SVG_CONTENT = 338;

    public static final int WIDTH = 339;

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
            case 334:
                w.write(getFilename());
                return true;
            case 335:
                w.write((int) getHeight());
                return true;
            case 336:
                w.object(getImageData());
                return true;
            case 337:
                w.write(getRemoteRef());
                return true;
            case 338:
                w.write(getSvgContent());
                return true;
            case 339:
                w.write((int) getWidth());
                return true;
        }
        return super.writeWire(w, key);
    }

    @Override
    public int writeWireWhole(dev.equo.swt.WidgetWire w, int key) {
        switch(key) {
            case 334:
                {
                    String v_ = getFilename();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.isEmpty())
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(334);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 335:
                {
                    int v_ = getHeight();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(335);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 336:
                {
                    ImageData v_ = getImageData();
                    dev.equo.swt.WireObject o_ = v_;
                    if (o_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(336);
                    w.object(o_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 337:
                {
                    Long v_ = getRemoteRef();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(337);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 338:
                {
                    String v_ = getSvgContent();
                    if (v_ == null)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    if (v_.isEmpty())
                        return dev.equo.swt.WidgetWire.AT_EMPTY;
                    w.key(338);
                    w.write(v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
            case 339:
                {
                    int v_ = getWidth();
                    if (v_ == 0)
                        return dev.equo.swt.WidgetWire.AT_ZERO;
                    w.key(339);
                    w.write((int) v_);
                    return dev.equo.swt.WidgetWire.WRITTEN;
                }
        }
        return super.writeWireWhole(w, key);
    }

    private static final int[] wireKeys_ = dev.equo.swt.WidgetWire.concat(VResource.allWireKeys_(), new int[] { 334, 335, 336, 337, 338, 339 });

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
        w.key(334);
        w.write(getFilename());
        w.key(335);
        w.write((int) getHeight());
        w.key(336);
        w.object(getImageData());
        w.key(337);
        w.write(getRemoteRef());
        w.key(338);
        w.write(getSvgContent());
        w.key(339);
        w.write((int) getWidth());
    }
}
