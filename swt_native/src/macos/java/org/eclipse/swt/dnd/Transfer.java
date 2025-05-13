/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2008 IBM Corporation and others.
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

/**
 * <code>Transfer</code> provides a mechanism for converting between a java
 * representation of data and a platform specific representation of data and
 * vice versa.  It is used in data transfer operations such as drag and drop and
 * clipboard copy/paste.
 *
 * <p>You should only need to become familiar with this class if you are
 * implementing a Transfer subclass and you are unable to subclass the
 * ByteArrayTransfer class.</p>
 *
 * @see ByteArrayTransfer
 * @see <a href="http://www.eclipse.org/swt/snippets/#dnd">Drag and Drop snippets</a>
 * @see <a href="http://www.eclipse.org/swt/examples.php">SWT Example: DNDExample</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 */
public abstract class Transfer {

    /**
     * Returns a list of the platform specific data types that can be converted using
     * this transfer agent.
     *
     * <p>Only the data type fields of the <code>TransferData</code> objects are filled
     * in.</p>
     *
     * @return a list of the data types that can be converted using this transfer agent
     */
    abstract public TransferData[] getSupportedTypes();

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
    abstract public boolean isSupportedType(TransferData transferData);

    /**
     * Registers a name for a data type and returns the associated unique identifier.
     *
     * <p>You may register the same type more than once, the same unique identifier
     * will be returned if the type has been previously registered.</p>
     *
     * <p>Note: On windows, do <b>not</b> call this method with pre-defined
     * Clipboard Format types such as CF_TEXT or CF_BITMAP because the
     * pre-defined identifier will not be returned</p>
     *
     * @param formatName the name of a data type
     *
     * @return the unique identifier associated with this data type
     */
    public static int registerType(String formatName) {
        return nat.org.eclipse.swt.dnd.Transfer.registerType(formatName);
    }

    ITransfer delegate;

    protected Transfer(ITransfer delegate) {
        this.delegate = delegate;
        delegate.setApi(this);
    }

    public ITransfer getDelegate() {
        return delegate;
    }
}
