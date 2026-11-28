package org.eclipse.swt.widgets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import dev.equo.swt.Config;
import dev.equo.swt.FlutterBridge;
import dev.equo.swt.harness.RecordingBridge;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Which presentation a dialog Shell gets: what JFace stores in it, its style and its parent decide. */
@Tag("native-unit")
class DialogPresentationNativeTest {

    private static final int DIALOG = SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL;

    private Display display;
    private Shell main;
    private boolean savedFlag;
    private String savedOverrides;

    @BeforeEach
    void setUp() {
        FlutterBridge.set(new RecordingBridge());
        display = DartMocks.dartDisplay();
        when(((DartDisplay) display.getImpl()).isValidThread()).thenReturn(true);
        when(display.getShells()).thenReturn(new Shell[0]);
        main = new Shell(display, SWT.SHELL_TRIM);
        savedFlag = Config.getConfigFlags().modern_dialogs;
        savedOverrides = Config.getConfigFlags().dialog_presentation;
        Config.getConfigFlags().modern_dialogs = true;
    }

    @AfterEach
    void tearDown() {
        Config.getConfigFlags().modern_dialogs = savedFlag;
        Config.getConfigFlags().dialog_presentation = savedOverrides;
        FlutterBridge.set(null);
    }

    private int presentationOf(Object jfaceWindow) {
        Shell shell = new Shell(main, DIALOG);
        shell.setData(jfaceWindow);
        return DialogPresentation.of(shell);
    }

    @Test
    @DisplayName("a JFace question, a toggle question and an error are alerts")
    void questionsAreAlerts() {
        assertThat(presentationOf(new MessageDialog(null, "Confirm Exit", null, "Exit?", MessageDialog.QUESTION, new String[] { "Exit", "Cancel" }, 0)))
                .isEqualTo(DialogPresentation.ALERT);
        assertThat(presentationOf(new MessageDialogWithToggle(null, "Confirm Exit", null, "Exit?", MessageDialog.QUESTION,
                new String[] { "Exit", "Cancel" }, 0, "Always exit without asking", false)))
                .isEqualTo(DialogPresentation.ALERT);
        assertThat(presentationOf(new ErrorDialog(null, "Problem", "Build failed", new Status(org.eclipse.core.runtime.IStatus.ERROR, "dialogs", "missing jar"), 0xFFFF)))
                .isEqualTo(DialogPresentation.ALERT);
    }

    @Test
    @DisplayName("a wizard and the preferences are pages")
    void wizardsAndPreferencesArePages() {
        Wizard wizard = new Wizard() {
            @Override
            public boolean performFinish() {
                return true;
            }
        };
        assertThat(presentationOf(new WizardDialog(null, wizard))).isEqualTo(DialogPresentation.PAGE);
        assertThat(presentationOf(new PreferenceDialog(null, new PreferenceManager()))).isEqualTo(DialogPresentation.PAGE);
    }

    @Test
    @DisplayName("a wizard opened from another dialog stacks in that dialog's frame, as a sheet")
    void wizardFromADialogIsASheet() {
        Shell preferences = new Shell(main, DIALOG);
        preferences.setData(new PreferenceDialog(null, new PreferenceManager()));
        Shell addJre = new Shell(preferences, DIALOG);
        addJre.setData(new WizardDialog(null, new Wizard() {
            @Override
            public boolean performFinish() {
                return true;
            }
        }));
        assertThat(DialogPresentation.of(preferences)).isEqualTo(DialogPresentation.PAGE);
        assertThat(DialogPresentation.of(addJre)).isEqualTo(DialogPresentation.SHEET);
    }

    @Test
    @DisplayName("a subclass takes the presentation of the nearest class it extends")
    void subclassInherits() {
        class PropertiesLikeDialog extends PreferenceDialog {
            PropertiesLikeDialog() {
                super(null, new PreferenceManager());
            }
        }
        assertThat(presentationOf(new PropertiesLikeDialog())).isEqualTo(DialogPresentation.PAGE);
    }

