package dev.equo;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

/**
 * A Label, a read-only Combo and a {@code FLAT | WRAP | RIGHT} ToolBar with a text-only item and two
 * image + text items, in one row: every text should draw at the same size.
 *
 * <p>Arguments: {@code native} runs it on native SWT ({@code equo} on Evolve); a second argument, a
 * PNG path, prints each widget's font and bounds, saves the shell's pixels there and exits.
 */
public class ToolBarTextBesideLabelSnippet {

	public static void main(String[] args) {
		if (args.length > 0 && args[0].equals("native")) Config.defaultToEclipse();
		else Config.defaultToEquo();

		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setText("ToolBarTextBesideLabelSnippet");
		shell.setLayout(new GridLayout(4, false));

		Label label = new Label(shell, SWT.NONE);
		label.setText("Scale:");
		Combo combo = new Combo(shell, SWT.READ_ONLY);
		combo.setItems("Linear", "Log");
		combo.select(0);
		Label label2 = new Label(shell, SWT.NONE);
		label2.setText("Design");

		ToolBar bar = new ToolBar(shell, SWT.FLAT | SWT.WRAP | SWT.RIGHT);
		Image image = icon(display);
		ToolItem textOnly = new ToolItem(bar, SWT.PUSH);
		textOnly.setText("Design");
		ToolItem withImage = new ToolItem(bar, SWT.CHECK);
		withImage.setImage(image);
		withImage.setText("Design");
		ToolItem withImage2 = new ToolItem(bar, SWT.CHECK);
		withImage2.setImage(image);
		withImage2.setText("Analysis");

		shell.pack();
		String dump = args.length > 1 ? args[1] : null;
		if (dump != null) shell.setLocation(-5000, -5000);
		shell.open();

		if (dump != null) {
			display.timerExec(1500, () -> {
				for (Control c : new Control[] {label, combo, label2, bar}) describe(c.getClass().getSimpleName(), c.getFont().getFontData(), c.getBounds().toString());
				for (ToolItem item : bar.getItems()) describe("ToolItem '" + item.getText() + "' image=" + (item.getImage() != null), bar.getFont().getFontData(), item.getBounds().toString());
				Image shot = new Image(display, shell.getSize().x, shell.getSize().y);
				GC gc = new GC(shot);
				shell.print(gc);
				gc.dispose();
				ImageLoader loader = new ImageLoader();
				loader.data = new ImageData[] {shot.getImageData()};
				loader.save(dump, SWT.IMAGE_PNG);
				System.out.println("SNIPPET saved " + dump);
				shell.dispose();
			});
		}
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) display.sleep();
		}
		image.dispose();
		display.dispose();
	}

	private static void describe(String what, FontData[] fd, String bounds) {
		System.out.println("SNIPPET " + what + " font=" + fd[0].getName() + " " + fd[0].getHeight() + "pt style=" + fd[0].getStyle() + " bounds=" + bounds);
	}

	private static Image icon(Display display) {
		ImageData data = new ImageData(16, 16, 24, new PaletteData(0xFF0000, 0xFF00, 0xFF));
		int blue = data.palette.getPixel(new RGB(40, 110, 200));
		for (int y = 2; y < 14; y++) for (int x = 2; x < 14; x++) data.setPixel(x, y, blue);
		return new Image(display, data);
	}
}
