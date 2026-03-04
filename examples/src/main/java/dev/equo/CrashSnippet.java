package dev.equo;                                                                                                                                                                                                                                        
                                                                                                                                                                                                                                                           
import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.*;

public class CrashSnippet {
  public static void main(String[] args) {
      Config.useEquo(Button.class); // triggers Config class loading → CrashReporter.init()

      Display display = new Display();
      Shell shell = new Shell(display);
      shell.setText("Crash Test");
      shell.setLayout(new RowLayout(SWT.VERTICAL));

      Button button = new Button(shell, SWT.PUSH);
      button.setText("Click to crash");
      button.addListener(SWT.Selection, e -> {
          throw new RuntimeException("Test crash from button click!");
      });

      shell.setSize(300, 100);
      shell.open();
      while (!shell.isDisposed()) {
          if (!display.readAndDispatch()) display.sleep();
      }
      display.dispose();
  }
}
