package org.eclipse.swt.graphics;

import org.eclipse.swt.*;

public interface ImplPattern extends ImplResource {

    Image _image();

    double[] _color1();

    double[] _color2();

    int _alpha1();

    int _alpha2();
}
