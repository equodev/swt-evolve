package dev.equo.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.Accessible;
import org.eclipse.swt.accessibility.DartAccessible;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.ControlEditor;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.Mocks;
import org.instancio.Instancio;
import org.instancio.InstancioObjectApi;
import org.instancio.Select;
import org.instancio.settings.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.function.Consumer;


@ExtendWith(Mocks.class)
public class SerializeTestBase {

    /**
     * Properties this backend changes without naming, held out of the completeness check below so
     * the rest of it can run.
     *
     * <p>{@code hasOwnBackground} is answered from {@code _background != null}, and
     * {@code _setBackground} names only {@code background}, so a control that gains or loses its
     * own background never tells the render side. It is a real gap, not a fixture artefact: nine
     * widgets witness the same one. The embedded backend cannot see it — there the property is a
     * constant {@code true} — which is why it surfaced only once this suite moved here. Delete the
     * entry with the fix, not around it.
     */
    private static final java.util.Set<String> UNNAMED_ON_PURPOSE = java.util.Set.of("hasOwnBackground");

    private final Settings settings;
    private MockedStatic<DartAccessible> mockedStatic;

    protected  SerializeTestBase() {
        settings = Settings.defaults()
                .set(Keys.ASSIGNMENT_TYPE, AssignmentType.METHOD)
                .set(Keys.ON_SET_METHOD_UNMATCHED, OnSetMethodUnmatched.INVOKE)
                .set(Keys.ARRAY_ELEMENTS_NULLABLE, false)
                .set(Keys.BOOLEAN_NULLABLE, false)
                .set(Keys.STRING_NULLABLE, false);
    }

    @BeforeAll
    static void useEquo() {
        Config.forceEquo();
    }

    @AfterAll
    static void reset() {
        Config.defaultToEclipse();
    }

    @BeforeEach
    void mocks() {
        FlutterBridge.set(new MockFlutterBridge());
        // Shell bounds and pop-up locations read these. The display mock is shared by every test
        // in the JVM, so they are undone after each test.
        Display display = DartMocks.dartDisplay();
        Mockito.when(display.getBounds()).thenReturn(new Rectangle(0, 0, 1280, 720));
        Mockito.when(display.getCursorLocation()).thenReturn(new Point(0, 0));
        Mockito.when(display.getDepth()).thenReturn(32);
        // Queued work runs only when a test runs it. Another class may have left the shared mock
        // running it inline, which lays a CTabFolder out, tooltip reset included, while it is filled.
        Mockito.doNothing().when(display).asyncExec(Mockito.any());
        // A caret blinks at the display's rate, which a mock answers 0 for: a caret that never blinks.
        Mockito.when(((DartDisplay) display.getImpl()).getCaretBlinkTime()).thenReturn(500);

        Accessible mockAcc = Mockito.mock(Accessible.class);
        mockedStatic = Mockito.mockStatic(DartAccessible.class);
        mockedStatic.when(() -> DartAccessible.internal_new_Accessible(Mockito.any())).thenReturn(mockAcc);
    }

