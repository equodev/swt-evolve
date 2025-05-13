package org.eclipse.swt.dnd;

import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public interface IDNDEvent {

    DNDEvent getApi();

    void setApi(DNDEvent api);
}
