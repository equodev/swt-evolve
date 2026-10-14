package org.eclipse.swt.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.AnimatedProgress;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.StyledText;
import org.junit.jupiter.params.provider.Arguments;

import java.util.function.Function;
import java.util.stream.Stream;

/** The Dart-backed controls a parameterized test goes over: a name, and a factory given the shell to build it in. */
final class DartControlFactories {

    private DartControlFactories() {
    }

    /** One factory per Dart-backed control class that declares its own computeSize. */
    static Stream<Arguments> withOwnComputeSize() {
        return Stream.of(
                control("Button ARROW", s -> new Button(s, SWT.ARROW)),
                control("Button PUSH", s -> new Button(s, SWT.PUSH)),
                control("Combo", s -> new Combo(s, SWT.DROP_DOWN)),
                control("Composite", s -> new Composite(s, SWT.NONE)),
                control("CoolBar", s -> new CoolBar(s, SWT.NONE)),
                control("DateTime", s -> new DateTime(s, SWT.DATE)),
                control("ExpandBar", s -> new ExpandBar(s, SWT.NONE)),
                control("Label", s -> new Label(s, SWT.NONE)),
                control("Label SEPARATOR", s -> new Label(s, SWT.SEPARATOR | SWT.HORIZONTAL)),
                control("Link", s -> new Link(s, SWT.NONE)),
                control("List", s -> new List(s, SWT.SINGLE)),
                control("ProgressBar", s -> new ProgressBar(s, SWT.HORIZONTAL)),
                control("Sash", s -> new Sash(s, SWT.VERTICAL)),
                control("Scale", s -> new Scale(s, SWT.HORIZONTAL)),
                control("Shell", s -> new Shell(s.getDisplay())),
                control("Slider", s -> new Slider(s, SWT.HORIZONTAL)),
                control("Spinner", s -> new Spinner(s, SWT.NONE)),
                control("TabFolder", s -> new TabFolder(s, SWT.TOP)),
                control("Table", s -> new Table(s, SWT.NONE)),
                control("Text", s -> new Text(s, SWT.SINGLE)),
                control("ToolBar", s -> new ToolBar(s, SWT.HORIZONTAL)),
                control("Tree", s -> new Tree(s, SWT.NONE)),
                control("AnimatedProgress", s -> new AnimatedProgress(s, SWT.HORIZONTAL)),
                control("CCombo", s -> new CCombo(s, SWT.NONE)),
                control("CLabel", s -> new CLabel(s, SWT.NONE)),
                control("StyledText", s -> new StyledText(s, SWT.NONE)));
    }

    private static Arguments control(String name, Function<Shell, Control> factory) {
        return Arguments.of(name, factory);
    }
}
