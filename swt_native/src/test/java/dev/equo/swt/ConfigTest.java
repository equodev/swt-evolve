package dev.equo.swt;

import com.equo.chromium.swt.Browser;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.*;
import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * {@link Config}'s choice between the Dart and the native implementation of a class. The part that
 * needs a native widget, which only the embedded backend hosts, is {@code ConfigHybridTest} there.
 */
public class ConfigTest {

    private static Config.Impl initialDefault;
    /** The dev.equo.swt.* properties as this class found them: the test task sets some for every test. */
    private static Map<String, String> initialProperties;

    @BeforeAll
    static void keep_the_process_state() {
        // Config's state is process-wide, and the per-test hooks below pin it. Restoring it after the
        // class keeps later test classes on the state they were written for.
        initialDefault = Config.defaultImpl;
        initialProperties = new HashMap<>();
        System.getProperties().forEach((k, v) -> {
            if (k.toString().startsWith(Config.PROPERTY_PREFIX)) initialProperties.put(k.toString(), v.toString());
        });
    }

    @AfterAll
    static void restore_the_process_default() {
        Config.defaultImpl = initialDefault;
    }

    @BeforeEach
    void restore_config_before() {
        restoreConfig();
    }

    @AfterEach
    void restore_config_after() {
        restoreConfig();
    }

    private static void restoreConfig() {
        // `equo` is Config's default; a class that ran earlier in this fork may have left `eclipse`.
        Config.defaultToEquo();
        // Config.reset() clears every dev.equo.swt.* property, the test task's included: put those back.
        Config.reset();
        initialProperties.forEach(System::setProperty);
    }

    @Test
    void should_default_to_swt() {
        Config.defaultToEclipse();
        assertThat(Config.defaultImpl).isEqualTo(Config.Impl.eclipse);
        assertThat(Config.isEquo(CTabItem.class)).isFalse();
    }

    @Test
    void should_default_to_equo() {
        Config.defaultToEclipse();
        Config.defaultToEquo();
        assertThat(Config.defaultImpl).isEqualTo(Config.Impl.equo);
        assertThat(Config.isEquo(CTabItem.class)).isTrue();
    }

    @Test
    void class_should_default_to_swt_with_config() {
        Config.useEclipse(Point.class);
        assertThat(Config.isEquo(Point.class)).isFalse();
    }

    @Test
    void class_should_default_to_equo_with_config() {
        Config.useEquo(Point.class);
        assertThat(Config.isEquo(Point.class)).isTrue();
    }

    @Test
    void class_should_default_to_equo_with_property() {
        System.setProperty("dev.equo.swt.Point", Config.Impl.equo.name());
        assertThat(Config.isEquo(Point.class)).isTrue();
    }

    @Test
    void class_should_default_to_swt_without_property() {
        System.clearProperty("dev.equo.swt.Point");
        assertThat(Config.isEquo(Point.class)).isFalse();
    }

    @Test
    void clear_drops_the_choice_for_its_class() {
        Config.useEquo(Point.class);
        Config.clear(Point.class);
        assertThat(Config.isEquo(Point.class)).isFalse();
    }

    @Test
    void reset_drops_every_per_class_choice() {
        Config.useEquo(Point.class);
        Config.useEclipse(Button.class);
        Config.reset();
        assertThat(Config.isEquo(Point.class)).isFalse();
        assertThat(Config.isEquo(Button.class)).isTrue();
    }

    @Test
    void the_eclipse_default_hands_the_browser_to_chromium() {
        Config.defaultToEclipse();
        assertThat(Config.isEquo(Browser.class, null)).isFalse();
    }

    @Test
    void the_eclipse_default_keeps_a_gc_without_a_dart_parent_native() {
        Config.defaultToEclipse();
        assertThat(Config.isEquoGC(GC.class, null)).isFalse();
    }

    @Test
    void run_with_eclipse_default_restores_the_previous_default() {
        Config.forceEquo();
        boolean[] inside = new boolean[1];

        Config.runWithEclipseDefault(() -> inside[0] = Config.isEquo(Button.class));

        assertThat(inside[0]).isFalse();
        assertThat(Config.defaultImpl).isEqualTo(Config.Impl.force_equo);
    }

