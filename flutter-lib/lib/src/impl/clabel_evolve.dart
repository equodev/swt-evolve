import 'package:flutter/material.dart';
import '../gen/clabel.dart';
import '../gen/widget.dart';
import '../gen/image.dart';
import '../gen/swt.dart';
import '../impl/canvas_evolve.dart';
import '../impl/color_utils.dart';
import './utils/image_utils.dart';

class CLabelImpl<T extends CLabelSwt, V extends VCLabel>
    extends CanvasImpl<T, V> {

  /// Helper method to build an image widget from VImage using ImageUtils with async support
  Widget _buildImageWidget(VImage? image, bool enabled) {
    return FutureBuilder<Widget?>(
      future: ImageUtils.buildVImageAsync(
        image,
        enabled: enabled,
        constraints: null,
        useBinaryImage: true,
        renderAsIcon: false,
      ),
      builder: (context, snapshot) {
        if (snapshot.connectionState == ConnectionState.done) {
          return snapshot.data ?? const SizedBox.shrink();
        }
        // Show placeholder while loading
        return const SizedBox(
          width: 16,
          height: 16,
          child: Center(
            child: SizedBox(
              width: 12,
              height: 12,
              child: CircularProgressIndicator(strokeWidth: 2),
            ),
          ),
        );
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    // Treat "<none>" as empty text
    final rawText = state.text ?? '';
    final text = rawText == '<none>' ? '' : rawText;
    final enabled = state.enabled ?? true;
    final image = state.image;

    final alignment = _getAlignment();

    final backgroundColor = colorFromVColor(
      state.background,
      defaultColor: Colors.transparent
    );

    final textColor = colorFromVColor(
      state.foreground,
      defaultColor: Theme.of(context).textTheme.bodyMedium?.color
    );

    Widget content;

    if (image != null && text.isNotEmpty) {
      // Image + Text
      final imageWidget = _buildImageWidget(image, enabled);
      content = Row(
        mainAxisSize: MainAxisSize.min,
        mainAxisAlignment: _getMainAxisAlignment(alignment),
        crossAxisAlignment: CrossAxisAlignment.center,
        children: [
          imageWidget,
          const SizedBox(width: 4),
          Flexible(
            child: Text(
              text,
              textAlign: alignment,
              overflow: TextOverflow.ellipsis,
              style: TextStyle(
                fontSize: 12,
                color: textColor,
              ),
            ),
          ),
        ],
      );
    } else if (image != null) {
      // Only image
      content = _buildImageWidget(image, enabled);
    } else if (text.isNotEmpty) {
      // Only text
      content = Text(
        text,
        textAlign: alignment,
        overflow: TextOverflow.ellipsis,
        style: TextStyle(
          fontSize: 12,
          color: textColor,
        ),
      );
    } else {
      // Empty CLabel
      content = const SizedBox.shrink();
    }

    // Render with content
    return wrap(
      Opacity(
        opacity: enabled ? 1.0 : 0.5,
        child: Container(
          decoration: BoxDecoration(
            color: backgroundColor,
          ),
          padding: EdgeInsets.fromLTRB(
            (state.leftMargin ?? 0).toDouble(),
            (state.topMargin ?? 0).toDouble(),
            (state.rightMargin ?? 0).toDouble(),
            (state.bottomMargin ?? 0).toDouble(),
          ),
          alignment: _getContainerAlignment(alignment),
          child: content,
        ),
      ),
    );
  }

  TextAlign _getAlignment() {
    if (state.alignment != null) {
      if ((state.alignment! & SWT.CENTER) != 0) return TextAlign.center;
      if ((state.alignment! & SWT.RIGHT) != 0) return TextAlign.right;
    }
    return TextAlign.left;
  }

  MainAxisAlignment _getMainAxisAlignment(TextAlign alignment) {
    switch (alignment) {
      case TextAlign.center:
        return MainAxisAlignment.center;
      case TextAlign.right:
        return MainAxisAlignment.end;
      default:
        return MainAxisAlignment.start;
    }
  }

  Alignment _getContainerAlignment(TextAlign alignment) {
    switch (alignment) {
      case TextAlign.center:
        return Alignment.center;
      case TextAlign.right:
        return Alignment.centerRight;
      default:
        return Alignment.centerLeft;
    }
  }
}