    @AfterEach
    void resetStatic() {
        FlutterBridge.set(null);
        mockedStatic.close();
        Display display = DartMocks.dartDisplay();
        Mockito.when(display.getBounds()).thenReturn(null);
        Mockito.when(display.getCursorLocation()).thenReturn(null);
        Mockito.when(display.getDepth()).thenReturn(0);
        Mockito.when(((DartDisplay) display.getImpl()).getCaretBlinkTime()).thenReturn(0);
        try {
            focusCaret(null);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
        // The dirty set is static and JVM-wide. A mock widget marked here and left in it is drained
        // by whoever calls update() next, and the renderer tests that follow block on the future
        // that comes back — which, for a mock, never completes. One class is enough to hang the
        // rest of the suite, so the widgets die with the test that made them.
        FlutterBridge.clearDirty();
    }

    /** {@code p} as the wire describes it; see {@link dev.equo.swt.harness.WireFrames#describe}. */
    protected java.util.Map<String, Object> serialize(Object p) {
        return dev.equo.swt.harness.WireFrames.describe(p);
    }

    /**
     * Asserts that what {@code w}'s setters said changed is what actually changed.
     *
     * <p>Two independent sources, on purpose. One is the widget's own record, filled by its setters
     * calling {@code markDirty}. The other is derived by comparing the widget's serialized state
     * before and after, which knows nothing about flags. A setter that forgets to flag drops its
     * property from every future update — silently, for that one property — and only a check that
     * does not consult the flags can see it.
     *
     * <p>Driven by {@code setAll}, so this covers every property the widget can be given a value
     * for rather than the ones someone thought to name. A property with no usable setter is not
     * reachable from an application either, so there is nothing to miss.
     */
    protected void assertNamesEveryChange(Widget w) {
        Widget target = w;
        firstSend(target);
        java.util.Map<String, Object> before = dev.equo.swt.delivery.Canon.canon(serialize(target));

        setAll(target);

        // Read the flags before serializing again: writing a widget whole is what marks it as no
        // longer outstanding, so the second serialize clears the very thing being measured.
        java.util.Set<String> flagged = new java.util.LinkedHashSet<>(flagsOf(target));
        java.util.Map<String, Object> after = dev.equo.swt.delivery.Canon.canon(serialize(target));

        java.util.Set<String> changed = dev.equo.swt.delivery.DeliveryAudit.changedKeys(before, after);
        changed.removeAll(UNNAMED_ON_PURPOSE);

        // Completeness is the property that matters, and the only one asserted: a change that was
        // not named is a change that never ships, silently, for as long as the widget lives.
        org.assertj.core.api.Assertions.assertThat(flagged)
                .as("%s: these changed but nothing named them, so they would never be sent",
                        w.getClass().getSimpleName())
                .containsAll(changed);

        // Being named without having changed only costs bytes, so it is measured rather than
        // failed on - the number is what says whether the saving is real, and it belongs in the
        // record next to the payload sizes rather than in a red build.
        java.util.Set<String> spurious = new java.util.LinkedHashSet<>(flagged);
        spurious.removeAll(changed);
        if (!spurious.isEmpty()) {
            System.out.printf("over-named %-22s %d of %d: %s%n", w.getClass().getSimpleName(),
                    spurious.size(), flagged.size(), spurious);
        }
    }

    /** Puts the widget in the state the check measures from: sent whole, nothing outstanding. */
    private void firstSend(Widget w) {
        serialize(w);
    }

    private static java.util.Set<String> flagsOf(Widget w) {
        return ((DartWidget) w.getImpl()).getValue().changedNames();
    }

    protected void setAll(Widget w) {
        InstancioObjectApi<Widget> inst = Instancio.ofObject(w)
                .withSettings(settings)
                .lenient()
                .ignore(Select.setter(Widget.class, "setImpl"))
                .ignore(Select.field(Widget.class, "state"))
                .ignore(Select.field(Widget.class, "style"));
        try {
            inst
                    .ignore(Select.types().of(Class.forName("org.eclipse.swt.internal.cocoa.NSObject")));
        } catch (ClassNotFoundException e) {}
        inst = inst
                .withFillType(FillType.POPULATE_NULLS_AND_DEFAULT_PRIMITIVES)
                .generate(Select.all(int.class), gen -> gen.ints().range(1, 1000))
                .generate(Select.all(Image.class), gen -> gen.oneOf(createTestImage()));
        if (!(w instanceof CoolItem)) {
            inst = inst.generate(Select.all(boolean.class), gen -> gen.booleans().probability(1.0)); // Always true
        }
        if (!(w instanceof Caret) && !(w instanceof TreeColumn) && !(w instanceof TableColumn) && !(w instanceof ToolItem) && !(w instanceof CoolItem)) {
            inst = inst.generate(Select.all(Font.class), gen -> gen.oneOf(new Font(Mocks.device(), Mocks.fontData())));
        }
        if (!(w instanceof Caret) && !(w instanceof TreeColumn) && !(w instanceof TableColumn) && !(w instanceof CoolItem)) {
            inst = inst.generate(Select.all(Color.class), gen -> gen.oneOf(new Color(Mocks.device(), Mocks.red(), Mocks.green(), Mocks.blue())));
        }
        if (w instanceof Canvas c)
            inst.generate(Select.all(Caret.class), gen -> gen.oneOf(new Caret(c, SWT.NONE)));
        // A generated renderer is built bare, with nothing to measure or dispose tabs with.
        if (w instanceof org.eclipse.swt.custom.CTabFolder)
            inst.ignore(Select.setter(org.eclipse.swt.custom.CTabFolder.class, "setRenderer"));
        inst.fill();
    }

    protected void setAll(ExpandItem w) {
        InstancioObjectApi<ExpandItem> inst = Instancio.ofObject(w)
                .withSettings(settings)
                .lenient()
                .ignore(Select.setter(Widget.class, "setImpl"))
                .ignore(Select.field(Widget.class, "state"))
                .ignore(Select.setter(ExpandItem.class, "setControl"));
        inst = inst
                .withFillType(FillType.POPULATE_NULLS_AND_DEFAULT_PRIMITIVES)
                .generate(Select.all(boolean.class), gen -> gen.booleans().probability(1.0)) // Always true
                .generate(Select.all(int.class), gen -> gen.ints().range(1, 1000))
                .generate(Select.all(Image.class), gen -> gen.oneOf(createTestImage()));
        inst.fill();
    }

    protected void setAll(ControlEditor w) {
        w.horizontalAlignment = SWT.LEFT;
        w.grabHorizontal = true;
        w.minimumWidth = 100;
        w.verticalAlignment = SWT.TOP;
        w.grabVertical = true;
        w.minimumHeight = 30;
    }

    protected void setAll(TableEditor w) {
        setAll((ControlEditor) w);
        w.setColumn(1);
    }

    protected void setAll(TreeEditor w) {
        setAll((ControlEditor) w);
        w.setColumn(1);
    }

    protected void setAll(TabItem w) {
        InstancioObjectApi<TabItem> inst = Instancio.ofObject(w)
                .withSettings(settings)
                .lenient()
                .ignore(Select.setter(Widget.class, "setImpl"))
                .ignore(Select.field(Widget.class, "state"));
        inst = inst
                .withFillType(FillType.POPULATE_NULLS_AND_DEFAULT_PRIMITIVES)
                .generate(Select.all(Image.class), gen -> gen.oneOf(createTestImage()));
        inst.fill();
    }

    protected void setAll(Color w) {
        // no setters
    }

    protected void setAll(org.eclipse.swt.custom.StyleRange sr) {
        // StyleRange fields are typically set via constructor or direct field access
    }

    protected void setAll(org.eclipse.swt.graphics.TextStyle ts) {
        // TextStyle fields are typically set via constructor or direct field access
    }

    protected void setAll(GC w) {
        InstancioObjectApi<GC> inst = Instancio.ofObject(w)
                .withSettings(settings)
                .lenient();
        try {
            inst
                    .ignore(Select.types().of(Class.forName("org.eclipse.swt.internal.cocoa.NSObject")));
        } catch (ClassNotFoundException e) {}
        inst = inst
                .withFillType(FillType.POPULATE_NULLS_AND_DEFAULT_PRIMITIVES)
                .generate(Select.all(boolean.class), gen -> gen.booleans().probability(1.0)) // Always true
                .generate(Select.all(int.class), gen -> gen.ints().range(1, 40))
                .ignore(Select.all(int[].class))
                .ignore(Select.field(GCData.class, "lineDashes"))
                .generate(Select.all(Color.class), gen -> gen.oneOf(new Color(Mocks.device(), Mocks.red(), Mocks.green(), Mocks.blue())));
        inst.fill();
    }

    protected void setAll(CCombo w) {
        InstancioObjectApi<CCombo> inst = Instancio.ofObject(w)
                .withSettings(settings)
                .lenient()
                .ignore(Select.setter(Widget.class, "setImpl"))
                .ignore(Select.field(Widget.class, "state"))
                .ignore(Select.field(Widget.class, "style"))
                .ignore(Select.setter(Control.class, "setBounds"))
                .ignore(Select.setter(Control.class, "setLayoutData"))
                .ignore(Select.setter(Control.class, "setBackgroundImage"))
                .ignore(Select.setter(Control.class, "setVisible"))
                .ignore(Select.setter(Control.class, "setEnabled"))
                .ignore(Select.setter(Composite.class, "setLayout"))
                .ignore(Select.setter(Control.class, "setTouchEnabled"));
        inst = ignoreSetterIfDeclared(inst, CCombo.class, "setAlignment");
        try {
            inst.ignore(Select.types().of(Class.forName("org.eclipse.swt.internal.cocoa.NSObject")));
        } catch (ClassNotFoundException e) {}
        inst = inst
                .withFillType(FillType.POPULATE_NULLS_AND_DEFAULT_PRIMITIVES)
                .generate(Select.all(boolean.class), gen -> gen.booleans().probability(1.0))
                .generate(Select.all(int.class), gen -> gen.ints().range(1, 1000))
                .generate(Select.all(String.class), gen -> gen.oneOf("one", "two", "three"))
                .generate(Select.all(Color.class), gen -> gen.oneOf(new Color(Mocks.device(), Mocks.red(), Mocks.green(), Mocks.blue())))
                .generate(Select.all(Font.class), gen -> gen.oneOf(new Font(Mocks.device(), Mocks.fontData())))
                .generate(Select.all(Image.class), gen -> gen.oneOf(createTestImage()));
        inst.fill();
    }

    /**
     * Ignores {@code owner}'s setter only when the SWT being compiled against declares it.
     * {@link Select#setter(Class, String)} resolves the method eagerly and fails the whole fill
     * with a usage error when the name matches nothing.
     */
    private static <T> InstancioObjectApi<T> ignoreSetterIfDeclared(
            InstancioObjectApi<T> inst, Class<?> owner, String setter) {
        for (Method m : owner.getDeclaredMethods()) {
            if (m.getName().equals(setter)) {
                return inst.ignore(Select.setter(owner, setter));
            }
        }
        return inst;
    }

    private Image createTestImage() {
        try {
            java.io.InputStream resourceStream = getClass().getClassLoader().getResourceAsStream("collapseall.png");
            if (resourceStream != null) {
                ImageData imageData = new ImageData(resourceStream);
                Image image = new Image(Mocks.device(), imageData);
                resourceStream.close();
                return image;
            }
        } catch (Exception e) {
            // Resource not available, create default image
        }
        return new Image(Mocks.device(), 16, 16);
    }

    protected void setAll(Image i) {
        i.setBackground(new Color(Mocks.device(), Mocks.red(), Mocks.green(), Mocks.blue()));
    }

    protected void setAll(Font f) {
        // Font is immutable, no setters to populate
    }

    protected void setAll(org.eclipse.swt.graphics.Cursor c) {
        // Cursor is immutable, no setters to populate
    }

    protected void setAll(org.eclipse.swt.widgets.Menu m) {
        // Menu doesn't have Font/Color setters like other widgets
        InstancioObjectApi<org.eclipse.swt.widgets.Menu> inst = Instancio.ofObject(m)
                .withSettings(settings)
                .lenient()
                .ignore(Select.setter(org.eclipse.swt.widgets.Widget.class, "setImpl"))
                .ignore(Select.field(org.eclipse.swt.widgets.Widget.class, "state"))
                .ignore(Select.field(org.eclipse.swt.widgets.Widget.class, "style"));
        try {
            inst.ignore(Select.types().of(Class.forName("org.eclipse.swt.internal.cocoa.NSObject")));
        } catch (ClassNotFoundException e) {}
        inst = inst
                .withFillType(FillType.POPULATE_NULLS_AND_DEFAULT_PRIMITIVES)
                .generate(Select.all(boolean.class), gen -> gen.booleans().probability(1.0))
                .generate(Select.all(int.class), gen -> gen.ints().range(1, 1000));
        inst.fill();
    }

    protected void setAll(org.eclipse.swt.widgets.MenuItem mi) {
        // MenuItem has Image but not Font/Color setters
        InstancioObjectApi<org.eclipse.swt.widgets.MenuItem> inst = Instancio.ofObject(mi)
                .withSettings(settings)
                .lenient()
                .ignore(Select.setter(org.eclipse.swt.widgets.Widget.class, "setImpl"))
                .ignore(Select.field(org.eclipse.swt.widgets.Widget.class, "state"))
                .ignore(Select.field(org.eclipse.swt.widgets.Widget.class, "style"));
        try {
            inst.ignore(Select.types().of(Class.forName("org.eclipse.swt.internal.cocoa.NSObject")));
        } catch (ClassNotFoundException e) {}
        inst = inst
                .withFillType(FillType.POPULATE_NULLS_AND_DEFAULT_PRIMITIVES)
                .generate(Select.all(boolean.class), gen -> gen.booleans().probability(1.0))
                .generate(Select.all(int.class), gen -> gen.ints().range(1, 1000))
                .generate(Select.all(Image.class), gen -> gen.oneOf(createTestImage()));
        inst.fill();
        // setID needs to be set via getValue() to bypass checkWidget() in tests
        ((DartMenuItem) mi.getImpl()).getValue().setID(Instancio.gen().ints().range(1, 1000).get());
    }

    /**
     * Names chosen to carry no web substitution, unlike the embedded suite's Arial/Helvetica/Courier.
     *
     * <p>The wire carries the substituted name, so an assertion against {@code getName()} only holds
     * for a name the table leaves alone.
     */
    protected void setAll(FontData fd) {
        InstancioObjectApi<FontData> inst = Instancio.ofObject(fd)
                .withSettings(settings)
                .lenient()
                .withFillType(FillType.POPULATE_NULLS_AND_DEFAULT_PRIMITIVES)
                .generate(Select.all(int.class), gen -> gen.ints().range(1, 20))
                .generate(Select.all(String.class), gen -> gen.oneOf("Roboto", "Inter", "Lato"));
        inst.fill();
    }

    protected void setAll(Accessible a) {
        InstancioObjectApi<Accessible> inst = Instancio.ofObject(a)
                .withSettings(settings)
                .lenient()
                .ignore(Select.setter(Accessible.class, "setImpl"))
                .withFillType(FillType.POPULATE_NULLS_AND_DEFAULT_PRIMITIVES)
                .generate(Select.all(int.class), gen -> gen.ints().range(1, 1000));
        inst.fill();
    }

    protected void setAll(org.eclipse.swt.widgets.ToolTip tt) {
        InstancioObjectApi<org.eclipse.swt.widgets.ToolTip> inst = Instancio.ofObject(tt)
                .withSettings(settings)
                .lenient()
                .ignore(Select.setter(org.eclipse.swt.widgets.Widget.class, "setImpl"))
                .ignore(Select.field(org.eclipse.swt.widgets.Widget.class, "state"))
                .ignore(Select.field(org.eclipse.swt.widgets.Widget.class, "style"));
        try {
            inst.ignore(Select.types().of(Class.forName("org.eclipse.swt.internal.cocoa.NSObject")));
        } catch (ClassNotFoundException e) {}
        inst = inst
                .withFillType(FillType.POPULATE_NULLS_AND_DEFAULT_PRIMITIVES)
                .generate(Select.all(boolean.class), gen -> gen.booleans().probability(1.0))
                .generate(Select.all(int.class), gen -> gen.ints().range(1, 1000));
        inst.fill();
    }

    /** What {@code setAll} cannot reach through setters; see {@link DartMocks#fillUnsettable}. */
    protected void fillUnsettable(Object w) throws ReflectiveOperationException {
        if (w instanceof Control c)
            DartMocks.fillUnsettable(c);
        if (w instanceof Widget || w instanceof ControlEditor)
            DartMocks.fillReferences(w);
        fillThroughApi(w);
    }

    /**
     * The properties only a call with more than one argument, an event, a layout or another object
     * sets: each class gets the calls an application or the render side would make.
     */
    private void fillThroughApi(Object w) throws ReflectiveOperationException {
        Color red = new Color(Mocks.device(), 200, 0, 0);
        Color blue = new Color(Mocks.device(), 0, 0, 200);
        if (w instanceof org.eclipse.swt.browser.Browser b) {
            new org.eclipse.swt.browser.BrowserFunction(b, "f");
        } else if (w instanceof org.eclipse.swt.custom.CLabel l) {
            // A gradient replaces the background image: one style holds each.
            if ((l.getStyle() & SWT.CENTER) != 0)
                l.setBackground(new Color[] {red, blue}, new int[] {50}, true);
        } else if (w instanceof org.eclipse.swt.custom.CTabFolder f) {
            // Minimized and maximized exclude each other: one style holds each.
            if ((f.getStyle() & SWT.CLOSE) != 0)
                f.setMaximized(true);
            org.eclipse.swt.custom.DartCTabFolder folder = (org.eclipse.swt.custom.DartCTabFolder) f.getImpl();
            try (Mocks.Delivery ignored = Mocks.deliverEvents(f.getDisplay())) {
                f.notifyListeners(SWT.Activate, new Event());
            }
            org.eclipse.swt.custom.CTabFolderHelper.handleShowList(folder, new Event());
            String tip = f.getToolTipText();
            layOutTabs(f);
            // The layout answers the tool tip of the tab under the cursor, which is none.
            f.setToolTipText(tip);
        } else if (w instanceof org.eclipse.swt.custom.CTabItem i) {
            i.getParent().setSize(400, 200);
            layOutTabs(i.getParent());
        } else if (w instanceof Label l) {
            // A label shows whichever of text and image was set last: one style shows the image.
            if ((l.getStyle() & SWT.CENTER) != 0)
                l.setImage(createTestImage());
        } else if (w instanceof MenuItem i) {
            if ((i.getStyle() & SWT.CASCADE) != 0)
                i.setMenu(new Menu(i.getParent()));
        } else if (w instanceof TableItem i) {
            drawOwnText(i.getParent(), i);
        } else if (w instanceof TreeItem i) {
            drawOwnText(i.getParent(), i);
            new TreeItem(i, SWT.NONE);
            i.setExpanded(true);
        } else if (w instanceof Caret c) {
            focusCaret(c);
        } else if (w instanceof Text t) {
            t.setSelection(1, 1);
        } else if (w instanceof Spinner s) {
            // A Verify listener that rewrites the text keeps its own in the field over the selection.
            s.addListener(SWT.Verify, e -> e.text = "kept");
            try (Mocks.Delivery ignored = Mocks.deliverEvents(s.getDisplay())) {
                s.setSelection(s.getSelection() == s.getMinimum() ? s.getMaximum() : s.getMinimum());
            }
        } else if (w instanceof org.eclipse.swt.custom.CCombo c) {
            // The client draws the list, so Java hears it is open from the client.
            org.eclipse.swt.custom.CComboHelper.listShownByFlutter((org.eclipse.swt.custom.DartCCombo) c.getImpl(), true);
        } else if (w instanceof org.eclipse.swt.custom.ScrolledComposite sc) {
            // An origin is only requested over content.
            sc.setContent(new Composite(sc, SWT.NONE));
            sc.setOrigin(1, 1);
        } else if (w instanceof Table t) {
            new TableColumn(t, SWT.NONE);
            new TableColumn(t, SWT.NONE);
            t.setColumnOrder(reversedOrder(t.getColumnCount()));
            measureTallRow(t, new TableItem(t, SWT.NONE));
        } else if (w instanceof Tree t) {
            new TreeColumn(t, SWT.NONE);
            new TreeColumn(t, SWT.NONE);
            t.setColumnOrder(reversedOrder(t.getColumnCount()));
            // The fill sets the direction to a number SWT refuses.
            t.setSortDirection(SWT.UP);
            measureTallRow(t, new TreeItem(t, SWT.NONE));
        } else if (w instanceof GC gc) {
            fill(gc);
        } else if (w instanceof Image i && i.getImpl() instanceof DartImage image && image._svgContent() != null) {
            // Set by the GC drawer once Flutter holds the pixels, which leaves no imageData to send:
            // the SVG image takes it, as Flutter draws its pixels anyway.
            image._setRemoteRef(1L);
        }
        fillLists(w);
    }

    /** The columns shown last to first, an order other than the one they were created in. */
    private static int[] reversedOrder(int columns) {
        int[] order = new int[columns];
        for (int i = 0; i < columns; i++)
            order[i] = columns - 1 - i;
        return order;
    }

    /**
     * An element in each list the value carries: a whole record leaves an empty list out, as it
     * does any property at its default, so an empty one is a write path no test takes.
     */
    private void fillLists(Object w) {
        if (w instanceof org.eclipse.swt.custom.SashForm f) {
            new Composite(f, SWT.NONE);
            new Composite(f, SWT.NONE);
            int[] weights = new int[f.getWeights().length];
            for (int i = 0; i < weights.length; i++)
                weights[i] = i + 1;
            f.setWeights(weights);
        } else if (w instanceof Table t) {
            org.eclipse.swt.custom.TableEditor editor = new org.eclipse.swt.custom.TableEditor(t);
            editor.setEditor(new Text(t, SWT.NONE), new TableItem(t, SWT.NONE), 0);
        } else if (w instanceof Tree t) {
            org.eclipse.swt.custom.TreeEditor editor = new org.eclipse.swt.custom.TreeEditor(t);
            editor.setEditor(new Text(t, SWT.NONE), new TreeItem(t, SWT.NONE), 0);
        } else if (w instanceof CoolBar b) {
            new CoolItem(b, SWT.NONE);
            new CoolItem(b, SWT.NONE);
            b.setItemLayout(new int[] {1, 0}, new int[] {1}, new Point[] {new Point(10, 10), new Point(20, 20)});
        } else if (w instanceof ToolBar b) {
            new ToolItem(b, SWT.PUSH);
        } else if (w instanceof ExpandBar b) {
            new ExpandItem(b, SWT.NONE);
        } else if (w instanceof Menu m) {
            new MenuItem(m, SWT.PUSH);
        } else if (w instanceof List l) {
            l.setItems(new String[] {"a", "b", "c", "d", "e", "f", "g", "h"});
            l.select(1);
            // Scrolled past the first item: more items than the list shows.
            l.setSize(100, 40);
            l.setTopIndex(3);
        } else if (w instanceof CCombo c) {
            c.setData("org.eclipse.swt.custom.CCombo.itemToolTips", new String[] {"tip"});
        }
        if (w instanceof Composite c && c.getChildren().length == 0)
            new Composite(c, SWT.NONE);
    }

    private void fill(GC gc) {
        Device device = gc.getDevice();
        gc.setLineAttributes(new LineAttributes(2, SWT.CAP_ROUND, SWT.JOIN_ROUND, SWT.LINE_CUSTOM,
                new float[] {2, 3}, 1, 10));
        gc.setFillRule(SWT.FILL_WINDING);
        gc.setTransform(new Transform(device, 1, 2, 3, 4, 5, 6));
        gc.setForegroundPattern(new Pattern(device, 0, 0, 10, 10, new Color(device, 200, 0, 0), new Color(device, 0, 0, 200)));
        gc.setBackgroundPattern(new Pattern(device, 0, 0, 10, 10, new Color(device, 0, 200, 0), new Color(device, 0, 0, 200)));
        // A clip is rectangles, a path or text: one style holds each.
        Path clip = new Path(device);
        if ((gc.getStyle() & SWT.RIGHT_TO_LEFT) != 0) {
            clip.addString("a", 0, 0, new Font(device, Mocks.fontData()));
            gc.setClipping(clip);
        } else if ((gc.getStyle() & SWT.LEFT_TO_RIGHT) != 0) {
            clip.addRectangle(1, 1, 10, 10);
            gc.setClipping(clip);
        }
        // The theme's colors for what the GC draws on, configured by the drawable's class.
        Class<?> drawable = Canvas.class;
        ConfigFlags flags = new ConfigFlags();
        flags.disable_swt_canvas_colors_by_class = java.util.Map.of(drawable.getName(), true);
        flags.canvas_hover_fills_by_class = java.util.Map.of(drawable.getName(), new RGB[] {new RGB(1, 2, 3)});
        flags.canvas_selection_fills_by_class = java.util.Map.of(drawable.getName(), new RGB[] {new RGB(4, 5, 6)});
        // A test can fill several GCs: what is restored is what came before the first.
        if (savedFlags == null)
            savedFlags = Config.getConfigFlags();
        Config.setConfigFlags(flags);
    }

    private ConfigFlags savedFlags;

    @AfterEach
    void restoreFlags() {
        if (savedFlags != null)
            Config.setConfigFlags(savedFlags);
        savedFlags = null;
    }

    /** Makes {@code caret} its display's focus caret, the one the client draws; null makes none. */
    private static void focusCaret(Caret caret) throws ReflectiveOperationException {
        Field current = DartDisplay.class.getDeclaredField("currentCaret");
        current.setAccessible(true);
        current.set(DartMocks.dartDisplay().getImpl(), caret);
    }

    /**
     * Has {@code parent} measure its rows taller than a line of its font, as an owner-drawing table
     * does in SWT.MeasureItem, and draws {@code row} once so the height is measured.
     */
    private void measureTallRow(Composite parent, Item row) throws ReflectiveOperationException {
        parent.addListener(SWT.MeasureItem, e -> e.height = 60);
        drawOwnText(parent, row);
        try (Mocks.Delivery ignored = Mocks.deliverEvents(parent.getDisplay())) {
            serialize(row);
        }
    }

    /**
     * Has {@code parent}'s listeners draw {@code row}'s text in place of the item's own, into the
     * overlay Flutter mounts for the row, which the mock bridge never reports.
     */
    private static void drawOwnText(Composite parent, Item row) throws ReflectiveOperationException {
        parent.addListener(SWT.EraseItem, e -> e.detail &= ~SWT.FOREGROUND);
        parent.addListener(SWT.PaintItem, e -> e.gc.drawText("painted", e.x, e.y, true));
        Field overlay = row.getImpl().getClass().getDeclaredField("ownerDrawOverlay");
        overlay.setAccessible(true);
        overlay.setBoolean(row.getImpl(), true);
    }

    /** Lays out the tab strip as the update a CTabFolder queues on the display, which the mock display never runs. */
    private static void layOutTabs(org.eclipse.swt.custom.CTabFolder f) throws ReflectiveOperationException {
        Class<?> folder = org.eclipse.swt.custom.DartCTabFolder.class;
        Method tabHeight = folder.getDeclaredMethod("updateTabHeight", boolean.class);
        Method items = folder.getDeclaredMethod("updateItems", int.class);
        tabHeight.setAccessible(true);
        items.setAccessible(true);
        tabHeight.invoke(f.getImpl(), false);
        items.invoke(f.getImpl(), f.getSelectionIndex());
    }

    /** The file path of a test resource, for the constructors that take one. */
    protected String resourcePath(String name) throws java.net.URISyntaxException {
        return java.nio.file.Paths.get(getClass().getClassLoader().getResource(name).toURI()).toString();
    }

    /** A value built in one style and filled, as the object the wire describes; see {@link #assertWritesEveryProperty}. */
    @FunctionalInterface
    protected interface Filled {
        Object in(int style) throws Exception;
    }

    /** A value built and filled; see {@link #assertWritesEveryProperty(java.util.Map, Built...)}. */
    @FunctionalInterface
    protected interface Built {
        Object get() throws Exception;
    }

    /** Property names paired with the reason no test can give them a non-default value. */
    protected static java.util.Map<String, String> unwritable(String... nameThenReason) {
        java.util.Map<String, String> reasons = new java.util.TreeMap<>();
        for (int i = 0; i < nameThenReason.length; i += 2)
            reasons.put(nameThenReason[i], nameThenReason[i + 1]);
        return reasons;
    }

    /**
     * Asserts that across {@code styles} the filled values leave none of their wire properties at
     * its default, apart from {@code unwritable}, which must stay unwritten. A whole record omits a
     * property at its default, so one that stays there is a write path no test takes.
     */
    protected void assertWritesEveryProperty(Filled filled, java.util.Map<String, String> unwritable, int... styles)
            throws Exception {
        Built[] built = new Built[styles.length];
        for (int i = 0; i < styles.length; i++) {
            int style = styles[i];
            built[i] = () -> filled.in(style);
        }
        assertWritesEveryProperty(unwritable, built);
    }

    /** As {@link #assertWritesEveryProperty(Filled, java.util.Map, int...)}, across the values {@code built}. */
    @SuppressWarnings("unchecked")
    protected void assertWritesEveryProperty(java.util.Map<String, String> unwritable, Built... built)
            throws Exception {
        java.util.Set<String> known = new java.util.TreeSet<>();
        java.util.Set<String> written = new java.util.TreeSet<>();
        String name = null;
        for (Built b : built) {
            Object value = b.get();
            name = value.getClass().getSimpleName();
            known.addAll(dev.equo.swt.harness.WireFrames.propertiesOf(value));
            // Writing an owner-drawn row runs its listeners, as the real display delivers them.
            try (Mocks.Delivery ignored = Mocks.deliverEvents(DartMocks.dartDisplay())) {
                written.addAll(serialize(value).keySet());
            }
        }
        written.retainAll(known);
        java.util.Set<String> expected = new java.util.TreeSet<>(known);
        expected.removeAll(unwritable.keySet());
        org.assertj.core.api.Assertions.assertThat(known)
                .as("%s: listed as unwritable but not a property", name)
                .containsAll(unwritable.keySet());
        if (!unwritable.isEmpty())
            org.assertj.core.api.Assertions.assertThat(written)
                    .as("%s: listed as unwritable but written, take them off the list", name)
                    .doesNotContainAnyElementsOf(unwritable.keySet());
        org.assertj.core.api.Assertions.assertThat(written)
                .as("%s: the fill leaves these at their default in every style", name)
                .containsAll(expected);
    }

    protected AssertConsumer node(String field) {
        return new AssertConsumer(field);
    }

    /** char[] crosses the bridge as its code units, which a record reads back as a list of numbers. */
    protected static int[] codeUnits(char[] chars) {
        if (chars == null) return null;
        int[] units = new int[chars.length];
        for (int i = 0; i < chars.length; i++)
            units[i] = chars[i];
        return units;
    }

    /**
     * Checks on one property of a record as the wire carried it (see
     * {@link dev.equo.swt.harness.WireValues}), for {@code assertThatWire(...).satisfies(...)}.
     */
    public static class AssertConsumer {
        private final String field;

        public AssertConsumer(String field) {
            this.field = field;
        }

        private Object value(Object record) {
            return dev.equo.swt.harness.WireValues.at(record, field);
        }

        private boolean present(Object record) {
            return value(record) != dev.equo.swt.harness.WireValues.MISSING;
        }

        private dev.equo.swt.harness.WireAssert at(Object record) {
            return dev.equo.swt.harness.WireAssert.assertThatWire(record).node(field);
        }

        /**
         * {@code value}. A value the client starts from - its default {@code def}, or nothing (zero,
         * false, empty) - may also travel as nothing: left out, or null.
         */
        public Consumer<Object> equalsTo(Object value, Object def) {
            return n -> {
                boolean startsFrom = java.util.Objects.equals(value, def) || nothing(value);
                if (startsFrom && (!present(n) || value(n) == null)) return;
                at(n).isEqualTo(value);
            };
        }

        private static boolean nothing(Object value) {
            if (value == null || Boolean.FALSE.equals(value)) return true;
            if (value instanceof Number number) return number.doubleValue() == 0;
            if (value instanceof CharSequence text) return text.length() == 0;
            if (value.getClass().isArray()) return java.lang.reflect.Array.getLength(value) == 0;
            if (value instanceof java.util.Collection<?> list) return list.isEmpty();
            return false;
        }

        /** An array of {@code value}'s length; an empty one may also travel as nothing. */
        public Consumer<Object> sameSizeAs(Object value) {
            return n -> {
                int length = value == null ? 0 : java.lang.reflect.Array.getLength(value);
                if (length == 0 && (!present(n) || value(n) == null)) return;
                at(n).hasSize(length);
            };
        }

        public Consumer<Object> equalsTo(Item value, Object def) {
            return n -> {
                if (value == def) {
                    if (present(n)) at(n).isNull();
                    return;
                }
                // An item travels as a reference to the item, which the client resolves by id.
                at(n).containsEntry("id", value.hashCode());
            };
        }

        /** A colour travels as its ARGB word. */
        public Consumer<Object> equalsTo(Color value, Object def) {
            return n -> {
                if (value == def) {
                    if (present(n)) at(n).isNull();
                    return;
                }
                at(n).isEqualTo(String.format("#%02x%02x%02x%02x",
                        value.getAlpha(), value.getRed(), value.getGreen(), value.getBlue()));
            };
        }

        /** A caret travels by id, described by a record of its own. */
        public Consumer<Object> equalsTo(Caret value, Object def) {
            return n -> {
                if (value == def) {
                    if (present(n)) at(n).isNull();
                    return;
                }
                at(n).containsEntry("id", value.hashCode()).containsEntry("swt", "Caret");
            };
        }

        /** An image travels by id, described by a record of its own. */
        public Consumer<Object> equalsTo(Image value, Object def) {
            return n -> {
                if (value == def) {
                    if (present(n)) at(n).isNull();
                    return;
                }
                at(n).containsEntry("id", value.hashCode());
            };
        }

        public Consumer<Object> equalsTo(ImageData value, Object def) {
            return n -> {
                if (value == def) {
                    if (present(n)) at(n).isNull();
                    return;
                }
                at(n).containsEntry("height", value.height)
                        .containsEntry("width", value.width)
                        .containsEntry("depth", value.depth);
            };
        }

        public Consumer<Object> equalsTo(Control[] value, Object def) {
            return n -> {
                if (value == def || value.length == 0) {
                    if (present(n)) at(n).hasSize(0);
                    return;
                }
                at(n).hasSize(value.length);
            };
        }

        public Consumer<Object> equalsTo(Font value, Object def) {
            return n -> {
                if (value == def) {
                    if (present(n)) at(n).isNull();
                    return;
                }
                FontData[] fontData = value.getFontData();
                if (fontData != null && fontData.length > 0) {
                    FontData fd = fontData[0];
                    dev.equo.swt.harness.WireAssert.assertThatWire(n).node(field + ".fontData[0]")
                            // The wire carries the name the client can actually render, which on
                            // the web backend is a substitution ("System" travels as "System
                            // Web"); the getter answers what SWT was given. The table is filled
                            // by WebFlutterServer's static init, so whether it applies depends
                            // on whether a renderer test has run first in this JVM — asking for
                            // the substituted name is right either way.
                            .containsEntry("name", FontMetricsUtil.substituteFontName(fd.getName()))
                            .containsEntry("height", fd.getHeight())
                            .containsEntry("style", fd.getStyle())
                            .doesNotContainKey("locale");
                } else {
                    dev.equo.swt.harness.WireAssert.assertThatWire(n).node(field + ".fontData").hasSize(0);
                }
            };
        }

    }

    static final protected Object orAbsentIfNull = null;
    static final protected int orAbsentIf0 = 0;
    static final protected boolean orAbsentIfFalse = false;

}
