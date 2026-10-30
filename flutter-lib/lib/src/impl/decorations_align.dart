enum DecorationsAlign {
  hleft,
  hright,
  vleft,
  vright;

  static DecorationsAlign fromString(String? s) {
    switch (s?.trim().toLowerCase()) {
      case 'hright':
        return hright;
      case 'vleft':
        return vleft;
      case 'vright':
        return vright;
      default:
        return hleft;
    }
  }

  bool get isVertical => this == vleft || this == vright;
  bool get isAtStart => this == vleft || this == hleft;
}
