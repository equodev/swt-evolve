import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import '../comm/comm.dart';
import '../gen/colordialog.dart';
import '../gen/rgb.dart';
import '../theme/theme_extensions/button_theme_extension.dart';
import '../theme/theme_extensions/colordialog_theme_extension.dart';
import 'utils/pointer.dart';

/// Sentinel the Java side (`DartColorDialog.open`) reads as "the user cancelled".
const int cancelledColor = -1;

int packRgb(Color color) =>
    ((color.r * 255).round() << 16) |
    ((color.g * 255).round() << 8) |
    (color.b * 255).round();

Color _fromVRgb(VRGB rgb) => Color.fromARGB(255, rgb.red, rgb.green, rgb.blue);

void showColorDialog(BuildContext context, VColorDialog value, int dialogId) {
  final title = (value.text?.isNotEmpty ?? false) ? value.text! : 'Color';
  showDialog<void>(
    context: context,
    barrierDismissible: false,
    builder: (ctx) => pointerInterceptor(ColorDialogContent(
      title: title,
      initial: value.rgb == null ? const Color(0xFFFFFFFF) : _fromVRgb(value.rgb!),
      customColors: value.rgbs?.map(_fromVRgb).toList() ?? const <Color>[],
      onResult: (packed) {
        Navigator.pop(ctx);
        EquoCommService.sendPayload('ColorDialog/$dialogId/close', '$packed');
      },
    )),
  );
}

/// The 16 basic colours the platform colour dialogs offer as one-click swatches.
const List<Color> _basicColors = [
  Color(0xFF000000), Color(0xFF808080), Color(0xFFC0C0C0), Color(0xFFFFFFFF),
  Color(0xFF800000), Color(0xFFFF0000), Color(0xFF808000), Color(0xFFFFFF00),
  Color(0xFF008000), Color(0xFF00FF00), Color(0xFF008080), Color(0xFF00FFFF),
  Color(0xFF000080), Color(0xFF0000FF), Color(0xFF800080), Color(0xFFFF00FF),
];

/// The picker itself. Transport-free on purpose: it reports the picked colour
/// (packed 0xRRGGBB, or [cancelledColor]) through [onResult], so a widget test
/// can drive it without a live comm channel.
class ColorDialogContent extends StatefulWidget {
  final String title;
  final Color initial;
  final List<Color> customColors;
  final ValueChanged<int> onResult;

  const ColorDialogContent({
    super.key,
    required this.title,
    required this.initial,
    required this.customColors,
    required this.onResult,
  });

  @override
  State<ColorDialogContent> createState() => _ColorDialogState();
}

class _ColorDialogState extends State<ColorDialogContent> {
  late HSVColor _hsv;
  late final TextEditingController _hexController;

  Color get _color => _hsv.toColor();

  @override
  void initState() {
    super.initState();
    _hsv = HSVColor.fromColor(widget.initial);
    _hexController = TextEditingController(text: _hexOf(widget.initial));
  }

  @override
  void dispose() {
    _hexController.dispose();
    super.dispose();
  }

  static String _hexOf(Color c) =>
      packRgb(c).toRadixString(16).padLeft(6, '0').toUpperCase();

  void _setColor(Color color) {
    setState(() {
      // A fully black/white colour has no meaningful hue of its own; keep the one
      // already on the wheel so dragging value back up doesn't jump to red.
      final next = HSVColor.fromColor(color);
      _hsv = next.saturation == 0 && next.value == 0
          ? next.withHue(_hsv.hue)
          : next;
      _hexController.text = _hexOf(color);
    });
  }

  void _setHsv(HSVColor hsv) {
    setState(() {
      _hsv = hsv;
      _hexController.text = _hexOf(hsv.toColor());
    });
  }

  void _close(int packed) => widget.onResult(packed);

  void _applyHex(String text) {
    final cleaned = text.replaceAll('#', '').trim();
    if (cleaned.length != 6) return;
    final parsed = int.tryParse(cleaned, radix: 16);
    if (parsed == null) return;
    _setColor(Color(0xFF000000 | parsed));
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context).extension<ColorDialogThemeExtension>()!;
    final btnTheme = Theme.of(context).extension<ButtonThemeExtension>()!;

