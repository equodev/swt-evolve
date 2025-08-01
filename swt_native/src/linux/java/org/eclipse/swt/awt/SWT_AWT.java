/**
 * ****************************************************************************
 *  Copyright (c) 2000, 2019 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *      IBM Corporation - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.swt.awt;

/* AWT Imports */
import java.awt.*;
import java.awt.Canvas;
import java.awt.event.*;
import java.lang.reflect.*;
/* SWT Imports */
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.*;
import org.eclipse.swt.internal.gtk.*;
import org.eclipse.swt.internal.gtk3.*;
import org.eclipse.swt.internal.gtk4.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.Composite;

/**
 * This class provides a bridge between SWT and AWT, so that it
 * is possible to embed AWT components in SWT and vice versa.
 *
 * @see <a href="http://www.eclipse.org/swt/snippets/#awt">Swing/AWT snippets</a>
 * @see <a href="http://www.eclipse.org/swt/">Sample code and further information</a>
 *
 * @since 3.0
 */
public class SWT_AWT {

    /**
     * The name of the embedded Frame class. The default class name
     * for the platform will be used if <code>null</code>.
     */
    public static String embeddedFrameClass;

    /**
     * Key for looking up the embedded frame for a Composite using
     * getData().
     */
    static String EMBEDDED_FRAME_KEY = "org.eclipse.swt.awt.SWT_AWT.embeddedFrame";

    static boolean loaded, swingInitialized;

    static native final long getAWTHandle(Object canvas);

    static native final void setDebug(Frame canvas, boolean debug);

    static native final Object initFrame(long handle, String className);

    static native final void validateWithBounds(Frame frame, int x, int y, int w, int h);

    static native final void synthesizeWindowActivation(Frame frame, boolean doActivate);

    static native final void registerListeners(Frame frame);

    static synchronized void loadLibrary() {
        if (loaded)
            return;
        loaded = true;
        /*
	* Note that the jawt library is loaded explicitly
	* because it cannot be found by the library loader.
	* All exceptions are caught because the library may
	* have been loaded already.
	*/
        try {
            System.loadLibrary("jawt");
        } catch (Throwable e) {
        }
        Library.loadLibrary("swt-awt");
    }

    static synchronized void initializeSwing() {
        if (swingInitialized)
            return;
        swingInitialized = true;
        /*
	* Feature in GTK.  The default X error handler
	* for GTK calls exit() after printing the X error.
	* Normally, this isn't that big a problem for SWT
	* applications because they don't cause X errors.
	* However, sometimes X errors are generated by AWT
	* that make SWT exit.  The fix is to hide all X
	* errors when AWT is running.
	*/
        if (OS.isX11()) {
            GDK.gdk_x11_display_error_trap_push(GDK.gdk_display_get_default());
        }
        try {
            /* Initialize the default focus traversal policy */
            Class<?> clazz = Class.forName("javax.swing.UIManager");
            Method method = clazz.getMethod("getDefaults");
            if (method != null)
                method.invoke(clazz);
        } catch (Throwable e) {
        }
    }

