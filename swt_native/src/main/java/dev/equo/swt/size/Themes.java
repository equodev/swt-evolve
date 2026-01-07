package dev.equo.swt.size;

public class Themes {

    static Theme theme = Theme.NonDefault;

    public enum Theme {

        NonDefault(ButtonTheme.getNonDefaultTheme(), LabelTheme.getNonDefaultTheme()), Default(ButtonTheme.getDefaultTheme(), LabelTheme.getDefaultTheme());

        Theme(ButtonTheme button, LabelTheme label) {
            this.button = button;
            this.label = label;
        }

        public final ButtonTheme button;

        public final LabelTheme label;
    }

    public static Theme getTheme() {
        return theme;
    }
}