    return Dialog(
      backgroundColor: theme.backgroundColor,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(theme.borderRadius),
      ),
      child: CallbackShortcuts(
        bindings: {
          const SingleActivator(LogicalKeyboardKey.escape): () => _close(cancelledColor),
        },
        child: Focus(
          autofocus: true,
          child: ConstrainedBox(
            constraints: BoxConstraints(maxWidth: theme.maxWidth),
            child: Padding(
              padding: theme.padding,
              child: Column(
                mainAxisSize: MainAxisSize.min,
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(widget.title, style: theme.titleStyle ?? const TextStyle()),
                  SizedBox(height: theme.titleContentSpacing),
                  SizedBox(
                    height: theme.saturationValueHeight,
                    child: _SaturationValueArea(
                      hsv: _hsv,
                      theme: theme,
                      onChanged: _setHsv,
                    ),
                  ),
                  SizedBox(height: theme.sectionSpacing),
                  _HueSlider(hsv: _hsv, theme: theme, onChanged: _setHsv),
                  SizedBox(height: theme.sectionSpacing),
                  _swatches(theme),
                  SizedBox(height: theme.sectionSpacing),
                  _previewAndHex(theme),
                  SizedBox(height: theme.contentButtonsSpacing),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.end,
                    children: [
                      _DialogButton(
                        label: 'Cancel',
                        isPrimary: false,
                        btnTheme: btnTheme,
                        onPressed: () => _close(cancelledColor),
                      ),
                      SizedBox(width: theme.buttonSpacing),
                      _DialogButton(
                        label: 'OK',
                        isPrimary: true,
                        btnTheme: btnTheme,
                        onPressed: () => _close(packRgb(_color)),
                      ),
                    ],
                  ),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }

  Widget _swatches(ColorDialogThemeExtension theme) {
    final colors = [..._basicColors, ...widget.customColors];
    return Wrap(
      spacing: theme.swatchSpacing,
      runSpacing: theme.swatchSpacing,
      children: [
        for (final c in colors)
          _Swatch(
            key: ValueKey('color-swatch-${packRgb(c)}'),
            color: c,
            selected: packRgb(c) == packRgb(_color),
            theme: theme,
            onTap: () => _setColor(c),
          ),
      ],
    );
  }

  Widget _previewAndHex(ColorDialogThemeExtension theme) {
    final labelStyle = theme.labelStyle ?? const TextStyle();
    return Row(
      children: [
        Container(
          width: theme.previewWidth,
          height: theme.previewHeight,
          decoration: BoxDecoration(
            color: _color,
            border: Border.all(color: theme.previewBorderColor),
            borderRadius: BorderRadius.circular(theme.previewBorderRadius),
          ),
        ),
        SizedBox(width: theme.previewSpacing),
        Text('#', style: labelStyle),
        SizedBox(
          width: theme.hexFieldWidth,
          child: TextField(
            controller: _hexController,
            maxLength: 6,
            style: labelStyle,
            decoration: const InputDecoration(counterText: '', isDense: true),
            onChanged: _applyHex,
            onSubmitted: _applyHex,
          ),
        ),
        Expanded(
          child: Text(
            'R ${(_color.r * 255).round()}  '
            'G ${(_color.g * 255).round()}  '
            'B ${(_color.b * 255).round()}',
            textAlign: TextAlign.right,
            overflow: TextOverflow.ellipsis,
            style: labelStyle,
          ),
        ),
      ],
    );
  }
}

class _Swatch extends StatelessWidget {
  final Color color;
  final bool selected;
  final ColorDialogThemeExtension theme;
  final VoidCallback onTap;

  const _Swatch({
    super.key,
    required this.color,
    required this.selected,
    required this.theme,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        width: theme.swatchSize,
        height: theme.swatchSize,
        decoration: BoxDecoration(
          color: color,
          border: Border.all(
            color: selected ? theme.selectedSwatchBorderColor : theme.swatchBorderColor,
            width: selected ? theme.selectedSwatchBorderWidth : theme.swatchBorderWidth,
          ),
          borderRadius: BorderRadius.circular(theme.swatchBorderRadius),
        ),
      ),
    );
  }
}

/// Saturation (x) / value (y) plane for the current hue.
class _SaturationValueArea extends StatelessWidget {
  final HSVColor hsv;
  final ColorDialogThemeExtension theme;
  final ValueChanged<HSVColor> onChanged;

  const _SaturationValueArea({
    required this.hsv,
    required this.theme,
    required this.onChanged,
  });

  @override
  Widget build(BuildContext context) {
    return LayoutBuilder(
      builder: (context, constraints) {
        void handle(Offset local) {
          final s = (local.dx / constraints.maxWidth).clamp(0.0, 1.0);
          final v = 1.0 - (local.dy / constraints.maxHeight).clamp(0.0, 1.0);
          onChanged(hsv.withSaturation(s).withValue(v));
        }

        return GestureDetector(
          onPanDown: (d) => handle(d.localPosition),
          onPanUpdate: (d) => handle(d.localPosition),
          child: CustomPaint(
            size: Size(constraints.maxWidth, constraints.maxHeight),
            painter: _SaturationValuePainter(hsv, theme),
          ),
        );
      },
    );
  }
}

class _SaturationValuePainter extends CustomPainter {
  final HSVColor hsv;

  final ColorDialogThemeExtension theme;

  _SaturationValuePainter(this.hsv, this.theme);

  @override
  void paint(Canvas canvas, Size size) {
    final rect = Offset.zero & size;
    canvas.drawRect(
      rect,
      Paint()
        ..shader = LinearGradient(
          colors: [Colors.white, HSVColor.fromAHSV(1, hsv.hue, 1, 1).toColor()],
        ).createShader(rect),
    );
    canvas.drawRect(
      rect,
      Paint()
        ..shader = const LinearGradient(
          begin: Alignment.topCenter,
          end: Alignment.bottomCenter,
          colors: [Colors.transparent, Colors.black],
        ).createShader(rect),
    );
    final center = Offset(hsv.saturation * size.width, (1 - hsv.value) * size.height);
    canvas.drawCircle(
        center,
        theme.thumbRadius,
        Paint()
          ..color = theme.thumbColor
          ..style = PaintingStyle.stroke
          ..strokeWidth = theme.thumbStrokeWidth);
    canvas.drawCircle(
        center,
        theme.thumbOutlineRadius,
        Paint()
          ..color = theme.thumbOutlineColor
          ..style = PaintingStyle.stroke);
  }

  @override
  bool shouldRepaint(_SaturationValuePainter old) => old.hsv != hsv || old.theme != theme;
}

class _HueSlider extends StatelessWidget {
  final HSVColor hsv;
  final ColorDialogThemeExtension theme;
  final ValueChanged<HSVColor> onChanged;

  const _HueSlider({
    required this.hsv,
    required this.theme,
    required this.onChanged,
  });

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      height: theme.hueSliderHeight,
      child: LayoutBuilder(
        builder: (context, constraints) {
          void handle(Offset local) {
            final hue = (local.dx / constraints.maxWidth).clamp(0.0, 1.0) * 360.0;
            onChanged(hsv.withHue(hue));
          }

          return GestureDetector(
            onPanDown: (d) => handle(d.localPosition),
            onPanUpdate: (d) => handle(d.localPosition),
            child: CustomPaint(
              size: Size(constraints.maxWidth, theme.hueSliderHeight),
              painter: _HuePainter(hsv.hue, theme),
            ),
          );
        },
      ),
    );
  }
}

