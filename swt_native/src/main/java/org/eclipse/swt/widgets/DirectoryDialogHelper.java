package org.eclipse.swt.widgets;

final class DirectoryDialogHelper {

    private DirectoryDialogHelper() {
    }

    /**
     * Opens the chooser Flutter draws over the server's directories and answers the one picked, or null
     * for a cancel. The pick becomes the filter path, as on the platform choosers. With no client
     * connected nobody could answer, so it is a cancel.
     */
    static String chooseOnClient(DartDirectoryDialog dialog) {
        if (dialog.dialogBridge(dialog.getParent().getDisplay()) == null) return null;
        ServerFiles.Choice choice = ServerFiles.choice(dialog.openDialogWithFlutter(dialog.getValue(),
                request -> ServerFiles.navigate(request, null, true)));
        if (choice == null) return null;
        dialog.filterPath = choice.directory;
        return choice.fullPath();
    }
}
