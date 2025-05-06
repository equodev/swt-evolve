package org.eclipse.swt.widgets;

import org.eclipse.swt.graphics.*;

public interface ITouchSource {

    /**
     * Returns the type of touch input this source generates, <code>true</code> for direct or <code>false</code> for indirect.
     *
     * @return <code>true</code> if the input source is direct, or <code>false</code> otherwise
     */
    boolean isDirect();

    /**
     * Returns the bounding rectangle of the device. For a direct source, this corresponds to the bounds of
     * the display device in pixels. For an indirect source, this contains the size of the device in points.
     * <p>
     * Note that the x and y values may not necessarily be 0 if the TouchSource is a direct source.
     *
     * @return the bounding rectangle of the input source
     */
    Rectangle getBounds();

    /**
     * Returns a string containing a concise, human-readable
     * description of the receiver.
     *
     * @return a string representation of the event
     */
    String toString();
}
