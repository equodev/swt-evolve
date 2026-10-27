/// The order a Tree's or a Table's columns are shown in, as SWT's `getColumnOrder()` names it.
library;

/// The column indexes in the order they are shown: [order] when it names each of the [count] columns
/// once, the creation order otherwise (no order set, or one sent for a different set of columns).
List<int> columnDisplayOrder(List<int>? order, int count) {
  if (order != null && order.length == count) {
    final seen = List<bool>.filled(count, false);
    var valid = true;
    for (final index in order) {
      if (index < 0 || index >= count || seen[index]) {
        valid = false;
        break;
      }
      seen[index] = true;
    }
    if (valid) return order;
  }
  return List<int>.generate(count, (i) => i);
}

/// The left edge of every column, by column index, with the columns of [widths] shown in [order].
List<double> columnLefts(List<double> widths, List<int> order) {
  final lefts = List<double>.filled(widths.length, 0.0);
  var left = 0.0;
  for (final index in order) {
    if (index < 0 || index >= widths.length) continue;
    lefts[index] = left;
    left += widths[index];
  }
  return lefts;
}

/// The place a header cell dragged to [x] is dropped at: the display position whose column spans [x],
/// the first or the last one past either end.
int dropDisplayIndex(List<double> widths, List<int> order, double x) {
  var left = 0.0;
  for (var position = 0; position < order.length; position++) {
    final index = order[position];
    final width = index >= 0 && index < widths.length ? widths[index] : 0.0;
    if (x < left + width) return position;
    left += width;
  }
  return order.isEmpty ? 0 : order.length - 1;
}

/// [order] with the column shown at [from] moved to [to].
List<int> moveColumn(List<int> order, int from, int to) {
  final result = List<int>.of(order);
  final index = result.removeAt(from);
  result.insert(to, index);
  return result;
}
