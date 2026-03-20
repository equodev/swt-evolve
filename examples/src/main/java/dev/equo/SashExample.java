package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class SashExample {

    public static void main(String[] args) {
        System.setProperty("dev.equo.swt.debug", "true");
//        System.setProperty("dev.equo.swt.Comopsite", "equo");
        Config.forceEquo();
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("SashForm Example");
        shell.setSize(800, 600);
        shell.setLayout(null);

        // Sash horizontal (divide izquierda / derecha)
        SashForm sash = new SashForm(shell, SWT.HORIZONTAL);
        sash.setBounds(0, 0, 800, 600);

        // Lado izquierdo (simula una vista)
        CTabFolder leftFolder = new CTabFolder(sash, SWT.BORDER);
        CTabItem leftTab = new CTabItem(leftFolder, SWT.NONE);
        leftTab.setText("Vista");
        leftTab.setControl(new Text(leftFolder, SWT.MULTI));

        // Lado derecho (simula editor)
        CTabFolder rightFolder = new CTabFolder(sash, SWT.BORDER);
        CTabItem rightTab = new CTabItem(rightFolder, SWT.NONE);
        rightTab.setText("Editor");
        rightTab.setControl(new Text(rightFolder, SWT.MULTI));

        // Proporción inicial (30% - 70%)
        sash.setWeights(new int[] {30, 70});

        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        display.dispose();
    }
}