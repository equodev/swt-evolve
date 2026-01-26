package dev.equo.swt.size;

public class Themes {

    static Theme theme = Theme.NonDefault;

    public enum Theme {

        NonDefault(ButtonTheme.getNonDefaultTheme(), LabelTheme.getNonDefaultTheme(), ComboTheme.getNonDefaultTheme(), CComboTheme.getNonDefaultTheme(), TextTheme.getNonDefaultTheme(), ProgressBarTheme.getNonDefaultTheme()), Default(ButtonTheme.getDefaultTheme(), LabelTheme.getDefaultTheme(), ComboTheme.getDefaultTheme(), CComboTheme.getDefaultTheme(), TextTheme.getDefaultTheme(), ProgressBarTheme.getDefaultTheme());

        Theme(ButtonTheme button, LabelTheme label, ComboTheme combo, CComboTheme cCombo, TextTheme text, ProgressBarTheme progressBar) {
            this.button = button;
            this.label = label;
            this.combo = combo;
            this.cCombo = cCombo;
            this.text = text;
            this.progressBar = progressBar;
        }

        public final ButtonTheme button;

        public final LabelTheme label;

        public final ComboTheme combo;

        public final CComboTheme cCombo;

        public final TextTheme text;

        public final ProgressBarTheme progressBar;
    }

    public static Theme getTheme() {
        return theme;
    }
}
