package org.eclipse.swt.widgets;

import com.equo.comm.api.ICommService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.values.ButtonValue;
import org.eclipse.swt.values.WidgetValue;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class FlutterButton extends FlutterControl implements IButton {

	public static FlutterClient CLIENT = null;
	private static CompletableFuture<Boolean> clientReady = new CompletableFuture<>();
	static {
		System.load(
				"/home/elias/Documents/Equo/swt-flutter/flutter-lib/build/linux/x64/release/runner/libflutter_library.so");
		CLIENT = new FlutterClient();
		CLIENT.createComm();
		CLIENT.getComm().on("ClientReady", p -> {
			if (!clientReady.isDone()) {
				System.out.println("ClientReady");
				clientReady.complete(true);
			}
		});
	}

	public static native long InitializeFlutterWindow(long hwnd, int port, long widgetId);

	public static native void CloseFlutterWindow();

	public static String getEvent(FlutterWidget w, String... events) {
		String ev = w.getClass().getSimpleName().substring("Flutter".length()) + "/" + w.hashCode();
		if (events.length > 0)
			ev += "/" + String.join("/", events);
		return ev;
	}

	static WidgetValue build(FlutterWidget widget) {
		WidgetValue.Builder builder = widget.builder();
//		checkAndBuildMenu(widget);

//		if (widget != null) {
//			List<WidgetValue> childrenValues = widget.children.stream().map(this::build).collect(Collectors.toList());
//			builder.setChildren(childrenValues);
//		}
		return builder.build();
	}

	private static Set<FlutterWidget> DIRTY = new HashSet<>();
	private static Serializer SERIALIZER = new Serializer();

	public static void handleDirty() {
		ICommService comm = CLIENT.getComm();
		for (FlutterWidget widget : DIRTY) {
			clientReady.thenRun(() -> {
				String event = getEvent(widget);
				System.out.println("will send: " + event);
				try {
					WidgetValue value = build(widget);
					String payload = SERIALIZER.to(value);
					// System.out.println("send: " + event + ": " + payload);
					comm.send(event, payload);
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		}
		DIRTY.clear();
	}

	public static void dirty(FlutterWidget control) {
		if (control == null)
			return;
		synchronized (DIRTY) {
			DIRTY.add(control);
		}
	}

	protected WidgetValue.Builder builder;

	@Override
	void createHandle(int index) {
		state |= HANDLE;
		parentComposite = new SWTComposite((SWTComposite)parent, SWT.NONE);
		handle = parentComposite.handle;
		InitializeFlutterWindow(handle, CLIENT.getPort(), this.hashCode());
		dirty(this);
	}

	public FlutterButton(IComposite parent, int style) {
		super(parent, style);
	}

	@Override
	public void addSelectionListener(SelectionListener listener) {
		checkWidget();
		if (listener == null)
			error(SWT.ERROR_NULL_ARGUMENT);
		TypedListener typedListener = new TypedListener(listener);
		addListener(SWT.Selection, typedListener);
		addListener(SWT.DefaultSelection, typedListener);
	}

	@Override
	public int getAlignment() {
		if ((style & SWT.ARROW) != 0) {
			if ((style & SWT.UP) != 0)
				return SWT.UP;
			if ((style & SWT.DOWN) != 0)
				return SWT.DOWN;
			if ((style & SWT.LEFT) != 0)
				return SWT.LEFT;
			if ((style & SWT.RIGHT) != 0)
				return SWT.RIGHT;
			return SWT.UP;
		}
		if ((style & SWT.LEFT) != 0)
			return SWT.LEFT;
		if ((style & SWT.CENTER) != 0)
			return SWT.CENTER;
		if ((style & SWT.RIGHT) != 0)
			return SWT.RIGHT;
		return SWT.LEFT;
	}

	@Override
	public boolean getGrayed() {
		return builder().getGrayed().orElse(false);
	}

	@Override
	public Image getImage() {
		return null;
	}

	@Override
	public boolean getSelection() {
		if ((style & (SWT.CHECK | SWT.RADIO | SWT.TOGGLE)) == 0)
			return false;
		return builder().getSelection().orElse(false);
	}

	@Override
	public String getText() {
		return builder().getText().orElse(null);
	}

	@Override
	public void removeSelectionListener(SelectionListener listener) {
		if (listener == null)
			error(SWT.ERROR_NULL_ARGUMENT);
		if (eventTable == null)
			return;
		eventTable.unhook(SWT.Selection, listener);
		eventTable.unhook(SWT.DefaultSelection, listener);
	}

	@Override
	public void setAlignment(int alignment) {
		if ((style & SWT.ARROW) != 0) {
			if ((style & (SWT.UP | SWT.DOWN | SWT.LEFT | SWT.RIGHT)) == 0)
				return;
			style &= ~(SWT.UP | SWT.DOWN | SWT.LEFT | SWT.RIGHT);
			style |= alignment & (SWT.UP | SWT.DOWN | SWT.LEFT | SWT.RIGHT);
		}
		if ((alignment & (SWT.LEFT | SWT.RIGHT | SWT.CENTER)) == 0)
			return;
		style &= ~(SWT.LEFT | SWT.RIGHT | SWT.CENTER);
		style |= alignment & (SWT.LEFT | SWT.RIGHT | SWT.CENTER);
	}

	@Override
	public void setGrayed(boolean grayed) {
		builder().setGrayed(grayed);
		dirty(this);
	}

	@Override
	public void setImage(Image image) {
	}

	@Override
	public void setSelection(boolean selected) {
		if ((style & (SWT.CHECK | SWT.RADIO | SWT.TOGGLE)) == 0)
			return;
		builder().setSelection(selected);
		dirty(this);
	}

	@Override
	public void setText(String string) {
		if (string == null)
			error(SWT.ERROR_NULL_ARGUMENT);
		if ((style & SWT.ARROW) != 0)
			return;
		builder().setText(string);
		dirty(this);
	}

	public ButtonValue.Builder builder() {
		if (builder == null)
			builder = ButtonValue.builder().setId(hashCode()).setStyle(style);
		return (ButtonValue.Builder) builder;
	}

	@Override
	protected void hookEvents() {
		super.hookEvents();
		String ev = getEvent(this);
		CLIENT.getComm().on(ev + "/Selection/Selection", p -> {
			System.out.println(ev + "/Selection/Selection event");
			builder().setSelection(!getSelection());
			display.asyncExec(() -> {
				if ((style & SWT.RADIO) != 0) {
					if ((parent.getStyle() & SWT.NO_RADIO_GROUP) == 0) {
						// selectRadio()
						for (IControl child : parent.getChildren()) {
							if (child instanceof FlutterButton button) {
								if (this != button) {
									button.setRadioSelection(false);
								}
							} else if (child instanceof SWTControl control) {
								control.setRadioSelection(false);
							}
						}
					}
				}
				sendEvent(SWT.Selection);
			});
		});
		CLIENT.getComm().on(ev + "/Selection/DefaultSelection", p -> {
			System.out.println(ev + "/Selection/DefaultSelection event");
			display.asyncExec(() -> {
				sendEvent(SWT.DefaultSelection);
			});
		});
	}

}
