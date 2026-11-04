/**
 * ****************************************************************************
 *  Copyright (c) 2005, 2009 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *      IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.swt.opengl;

/**
 * The GLData class is a device-independent description
 * of the pixel format attributes of a GL drawable.
 *
 * @see GLCanvas
 * @see <a href="https://eclipse.dev/eclipse/swt/snippets/#opengl">OpenGL snippets</a>
 * @see <a href="https://eclipse.dev/eclipse/swt/">Sample code and further information</a>
 *
 * @since 3.2
 */
public class GLData implements dev.equo.swt.WireObject {

    /**
     * Specifies a double-buffered surface.  During context
     * creation, only double-buffered formats are considered
     * when set to true.
     */
    public boolean doubleBuffer;

    /**
     * Specifies a stereo surface.  During context creation,
     * only stereo formats are considered when set to true.
     */
    public boolean stereo;

    /**
     * The size in bits of the color buffer's red channel.
     * During context creation, this specifies the minimum
     * required red bits.
     */
    public int redSize;

    /**
     * The size in bits of the color buffer's green channel.
     * During context creation, this specifies the minimum
     * required green bits.
     */
    public int greenSize;

    /**
     * The size in bits of the color buffer's blue channel.
     * During context creation, this specifies the minimum
     * required blue bits.
     */
    public int blueSize;

    /**
     * The size in bits of the color buffer's alpha channel.
     * During context creation, this specifies the minimum
     * required alpha bits.
     */
    public int alphaSize;

    /**
     * The size in bits of the depth buffer.  During context
     * creation, the smallest depth buffer of at least the
     * specified value is preferred, or zero for no depth
     * buffer.
     */
    public int depthSize;

    /**
     * The desired number of stencil bitplanes.  During
     * context creation, the smallest stencil buffer of at
     * least the specified value is preferred, or zero for
     * no stencil buffer.
     */
    public int stencilSize;

    /**
     * The size in bits of the accumulation buffer's red
     * channel. During context creation, this specifies the
     * minimum required red bits.
     */
    public int accumRedSize;

    /**
     * The size in bits of the accumulation buffer's green
     * channel. During context creation, this specifies the
     * minimum required green bits.
     */
    public int accumGreenSize;

    /**
     * The size in bits of the accumulation buffer's blue
     * channel. During context creation, this specifies the
     * minimum required blue bits.
     */
    public int accumBlueSize;

    /**
     * The size in bits of the accumulation buffer's alpha
     * channel. During context creation, this specifies the
     * minimum required alpha bits.
     */
    public int accumAlphaSize;

    /**
     * The number of multisample buffers used by this context.
     * During context creation, this specifies the minimum
     * number of multisample buffers requested.
     */
    public int sampleBuffers;

    /**
     * The number of samples accepted in the multisample buffer.
     * During creation, pixel formats with the smallest number of
     * samples that meets or exceeds the specified minimum number
     * are preferred.
     */
    public int samples;

    /**
     * Another GLCanvas whose texture namespace and display lists
     * should be shared.
     *
     * @since 3.5
     */
    public GLCanvas shareContext;

    /**
     * Returns a string containing a concise, human-readable
     * description of the receiver.
     *
     * @return a string representation of the data
     */
    @Override
    public String toString() {
        return (doubleBuffer ? "doubleBuffer," : "") + (stereo ? "stereo," : "") + "r:" + redSize + " g:" + greenSize + " b:" + blueSize + " a:" + alphaSize + "," + "depth:" + depthSize + ",stencil:" + stencilSize + ",accum r:" + accumRedSize + "g:" + accumGreenSize + "b:" + accumBlueSize + "a:" + accumAlphaSize + ",sampleBuffers:" + sampleBuffers + ",samples:" + samples;
    }

    @Override
    public int wireTypeId() {
        return 50;
    }

    @Override
    public void writeWireFields(dev.equo.swt.WidgetWire w) {
        w.key(303);
        w.write(doubleBuffer);
        w.key(310);
        w.write(stereo);
        w.key(305);
        w.write((int) redSize);
        w.key(304);
        w.write((int) greenSize);
        w.key(301);
        w.write((int) blueSize);
        w.key(300);
        w.write((int) alphaSize);
        w.key(302);
        w.write((int) depthSize);
        w.key(309);
        w.write((int) stencilSize);
        w.key(299);
        w.write((int) accumRedSize);
        w.key(298);
        w.write((int) accumGreenSize);
        w.key(297);
        w.write((int) accumBlueSize);
        w.key(296);
        w.write((int) accumAlphaSize);
        w.key(306);
        w.write((int) sampleBuffers);
        w.key(307);
        w.write((int) samples);
        w.key(308);
        w.ref(shareContext);
    }

    @Override
    public boolean readWire(int key, dev.equo.swt.WireReader r) {
        switch(key) {
            case 303:
                doubleBuffer = r.bool();
                return true;
            case 310:
                stereo = r.bool();
                return true;
            case 305:
                redSize = r.int32();
                return true;
            case 304:
                greenSize = r.int32();
                return true;
            case 301:
                blueSize = r.int32();
                return true;
            case 300:
                alphaSize = r.int32();
                return true;
            case 302:
                depthSize = r.int32();
                return true;
            case 309:
                stencilSize = r.int32();
                return true;
            case 299:
                accumRedSize = r.int32();
                return true;
            case 298:
                accumGreenSize = r.int32();
                return true;
            case 297:
                accumBlueSize = r.int32();
                return true;
            case 296:
                accumAlphaSize = r.int32();
                return true;
            case 306:
                sampleBuffers = r.int32();
                return true;
            case 307:
                samples = r.int32();
                return true;
        }
        return false;
    }
}
