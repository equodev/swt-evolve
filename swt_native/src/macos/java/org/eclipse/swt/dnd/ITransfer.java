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

    Transfer getApi();

    void setApi(Transfer api);
}
