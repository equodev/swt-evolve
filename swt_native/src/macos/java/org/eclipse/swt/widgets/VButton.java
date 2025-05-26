package org.eclipse.swt.widgets;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import com.dslplatform.json.*;

@CompiledJson()
class VButton extends VControl {

    public int alignment;

    public boolean grayed;

    @JsonAttribute(ignore = true)
    public Image image;

    public boolean selection;

    public String text;
}
