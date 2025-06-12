/**
 * ****************************************************************************
 *  Copyright (c) 2010 IBM Corporation and others.
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
package org.eclipse.swt.accessibility;

import org.eclipse.swt.internal.cocoa.*;

/**
 * This class is used to describe a table column for objects that have an accessible
 * role of ACC.ROLE_TABLE, but aren't implemented like NSTableViews.
 *
 * Instances of this class represent one row in a table. Cocoa accessibility expects
 * rows to report their location, and assumes the cells of the table are children of the rows.
 *
 * @see TableAccessibleDelegate
 */
class SwtAccessibleTableRow extends SwtAccessible implements IAccessibleTableRow {

    public SwtAccessibleTableRow(Accessible accessible, int childID, AccessibleTableRow api) {
        super(accessible, api);
        index = childID;
        addAccessibleControlListener(new AccessibleControlAdapter() {

            @Override
            public void getChildCount(AccessibleControlEvent e) {
                e.detail = Math.max(1, ((SwtAccessible) parent.getImpl()).getColumnCount());
            }

            @Override
            public void getChildren(AccessibleControlEvent e) {
                int validColumnCount = Math.max(1, ((SwtAccessible) parent.getImpl()).getColumnCount());
                Object[] children = new Object[validColumnCount];
                AccessibleTableEvent event = new AccessibleTableEvent(this);
                for (int i = 0; i < validColumnCount; i++) {
                    event.column = i;
                    event.row = index;
                    for (int j = 0; j < ((SwtAccessible) parent.getImpl()).accessibleTableListeners.size(); j++) {
                        AccessibleTableListener listener = ((SwtAccessible) parent.getImpl()).accessibleTableListeners.get(j);
                        listener.getCell(event);
                    }
                    if (event.accessible != null) {
                        ((SwtAccessible) event.accessible.getImpl()).parent = SwtAccessibleTableRow.this.getApi();
                    }
                    children[i] = event.accessible;
                }
                e.children = children;
            }

            @Override
            public void getLocation(AccessibleControlEvent e) {
                int validColumnCount = Math.max(1, ((SwtAccessible) parent.getImpl()).getColumnCount());
                Accessible[] children = new Accessible[validColumnCount];
                AccessibleTableEvent event = new AccessibleTableEvent(this);
                for (int i = 0; i < validColumnCount; i++) {
                    event.column = i;
                    event.row = index;
                    for (int j = 0; j < ((SwtAccessible) parent.getImpl()).accessibleTableListeners.size(); j++) {
                        AccessibleTableListener listener = ((SwtAccessible) parent.getImpl()).accessibleTableListeners.get(j);
                        listener.getCell(event);
                    }
                    children[i] = event.accessible;
                }
                // Ask first child for position.
                NSValue positionObj = (NSValue) ((SwtAccessible) children[0].getImpl()).getPositionAttribute(ACC.CHILDID_SELF);
                NSPoint position = positionObj.pointValue();
                // Ask all children for size.
                int height = 0;
                int width = 0;
                for (int j = 0; j < children.length; j++) {
                    NSValue sizeObj = (NSValue) ((SwtAccessible) children[j].getImpl()).getSizeAttribute(ACC.CHILDID_SELF);
                    NSSize size = sizeObj.sizeValue();
                    if (size.height > height)
                        height = (int) size.height;
                    width += size.width;
                }
                e.x = (int) position.x;
                // Flip y coordinate for Cocoa.
                NSArray screens = NSScreen.screens();
                if (screens == null)
                    return;
                NSScreen screen = new NSScreen(screens.objectAtIndex(0));
                NSRect frame = screen.frame();
                e.y = (int) (frame.height - position.y - height);
                e.width = width;
                e.height = height;
            }

            @Override
            public void getRole(AccessibleControlEvent e) {
                int childID = e.childID;
                if (childID == ACC.CHILDID_SELF) {
                    e.detail = ACC.ROLE_ROW;
                } else {
                    e.detail = ACC.ROLE_TABLECELL;
                }
            }

            @Override
            public void getFocus(AccessibleControlEvent e) {
                AccessibleTableEvent event = new AccessibleTableEvent(this);
                event.column = 0;
                event.row = index;
                for (int j = 0; j < ((SwtAccessible) parent.getImpl()).accessibleTableListeners.size(); j++) {
                    AccessibleTableListener listener = ((SwtAccessible) parent.getImpl()).accessibleTableListeners.get(j);
                    listener.getCell(event);
                }
                if (event.accessible != null) {
                    NSNumber focusedObj = (NSNumber) ((SwtAccessible) event.accessible.getImpl()).getFocusedAttribute(ACC.CHILDID_SELF);
                    e.childID = focusedObj.boolValue() ? ACC.CHILDID_SELF : ACC.CHILDID_NONE;
                } else {
                    e.childID = ACC.CHILDID_NONE;
                }
            }
        });
        addAccessibleTableListener(new AccessibleTableAdapter() {

            @Override
            public void isColumnSelected(AccessibleTableEvent e) {
                e.isSelected = false;
            }

            @Override
            public void isRowSelected(AccessibleTableEvent e) {
                // Delegate to the parent table.
                AccessibleTableEvent event = new AccessibleTableEvent(this);
                event.row = e.row;
                for (int i = 0; i < ((SwtAccessible) parent.getImpl()).accessibleTableListeners.size(); i++) {
                    AccessibleTableListener listener = ((SwtAccessible) parent.getImpl()).accessibleTableListeners.get(i);
                    listener.isRowSelected(event);
                }
                e.isSelected = event.isSelected;
            }
        });
    }

    void getChildAtPoint(AccessibleControlEvent e) {
        int validColumnCount = Math.max(1, ((SwtAccessible) parent.getImpl()).getColumnCount());
        Accessible[] children = new Accessible[validColumnCount];
        AccessibleTableEvent event = new AccessibleTableEvent(this.getApi());
        for (int i = 0; i < validColumnCount; i++) {
            event.column = i;
            event.row = index;
            for (int j = 0; j < ((SwtAccessible) parent.getImpl()).accessibleTableListeners.size(); j++) {
                AccessibleTableListener listener = ((SwtAccessible) parent.getImpl()).accessibleTableListeners.get(j);
                listener.getCell(event);
            }
            children[i] = event.accessible;
        }
        for (int j = 0; j < children.length; j++) {
            NSValue positionObj = (NSValue) ((SwtAccessible) children[j].getImpl()).getPositionAttribute(index);
            NSPoint position = positionObj.pointValue();
            NSValue sizeObj = (NSValue) ((SwtAccessible) children[j].getImpl()).getSizeAttribute(index);
            NSSize size = sizeObj.sizeValue();
            if (position.x <= e.x && e.x <= position.x + size.width) {
                ((SwtAccessible) children[j].getImpl()).parent = this.getApi();
                e.accessible = children[j];
                break;
            }
        }
    }

    public AccessibleTableRow getApi() {
        if (api == null)
            api = AccessibleTableRow.createApi(this);
        return (AccessibleTableRow) api;
    }
}
