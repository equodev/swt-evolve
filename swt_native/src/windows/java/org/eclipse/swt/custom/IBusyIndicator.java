package org.eclipse.swt.custom;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public interface IBusyIndicator {

    BusyIndicator getApi();

    void setApi(BusyIndicator api);
}
