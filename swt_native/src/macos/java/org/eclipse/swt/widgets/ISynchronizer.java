package org.eclipse.swt.widgets;

import java.util.*;
import java.util.concurrent.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;

public interface ISynchronizer {

    Synchronizer getApi();

    void setApi(Synchronizer api);
}