    /**
     * Returns a <code>java.awt.Frame</code> which is the embedded frame
     * associated with the specified composite.
     *
     * @param parent the parent <code>Composite</code> of the <code>java.awt.Frame</code>
     * @return a <code>java.awt.Frame</code> the embedded frame or <code>null</code>.
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
     * </ul>
     *
     * @since 3.2
     */
    public static Frame getFrame(Composite parent) {
        if (parent == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        if ((parent.getStyle() & SWT.EMBEDDED) == 0)
            return null;
        return (Frame) parent.getData(EMBEDDED_FRAME_KEY);
    }

    /**
     * Creates a new <code>java.awt.Frame</code>. This frame is the root for
     * the AWT components that will be embedded within the composite. In order
     * for the embedding to succeed, the composite must have been created
     * with the SWT.EMBEDDED style.
     * <p>
     * IMPORTANT: As of JDK1.5, the embedded frame does not receive mouse events.
     * When a lightweight component is added as a child of the embedded frame,
     * the cursor does not change. In order to work around both these problems, it is
     * strongly recommended that a heavyweight component such as <code>java.awt.Panel</code>
     * be added to the frame as the root of all components.
     * </p>
     *
     * @param parent the parent <code>Composite</code> of the new <code>java.awt.Frame</code>
     * @return a <code>java.awt.Frame</code> to be the parent of the embedded AWT components
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the parent Composite does not have the SWT.EMBEDDED style</li>
     * </ul>
     *
     * @since 3.0
     */
    public static Frame new_Frame(final Composite parent) {
        if (parent == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        if ((parent.getStyle() & SWT.EMBEDDED) == 0) {
            SWT.error(SWT.ERROR_INVALID_ARGUMENT);
        }
        long handle = parent.getEmbeddedHandle();
        /*
	 * Some JREs have implemented the embedded frame constructor to take an integer
	 * and other JREs take a long.  To handle this binary incompatibility, use
	 * reflection to create the embedded frame.
	 */
        String className = embeddedFrameClass != null ? embeddedFrameClass : "sun.awt.X11.XEmbeddedFrame";
        try {
            if (embeddedFrameClass != null) {
                Class.forName(className);
            }
            loadLibrary();
        } catch (ClassNotFoundException cne) {
            SWT.error(SWT.ERROR_NOT_IMPLEMENTED, cne);
        } catch (Throwable e) {
            SWT.error(SWT.ERROR_UNSPECIFIED, e, " [Error while starting AWT]");
        }
        initializeSwing();
        final Frame[] frame = new Frame[1];
        Object object = initFrame(handle, className);
        if (object == null || !(object instanceof Frame)) {
            SWT.error(SWT.ERROR_UNSPECIFIED, new Throwable(), " [Error while starting AWT]");
        }
        frame[0] = (Frame) object;
        parent.setData(EMBEDDED_FRAME_KEY, frame[0]);
        if (Device.DEBUG) {
            setDebug(frame[0], true);
        }
        /* Call registerListeners() to make XEmbed focus traversal work */
        registerListeners(frame[0]);
        final AWTEventListener awtListener = event -> {
            if (event.getID() == WindowEvent.WINDOW_OPENED) {
                final Window window = (Window) event.getSource();
                if (window.getParent() == frame[0]) {
                    parent.getDisplay().asyncExec(() -> {
                        if (parent.isDisposed())
                            return;
                        Shell shell = parent.getShell();
                        long awtHandle = getAWTHandle(window);
                        if (awtHandle == 0)
                            return;
                        long xWindow;
                        if (GTK.GTK4) {
                            xWindow = GDK.gdk_x11_surface_get_xid(GTK4.gtk_native_get_surface(GTK4.gtk_widget_get_native(shell.getHandle())));
                        } else {
                            xWindow = GDK.gdk_x11_window_get_xid(GTK3.gtk_widget_get_window(GTK3.gtk_widget_get_toplevel(shell.getHandle())));
                        }
                        OS.XSetTransientForHint(GDK.gdk_x11_display_get_xdisplay(GDK.gdk_display_get_default()), awtHandle, xWindow);
                    });
                }
            }
        };
        frame[0].getToolkit().addAWTEventListener(awtListener, AWTEvent.WINDOW_EVENT_MASK);
        final Listener shellListener = e -> {
            switch(e.type) {
                case SWT.Deiconify:
                    EventQueue.invokeLater(() -> frame[0].dispatchEvent(new WindowEvent(frame[0], WindowEvent.WINDOW_DEICONIFIED)));
                    break;
                case SWT.Iconify:
                    EventQueue.invokeLater(() -> frame[0].dispatchEvent(new WindowEvent(frame[0], WindowEvent.WINDOW_ICONIFIED)));
                    break;
            }
        };
        Shell shell = parent.getShell();
        shell.addListener(SWT.Deiconify, shellListener);
        shell.addListener(SWT.Iconify, shellListener);
        Listener listener = e -> {
            switch(e.type) {
                case SWT.Dispose:
                    Shell shell1 = parent.getShell();
                    shell1.removeListener(SWT.Deiconify, shellListener);
                    shell1.removeListener(SWT.Iconify, shellListener);
                    parent.setVisible(false);
                    EventQueue.invokeLater(() -> {
                        frame[0].getToolkit().removeAWTEventListener(awtListener);
                        frame[0].dispose();
                    });
                    break;
                case SWT.Resize:
                    final Rectangle clientArea = DPIUtil.autoScaleUp(parent.getClientArea());
                    EventQueue.invokeLater(() -> frame[0].setSize(clientArea.width, clientArea.height));
                    break;
            }
        };
        parent.addListener(SWT.Dispose, listener);
        parent.addListener(SWT.Resize, listener);
        parent.getDisplay().asyncExec(() -> {
            if (parent.isDisposed())
                return;
            final Rectangle clientArea = DPIUtil.autoScaleUp(parent.getClientArea());
            EventQueue.invokeLater(() -> {
                frame[0].setSize(clientArea.width, clientArea.height);
                frame[0].validate();
            });
        });
        return frame[0];
    }

    /**
     * Creates a new <code>Shell</code>. This Shell is the root for
     * the SWT widgets that will be embedded within the AWT canvas.
     *
     * @param display the display for the new Shell
     * @param parent the parent <code>java.awt.Canvas</code> of the new Shell
     * @return a <code>Shell</code> to be the parent of the embedded SWT widgets
     *
     * @exception IllegalArgumentException <ul>
     *    <li>ERROR_NULL_ARGUMENT - if the display is null</li>
     *    <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
     *    <li>ERROR_INVALID_ARGUMENT - if the parent's peer is not created</li>
     * </ul>
     *
     * @since 3.0
     */
    public static Shell new_Shell(final Display display, final Canvas parent) {
        if (display == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        if (parent == null)
            SWT.error(SWT.ERROR_NULL_ARGUMENT);
        long handle = 0;
        try {
            loadLibrary();
            handle = getAWTHandle(parent);
        } catch (Throwable e) {
            SWT.error(SWT.ERROR_NOT_IMPLEMENTED, e);
        }
        if (handle == 0)
            SWT.error(SWT.ERROR_INVALID_ARGUMENT, null, " [peer not created]");
        final Shell shell = Shell.gtk_new(display, handle);
        final ComponentListener listener = new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
                display.syncExec(() -> {
                    if (shell.isDisposed())
                        return;
                    Dimension dim = parent.getSize();
                    shell.setSize(DPIUtil.autoScaleDown(new Point(dim.width, dim.height)));
                });
            }
        };
        parent.addComponentListener(listener);
        shell.addListener(SWT.Dispose, event -> parent.removeComponentListener(listener));
        shell.setVisible(true);
        return shell;
    }
}
