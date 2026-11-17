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
        w.key(2503);
        w.write(doubleBuffer);
        w.key(2510);
        w.write(stereo);
        w.key(2505);
        w.write((int) redSize);
        w.key(2504);
        w.write((int) greenSize);
        w.key(2501);
        w.write((int) blueSize);
        w.key(2500);
        w.write((int) alphaSize);
        w.key(2502);
        w.write((int) depthSize);
        w.key(2509);
        w.write((int) stencilSize);
        w.key(2499);
        w.write((int) accumRedSize);
        w.key(2498);
        w.write((int) accumGreenSize);
        w.key(2497);
        w.write((int) accumBlueSize);
        w.key(2496);
        w.write((int) accumAlphaSize);
        w.key(2506);
        w.write((int) sampleBuffers);
        w.key(2507);
        w.write((int) samples);
        w.key(2508);
        w.ref(shareContext);
    }

    @Override
    public boolean readWire(int key, dev.equo.swt.WireReader r) {
        switch(key) {
            case 2503:
                doubleBuffer = r.bool();
                return true;
            case 2510:
                stereo = r.bool();
                return true;
            case 2505:
                redSize = r.int32();
                return true;
            case 2504:
                greenSize = r.int32();
                return true;
            case 2501:
                blueSize = r.int32();
                return true;
            case 2500:
                alphaSize = r.int32();
                return true;
            case 2502:
                depthSize = r.int32();
                return true;
            case 2509:
                stencilSize = r.int32();
                return true;
            case 2499:
                accumRedSize = r.int32();
                return true;
            case 2498:
                accumGreenSize = r.int32();
                return true;
            case 2497:
                accumBlueSize = r.int32();
                return true;
            case 2496:
                accumAlphaSize = r.int32();
                return true;
            case 2506:
                sampleBuffers = r.int32();
                return true;
            case 2507:
                samples = r.int32();
                return true;
        }
        return false;
    }
}