    @Test
    void run_with_eclipse_default_restores_the_previous_default_when_the_action_throws() {
        try {
            Config.runWithEclipseDefault(() -> {
                throw new IllegalStateException();
            });
        } catch (IllegalStateException expected) {
        }

        assertThat(Config.defaultImpl).isEqualTo(Config.Impl.equo);
    }

    @Nested
    class EquoWidgetDefaults {

        @Test
        void button_should_default_to_equo() {
            assertThat(Config.isEquo(Button.class)).isTrue();
        }

        @Test
        void button_should_default_to_swt_with_config() {
            Config.useEclipse(Button.class);
            assertThat(Config.isEquo(Button.class)).isFalse();
        }

        @Test
        void button_should_be_equo_with_property_when_the_default_is_eclipse() {
            Config.defaultToEclipse();
            System.setProperty("dev.equo.swt.Button", Config.Impl.equo.name());
            assertThat(Config.isEquo(Button.class)).isTrue();
        }

        @Test
        void button_should_default_to_eclipse_with_property_as_eclipse() {
            System.setProperty("dev.equo.swt.Button", "eclipse");
            assertThat(Config.isEquo(Button.class)).isFalse();
        }

    }

    @Nested
    class GlobalDefaultRespect {

        @Test
        void should_respect_global_eclipse_default_for_button() {
            Config.defaultToEclipse();
            assertThat(Config.isEquo(Button.class)).isFalse();
        }

        @Test
        void should_respect_global_eclipse_default_for_ctabitem() {
            Config.defaultToEclipse();
            assertThat(Config.isEquo(CTabItem.class)).isFalse();
        }

        @Test
        void should_allow_per_widget_override_with_global_eclipse() {
            Config.defaultToEclipse();
            Config.useEquo(Button.class);
            assertThat(Config.isEquo(Button.class)).isTrue();
        }

        @Test
        void should_respect_force_equo_for_all_widgets() {
            Config.forceEquo();
            assertThat(Config.isEquo(Button.class)).isTrue();
            assertThat(Config.isEquo(Point.class)).isTrue();
        }

        @Test
        void per_widget_property_should_override_global_default() {
            Config.defaultToEclipse();
            Config.useEquo(CTabItem.class);
            assertThat(Config.isEquo(CTabItem.class)).isTrue();
        }
    }

    /** How a class that picks its implementation by its parent is answered under a Dart one. */
    @Nested
    class UnderADartParent {

        @Test
        void a_child_is_dart_even_when_the_default_is_eclipse() {
            Config.defaultToEclipse();
            assertThat(Config.isEquo(Button.class, Mocks.shell())).isTrue();
        }

        @Test
        void a_class_set_to_eclipse_stays_native() {
            Config.useEclipse(Button.class);
            assertThat(Config.isEquo(Button.class, Mocks.shell())).isFalse();
        }

        @Test
        void an_id_property_chooses_for_that_one_widget() {
            Shell shell = Mocks.shell();
            System.setProperty(Config.PROPERTY_PREFIX + Config.getId(Button.class, shell), "eclipse");
            assertThat(Config.isEquo(Button.class, shell)).isFalse();
            assertThat(Config.isEquo(Label.class, shell)).isTrue();
        }

        @Test
        void file_dialogs_stay_native_once_a_display_bridge_runs() {
            // DisplayBridge sets dev.equo.swt.desktop=false for the backend that ships.
            System.setProperty("dev.equo.swt.desktop", "false");
            assertThat(Config.isEquo(FileDialog.class, Mocks.shell())).isFalse();
            assertThat(Config.isEquo(DirectoryDialog.class, Mocks.shell())).isFalse();

            System.setProperty("dev.equo.swt.desktop", "true");
            assertThat(Config.isEquo(FileDialog.class, Mocks.shell())).isTrue();
        }

        @Test
        void the_parent_data_chooses_for_its_children() {
            Shell shell = Mocks.shell();
            when(shell.getData(Config.getKey(Button.class))).thenReturn(Config.Impl.eclipse);
            assertThat(Config.isEquo(Button.class, shell)).isFalse();
            when(shell.getData(Config.getKey(Button.class))).thenReturn("eclipse");
            assertThat(Config.isEquo(Button.class, shell)).isFalse();
        }
    }

