/**
 * ****************************************************************************
 *  Copyright (c) 2007, 2017 IBM Corporation and others.
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
package org.eclipse.swt.dnd;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.internal.*;
import org.eclipse.swt.internal.ole.win32.*;
import org.eclipse.swt.internal.win32.*;

/**
 * The class <code>ImageTransfer</code> provides a platform specific mechanism
 * for converting an Image represented as a java <code>ImageData</code> to a
 * platform specific representation of the data and vice versa.
 *
 * <p>An example of a java <code>ImageData</code> is shown below:</p>
 *
 * <pre><code>
 *     Image image = new Image(display, "C:\\temp\\img1.gif");
 *     ImageData imgData = image.getImageData();
 * </code></pre>
 *
 * @see Transfer
 *
 * @since 3.4
 */
public class ImageTransfer extends ByteArrayTransfer {

    ImageTransfer() {
        this((IImageTransfer) null);
        setImpl(new SwtImageTransfer(this));
    }

    /**
     * Returns the singleton instance of the ImageTransfer class.
     *
     * @return the singleton instance of the ImageTransfer class
     */
    public static ImageTransfer getInstance() {
        return SwtImageTransfer.getInstance();
    }

    /**
     * This implementation of <code>javaToNative</code> converts an ImageData object represented
     * by java <code>ImageData</code> to a platform specific representation.
     *
     * @param object a java <code>ImageData</code> containing the ImageData to be converted
     * @param transferData an empty <code>TransferData</code> object that will
     *  	be filled in on return with the platform specific format of the data
     *
     * @see Transfer#nativeToJava
     */
    public void javaToNative(Object object, TransferData transferData) {
        getImpl().javaToNative(object, transferData);
    }

    /**
     * This implementation of <code>nativeToJava</code> converts a platform specific
     * representation of an image to java <code>ImageData</code>.
     *
     * @param transferData the platform specific representation of the data to be converted
     * @return a java <code>ImageData</code> of the image if the conversion was successful;
     * 		otherwise null
     *
     * @see Transfer#javaToNative
     */
    public Object nativeToJava(TransferData transferData) {
        return getImpl().nativeToJava(transferData);
    }

    protected int[] getTypeIds() {
        return getImpl().getTypeIds();
    }

    protected String[] getTypeNames() {
        return getImpl().getTypeNames();
    }

    protected boolean validate(Object object) {
        return getImpl().validate(object);
    }

    protected ImageTransfer(IImageTransfer impl) {
        super(impl);
    }

    static ImageTransfer createApi(IImageTransfer impl) {
        return new ImageTransfer(impl);
    }

    public IImageTransfer getImpl() {
        return (IImageTransfer) super.getImpl();
    }
}
