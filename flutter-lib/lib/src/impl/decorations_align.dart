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

  static DecorationsAlign? fromJson(String? s) => s != null ? fromString(s) : null;
  static String? toJson(DecorationsAlign? a) => a?.name;

  bool get isVertical => this == vleft || this == vright;
  bool get isAtStart => this == vleft || this == hleft;
}
