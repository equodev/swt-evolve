package dev.equo.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.swt.widgets.Mocks.shell;
import static org.mockito.Mockito.*;

@ExtendWith(Mocks.class)
@ExtendWith(MockFlutterBridge.Extension.class)
class DynCompositeTest {

    @BeforeEach
    void enableDyn() {
        Config.defaultToEquo();
        Config.dynEnabled = true;
    }

    @AfterEach
    void reset() {
        System.clearProperty(Config.PROPERTY_PREFIX+"Composite");
        System.clearProperty(Config.PROPERTY_PREFIX+"ProblemsView");
        Config.dynEnabled = false;
    }

    @Test
    void it_should_not_be_dynComposite_if_not_forced() {
        Config.useEquo(Composite.class);

        Composite comp = new Composite(shell(), SWT.NONE);

        assertThat(comp.getImpl())
                .isNotInstanceOf(DynComposite.class)
                .isInstanceOf(DartComposite.class);
    }

    @Test
    void it_should_be_dynComposite_when_forced_on_parent() {
        Shell shell = shell();
        when(shell.getData(Config.PROPERTY_PREFIX+"Composite")).thenReturn("dyn");

        Composite comp = new Composite(shell, SWT.NONE);
        assertThat(comp.getImpl()).isInstanceOf(DynComposite.class);
    }

    @Test
    void dynComposite_should_convert_on_setData() {
        Config.useEquo(Composite.class);
        Shell shell = shell();
        when(shell.getData(Config.PROPERTY_PREFIX+"Composite")).thenReturn("dyn");

        Composite comp = new Composite(shell, SWT.NONE);
        assertThat(comp.getImpl()).isInstanceOf(DynComposite.class);

        comp.setData("modelElement", "ProblemsView");

        assertThat(comp.getImpl()).isInstanceOf(DartComposite.class);
    }

    @Test
    void dynComposite_should_convert_on_setData_to_dart_and_keep_state() {
        System.setProperty(Config.PROPERTY_PREFIX+"ProblemsView", Config.Impl.equo.name());
        Shell shell = shell();
        when(shell.getData(Config.PROPERTY_PREFIX+"Composite")).thenReturn("dyn");

        Composite comp = new Composite(shell, SWT.NONE);
        assertThat(comp.getImpl()).isInstanceOf(DynComposite.class);
        FillLayout layout = new FillLayout();
        comp.setLayout(layout);
        comp.setSize(37, 55);

        assertThat(comp.getData()).isNull();
        comp.setData("modelElement", "ProblemsView");

        assertThat(comp.getImpl()).isInstanceOf(DartComposite.class);
        assertThat(comp.getLayout()).isEqualTo(layout);
        assertThat(comp.getSize()).isEqualTo(new Point(37,55));
        assertThat(comp.getData("modelElement")).isEqualTo("ProblemsView");
        assertThat(comp.getData()).isNull();
    }

//    @Test
//    void dynComposite_should_convert_on_computeSize_to_swt_and_keep_state() {
//        MockedConstruction<SwtComposite> mockedSwt = Mockito.mockConstruction(SwtComposite.class,
//                withSettings().defaultAnswer(CALLS_REAL_METHODS), (mock, context) -> {
//                    // context.arguments() has the constructor args used
////                    doReturn(someValue).when(mock).methodToStub();
//                } );
//
//        Shell shell = shell();
//        when(shell.getData(Config.PROPERTY_PREFIX+"Composite")).thenReturn("dyn");
//
//        Composite comp = new Composite(shell, SWT.NONE);
//        assertThat(comp.getImpl()).isInstanceOf(DynComposite.class);
//        GridData layoutData = new GridData();
//        comp.setLayoutData(layoutData);
//        comp.setSize(56, 80);
//
//        comp.computeSize(SWT.DEFAULT, SWT.DEFAULT);
//
////        assertThat(comp.getImpl()).isInstanceOf(SwtComposite.class);
////        assertThat(mockedSwt.constructed()).hasSize(1);
////        mockedSwt.constructed().get(0)
//        assertThat(comp.getLayoutData()).isEqualTo(layoutData);
//        assertThat(comp.getSize()).isEqualTo(new Point(56,80));
////        mockedSwt.close();
//    }
}
