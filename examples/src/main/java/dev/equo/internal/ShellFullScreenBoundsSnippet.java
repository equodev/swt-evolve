package dev.equo.internal;

import dev.equo.swt.Config;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Puts a shell in full-screen mode and compares what it covers with its monitor. Native Win32
 * full screen covers the monitor's whole bounds, taskbar included, not just its client area. The
 * snippet prints the SWT view (shell bounds against monitor bounds and client area) and the OS
 * view: every visible top-level window of this process with its GetWindowRect, its monitor's
 * rcMonitor and rcWork, and the taskbar's rect, read by a Win32 probe run through PowerShell.
 * Then it leaves full screen and prints both views again, which should match the shell's before.
 * Windows only.
 */
public class ShellFullScreenBoundsSnippet {
    public static void main(String[] args) {
        Config.forceEquo();

        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("ShellFullScreenBoundsSnippet");
        shell.setLayout(new FillLayout());
        new Label(shell, SWT.CENTER).setText("Full screen");
        shell.setBounds(100, 100, 600, 400);
        shell.open();

        display.timerExec(1000, () -> {
            print("before", shell);
            shell.setFullScreen(true);
            display.timerExec(1500, () -> {
                print("after setFullScreen(true)", shell);
                new Thread(() -> {
                    probeOsWindows();
                    display.asyncExec(() -> {
                        shell.setFullScreen(false);
                        display.timerExec(1500, () -> {
                            print("after setFullScreen(false)", shell);
                            new Thread(() -> {
                                probeOsWindows();
                                display.asyncExec(shell::close);
                            }).start();
                        });
                    });
                }).start();
            });
        });

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }

    static void print(String when, Shell shell) {
        Monitor monitor = shell.getMonitor();
        System.out.println("[" + when + "] getFullScreen=" + shell.getFullScreen()
                + " shell.getBounds=" + shell.getBounds()
                + " monitor.getBounds=" + monitor.getBounds()
                + " monitor.getClientArea=" + monitor.getClientArea());
    }

    static final String PROBE = String.join("\n",
            "Add-Type -TypeDefinition @'",
            "using System; using System.Runtime.InteropServices; using System.Text;",
            "public static class WinProbe {",
            "  [StructLayout(LayoutKind.Sequential)] public struct RECT { public int L, T, R, B; public override string ToString() { return \"(\" + L + \",\" + T + \" \" + (R - L) + \"x\" + (B - T) + \")\"; } }",
            "  [StructLayout(LayoutKind.Sequential)] public struct MONITORINFO { public int cbSize; public RECT rcMonitor; public RECT rcWork; public int dwFlags; }",
            "  public delegate bool EnumProc(IntPtr h, IntPtr l);",
            "  [DllImport(\"user32\")] static extern bool SetProcessDpiAwarenessContext(IntPtr v);",
            "  [DllImport(\"user32\")] static extern bool EnumWindows(EnumProc cb, IntPtr l);",
            "  [DllImport(\"user32\")] static extern uint GetWindowThreadProcessId(IntPtr h, out uint pid);",
            "  [DllImport(\"user32\")] static extern bool IsWindowVisible(IntPtr h);",
            "  [DllImport(\"user32\", CharSet = CharSet.Unicode)] static extern int GetWindowText(IntPtr h, StringBuilder s, int n);",
            "  [DllImport(\"user32\", CharSet = CharSet.Unicode)] static extern int GetClassName(IntPtr h, StringBuilder s, int n);",
            "  [DllImport(\"user32\")] static extern bool GetWindowRect(IntPtr h, out RECT r);",
            "  [DllImport(\"user32\")] static extern IntPtr MonitorFromWindow(IntPtr h, int flags);",
            "  [DllImport(\"user32\")] static extern bool GetMonitorInfo(IntPtr m, ref MONITORINFO mi);",
            "  [DllImport(\"user32\", CharSet = CharSet.Unicode)] static extern IntPtr FindWindow(string c, string t);",
            "  [DllImport(\"user32\")] static extern IntPtr GetForegroundWindow();",
            "  [DllImport(\"user32\")] static extern int GetWindowLong(IntPtr h, int i);",
            "  public static void Run(uint pid) {",
            "    SetProcessDpiAwarenessContext(new IntPtr(-4));",
            "    IntPtr fg = GetForegroundWindow();",
            "    EnumWindows((h, l) => {",
            "      uint p; GetWindowThreadProcessId(h, out p);",
            "      if (p != pid || !IsWindowVisible(h)) return true;",
            "      var t = new StringBuilder(256); GetWindowText(h, t, 256);",
            "      var c = new StringBuilder(256); GetClassName(h, c, 256);",
            "      RECT r; GetWindowRect(h, out r);",
            "      var mi = new MONITORINFO(); mi.cbSize = Marshal.SizeOf(mi); GetMonitorInfo(MonitorFromWindow(h, 2), ref mi);",
            "      bool covers = r.L <= mi.rcMonitor.L && r.T <= mi.rcMonitor.T && r.R >= mi.rcMonitor.R && r.B >= mi.rcMonitor.B;",
            "      bool equals = r.L == mi.rcMonitor.L && r.T == mi.rcMonitor.T && r.R == mi.rcMonitor.R && r.B == mi.rcMonitor.B;",
            "      int style = GetWindowLong(h, -16);",
            "      Console.WriteLine(\"os window class=\" + c + \" title='\" + t + \"' GetWindowRect=\" + r + \" rcMonitor=\" + mi.rcMonitor + \" rcWork=\" + mi.rcWork + \" coversMonitor=\" + covers + \" equalsMonitor=\" + equals + \" maximized=\" + ((style & 0x01000000) != 0) + \" caption=\" + ((style & 0x00C00000) == 0x00C00000) + \" thickframe=\" + ((style & 0x00040000) != 0) + \" foreground=\" + (h == fg));",
            "      return true; }, IntPtr.Zero);",
            "    IntPtr tray = FindWindow(\"Shell_TrayWnd\", null); RECT tr; GetWindowRect(tray, out tr);",
            "    Console.WriteLine(\"taskbar GetWindowRect=\" + tr + \" visible=\" + IsWindowVisible(tray));",
            "  }",
            "}",
            "'@",
            "[WinProbe]::Run(" + ProcessHandle.current().pid() + ")");

    static void probeOsWindows() {
        try {
            Path script = Files.createTempFile("winprobe", ".ps1");
            Files.write(script, PROBE.getBytes(StandardCharsets.UTF_8));
            Process p = new ProcessBuilder("powershell", "-NoProfile", "-ExecutionPolicy", "Bypass", "-File", script.toString())
                    .redirectErrorStream(true).start();
            try (BufferedReader out = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                out.lines().filter(s -> !s.isBlank()).forEach(System.out::println);
            }
            p.waitFor();
            Files.delete(script);
        } catch (Exception e) {
            System.out.println("probe failed: " + e);
        }
    }
}