    @Test
    @DisplayName("a progress monitor dialog is a progress card")
    void progress() {
        assertThat(presentationOf(new ProgressMonitorDialog(null))).isEqualTo(DialogPresentation.PROGRESS);
    }

    @Test
    @DisplayName("any other modal dialog with a parent is a sheet, JFace or not")
    void otherModalDialogsAreSheets() {
        assertThat(presentationOf(new TitleAreaDialog(null))).isEqualTo(DialogPresentation.SHEET);
        assertThat(DialogPresentation.of(new Shell(main, DIALOG))).isEqualTo(DialogPresentation.SHEET);
        assertThat(DialogPresentation.of(new Shell(main, SWT.DIALOG_TRIM | SWT.PRIMARY_MODAL))).isEqualTo(DialogPresentation.SHEET);
    }

    @Test
    @DisplayName("top-level, modeless, untitled and popup shells are left as they are")
    void untouched() {
        assertThat(DialogPresentation.of(main)).isEqualTo(DialogPresentation.NONE);
        assertThat(DialogPresentation.of(new Shell(display, DIALOG))).isEqualTo(DialogPresentation.NONE);
        assertThat(DialogPresentation.of(new Shell(main, SWT.DIALOG_TRIM))).isEqualTo(DialogPresentation.NONE);
        assertThat(DialogPresentation.of(new Shell(main, SWT.NO_TRIM | SWT.APPLICATION_MODAL))).isEqualTo(DialogPresentation.NONE);
        assertThat(DialogPresentation.of(new Shell(main, SWT.ON_TOP | SWT.TOOL | SWT.APPLICATION_MODAL))).isEqualTo(DialogPresentation.NONE);
        assertThat(DialogPresentation.of(new Shell(main, DIALOG | SWT.TOOL))).as("a titled tool popup").isEqualTo(DialogPresentation.NONE);
    }

    @Test
    @DisplayName("the flag off leaves every shell as it is")
    void flagOff() {
        Config.getConfigFlags().modern_dialogs = false;
        assertThat(presentationOf(new MessageDialog(null, "t", null, "m", MessageDialog.QUESTION, new String[] { "OK" }, 0))).isEqualTo(DialogPresentation.NONE);
        assertThat(DialogPresentation.of(new Shell(main, DIALOG))).isEqualTo(DialogPresentation.NONE);
    }

    @Test
    @DisplayName("an application pins a shell's presentation through its data")
    void dataOverride() {
        Shell shell = new Shell(main, DIALOG);
        shell.setData(DialogPresentation.DATA_KEY, "page");
        assertThat(DialogPresentation.of(shell)).isEqualTo(DialogPresentation.PAGE);
        shell.setData(DialogPresentation.DATA_KEY, "window");
        assertThat(DialogPresentation.of(shell)).isEqualTo(DialogPresentation.NONE);
        shell.setData(DialogPresentation.DATA_KEY, "alert");
        assertThat(DialogPresentation.of(shell)).isEqualTo(DialogPresentation.ALERT);
        shell.setData(DialogPresentation.DATA_KEY, "sheet");
        assertThat(DialogPresentation.of(shell)).isEqualTo(DialogPresentation.SHEET);
        shell.setData(DialogPresentation.DATA_KEY, "progress");
        assertThat(DialogPresentation.of(shell)).isEqualTo(DialogPresentation.PROGRESS);
        shell.setData(DialogPresentation.DATA_KEY, "bogus");
        assertThat(DialogPresentation.of(shell)).as("an unknown name falls back to the shell's kind").isEqualTo(DialogPresentation.SHEET);
    }