class _HuePainter extends CustomPainter {
  final double hue;
  final ColorDialogThemeExtension theme;

  _HuePainter(this.hue, this.theme);

  @override
  void paint(Canvas canvas, Size size) {
    final rect = Offset.zero & size;
    canvas.drawRect(
      rect,
      Paint()
        ..shader = LinearGradient(
          colors: [
            for (var i = 0; i <= 6; i++) HSVColor.fromAHSV(1, i * 60.0 % 360, 1, 1).toColor(),
          ],
        ).createShader(rect),
    );
    final x = (hue / 360.0) * size.width;
    canvas.drawRect(
      Rect.fromLTWH(x - theme.hueThumbWidth / 2, 0, theme.hueThumbWidth, size.height),
      Paint()
        ..color = theme.hueThumbColor
        ..style = PaintingStyle.stroke
        ..strokeWidth = theme.hueThumbStrokeWidth,
    );
  }

  @override
  bool shouldRepaint(_HuePainter old) => old.hue != hue || old.theme != theme;
}

class _DialogButton extends StatefulWidget {
  final String label;
  final bool isPrimary;
  final ButtonThemeExtension btnTheme;
  final VoidCallback onPressed;

  const _DialogButton({
    required this.label,
    required this.isPrimary,
    required this.btnTheme,
    required this.onPressed,
  });

  @override
  State<_DialogButton> createState() => _DialogButtonState();
}

class _DialogButtonState extends State<_DialogButton> {
  bool _isHovering = false;

  @override
  Widget build(BuildContext context) {
    final theme = widget.btnTheme;
    final bgColor = _isHovering
        ? (widget.isPrimary ? theme.pushButtonHoverColor : theme.secondaryButtonHoverColor)
        : (widget.isPrimary ? theme.pushButtonColor : theme.secondaryButtonColor);
    final textColor =
        widget.isPrimary ? theme.pushButtonTextColor : theme.secondaryButtonTextColor;
    final borderColor =
        widget.isPrimary ? theme.pushButtonBorderColor : theme.secondaryButtonBorderColor;

    return MouseRegion(
      onEnter: (_) => setState(() => _isHovering = true),
      onExit: (_) => setState(() => _isHovering = false),
      child: Material(
        color: bgColor,
        borderRadius: BorderRadius.circular(theme.pushButtonBorderRadius),
        child: InkWell(
          onTap: widget.onPressed,
          borderRadius: BorderRadius.circular(theme.pushButtonBorderRadius),
          splashColor: theme.splashColor,
          highlightColor: theme.highlightColor,
          child: Container(
            decoration: BoxDecoration(
              borderRadius: BorderRadius.circular(theme.pushButtonBorderRadius),
              border: Border.all(color: borderColor, width: theme.pushButtonBorderWidth),
            ),
            padding: theme.pushButtonPadding,
            child: Text(
              widget.label,
              style: (theme.pushButtonFontStyle ?? const TextStyle()).copyWith(color: textColor),
            ),
          ),
        ),
      ),
    );
  }
}
