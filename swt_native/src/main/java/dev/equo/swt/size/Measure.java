package dev.equo.swt.size;

import com.dslplatform.json.CompiledJson;
import org.eclipse.swt.graphics.Point;

@CompiledJson
public class Measure {
    public Point widget;
    public PointD text;
    public TextStyle textStyle;
    public PointD image;

    public Measure(Point widget, PointD text, TextStyle textStyle, PointD image) {
        this.widget = widget;
        this.text = text;
        this.textStyle = textStyle;
        this.image = image;
    }

    public Measure() {
    }
}