    @Test
    @DisplayName("of several class overrides the one naming the class applies, and an unknown presentation is ignored")
    void severalClassOverrides() {
        Config.getConfigFlags().dialog_presentation = "a.B=page, org.eclipse.jface.dialogs.MessageDialog = sheet, c.D";
        assertThat(presentationOf(new MessageDialog(null, "t", null, "m", MessageDialog.QUESTION, new String[] { "OK" }, 0)))
                .isEqualTo(DialogPresentation.SHEET);
        Config.getConfigFlags().dialog_presentation = "org.eclipse.jface.dialogs.MessageDialog=bogus";
        assertThat(presentationOf(new MessageDialog(null, "t", null, "m", MessageDialog.QUESTION, new String[] { "OK" }, 0)))
                .isEqualTo(DialogPresentation.ALERT);
        Config.getConfigFlags().dialog_presentation = "org.eclipse.jface.dialogs.MessageDialog, org.eclipse.jface.dialogs.MessageDialog=page";
        assertThat(presentationOf(new MessageDialog(null, "t", null, "m", MessageDialog.QUESTION, new String[] { "OK" }, 0)))
                .as("an entry with no presentation is skipped").isEqualTo(DialogPresentation.PAGE);
        Config.getConfigFlags().dialog_presentation = "a.B=page";
        assertThat(presentationOf(new MessageDialog(null, "t", null, "m", MessageDialog.QUESTION, new String[] { "OK" }, 0)))
                .as("overrides naming other classes").isEqualTo(DialogPresentation.ALERT);
        Config.getConfigFlags().dialog_presentation = null;
        assertThat(presentationOf(new MessageDialog(null, "t", null, "m", MessageDialog.QUESTION, new String[] { "OK" }, 0)))
                .isEqualTo(DialogPresentation.ALERT);
    }

    @Test
    @DisplayName("an override names a class exactly: one for a subclass, or with no class, leaves its superclass alone")
    void overrideNamesAClassExactly() {
        Config.getConfigFlags().dialog_presentation = "org.eclipse.jface.dialogs.MessageDialogWithToggle=page, =sheet";
        assertThat(presentationOf(new MessageDialog(null, "t", null, "m", MessageDialog.QUESTION, new String[] { "OK" }, 0)))
                .isEqualTo(DialogPresentation.ALERT);
    }

    @Test
    @DisplayName("no shell, or a disposed one, has no presentation")
    void disposedShellIsNone() {
        assertThat(DialogPresentation.of(null)).isEqualTo(DialogPresentation.NONE);
        Shell shell = new Shell(main, DIALOG);
        shell.dispose();
        assertThat(DialogPresentation.of(shell)).isEqualTo(DialogPresentation.NONE);
    }

    @Test
    @DisplayName("a system property pins a dialog class, its subclasses included")
    void classOverride() {
        Config.getConfigFlags().dialog_presentation =
                "org.eclipse.jface.dialogs.TitleAreaDialog=page, org.eclipse.jface.dialogs.MessageDialog=window";
        assertThat(presentationOf(new TitleAreaDialog(null))).isEqualTo(DialogPresentation.PAGE);
        assertThat(presentationOf(new MessageDialog(null, "t", null, "m", MessageDialog.QUESTION, new String[] { "OK" }, 0)))
                .isEqualTo(DialogPresentation.NONE);
    }

