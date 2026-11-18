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

    private final Settings settings;
    private MockedStatic<DartAccessible> mockedStatic;

    /**
     * The seed the next fill draws its values from, while an assertion needs the same values on every
     * run; each fill takes the next, so a widget and its twin are filled differently.
     */
    private Long fillSeed;

    private Settings settings() {
        return fillSeed == null ? settings : Settings.from(settings).set(Keys.SEED, fillSeed++);
    }

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

    /**
     * Marks a setter makes although the value it names reads as before, by "Widget.setter property"
     * ("*" for every widget; "changed setter" for the change call alone), each with why it stays.
     */
    private static final String SAME_CONTENT = "a new image or region with the content of the one held travels "
            + "under the same name, so the client holds it already; telling the two apart would compare pixels";

    private static final String TWIN_IMAGE = "an image with the pixels of another in the same record travels "
            + "under that one's name, so a whole description names it after whichever of the two was set "
            + "last; the client holds the same pixels under either";

    private static final String OFFSCREEN_AND_BACK = "upstream moves the control it held off screen and lays out "
            + "the one it is given, which is the same control; each step is a change, only the two together are not";

    private static final String IN_PLACE = "a table's or tree's rows are described inside its list "
            + "rather than named: the list reads as it did while a row in it changed";

    private static final java.util.Map<String, String> MARKED_ON_PURPOSE = unwritable(
            "*.changed setRegion(Region) region", SAME_CONTENT,
            "TreeItem.changed setExpanded(boolean) Tree.items", IN_PLACE,
            "TreeItem.changed setItemCount(int) Tree.items", IN_PLACE,
            "Tree.changed setItemCount(int) items", IN_PLACE,
            "Table.changed setItemCount(int) items", IN_PLACE,
            "*.changed setImage(Image) image", SAME_CONTENT,
            "*.changed setFont(Font) font", "another Font with the data of the one held travels as the same "
                    + "value; Font compares by the object, and telling the two apart would compare font data",
            "ToolItem.changed setDisabledImage(Image) disabledImage", SAME_CONTENT,
            "ToolItem.changed setHotImage(Image) hotImage", SAME_CONTENT,
            "CTabItem.setControl(Control) Button.visible", "upstream hides the control it is given and shows it "
                    + "again when it is the same; each step is a change, only the two together are not",
            "ScrolledComposite.setContent(Control) Button.bounds", "upstream moves the content to the origin and "
                    + "lays it out again; each step is a change, only the two together are not",
            "CBanner.setLeft(Control) Button.bounds", OFFSCREEN_AND_BACK,
            "CBanner.setRight(Control) Button.bounds", OFFSCREEN_AND_BACK,
            "CBanner.setBottom(Control) Button.bounds", OFFSCREEN_AND_BACK,
            "ViewForm.setContent(Control) Button.bounds", OFFSCREEN_AND_BACK,
            "ViewForm.setTopLeft(Control) Button.bounds", OFFSCREEN_AND_BACK,
            "ViewForm.setTopCenter(Control) Button.bounds", OFFSCREEN_AND_BACK,
            "ViewForm.setTopRight(Control) Button.bounds", OFFSCREEN_AND_BACK,
            "CoolItem.setSize(int,int) Button.bounds", "the item lays its control out at a size the bar then "
                    + "corrects; each step is a change, only the two together are not",
            "CoolItem.setSize(Point) Button.bounds", "as setSize(int,int), which it calls",
            "CTabFolder.changed setBackground(Color[],int[]) Button.background", "upstream clears a top-right "
                    + "control's background for a gradient image Evolve does not draw, and gives it back as the "
                    + "folder lays its controls out; each step is a change, only the two together are not");

    /**
     * Setters that change a property of their own widget on the wire without marking it, keyed as in
     * MARKED_ON_PURPOSE, each with the route the change reaches the client by instead.
     */
    private static final java.util.Map<String, String> UNMARKED_ON_PURPOSE = unwritable(
            "Button.changed setBackgroundImage(Image) image", TWIN_IMAGE,
            "CTabFolder.changed setBackgroundImage(Image) selectionBgImage", TWIN_IMAGE,
            "ToolItem.changed setImage(Image) disabledImage", TWIN_IMAGE,
            "ToolItem.changed setImage(Image) hotImage", TWIN_IMAGE);

    /**
     * Wire properties no setter of the widget changes, by "Widget.property" ("*" for every widget),
     * each with what changes them instead. A property the wire compares is left out: its marks do
     * not count here.
     */
    private static final java.util.Map<String, String> SET_ELSEWHERE = unwritable(
            "*.dragSource", "a DragSource created on the control sets it",
            "*.dragSourceId", "a DragSource created on the control sets it",
            "*.dropTargetId", "a DropTarget created on the control sets it",
            "*.hasOwnBackground", "setBackground changes it; a widget whose client never reads it writes nothing for it",
            "*.inheritsBackground", "follows the parent's background mode, which the parent's setter changes",
            "*.tabStop", "computed from the widget's kind and its parent's tab list",
            "*.children", "changes as children are created, disposed or moved",
            "*.items", "changes as items are created or disposed",
            "*.columns", "changes as columns are created or disposed",
            "*.horizontalBar", "a scroll bar exists by the H_SCROLL style the widget is created with",
            "*.verticalBar", "a scroll bar exists by the V_SCROLL style the widget is created with",
            "*.style", "set by the constructor; no setter of this widget rewrites its style bits",
            "*.font", "the client never reads the font off this widget, so nothing is written for it",
            "Table.editors", "a TableEditor created on the table sets it",
            "Tree.editors", "a TreeEditor created on the tree sets it",
            "Table.columnOrder", "follows the columns, which change as they are created or disposed",
            "Tree.columnOrder", "follows the columns, which change as they are created or disposed",
            "Table.ownerDrawItemHeight", "a MeasureItem listener sets it",
            "Tree.ownerDrawItemHeight", "a MeasureItem listener sets it",
            "Table.compactRows", "follows the process-wide native item height switch, which no setter of the table changes",
            "Tree.compactRows", "follows the process-wide native item height switch, which no setter of the tree changes",
            "Caret.focused", "follows the focus of the control the caret belongs to",
            "CTabItem.showing", "follows the folder's layout of its tabs",
            "CTabFolder.showListPopupSeq", "counts the chevron's list pop-ups, which a click opens",
            "CTabFolder.stripLayoutSeq", "counts the layouts of the tab strip",
            "ExpandItem.headerHeight", "follows the bar's font and the item's image",
            "Combo.caretPosition", "only the client's caret reports move it",
            "Spinner.keptText", "follows a Verify listener that refuses or rewrites the text",
            "Browser.functionNames", "a BrowserFunction created on the browser sets it",
            "Shell.dialogs", "a dialog opened on the shell sets it",
            "Shell.menuStripHeight", "the measured height of a menu strip, the same for every shell",
            "Shell.ownMenuStrip", "follows the items of the shell's menu bar, which their creation changes "
                    + "(Sizes.menuStripChanged)",
            "CTabFolder.chevronVisible", "only the package-private setChevronVisible changes it, which nothing calls",
            "CTabFolder.showChevron", "follows the layout of the tabs, which marks it when it flips (layoutItems)",
            "CTabFolder.highlight", "activation sends it on a channel of its own (CTabFolderHelper.redrawActivation)",
            "CTabFolder.tabHeight", "the client is sent the strip the folder measures in its queued update, which "
                    + "a test's display does not run",
            "Button.primary", "the shell's setDefaultButton sets it",
            "Caret.blinkRate", "the display's caret blink time sets it",
            "CCombo.itemTooltips", "setData(\"org.eclipse.swt.custom.CCombo.itemToolTips\", String[]) sets it",
            "CCombo.listVisible", "the list opens only while the CCombo is visible, which a test's shell is not",
            "Combo.listVisible", "the list opens only while the Combo is visible, which a test's shell is not",
            "SashForm.weights", "it takes one weight per child, and the SashForm swept has none",
            "CoolBar.itemOrder", "setItemLayout takes one entry per item, and the CoolBar swept has none",
            "CoolBar.itemSizes", "setItemLayout takes one entry per item, and the CoolBar swept has none",
            "CoolBar.wrapIndices", "it indexes the items, and the CoolBar swept has none",
            "TableColumn.alignment", "the column swept is its table's first, which is always left aligned, as "
                    + "natively; MarkSetBackTest sets a second column's",
            "TreeColumn.alignment", "the column swept is its tree's first, which is always left aligned, as "
                    + "natively; MarkSetBackTest sets a second column's");

    /**
     * Asserts that no setter of the widget, given the value its getter returns, marks a property whose
     * wire value it leaves as it was, on any widget: a mark tells the client its copy is out of date,
     * and only a compared property ({@code wireCompared}) is checked again before it is written. Run
     * per style, on the widget as built and once each setter has given it another value, so a default,
     * a set value and every style's own properties are tried. Asserts as well that every wire property
     * is changed by some setter, or is listed with what changes it instead, so a property added later
     * is either swept or named.
     */
    @SuppressWarnings("unchecked")
    protected <W extends Widget> void assertSetAgainNamesNothing(java.util.function.IntFunction<W> make,
            Consumer<W> fill, java.util.Map<String, String> unwritable, int... styles) {
        java.util.Set<String> marked = new java.util.TreeSet<>();
        java.util.Map<String, java.util.Set<String>> reached = new java.util.TreeMap<>();
        java.util.Set<String> unmarked = new java.util.TreeSet<>();
        String name = null;
        java.util.Set<String> wire = new java.util.TreeSet<>();
        java.util.Set<String> compared = new java.util.TreeSet<>();
        java.util.Set<String> unread = new java.util.TreeSet<>();
        for (int style : styles) {
            W probe = make.apply(style);
            name = probe.getClass().getSimpleName();
            VWidget value = ((DartWidget) probe.getImpl()).getValue();
            wire.addAll(SetAgainSweep.wireNames(value));
            for (int key : value.wireKeys()) {
                if (value.wireCompared(key)) compared.add(value.wireName(key));
                if (!SetAgainSweep.written(value, key)) unread.add(value.wireName(key));
            }
            // Seeded per style, so what the sweep reaches is the same on every run.
            fillSeed = (long) style;
            SetAgainSweep.Result found;
            try {
                found = SetAgainSweep.run(() -> make.apply(style), w -> fill.accept((W) w));
            } finally {
                fillSeed = null;
            }
            found.marked.forEach((setter, names) -> names.forEach(n -> marked.add(setter + " " + n)));
            found.reached.forEach((p, by) -> reached.computeIfAbsent(p, k -> new java.util.TreeSet<>()).addAll(by));
            found.unmarked.forEach((setter, names) -> names.forEach(n -> unmarked.add(setter + " " + n)));
        }
        String widget = name;
        report(widget, wire, compared, unread, reached, unwritable, marked, unmarked);
        java.util.Set<String> unexplained = new java.util.TreeSet<>(marked);
        unexplained.removeIf(m -> accepted(MARKED_ON_PURPOSE, widget, m) != null);
        org.assertj.core.api.Assertions.assertThat(unexplained)
                .as("%s: these setters mark a property whose wire value they leave as it was", widget)
                .isEmpty();

        java.util.Set<String> lost = new java.util.TreeSet<>(unmarked);
        lost.removeIf(m -> accepted(UNMARKED_ON_PURPOSE, widget, m) != null);
        org.assertj.core.api.Assertions.assertThat(lost)
                .as("%s: these setters change a property on the wire without marking it, so the client never "
                        + "gets the change", widget)
                .isEmpty();

        java.util.Set<String> unreached = new java.util.TreeSet<>(wire);
        unreached.removeAll(reached.keySet());
        unreached.removeAll(compared);
        // A property the client of this widget never reads is written by no change: no setter needs to reach it.
        unreached.removeAll(unread);
        // Never written, so never changed on the wire: the test above says why for each.
        unreached.removeAll(unwritable.keySet());
        java.util.Set<String> unlisted = new java.util.TreeSet<>(unreached);
        unlisted.removeIf(p -> SET_ELSEWHERE.containsKey(widget + "." + p) || SET_ELSEWHERE.containsKey("*." + p));
        org.assertj.core.api.Assertions.assertThat(unlisted)
                .as("%s: no setter changes these; sweep them or say in SET_ELSEWHERE what does", widget)
                .isEmpty();
        java.util.Set<String> stale = new java.util.TreeSet<>();
        SET_ELSEWHERE.keySet().stream().filter(k -> k.startsWith(widget + "."))
                .map(k -> k.substring(widget.length() + 1)).filter(reached::containsKey).forEach(stale::add);
        org.assertj.core.api.Assertions.assertThat(stale)
                .as("%s: listed in SET_ELSEWHERE but a setter changes them; take them off", widget)
                .isEmpty();
    }

    /** Why a sweep finding is accepted, from [table] by the keys the assertions look it up by; null when it is not. */
    private static String accepted(java.util.Map<String, String> table, String widget, String finding) {
        String call = finding.replaceFirst("^(as built |changed )", "");
        for (String key : new String[] {widget + "." + call, "*." + call, widget + "." + finding, "*." + finding}) {
            if (table.containsKey(key)) return table.get(key);
        }
        return null;
    }

    /**
     * With {@code -DsetAgainSweep.report=<dir>}, writes {@code <dir>/<Widget>.tsv}: each wire property
     * with how a change to it reaches the client, then each accepted finding with its reason.
     */
    private static void report(String widget, java.util.Set<String> wire, java.util.Set<String> compared,
            java.util.Set<String> unread, java.util.Map<String, java.util.Set<String>> reached,
            java.util.Map<String, String> unwritable, java.util.Set<String> marked, java.util.Set<String> unmarked) {
        String dir = System.getProperty("setAgainSweep.report");
        if (dir == null) return;
        StringBuilder out = new StringBuilder();
        for (String p : wire) {
            String how;
            String why;
            if (unread.contains(p)) {
                how = "unread";
                why = "the client of this widget never reads it, so nothing is written";
            } else if (compared.contains(p)) {
                how = "compared";
                why = "marked where it may have changed; written only when it differs from what was sent";
            } else if (reached.containsKey(p)) {
                how = "setter";
                why = String.join(", ", reached.get(p));
            } else if (unwritable.containsKey(p)) {
                how = "unwritable";
                why = unwritable.get(p);
            } else {
                how = "elsewhere";
                why = SET_ELSEWHERE.getOrDefault(widget + "." + p, SET_ELSEWHERE.get("*." + p));
            }
            out.append(widget).append('\t').append(p).append('\t').append(how).append('\t').append(why).append('\n');
        }
        for (String m : marked) {
            String why = accepted(MARKED_ON_PURPOSE, widget, m);
            if (why != null) out.append(widget).append('\t').append(m).append("\tmarked on purpose\t").append(why).append('\n');
        }
        for (String m : unmarked) {
            String why = accepted(UNMARKED_ON_PURPOSE, widget, m);
            if (why != null) out.append(widget).append('\t').append(m).append("\tunmarked on purpose\t").append(why).append('\n');
        }
        try {
            java.nio.file.Path file = java.nio.file.Paths.get(dir, widget + ".tsv");
            java.nio.file.Files.createDirectories(file.getParent());
            java.nio.file.Files.write(file, out.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
        } catch (java.io.IOException e) {
            throw new java.io.UncheckedIOException(e);
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
                .withSettings(settings())
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
        // A direct write to the public field bypasses the setter's dirty mark; when the setter then
        // receives the same random value it returns early and the change is never named.
        if (w instanceof org.eclipse.swt.custom.SashForm)
            inst.ignore(Select.field(org.eclipse.swt.custom.SashForm.class, "SASH_WIDTH"));
        inst.fill();
    }

    protected void setAll(ExpandItem w) {
        InstancioObjectApi<ExpandItem> inst = Instancio.ofObject(w)
                .withSettings(settings())
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
                .withSettings(settings())
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
                .withSettings(settings())
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
                .withSettings(settings())
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
                .withSettings(settings())
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
                .withSettings(settings())
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
                .withSettings(settings())
                .lenient()
                .withFillType(FillType.POPULATE_NULLS_AND_DEFAULT_PRIMITIVES)
                .generate(Select.all(int.class), gen -> gen.ints().range(1, 20))
                .generate(Select.all(String.class), gen -> gen.oneOf("Roboto", "Inter", "Lato"));
        inst.fill();
    }

    protected void setAll(Accessible a) {
        InstancioObjectApi<Accessible> inst = Instancio.ofObject(a)
                .withSettings(settings())
                .lenient()
                .ignore(Select.setter(Accessible.class, "setImpl"))
                .withFillType(FillType.POPULATE_NULLS_AND_DEFAULT_PRIMITIVES)
                .generate(Select.all(int.class), gen -> gen.ints().range(1, 1000));
        inst.fill();
    }

    protected void setAll(org.eclipse.swt.widgets.ToolTip tt) {
        InstancioObjectApi<org.eclipse.swt.widgets.ToolTip> inst = Instancio.ofObject(tt)
                .withSettings(settings())
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
            // A column of width 0 is hidden and gets no owner-draw events: size them as an app does.
            new TableColumn(t, SWT.NONE).setWidth(50);
            new TableColumn(t, SWT.NONE).setWidth(50);
            t.setColumnOrder(reversedOrder(t.getColumnCount()));
            // The fill sets the direction to a number SWT refuses.
            t.setSortDirection(SWT.UP);
            measureTallRow(t, new TableItem(t, SWT.NONE));
        } else if (w instanceof Tree t) {
            // A column of width 0 is hidden and gets no owner-draw events: size them as an app does.
            new TreeColumn(t, SWT.NONE).setWidth(50);
            new TreeColumn(t, SWT.NONE).setWidth(50);
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
            try (Mocks.Delivery ignored = Mocks.deliverEvents(DartMocks.dartDisplay());
                 AutoCloseable compact = compactRowsFor(value)) {
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

    /**
     * Turns native item heights off while a Table or Tree is written, so its compactRows is not
     * at its default; an application sets this for the whole process, so it is put back after.
     */
    private static AutoCloseable compactRowsFor(Object value) {
        if (!(value instanceof Table) && !(value instanceof Tree))
            return () -> { };
        String key = org.eclipse.swt.widgets.DisplayHelper.USE_NATIVE_ITEM_HEIGHT;
        String previous = System.getProperty(key);
        System.setProperty(key, "false");
        return () -> {
            if (previous == null)
                System.clearProperty(key);
            else
                System.setProperty(key, previous);
        };
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
                        GraphicsUtils.getAlpha(value), value.getRed(), value.getGreen(), value.getBlue()));
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
