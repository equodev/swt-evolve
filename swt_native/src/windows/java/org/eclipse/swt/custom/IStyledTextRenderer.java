package org.eclipse.swt.custom;

import java.util.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public interface IStyledTextRenderer extends ImplStyledTextRenderer {

    /**
     * See {@link TextLayout#setFixedLineMetrics}
     *
     * @since 3.125
     */
    void setFixedLineMetrics(FontMetrics metrics);

    boolean hasVerticalIndent();

    StyledTextRenderer getApi();

    void setApi(StyledTextRenderer api);
}
