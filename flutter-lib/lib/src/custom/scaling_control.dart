import 'package:flutter/material.dart';

import '../impl/widget_config.dart';
import '../theme/theme_extensions/scaling_scale_theme_extension.dart';

class ScalingScaleControl extends StatefulWidget {
  const ScalingScaleControl({super.key});

  @override
  State<ScalingScaleControl> createState() => _ScalingScaleControlState();
}

class _ScalingScaleControlState extends State<ScalingScaleControl> {
  double _scale = 1.0;

  @override
  void initState() {
    super.initState();
    _scale = appScaleNotifier.value;
    appScaleNotifier.addListener(_onExternalScaleChange);
  }

  @override
  void dispose() {
    appScaleNotifier.removeListener(_onExternalScaleChange);
    super.dispose();
  }

  void _onExternalScaleChange() {
    final v = appScaleNotifier.value;
    if (v != _scale) setState(() => _scale = v);
  }

  void _onScaleChanged(double value) {
    setState(() => _scale = value);
  }

  void _onScaleChangeEnd(double value) {
    setState(() => _scale = value);
    appScaleNotifier.value = value;
  }

  void _resetScale() {
    const reset = 1.0;
    setState(() => _scale = reset);
    appScaleNotifier.value = reset;
  }

  @override
  Widget build(BuildContext context) {
    final ext = Theme.of(context).extension<ScalingScaleThemeExtension>();
    if (ext == null) return const SizedBox.shrink();

    final percentText = '${(_scale * 100).round()}%';

    return Material(
      type: MaterialType.transparency,
      child: Row(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.center,
        children: [
          SizedBox(
            width: ext.sliderWidth,
            child: SliderTheme(
              data: SliderTheme.of(context).copyWith(
                trackHeight: ext.sliderTrackHeight,
                thumbShape: RoundSliderThumbShape(
                  enabledThumbRadius: ext.thumbRadius,
                ),
                overlayShape: RoundSliderOverlayShape(
                  overlayRadius: ext.overlayRadius,
                ),
              ),
              child: Slider(
                value: _scale,
                min: ext.sliderMin,
                max: ext.sliderMax,
                onChanged: _onScaleChanged,
                onChangeEnd: _onScaleChangeEnd,
              ),
            ),
          ),
          Tooltip(
            message: ext.resetTooltip,
            child: GestureDetector(
              onDoubleTap: _resetScale,
              child: Text(
                percentText,
                style: TextStyle(
                  fontSize: ext.labelTextSize,
                  color: ext.labelTextColor,
                ),
              ),
            ),
          ),
          SizedBox(width: ext.trailingGap),
        ],
      ),
    );
  }
}