    @Nested
    class DependentWidgetActivation {

        // Tree group tests
        @Test
        void activating_tree_should_activate_treeitem() {
            Config.defaultToEclipse();
            System.setProperty("dev.equo.swt.Tree", "equo");
            assertThat(Config.isForced(Tree.class)).isEqualTo(Config.Impl.equo);
            assertThat(Config.isForced(TreeItem.class)).isEqualTo(Config.Impl.equo);
            assertThat(Config.isForced(TreeColumn.class)).isEqualTo(Config.Impl.equo);
        }

        @Test
        void activating_treeitem_should_activate_tree_and_treecolumn() {
            Config.defaultToEclipse();
            System.setProperty("dev.equo.swt.TreeItem", "equo");
            assertThat(Config.isForced(Tree.class)).isEqualTo(Config.Impl.equo);
            assertThat(Config.isForced(TreeItem.class)).isEqualTo(Config.Impl.equo);
            assertThat(Config.isForced(TreeColumn.class)).isEqualTo(Config.Impl.equo);
        }

        @Test
        void activating_treecolumn_should_activate_tree_and_treeitem() {
            Config.defaultToEclipse();
            System.setProperty("dev.equo.swt.TreeColumn", "equo");
            assertThat(Config.isForced(Tree.class)).isEqualTo(Config.Impl.equo);
            assertThat(Config.isForced(TreeItem.class)).isEqualTo(Config.Impl.equo);
            assertThat(Config.isForced(TreeColumn.class)).isEqualTo(Config.Impl.equo);
        }

        // Table group tests
        @Test
        void activating_table_should_activate_tableitem_and_tablecolumn() {
            Config.defaultToEclipse();
            System.setProperty("dev.equo.swt.Table", "equo");
            assertThat(Config.isForced(Table.class)).isEqualTo(Config.Impl.equo);
            assertThat(Config.isForced(TableItem.class)).isEqualTo(Config.Impl.equo);
            assertThat(Config.isForced(TableColumn.class)).isEqualTo(Config.Impl.equo);
        }

        @Test
        void activating_tableitem_should_activate_table_and_tablecolumn() {
            Config.defaultToEclipse();
            System.setProperty("dev.equo.swt.TableItem", "equo");
            assertThat(Config.isForced(Table.class)).isEqualTo(Config.Impl.equo);
            assertThat(Config.isForced(TableItem.class)).isEqualTo(Config.Impl.equo);
            assertThat(Config.isForced(TableColumn.class)).isEqualTo(Config.Impl.equo);
        }

        // CTabFolder group tests
        @Test
        void activating_ctabfolder_should_activate_ctabitem_and_renderer() {
            Config.defaultToEclipse();
            System.setProperty("dev.equo.swt.CTabFolder", "equo");
            assertThat(Config.isForced(CTabFolder.class)).isEqualTo(Config.Impl.equo);
            assertThat(Config.isForced(CTabItem.class)).isEqualTo(Config.Impl.equo);
            assertThat(Config.isForced(CTabFolderRenderer.class)).isEqualTo(Config.Impl.equo);
        }

        @Test
        void activating_ctabitem_should_activate_ctabfolder_and_renderer() {
            Config.defaultToEclipse();
            System.setProperty("dev.equo.swt.CTabItem", "equo");
            assertThat(Config.isForced(CTabFolder.class)).isEqualTo(Config.Impl.equo);
            assertThat(Config.isForced(CTabItem.class)).isEqualTo(Config.Impl.equo);
            assertThat(Config.isForced(CTabFolderRenderer.class)).isEqualTo(Config.Impl.equo);
        }

        // ToolBar group tests
        @Test
        void activating_toolbar_should_activate_toolitem() {
            Config.defaultToEclipse();
            System.setProperty("dev.equo.swt.ToolBar", "equo");
            assertThat(Config.isForced(ToolBar.class)).isEqualTo(Config.Impl.equo);
            assertThat(Config.isForced(ToolItem.class)).isEqualTo(Config.Impl.equo);
        }

