package org.eclipse.swt.dnd;

public interface IFileTransfer extends IByteArrayTransfer {

    /**
     * This implementation of <code>javaToNative</code> converts a list of file names
     * represented by a java <code>String[]</code> to a platform specific representation.
     * Each <code>String</code> in the array contains the absolute path for a single
     * file or directory.
     *
     * @param object a java <code>String[]</code> containing the file names to be converted
     * @param transferData an empty <code>TransferData</code> object that will
     *  	be filled in on return with the platform specific format of the data
     *
     * @see Transfer#nativeToJava
     */
    void javaToNative(Object object, ITransferData transferData);

    /**
     * This implementation of <code>nativeToJava</code> converts a platform specific
     * representation of a list of file names to a java <code>String[]</code>.
     * Each String in the array contains the absolute path for a single file or directory.
     *
     * @param transferData the platform specific representation of the data to be converted
     * @return a java <code>String[]</code> containing a list of file names if the conversion
     * 		was successful; otherwise null
     *
     * @see Transfer#javaToNative
     */
    Object nativeToJava(ITransferData transferData);

    FileTransfer getApi();
}
