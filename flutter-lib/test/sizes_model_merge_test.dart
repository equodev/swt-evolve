import 'package:flutter_test/flutter_test.dart';

import '../tool/measure.dart';

/// A per-widget measurement run (`measure_button.dart`) only measures its own widget, so writing
/// what it derived over the whole model would reduce it to that widget — and silently take every
/// other widget's Java out of the check that reads it back.
void main() {
  Map<String, dynamic> model({
    required List<String> widgets,
    required Map<String, Map<String, dynamic>> themes,
  }) => {
    'version': sizesModelVersion,
    'widgets': {
      for (final w in widgets)
        'org.eclipse.swt.widgets.$w': {
          'cases': [
            {
              'name': '${w}_NONE_only',
              'style': 'NONE',
              'width': 1.0,
              'height': 1.0,
              'useFontTheme': false,
              'expectedComponents': const {'text': ''},
              'discoveredComponents': const {},
            },
          ],
        },
    },
    'themeCaseFqnByWidget': {
      for (final w in widgets) w: 'org.eclipse.swt.widgets.$w',
    },
    'themes': themes,
    'chrome': const {},
  };

  test('a run that measured one widget keeps every other widget', () {
    final merged = WidgetMeasurer().mergeSizesModel(
      model(
        widgets: ['Button', 'Label'],
        themes: {
          'Default': {
            'Button:PUSH': {'fontSize': 12},
            'Label:LEFT': {'fontSize': 12},
          },
        },
      ),
      model(
        widgets: ['Button'],
        themes: {
          'Default': {
            'Button:PUSH': {'fontSize': 14},
          },
        },
      ),
    );

    expect((merged['widgets'] as Map).keys, {
      'org.eclipse.swt.widgets.Button',
      'org.eclipse.swt.widgets.Label',
    });
    expect(
      ((merged['themes'] as Map)['Default'] as Map)['Button:PUSH'],
      {'fontSize': 14},
      reason: 'the measured widget takes the fresh value',
    );
    expect(
      ((merged['themes'] as Map)['Default'] as Map)['Label:LEFT'],
      {'fontSize': 12},
      reason: 'a widget this run did not measure keeps what was committed',
    );
  });

  test('a style the re-measured widget no longer has is dropped', () {
    final merged = WidgetMeasurer().mergeSizesModel(
      model(
        widgets: ['Button'],
        themes: {
          'Default': {
            'Button:PUSH': {'fontSize': 12},
            'Button:GONE': {'fontSize': 12},
          },
        },
      ),
      model(
        widgets: ['Button'],
        themes: {
          'Default': {
            'Button:PUSH': {'fontSize': 14},
          },
        },
      ),
    );

    expect(((merged['themes'] as Map)['Default'] as Map).keys, {'Button:PUSH'});
  });

  test('a model from an older shape is replaced rather than merged', () {
    final fresh = model(widgets: ['Button'], themes: const {});
    final merged = WidgetMeasurer().mergeSizesModel({
      'version': sizesModelVersion - 1,
      'widgets': {'org.eclipse.swt.widgets.Label': const {}},
      'themeCaseFqnByWidget': const {},
      'themes': const {},
      'chrome': const {},
    }, fresh);

    expect(merged, same(fresh));
  });
}
