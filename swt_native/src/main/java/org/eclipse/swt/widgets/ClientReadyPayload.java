package org.eclipse.swt.widgets;

import com.dslplatform.json.CompiledJson;
import com.dslplatform.json.CompiledJson.ObjectFormatPolicy;

@CompiledJson(objectFormatPolicy = ObjectFormatPolicy.FULL)
public class ClientReadyPayload {
    public int width;
    public int height;
    public boolean isFirst;
}
