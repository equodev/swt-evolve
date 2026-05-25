package org.eclipse.swt.graphics;

import org.eclipse.swt.*;

public interface IGCData extends ImplGCData {

    GCData getApi();

    void setApi(GCData api);
}
