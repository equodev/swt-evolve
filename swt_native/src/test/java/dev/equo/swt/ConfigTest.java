package dev.equo.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolderRenderer;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class ConfigTest {

    @BeforeEach
    void defaults_swt() {
        Config.defaultImpl = Config.Impl.eclipse;
    }

    @AfterEach
    void reset() {
        Config.defaultImpl = Config.Impl.eclipse;
        System.clearProperty("dev.equo.swt.Point");
        System.clearProperty("dev.equo.swt.CTabItem");
        System.clearProperty("dev.equo.swt.CTabFolder");
        System.clearProperty("dev.equo.swt.CTabFolderRenderer");
        System.clearProperty("dev.equo.swt.CTabFolderLayout");
        System.clearProperty("dev.equo.swt.Button");
        System.clearProperty("dev.equo.swt.Tree");
        System.clearProperty("dev.equo.swt.TreeItem");
        System.clearProperty("dev.equo.swt.TreeColumn");
        System.clearProperty("dev.equo.swt.Table");
        System.clearProperty("dev.equo.swt.TableItem");
        System.clearProperty("dev.equo.swt.TableColumn");
        System.clearProperty("dev.equo.swt.ToolBar");
        System.clearProperty("dev.equo.swt.ToolItem");
        System.clearProperty("dev.equo.swt.Menu");
        System.clearProperty("dev.equo.swt.MenuItem");
    }

    @Test
    void should_default_to_swt() {
        System.clearProperty("dev.equo.swt.Point");
        Config.defaultToEclipse();
        assertThat(Config.defaultImpl).isEqualTo(Config.Impl.eclipse);
        assertThat(Config.isEquo(Point.class)).isFalse();
    }

    @Test
    void should_default_to_equo() {
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

    @Nested
    class EquoWidgetDefaults {

        @Test
        void button_should_default_to_swt_with_config() {
            Config.useEclipse(Button.class);
            assertThat(Config.isEquo(Button.class)).isFalse();
        }

        @Test
        void button_should_default_to_equo_with_config() {
            Config.useEquo(Button.class);
            assertThat(Config.isEquo(Button.class)).isTrue();
        }

        @Test
        void button_should_default_to_equo_with_property() {
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

        @AfterEach
        void cleanup() {
            System.clearProperty("dev.equo.swt.Button");
            System.clearProperty("dev.equo.swt.CTabItem");
            Config.defaultToEclipse();
        }

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

    @Nested
    class DependentWidgetActivation {

        @AfterEach
        void cleanup() {
            System.clearProperty("dev.equo.swt.Tree");
            System.clearProperty("dev.equo.swt.TreeItem");
            System.clearProperty("dev.equo.swt.TreeColumn");
            System.clearProperty("dev.equo.swt.Table");
            System.clearProperty("dev.equo.swt.TableItem");
            System.clearProperty("dev.equo.swt.TableColumn");
            System.clearProperty("dev.equo.swt.CTabFolder");
            System.clearProperty("dev.equo.swt.CTabItem");
            System.clearProperty("dev.equo.swt.CTabFolderRenderer");
            System.clearProperty("dev.equo.swt.CTabFolderLayout");
            System.clearProperty("dev.equo.swt.ToolBar");
            System.clearProperty("dev.equo.swt.ToolItem");
            System.clearProperty("dev.equo.swt.Menu");
            System.clearProperty("dev.equo.swt.MenuItem");
            System.clearProperty("dev.equo.swt.TaskBar");
            System.clearProperty("dev.equo.swt.TaskItem");
            Config.defaultToEclipse();
        }

        // Tree group tests
        @Test
        void activating_tree_should_activate_treeitem() {
            Config.defaultToEclipse();
            System.setProperty("dev.equo.swt.Tree", "equo");
            assertThat(Config.isEquoForced(Tree.class)).isTrue();
            assertThat(Config.isEquoForced(TreeItem.class)).isTrue();
            assertThat(Config.isEquoForced(TreeColumn.class)).isTrue();
        }

        @Test
        void activating_treeitem_should_activate_tree_and_treecolumn() {
            Config.defaultToEclipse();
            System.setProperty("dev.equo.swt.TreeItem", "equo");
            assertThat(Config.isEquoForced(Tree.class)).isTrue();
            assertThat(Config.isEquoForced(TreeItem.class)).isTrue();
            assertThat(Config.isEquoForced(TreeColumn.class)).isTrue();
        }

        @Test
        void activating_treecolumn_should_activate_tree_and_treeitem() {
            Config.defaultToEclipse();
            System.setProperty("dev.equo.swt.TreeColumn", "equo");
            assertThat(Config.isEquoForced(Tree.class)).isTrue();
            assertThat(Config.isEquoForced(TreeItem.class)).isTrue();
            assertThat(Config.isEquoForced(TreeColumn.class)).isTrue();
        }

        // Table group tests
        @Test
        void activating_table_should_activate_tableitem_and_tablecolumn() {
            Config.defaultToEclipse();
            System.setProperty("dev.equo.swt.Table", "equo");
            assertThat(Config.isEquoForced(Table.class)).isTrue();
            assertThat(Config.isEquoForced(TableItem.class)).isTrue();
            assertThat(Config.isEquoForced(TableColumn.class)).isTrue();
        }

        @Test
        void activating_tableitem_should_activate_table_and_tablecolumn() {
            Config.defaultToEclipse();
            System.setProperty("dev.equo.swt.TableItem", "equo");
            assertThat(Config.isEquoForced(Table.class)).isTrue();
            assertThat(Config.isEquoForced(TableItem.class)).isTrue();
            assertThat(Config.isEquoForced(TableColumn.class)).isTrue();
        }

        // CTabFolder group tests
        @Test
        void activating_ctabfolder_should_activate_ctabitem_and_renderer() {
            Config.defaultToEclipse();
            System.setProperty("dev.equo.swt.CTabFolder", "equo");
            assertThat(Config.isEquoForced(CTabFolder.class)).isTrue();
            assertThat(Config.isEquoForced(CTabItem.class)).isTrue();
            assertThat(Config.isEquoForced(CTabFolderRenderer.class)).isTrue();
        }

        @Test
        void activating_ctabitem_should_activate_ctabfolder_and_renderer() {
            Config.defaultToEclipse();
            System.setProperty("dev.equo.swt.CTabItem", "equo");
            assertThat(Config.isEquoForced(CTabFolder.class)).isTrue();
            assertThat(Config.isEquoForced(CTabItem.class)).isTrue();
            assertThat(Config.isEquoForced(CTabFolderRenderer.class)).isTrue();
        }

        // ToolBar group tests
        @Test
        void activating_toolbar_should_activate_toolitem() {
            Config.defaultToEclipse();
            System.setProperty("dev.equo.swt.ToolBar", "equo");
            assertThat(Config.isEquoForced(ToolBar.class)).isTrue();
            assertThat(Config.isEquoForced(ToolItem.class)).isTrue();
        }

        @Test
        void activating_toolitem_should_activate_toolbar() {
            Config.defaultToEclipse();
            System.setProperty("dev.equo.swt.ToolItem", "equo");
            assertThat(Config.isEquoForced(ToolBar.class)).isTrue();
            assertThat(Config.isEquoForced(ToolItem.class)).isTrue();
        }

        // Explicit disable should override group activation
        @Test
        void explicit_disable_should_override_group_activation() {
            Config.defaultToEclipse();
            System.setProperty("dev.equo.swt.Tree", "equo");
            System.setProperty("dev.equo.swt.TreeItem", "eclipse");
            assertThat(Config.isEquoForced(Tree.class)).isTrue();
            assertThat(Config.isEquoForced(TreeItem.class)).isFalse();
            assertThat(Config.isEquoForced(TreeColumn.class)).isTrue();
        }

        @Test
        void explicit_disable_of_parent_should_not_affect_child_group_activation() {
            Config.defaultToEclipse();
            System.setProperty("dev.equo.swt.Tree", "eclipse");
            System.setProperty("dev.equo.swt.TreeItem", "equo");
            assertThat(Config.isEquoForced(Tree.class)).isFalse();
            assertThat(Config.isEquoForced(TreeItem.class)).isTrue();
            assertThat(Config.isEquoForced(TreeColumn.class)).isTrue();
        }

        // Widgets without dependencies should not be affected
        @Test
        void widgets_without_dependencies_should_not_be_affected() {
            Config.defaultToEclipse();
            System.setProperty("dev.equo.swt.Tree", "equo");
            assertThat(Config.isEquoForced(Button.class)).isFalse();
            assertThat(Config.isEquoForced(Label.class)).isFalse();
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
            assertThat(Config.isEquoForced(TaskBar.class)).isTrue();
            assertThat(Config.isEquoForced(TaskItem.class)).isTrue();
        }

        @Test
        void activating_taskitem_should_activate_taskbar() {
            Config.defaultToEclipse();
            System.setProperty("dev.equo.swt.TaskItem", "equo");
            assertThat(Config.isEquoForced(TaskBar.class)).isTrue();
            assertThat(Config.isEquoForced(TaskItem.class)).isTrue();
        }

        @Test
        void should_return_correct_dependency_group_for_taskbar() {
            var group = Config.getDependencyGroup(TaskBar.class);
            assertThat(group).containsExactlyInAnyOrder("TaskBar", "TaskItem");
        }
    }

    @Nested
    @ExtendWith(Mocks.class)
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
