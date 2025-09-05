extension type StyleBits(int s) {
  bool has(int bit) {
    return s.has(bit);
  }

  bool hasAny(int bit1, bit2) {
    return s.hasAny(bit1, bit2);
  }

  bool operator <(dynamic flag) {
    return switch (flag) {
      <int>[...] => flag.every(has),
      (int a, int b) => has(a) && has(b),
      (int a, int b, int c) => has(a) && has(b) && has(c),
      _ => has(flag),
    };
  }
}

extension StyleTester on int {
  bool has(int bit) {
    return this & (bit) != 0;
  }

  bool hasAny(int bit1, bit2) {
    return this & (bit1 | bit2) != 0;
  }
}
