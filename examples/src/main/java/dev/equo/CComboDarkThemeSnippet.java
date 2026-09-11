package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * CCombo under a dark theme, with and without application-supplied colors.
 *
 * A themed application colors its own controls, handing each one a color it read from
 * Display#getSystemColor. Every row is the same CCombo; only where its background and
 * foreground come from differs. Open each dropdown and compare the closed field, the
 * dropdown panel and the item text against each other and against the dark surface.
 */
public class CComboDarkThemeSnippet {

	private static final String[] ITEMS = { "TWT", "TVD", "TVDSS", "FREQ" };

	public static void main(String[] args) {
		System.setProperty("swt.evolve.force_theme", "dark");

		Config.useEquo(CCombo.class);
		Config.useEquo(Label.class);

		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setText("CCombo - dark theme color sources");
		shell.setLayout(new GridLayout(2, false));

		row(shell, "theme only (no color set)", null, null);
		row(shell, "WIDGET_BACKGROUND / WIDGET_FOREGROUND",
				display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND),
				display.getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));
		row(shell, "LIST_BACKGROUND / LIST_FOREGROUND",
				display.getSystemColor(SWT.COLOR_LIST_BACKGROUND),
				display.getSystemColor(SWT.COLOR_LIST_FOREGROUND));
		row(shell, "TEXT_DISABLED_BACKGROUND / LIST_FOREGROUND",
				display.getSystemColor(SWT.COLOR_TEXT_DISABLED_BACKGROUND),
				display.getSystemColor(SWT.COLOR_LIST_FOREGROUND));

		shell.setSize(560, 260);
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

	private static void row(Composite parent, String name, Color background, Color foreground) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(name);

		CCombo combo = new CCombo(parent, SWT.BORDER | SWT.READ_ONLY);
		combo.setLayoutData(new GridData(180, SWT.DEFAULT));
		combo.setItems(ITEMS);
		combo.select(2);
		if (background != null)
			combo.setBackground(background);
		if (foreground != null)
			combo.setForeground(foreground);

		System.out.println(name
				+ " -> background=" + rgb(background)
				+ ", foreground=" + rgb(foreground));
	}

	private static String rgb(Color color) {
		return color == null
				? "unset"
				: String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
	}
}
