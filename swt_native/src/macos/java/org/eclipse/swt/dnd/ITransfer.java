package org.eclipse.swt.dnd;

public interface ITransfer {

    /**
     * Returns a list of the platform specific data types that can be converted using
     * this transfer agent.
     *
     * <p>Only the data type fields of the <code>TransferData</code> objects are filled
     * in.</p>
     *
     * @return a list of the data types that can be converted using this transfer agent
     */
    abstract ITransferData[] getSupportedTypes();

    /**
     * Returns true if the <code>TransferData</code> data type can be converted
     * using this transfer agent, or false otherwise (including if transferData is
     * <code>null</code>).
     *
     * @param transferData a platform specific description of a data type; only the data
     *  type fields of the <code>TransferData</code> object need to be filled in
     *
     * @return true if the transferData data type can be converted using this transfer
     * agent
     */
    abstract boolean isSupportedType(ITransferData transferData);

    /**
     * Returns the platform specific ids of the  data types that can be converted using
     * this transfer agent.
     *
     * @return the platform specific ids of the data types that can be converted using
     * this transfer agent
     */
    abstract int[] getTypeIds();

    /**
     * Returns the platform specific names of the  data types that can be converted
     * using this transfer agent.
     *
     * @return the platform specific names of the data types that can be converted
     * using this transfer agent.
     */
    abstract String[] getTypeNames();

    /**
     * Converts a java representation of data to a platform specific representation of
     * the data.
     *
     * <p>On a successful conversion, the transferData.result field will be set as follows:</p>
     * <ul>
     * <li>Windows: COM.S_OK
     * <li>GTK: 1
     * </ul>
     *
     * <p>If this transfer agent is unable to perform the conversion, the transferData.result
     * field will be set to a failure value as follows:</p>
     * <ul>
     * <li>Windows: COM.DV_E_TYMED or COM.E_FAIL
     * <li>GTK: 0
     * </ul>
     *
     * @param object a java representation of the data to be converted; the type of
     * Object that is passed in is dependent on the <code>Transfer</code> subclass.
     *
     * @param transferData an empty TransferData object; this object will be
     * filled in on return with the platform specific representation of the data
     *
     * @exception org.eclipse.swt.SWTException <ul>
     *    <li>ERROR_INVALID_DATA - if object does not contain data in a valid format or is <code>null</code></li>
     * </ul>
     */
    abstract void javaToNative(Object object, ITransferData transferData);

    /**
     * Converts a platform specific representation of data to a java representation.
     *
     * @param transferData the platform specific representation of the data to be
     * converted
     *
     * @return a java representation of the converted data if the conversion was
     * successful; otherwise null.  If transferData is <code>null</code> then
     * <code>null</code> is returned.  The type of Object that is returned is
     * dependent on the <code>Transfer</code> subclass.
     */
    abstract Object nativeToJava(ITransferData transferData);

    /**
     * Test that the object is of the correct format for this Transfer class.
     *
     * @param object a java representation of the data to be converted
     *
     * @return true if object is of the correct form for this transfer type
     *
     * @since 3.1
     */
    boolean validate(Object object);

    Transfer getApi();

    void setApi(Transfer api);
}
