package org.eclipse.swt.dnd;

public interface IByteArrayTransfer extends ITransfer {

    TransferData[] getSupportedTypes();

    boolean isSupportedType(TransferData transferData);

    /**
     * This implementation of <code>javaToNative</code> converts a java
     * <code>byte[]</code> to a platform specific representation.
     *
     * @param object a java <code>byte[]</code> containing the data to be converted
     * @param transferData an empty <code>TransferData</code> object that will
     *  	be filled in on return with the platform specific format of the data
     *
     * @see Transfer#nativeToJava
     */
    void javaToNative(Object object, TransferData transferData);

    /**
     * This implementation of <code>nativeToJava</code> converts a platform specific
     * representation of a byte array to a java <code>byte[]</code>.
     *
     * @param transferData the platform specific representation of the data to be converted
     * @return a java <code>byte[]</code> containing the converted data if the conversion was
     * 		successful; otherwise null
     *
     * @see Transfer#javaToNative
     */
    Object nativeToJava(TransferData transferData);

    ByteArrayTransfer getApi();
}
