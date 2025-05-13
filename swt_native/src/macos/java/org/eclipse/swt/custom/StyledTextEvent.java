/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2012 IBM Corporation and others.
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
package org.eclipse.swt.custom;

import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public class StyledTextEvent extends Event {

    // used by LineStyleEvent
    public int[] ranges;

    public StyleRange[] styles;

    public int alignment;

    public int indent;

    public int verticalIndent;

    public int wrapIndent;

    public boolean justify;

    public Bullet bullet;

    public int bulletIndex;

    public int[] tabStops;

    // used by LineBackgroundEvent
    public Color lineBackground;

    // used by TextChangedEvent
    public int replaceCharCount;

    public int newCharCount;

    public int replaceLineCount;

    public int newLineCount;

    // used by PaintObjectEvent
    public int x;

    public int y;

    public int ascent;

    public int descent;

    public StyleRange style;

    public StyledTextEvent(StyledTextContent content) {
        super();
        data = content;
    }
}