        @Test
        void activating_toolitem_should_activate_toolbar() {
            Config.defaultToEclipse();
            System.setProperty("dev.equo.swt.ToolItem", "equo");
            assertThat(Config.isForced(ToolBar.class)).isEqualTo(Config.Impl.equo);
            assertThat(Config.isForced(ToolItem.class)).isEqualTo(Config.Impl.equo);
        }

        // Explicit disable should override group activation
        @Test
        void explicit_disable_should_override_group_activation() {
            Config.defaultToEclipse();
            System.setProperty("dev.equo.swt.Tree", "equo");
            System.setProperty("dev.equo.swt.TreeItem", "eclipse");
            assertThat(Config.isForced(Tree.class)).isEqualTo(Config.Impl.equo);
            assertThat(Config.isForced(TreeItem.class)).isEqualTo(Config.Impl.eclipse);
            assertThat(Config.isForced(TreeColumn.class)).isEqualTo(Config.Impl.equo);
        }

        @Test
        void explicit_disable_of_parent_should_not_affect_child_group_activation() {
            Config.defaultToEclipse();
            System.setProperty("dev.equo.swt.Tree", "eclipse");
            System.setProperty("dev.equo.swt.TreeItem", "equo");
            assertThat(Config.isForced(Tree.class)).isEqualTo(Config.Impl.eclipse);
            assertThat(Config.isForced(TreeItem.class)).isEqualTo(Config.Impl.equo);
            assertThat(Config.isForced(TreeColumn.class)).isEqualTo(Config.Impl.eclipse);
        }

        // Widgets without dependencies should not be affected
        @Test
        void widgets_without_dependencies_should_not_be_affected() {
            Config.defaultToEclipse();
            System.setProperty("dev.equo.swt.Tree", "equo");
            assertThat(Config.isForced(Button.class)).isNull();
            assertThat(Config.isForced(Label.class)).isNull();
        }

        // Test getDependencyGroup
        @Test
        void should_return_correct_dependency_group_for_tree() {
            var group = Config.getDependencyGroup(Tree.class);
            assertThat(group).containsExactlyInAnyOrder("Tree", "TreeItem", "TreeColumn");
        }

        @Test
        void should_return_correct_dependency_group_for_table() {
            var group = Config.getDependencyGroup(Table.class);
            assertThat(group).containsExactlyInAnyOrder("Table", "TableItem", "TableColumn");
        }

        @Test
        void should_return_null_for_widgets_without_dependencies() {
            var group = Config.getDependencyGroup(Button.class);
            assertThat(group).isNull();
        }

        // TaskBar group tests
        @Test
        void activating_taskbar_should_activate_taskitem() {
            Config.defaultToEclipse();
            System.setProperty("dev.equo.swt.TaskBar", "equo");
            assertThat(Config.isForced(TaskBar.class)).isEqualTo(Config.Impl.equo);
            assertThat(Config.isForced(TaskItem.class)).isEqualTo(Config.Impl.equo);
        }

        @Test
        void activating_taskitem_should_activate_taskbar() {
            Config.defaultToEclipse();
            System.setProperty("dev.equo.swt.TaskItem", "equo");
            assertThat(Config.isForced(TaskBar.class)).isEqualTo(Config.Impl.equo);
            assertThat(Config.isForced(TaskItem.class)).isEqualTo(Config.Impl.equo);
        }

        @Test
        void should_return_correct_dependency_group_for_taskbar() {
            var group = Config.getDependencyGroup(TaskBar.class);
            assertThat(group).containsExactlyInAnyOrder("TaskBar", "TaskItem");
        }
    }

    @Nested
    class ClassName {
        @Test
        void widget_name_for_swt() {
            String type = Config.getSwtBaseClassName(Composite.class);
            assertThat(type).isEqualTo("Composite");
        }

        @Test
        void widget_name_for_subclass() {
            String type = Config.getSwtBaseClassName(MyComp.class);
            assertThat(type).isEqualTo("Composite");
        }

