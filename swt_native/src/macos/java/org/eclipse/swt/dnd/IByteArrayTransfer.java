package org.eclipse.swt.dnd;

public interface IByteArrayTransfer extends ITransfer {

    ITransferData[] getSupportedTypes();

    boolean isSupportedType(ITransferData transferData);

    ByteArrayTransfer getApi();
}
