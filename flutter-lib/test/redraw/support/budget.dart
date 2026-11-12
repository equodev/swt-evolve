// The most an operation may rebuild and redraw. Counts in a test are exact, so a budget is what the
// operation costs today: going over it is a regression, and coming in under it means the budget
// should come down with the change that earned it.
//
// --dart-define=REDRAW_RECORD=true prints every measured cost as a budget table to paste instead.

import 'package:flutter_test/flutter_test.dart';

import 'redraw_probe.dart';

const bool recordBudgets = bool.fromEnvironment('REDRAW_RECORD');

class Budget {
  const Budget(this.builds, this.paints, this.windows);

  final int builds;
  final int paints;

  /// Area drawn again, in windows ([RedrawCost.windows]).
  final double windows;
}

/// Checks every cost in [costs] against its budget in [budgets], all failures reported at once.
void expectWithinBudgets(List<RedrawCost> costs, Map<String, Budget> budgets) {
  if (recordBudgets) {
    final table = StringBuffer('const budgets = {\n');
    for (final c in costs) {
      table.writeln("  '${c.name}': Budget(${c.builds}, ${c.paints}, ${(c.windows + 0.005).toStringAsFixed(2)}),");
    }
    table.write('};');
    // ignore: avoid_print
    print(table);
    return;
  }
  final over = <String>[];
  final under = <String>[];
  for (final c in costs) {
    final b = budgets[c.name];
    if (b == null) {
      over.add('${c.name}: no budget (measured $c)');
      continue;
    }
    if (c.builds > b.builds || c.paints > b.paints || c.windows > b.windows + 0.005) {
      over.add('$c\n    budget: builds=${b.builds} paints=${b.paints} windows=${b.windows}\n${c.detail}');
    } else if (c.builds < b.builds * 0.8 || c.paints < b.paints * 0.8 || c.windows < b.windows - 0.05) {
      under.add('$c (budget builds=${b.builds} paints=${b.paints} windows=${b.windows})');
    }
  }
  if (under.isNotEmpty) {
    // ignore: avoid_print
    print('[redraw] under budget, lower it:\n  ${under.join('\n  ')}');
  }
  expect(over, isEmpty, reason: 'over its redraw budget:\n${over.join('\n')}');
}