        @Test
        void widget_name_for_anonymous() {
            Config.forceEquo();

            Composite anonym = new Composite(null) {};
            String type = Config.getSwtBaseClassName(anonym.getClass());
            assertThat(type).isEqualTo("Composite");
        }

        @Test
        void widget_name_for_anonymous_subclass() {
            Config.forceEquo();

            Composite anonym = new MyComp() {};
            String type = Config.getSwtBaseClassName(anonym.getClass());
            assertThat(type).isEqualTo("Composite");
        }

        class MyComp extends Composite {
            public MyComp() {
                super(null);
            }
        }
    }

    @Nested
    class GetId {

        private Composite mockComposite(Composite parent, Control... children) {
            Composite c = mock(Composite.class);
            when(c.getChildren()).thenReturn(children);
            when(c.getParent()).thenReturn(parent);
            return c;
        }

        private final String C = mock(Composite.class).getClass().getSimpleName();
        private final String S = mock(Shell.class).getClass().getSimpleName();

        @Test
        void leaf_widget_position_is_parent_children_count() {
            Composite root = mockComposite(null, mock(Control.class), mock(Control.class));

            String id = Config.getId(Button.class, root);

            assertThat(id).isEqualTo("/" + C + "/-1/Button/3");
        }

        @Test
        void parent_position_should_be_its_index_not_sibling_count() {
            Composite grandparent = mock(Composite.class);
            Composite parent = mockComposite(grandparent);
            when(grandparent.getChildren()).thenReturn(new Control[]{mock(Control.class), parent, mock(Control.class)});

            String id = Config.getId(Button.class, parent);

            assertThat(id).isEqualTo("/" + C + "/-1/" + C + "/2/Button/1");
        }

        @Test
        void first_child_parent_position_should_be_zero() {
            Composite root = mock(Composite.class);
            Composite parent = mockComposite(root, mock(Control.class));
            when(root.getChildren()).thenReturn(new Control[]{parent, mock(Control.class), mock(Control.class)});

            String id = Config.getId(Button.class, parent);

            assertThat(id).isEqualTo("/" + C + "/-1/" + C + "/1/Button/2");
        }

        @Test
        void shell_with_composite_child() {
            Display display = mock(Display.class);
            Shell shell = mock(Shell.class);
            when(shell.getDisplay()).thenReturn(display);
            when(shell.getParent()).thenReturn(null);
            when(shell.getChildren()).thenReturn(new Control[]{});
            when(display.getShells()).thenReturn(new Shell[]{shell});

            String id = Config.getId(Composite.class, shell);

            assertThat(id).isEqualTo("/" + S + "/-1/Composite/1");
        }

        @Test
        void shell_with_shell_child_with_composite() {
            Display display = mock(Display.class);
            Shell parentShell = mock(Shell.class);
            Shell childShell = mock(Shell.class);
            when(parentShell.getDisplay()).thenReturn(display);
            when(parentShell.getParent()).thenReturn(null);
            when(parentShell.getShells()).thenReturn(new Shell[]{childShell});
            when(parentShell.getChildren()).thenReturn(new Control[]{childShell});
            when(childShell.getDisplay()).thenReturn(display);
            when(childShell.getParent()).thenReturn(parentShell);
            when(childShell.getChildren()).thenReturn(new Control[]{});
            when(display.getShells()).thenReturn(new Shell[]{parentShell, childShell});

            String id = Config.getId(Composite.class, childShell);

            assertThat(id).isEqualTo("/" + S + "/-1/" + S + "/1/Composite/1");
        }

        @Test
        void deeply_nested_positions_should_reflect_indices() {
            Composite root = mock(Composite.class);
            when(root.getParent()).thenReturn(null);

            Composite grandparent = mockComposite(root);
            when(root.getChildren()).thenReturn(new Control[]{mock(Control.class), grandparent, mock(Control.class)});

            Composite parent = mockComposite(grandparent, mock(Control.class), mock(Control.class));
            when(grandparent.getChildren()).thenReturn(new Control[]{mock(Control.class), parent, mock(Control.class), mock(Control.class)});

            String id = Config.getId(Button.class, parent);

            assertThat(id).isEqualTo("/" + C + "/-1/" + C + "/2/" + C + "/2/Button/3");
        }
    }

}
