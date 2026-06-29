package org.eclipse.swt.dnd;

import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.internal.*;
import org.eclipse.swt.widgets.*;

public interface IImageTransfer extends IByteArrayTransfer {

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
    void javaToNative(Object object, TransferData transferData);

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
    Object nativeToJava(TransferData transferData);

    int[] getTypeIds();

    String[] getTypeNames();

    boolean validate(Object object);

    ImageTransfer getApi();
}
