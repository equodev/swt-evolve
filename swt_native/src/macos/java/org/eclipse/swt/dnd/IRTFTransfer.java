package org.eclipse.swt.dnd;

public interface IRTFTransfer extends IByteArrayTransfer {

    /**
     * This implementation of <code>javaToNative</code> converts RTF-formatted text
     * represented by a java <code>String</code> to a platform specific representation.
     *
     * @param object a java <code>String</code> containing RTF text
     * @param transferData an empty <code>TransferData</code> object that will
     *  	be filled in on return with the platform specific format of the data
     *
     * @see Transfer#nativeToJava
     */
    void javaToNative(Object object, ITransferData transferData);

    /**
     * This implementation of <code>nativeToJava</code> converts a platform specific
     * representation of RTF text to a java <code>String</code>.
     *
     * @param transferData the platform specific representation of the data to be converted
     * @return a java <code>String</code> containing RTF text if the conversion was successful;
     * 		otherwise null
     *
     * @see Transfer#javaToNative
     */
    Object nativeToJava(ITransferData transferData);

    int[] getTypeIds();

    String[] getTypeNames();

    boolean validate(Object object);

    RTFTransfer getApi();
}
