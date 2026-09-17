package org.eclipse.swt.widgets;

class Impls {

    static IDialog newDialog(Shell parent, org.eclipse.swt.widgets.Dialog api) {
        return new DartDialog(parent, api) {
        };
    }

    static IDialog newDialog(Shell parent, int style, org.eclipse.swt.widgets.Dialog api) {
        return new DartDialog(parent, style, api) {
        };
    }

    static IDirectoryDialog newDirectoryDialog(Shell parent, org.eclipse.swt.widgets.DirectoryDialog api) {
        return new DartDirectoryDialog(parent, api);
    }

    static IDirectoryDialog newDirectoryDialog(Shell parent, int style, org.eclipse.swt.widgets.DirectoryDialog api) {
        return new DartDirectoryDialog(parent, style, api);
    }

    static IIFileDialog newFileDialog(Shell parent, org.eclipse.swt.widgets.FileDialog api) {
        return new DartFileDialog(parent, api);
    }

    static IIFileDialog newFileDialog(Shell parent, int style, org.eclipse.swt.widgets.FileDialog api) {
        return new DartFileDialog(parent, style, api);
    }
}