    @Test
    @DisplayName("turned off while running, every shell is described again as a window")
    void switchedAtRuntime() {
        when(display.getShells()).thenReturn(new Shell[0]);
        Shell shell = new Shell(main, DIALOG);
        when(display.getShells()).thenReturn(new Shell[] { main, shell });
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        doAnswer(i -> data.put(i.getArgument(0), i.getArgument(1))).when(display).setData(anyString(), any());
        when(display.getData(anyString())).thenAnswer(i -> data.get(i.<String>getArgument(0)));
        DartDisplay impl = (DartDisplay) display.getImpl();
        DisplayBridge client = org.mockito.Mockito.mock(DisplayBridge.class);
        impl.displayBridge = client;
        try {
            DialogPresentation.setEnabled(display, false);
        } finally {
            impl.displayBridge = null;
        }
        org.mockito.Mockito.verify(client).sendDisplayUpdate(impl);
        assertThat(DialogPresentation.enabled(display)).isFalse();
        assertThat(Config.getConfigFlags().modern_dialogs).as("the JVM's flag, shared by every session, is not changed").isTrue();
        dev.equo.swt.ConfigFlags sent = DialogPresentation.flagsFor(display);
        assertThat(sent.modern_dialogs).as("the client gets this display's value").isFalse();
        assertThat(sent).isNotSameAs(Config.getConfigFlags());
        assertThat(((DartShell) shell.getImpl()).getValue().getPresentation()).isEqualTo(DialogPresentation.NONE);
        DialogPresentation.setEnabled(display, true);
        assertThat(((DartShell) shell.getImpl()).getValue().getPresentation()).isEqualTo(DialogPresentation.SHEET);
    }

    @Test
    @DisplayName("with a window per shell, a presented dialog of the main window is still drawn inside it")
    void drawnInParentUnderMultiWindow() {
        main.setSize(1200, 800);
        Shell sheet = new Shell(main, DIALOG);
        sheet.setSize(500, 300);
        assertThat(DialogPresentation.drawnInParent(sheet, main)).isTrue();

        Shell tooBig = new Shell(main, DIALOG);
        tooBig.setSize(3000, 300);
        assertThat(DialogPresentation.drawnInParent(tooBig, main)).as("larger than the window").isFalse();
        Shell tooTall = new Shell(main, DIALOG);
        tooTall.setSize(500, 3000);
        assertThat(DialogPresentation.drawnInParent(tooTall, main)).as("taller than the window").isFalse();

        Shell pinned = new Shell(main, DIALOG);
        pinned.setData(dev.equo.swt.WindowPolicy.SHELL_DATA_KEY, Boolean.TRUE);
        assertThat(DialogPresentation.drawnInParent(pinned, main)).as("the application asked for a window").isFalse();

        Shell resolved = new Shell(main, DIALOG);
        dev.equo.swt.WindowPolicy.setResolver((shell, isMain) -> shell == resolved ? Boolean.TRUE : null);
        try {
            assertThat(DialogPresentation.drawnInParent(resolved, main)).as("the application's resolver asked for a window").isFalse();
        } finally {
            dev.equo.swt.WindowPolicy.setResolver(null);
        }

        Shell other = new Shell(display, SWT.SHELL_TRIM);
        Shell ofOther = new Shell(other, DIALOG);
        assertThat(DialogPresentation.drawnInParent(ofOther, main)).as("a dialog of another window").isFalse();
        assertThat(DialogPresentation.drawnInParent(new Shell(main, SWT.DIALOG_TRIM), main)).as("modeless").isFalse();

        String saved = System.getProperty(dev.equo.swt.WindowPolicy.MODAL_PROPERTY);
        System.setProperty(dev.equo.swt.WindowPolicy.MODAL_PROPERTY, "own");
        try {
            assertThat(DialogPresentation.drawnInParent(new Shell(main, DIALOG), main)).as("modal shells asked into windows of their own").isFalse();
        } finally {
            if (saved == null) System.clearProperty(dev.equo.swt.WindowPolicy.MODAL_PROPERTY);
            else System.setProperty(dev.equo.swt.WindowPolicy.MODAL_PROPERTY, saved);
        }
    }

    @Test
    @DisplayName("the presentation travels on the shell's record")
    void onTheWire() {
        Shell shell = new Shell(main, DIALOG);
        shell.setData(new MessageDialog(null, "t", null, "m", MessageDialog.QUESTION, new String[] { "OK" }, 0));
        assertThat(((DartShell) shell.getImpl()).getValue().getPresentation()).isEqualTo(DialogPresentation.ALERT);
    }
}
