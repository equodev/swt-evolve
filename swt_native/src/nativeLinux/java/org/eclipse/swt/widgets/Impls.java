package org.eclipse.swt.widgets;

import dev.equo.swt.Config;

class Impls {

    static IIFileDialog newFileDialog(Shell parent, org.eclipse.swt.widgets.FileDialog api) {
        return Config.isEquo(org.eclipse.swt.widgets.FileDialog.class, parent) ? new DartFileDialog(parent, api) : new SwtFileDialog(parent, api);
    }

    static IIFileDialog newFileDialog(Shell parent, int style, org.eclipse.swt.widgets.FileDialog api) {
        return Config.isEquo(org.eclipse.swt.widgets.FileDialog.class, parent) ? new DartFileDialog(parent, style, api) : new SwtFileDialog(parent, style, api);
    }

    static IDirectoryDialog newDirectoryDialog(Shell parent, org.eclipse.swt.widgets.DirectoryDialog api) {
        return Config.isEquo(org.eclipse.swt.widgets.DirectoryDialog.class, parent) ? new DartDirectoryDialog(parent, api) : new SwtDirectoryDialog(parent, api);
    }

    static IDirectoryDialog newDirectoryDialog(Shell parent, int style, org.eclipse.swt.widgets.DirectoryDialog api) {
        return Config.isEquo(org.eclipse.swt.widgets.DirectoryDialog.class, parent) ? new DartDirectoryDialog(parent, style, api) : new SwtDirectoryDialog(parent, style, api);
    }
}
