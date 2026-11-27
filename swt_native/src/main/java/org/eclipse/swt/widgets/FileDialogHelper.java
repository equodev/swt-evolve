package org.eclipse.swt.widgets;

final class FileDialogHelper {

    private FileDialogHelper() {
    }

    /**
     * Opens the chooser Flutter draws over the server's file system and answers the path of the first
     * file picked, or null for a cancel. The choice sets what the getters report, as the platform
     * choosers do: the directory as the filter path, the names picked there, and the filter used. With
     * no client connected nobody could answer, so it is a cancel.
     */
    static String chooseOnClient(DartFileDialog dialog) {
        if (dialog.dialogBridge(dialog.getParent().getDisplay()) == null) return null;
        ServerFiles.Choice choice = ServerFiles.choice(dialog.openDialogWithFlutter(dialog.getValue(),
                request -> ServerFiles.navigate(request, dialog.filterExtensions, false)));
        if (choice == null) return null;
        if (choice.filterIndex >= 0) dialog.filterIndex = choice.filterIndex;
        dialog.filterPath = choice.directory;
        dialog.fileNames = choice.names;
        if (choice.names.length > 0) dialog.fileName = choice.names[0];
        return choice.fullPath();
    }
}
