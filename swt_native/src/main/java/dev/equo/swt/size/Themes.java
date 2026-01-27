package dev.equo.swt.size;

public class Themes {

    static Theme theme = Theme.NonDefault;

    public enum Theme {

        NonDefault(ButtonTheme.getNonDefaultTheme(), LabelTheme.getNonDefaultTheme(), ComboTheme.getNonDefaultTheme(), CComboTheme.getNonDefaultTheme(), TextTheme.getNonDefaultTheme(), ProgressBarTheme.getNonDefaultTheme(), CLabelTheme.getNonDefaultTheme(), LinkTheme.getNonDefaultTheme()), Default(ButtonTheme.getDefaultTheme(), LabelTheme.getDefaultTheme(), ComboTheme.getDefaultTheme(), CComboTheme.getDefaultTheme(), TextTheme.getDefaultTheme(), ProgressBarTheme.getDefaultTheme(), CLabelTheme.getDefaultTheme(), LinkTheme.getDefaultTheme());

        Theme(ButtonTheme button, LabelTheme label, ComboTheme combo, CComboTheme cCombo, TextTheme text, ProgressBarTheme progressBar, CLabelTheme cLabel, LinkTheme link) {
            this.button = button;
            this.label = label;
            this.combo = combo;
            this.cCombo = cCombo;
            this.text = text;
            this.progressBar = progressBar;
            this.cLabel = cLabel;
            this.link = link;
        }

        public final ButtonTheme button;

        public final LabelTheme label;

        public final ComboTheme combo;

        public final CComboTheme cCombo;

        public final TextTheme text;

        public final ProgressBarTheme progressBar;

        public final CLabelTheme cLabel;

        public final LinkTheme link;
    }

    public static Theme getTheme() {
        return theme;
    }
}
