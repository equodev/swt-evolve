package dev.equo.swt;

public enum DecorationsAlign {
    HLEFT, HRIGHT, VLEFT, VRIGHT;

    public static DecorationsAlign fromString(String s) {
        if (s == null) return HLEFT;
        switch (s.trim().toLowerCase()) {
            case "hright": return HRIGHT;
            case "vleft":  return VLEFT;
            case "vright": return VRIGHT;
            default:       return HLEFT;
        }
    }

    public boolean isVertical() { return this == VLEFT || this == VRIGHT; }
    public boolean isAtStart()  { return this == VLEFT || this == HLEFT; }
}
