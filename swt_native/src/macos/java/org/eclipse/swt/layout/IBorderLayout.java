package org.eclipse.swt.layout;

import static org.eclipse.swt.SWT.*;
import java.util.*;
import java.util.AbstractMap.*;
import java.util.Map.*;
import java.util.function.*;
import java.util.stream.*;
import java.util.stream.IntStream.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public interface IBorderLayout extends ILayout {

    String toString();

    BorderLayout getApi();
}
