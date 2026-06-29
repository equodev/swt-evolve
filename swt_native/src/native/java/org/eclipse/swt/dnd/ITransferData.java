package org.eclipse.swt.dnd;

public interface ITransferData extends ImplTransferData {

    TransferData getApi();

    void setApi(TransferData api);
}
