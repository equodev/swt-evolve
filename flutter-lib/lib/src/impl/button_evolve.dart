import 'dart:async';

import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import '../gen/button.dart';
import '../gen/color.dart';
import '../gen/swt.dart';
import '../gen/widgets.dart' show WireKey;
import '../impl/composite_evolve.dart';
import '../impl/control_evolve.dart';
import '../theme/theme_extensions/button_theme_extension.dart';
import '../theme/theme_settings/button_theme_settings.dart';
import 'utils/font_utils.dart';
import 'utils/image_utils.dart';
import 'utils/kept_rebuild.dart';
import 'utils/mnemonic_text.dart';
import 'utils/text_utils.dart';
import 'utils/widget_utils.dart';
import 'widget_config.dart';
import 'utils/quiet_animations.dart';
import 'utils/cached_text.dart';
import 'utils/enter_to_the_shell.dart';

class ButtonImpl<T extends ButtonSwt, V extends VButton>
    extends ControlImpl<T, V> {
  bool _hasFocus = false;

  final FocusNode _focusNode = FocusNode(debugLabel: 'Button');

  @override
  FocusNode? get swtFocusNode => _focusNode;

  static IconData _getArrowIcon(int alignment) {
    switch (alignment) {
      case SWT.UP:
        return Icons.keyboard_arrow_up;
      case SWT.DOWN:
        return Icons.keyboard_arrow_down;
      case SWT.LEFT:
        return Icons.keyboard_arrow_left;
      case SWT.RIGHT:
        return Icons.keyboard_arrow_right;
      default:
        return Icons.keyboard_arrow_down;
    }
  }

  /// What a check box or radio draws of its selection is the box alone: a change of nothing else
  /// rebuilds only the box, which follows [_selectionTicks].
  static const _selectionOnly = {WireKey.k_Button_selection, WireKey.k_Button_grayed};
  final _selectionTicks = ValueNotifier<int>(0);

  /// The check box's tick, the same instance while it looks the same, so that a selection change
  /// does not rebuild it.
  CachedIcon? _tick;

  CachedIcon _tickOf(double size, Color color) {
    final tick = _tick;
    if (tick != null && tick.size == size && tick.color == color) return tick;
    return _tick = CachedIcon(Icons.check, size: size, color: color);
  }

  @override
  void setValue(V value) {
    if (mounted &&
        (lastChange?.isOnly(_selectionOnly) ?? false) &&
        (hasStyle(state.style, SWT.CHECK) || hasStyle(state.style, SWT.RADIO))) {
      _selectionTicks.value++;
      return;
    }
    super.setValue(value);
  }

  @override
  void dispose() {
    _selectionTicks.dispose();
    _focusNode.dispose();
    super.dispose();
  }

  /// A push button's surface: its border, padding and label, built with the button and shown by
  /// [KeptSlot] inside the InkWell. A rebuild from this button's own [setState] has marked the
  /// slot already, so the InkWell and all it builds are kept as they were and only the surface
  /// rebuilds; any other rebuild builds them anew, with a new slot.
  Widget? _surface;
  final KeptRebuild _surfaceRebuild = KeptRebuild();

  /// The enablement the surface was last built with.
  bool? _surfaceEnabled;

  @override
  void setState(VoidCallback fn) {
    super.setState(fn);
    _surfaceRebuild.mark();
    _hoverableRebuild.mark();
  }

  /// A push, toggle or arrow button draws its own surface, which stays the theme's (SWT calls
  /// setBackground a hint and Win32 ignores it on buttons), so its text stays the theme's too:
  /// the application's foreground was chosen against the application's background, and applying
  /// one without the other is what leaves white text on a light surface. Check and radio text sits
  /// on the parent's ground, which is the application's, so their foreground is honoured.
  VColor? get _ownForeground =>
      hasStyle(state.style, SWT.CHECK) || hasStyle(state.style, SWT.RADIO)
          ? state.foreground
          : null;

  /// The ground [_wrapWithSwtBackground] fills behind the button, or null when it fills none.
  Color? _swtBackground(BuildContext context) {
    if (getConfigFlags().use_swt_colors ?? false) return null;
    final defaultBg = _widgetTheme.controlBackgroundColor;
    final parentBg = ParentBackgroundScope.backgroundOf(context);
    final bg = getBackgroundColor(
      background: state.background,
      defaultColor: parentBg ?? defaultBg,
      context: context,
    );
    return bg ?? parentBg ?? defaultBg;
  }

  Widget _wrapWithSwtBackground(BuildContext context, Widget button) {
    final ground = _swtBackground(context);
    if (ground != null) {
      return Container(
        color: ground,
        child: button,
      );
    }

    // Nothing is filled behind the button: its own colour is the button's, not the ground around
    // it, and painting it into the square box under a rounded button only ever showed at the four
    // corners. The parent's ground shows through instead, which is what surrounds a button.
    // The surface itself stays the theme's -- SWT calls setBackground a hint the platform may
    // override, and Win32 ignores it on Buttons.
    return button;
  }

  /// Looked up when the theme changes rather than on every build: Theme.of localizes the theme
  /// each time it is called.
  late ButtonThemeExtension _widgetTheme;

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    _widgetTheme = Theme.of(context).extension<ButtonThemeExtension>()!;
  }

  @override
  Widget build(BuildContext context) {
    final widgetTheme = _widgetTheme;

    final enabled = state.enabled ?? false;
    final text = stripAccelerators(state.text);

    // Push and toggle buttons draw their focus ring themselves, around the surface inside the
    // bezel margin rather than around the whole frame.
    final Widget button;
    if (hasStyle(state.style, SWT.CHECK)) {
      button = _withFocusRing(widgetTheme, _buildCheckBox(context, widgetTheme, enabled, text));
    } else if (hasStyle(state.style, SWT.RADIO)) {
      button = _withFocusRing(widgetTheme, _buildRadioButton(context, widgetTheme, enabled, text));
    } else if (hasStyle(state.style, SWT.TOGGLE)) {
      button = _buildToggleButton(context, widgetTheme, enabled, text);
    } else if (hasStyle(state.style, SWT.ARROW)) {
      button = _withFocusRing(widgetTheme, _buildArrowButton(context, widgetTheme, enabled, text));
    } else {
      button = _buildPushButton(context, widgetTheme, enabled, text);
    }
    return _selectOnReleaseInside(leaveEnterToTheShell(button));
  }

  bool _primaryPressed = false;
  /// Whether the button was selected during this press, by Flutter's tap or by the keyboard.
  bool _selectedThisPress = false;

  /// A check box's or radio's box, the gap after it and its label: the part of it that is the
  /// button, as on GTK, whose check button is exactly that wide. Handed over by [_RowBox] rather than
  /// found through a GlobalKey, which schedules builds during the build that changes enablement.
  RenderBox? _indicatorRow;

  bool get _selectsOverIndicatorRow => hasStyle(state.style, SWT.CHECK) || hasStyle(state.style, SWT.RADIO);

  /// Whether [e] is over the button: anywhere in its bounds, or for a check box or radio over its
  /// indicator row's content -- box, gap and label, not the row's empty end -- at any height of the
  /// row, above or below a label shorter than the box too.
  bool _isOverButton(PointerEvent e) {
    if (!_selectsOverIndicatorRow) return isOverSelf(e);
    final row = _indicatorRow;
    if (row == null || !row.attached || !row.hasSize) return false;
    final local = row.globalToLocal(e.position);
    if (local.dy < 0 || local.dy >= row.size.height) return false;
    return row.hitTest(BoxHitTestResult(), position: Offset(local.dx, row.size.height / 2));
  }

  /// A native button is selected by a primary release over it after a press on it, however far the
  /// pointer travelled while held; Flutter's tap gives up once it moves past its slop.
  Widget _selectOnReleaseInside(Widget button) => Listener(
        onPointerDown: (e) {
          _primaryPressed = e.buttons == kPrimaryButton && (state.enabled ?? false)
              && _isOverButton(e);
          _selectedThisPress = false;
        },
        onPointerUp: (e) {
          if (!_primaryPressed) return;
          _primaryPressed = false;
          if (!_isOverButton(e)) return;
          // After the gesture arena, which delivers a tap within its slop on this same release.
          scheduleMicrotask(() {
            if (mounted && !_selectedThisPress && (state.enabled ?? false)) _onPressed();
          });
        },
        onPointerCancel: (_) => _primaryPressed = false,
        child: button,
      );


  // Always a DecoratedBox, so gaining focus never remounts the button and drops that focus.
  Widget _withFocusRing(ButtonThemeExtension widgetTheme, Widget button) => DecoratedBox(
        position: DecorationPosition.foreground,
        decoration: getButtonFocusRingDecoration(widgetTheme, focused: _hasFocus),
        child: button,
      );

  /// The transparent margin Java reserved on each side of a bezelled button's frame (see
  /// `push_button_margin`). The surface is painted inside it; the frame itself -- semantics,
  /// GC overlay -- still spans the bounds.
  double get _bezelMargin {
    final bezelled = (hasStyle(state.style, SWT.PUSH) || hasStyle(state.style, SWT.TOGGLE)) &&
        !hasStyle(state.style, SWT.FLAT) &&
        !hasStyle(state.style, SWT.WRAP);
    return bezelled ? (getConfigFlags().push_button_margin ?? 0).toDouble() : 0;
  }

  BoxConstraints? _surfaceConstraints(double margin) {
    final constraints = getConstraintsFromBounds(state.bounds);
    if (constraints == null || margin == 0) return constraints;
    final width = (constraints.maxWidth - 2 * margin).clamp(0.0, double.infinity);
    return constraints.copyWith(minWidth: width, maxWidth: width);
  }

  Widget _insideBezelMargin(
    ButtonThemeExtension widgetTheme,
    Widget surface,
    double margin,
  ) {
    final ringed = _withFocusRing(widgetTheme, surface);
    return margin == 0
        ? ringed
        : Padding(padding: EdgeInsets.symmetric(horizontal: margin), child: ringed);
  }

  Widget _buildPushButton(
    BuildContext context,
    ButtonThemeExtension widgetTheme,
    bool enabled,
    String? text,
  ) {
    final isPressed = state.selection ?? false;
    final hasFlat = hasStyle(state.style, SWT.FLAT);
    final isPrimary = state.primary ?? false;
    final margin = _bezelMargin;
    final constraints = _surfaceConstraints(margin);
    final hasValidBounds = hasBounds(state.bounds);

    final baseBackgroundColor = getButtonBackgroundColor(
      state,
      widgetTheme,
      null,
      enabled: enabled,
      isPrimary: isPrimary,
    );

    final hoverBackgroundColor = getPushButtonHoverBackgroundColor(
      state,
      widgetTheme,
      enabled: enabled,
      isPrimary: isPrimary,
    );

    final pressedBackgroundColor = getPushButtonPressedColor(widgetTheme, isPrimary);

    final borderColor = getPushButtonBorderColor(
      state,
      widgetTheme,
      isPrimary: isPrimary,
      enabled: enabled,
      primaryBorderDisabled: widgetTheme.pushButtonDisabledColor,
      secondaryBorderDisabled: widgetTheme.pushButtonDisabledColor,
    );

    final enablementChanged = _surfaceEnabled != null && _surfaceEnabled != enabled;
    _surfaceEnabled = enabled;
    _surface = QuietAnimatedContainer(
      // The border changes at once with the enablement, as the fill, the label and the control's
      // opacity do.
      duration: enablementChanged ? Duration.zero : widgetTheme.buttonPressDelay,
      constraints: constraints,
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(
          widgetTheme.pushButtonBorderRadius,
        ),
        border: Border.all(
          color: borderColor,
          width: widgetTheme.pushButtonBorderWidth,
        ),
      ),
      padding: _fitContentPadding(
        widgetTheme.pushButtonPadding,
        horizontalRoom: hasValidBounds
            ? state.bounds!.width.toDouble() -
                  margin * 2 -
                  widgetTheme.pushButtonBorderWidth * 2 -
                  _pushContentWidth(
                    context,
                    text,
                    widgetTheme.pushButtonFontStyle,
                    getPushButtonTextColor(widgetTheme, isPrimary, enabled),
                  )
            : null,
      ),
      child: _buildButtonContent(
        context,
        text,
        widgetTheme,
        enabled,
        getPushButtonTextColor(widgetTheme, isPrimary, enabled),
        widgetTheme.pushButtonFontStyle,
        hasBounds: hasValidBounds,
      ),
    );

    _pushLook = (
      base: baseBackgroundColor,
      hover: hoverBackgroundColor,
      pressed: pressedBackgroundColor,
      isPressed: isPressed,
      enabled: enabled,
      keepsInk: _surfaceRebuild.marked,
      surface: KeptSlot(_surfaceRebuild, () => _surface),
    );

    // The chain around the hoverable surface changes with none of the above, so it is kept while
    // what it is built from is; the surface then rebuilds on its own (see [_hoverableRebuild]), as it does
    // from this button's own setState. Any other rebuild builds it anew.
    final key = (
      _swtBackground(context),
      _hasFocus,
      margin,
      widgetTheme,
      getConfigFlags(),
    );
    var chain = _pushChain;
    if (chain == null || !_hoverableRebuild.marked || key != _pushChainKey) {
      _pushChainKey = key;
      chain = _pushChain = _wrapWithSwtBackground(
          context, _insideBezelMargin(widgetTheme, _HoverableButton(widgetTheme: widgetTheme), margin));
    }
    return wrap(chain);
  }

  /// What a push button's [_HoverableButton] draws that changes with the state, read by it when it
  /// builds.
  _PushLook? _pushLook;
  Widget? _pushChain;
  Object? _pushChainKey;
  final KeptRebuild _hoverableRebuild = KeptRebuild();

  /// The width the content row wants: the label in the style it will actually
  /// be painted in, plus the image cell the row puts before it.
  double _pushContentWidth(
    BuildContext context,
    String? text,
    TextStyle? baseTextStyle,
    Color textColor,
  ) {
    double width = 0;
    final imageData = state.image?.imageData;
    if (imageData != null) {
      width += imageData.width?.toDouble() ?? 0;
    }
    if (text != null && text.isNotEmpty) {
      // The colour does not change the width.
      final key = (text, state.font, baseTextStyle, getConfigFlags(), textMeasureEpoch);
      if (key != _textWidthKey) {
        _textWidthKey = key;
        _textWidth = singleLineTextWidth(text, _labelStyle(context, textColor, baseTextStyle));
      }
      width += _textWidth;
    }
    return width;
  }

  Object? _textWidthKey;
  double _textWidth = 0;

  /// The label's style, the same instance while what it is made from is: a style compared with an
  /// equal one that is not the same object, or hashed as a cache key, is compared property by
  /// property. The last two are kept, so that a button toggling its enablement finds both.
  TextStyle _labelStyle(BuildContext context, Color textColor, TextStyle? baseTextStyle) {
    final font = state.font;
    final flags = getConfigFlags();
    // From the application's own font, the style depends on where the button is.
    if ((flags.use_swt_fonts ?? false) && font != null) {
      return getTextStyle(context: context, font: font, textColor: textColor, baseTextStyle: baseTextStyle);
    }
    final key = (font, textColor, baseTextStyle, flags);
    for (final (kept, style) in _labelStyles) {
      if (kept == key) return style;
    }
    final style = getTextStyle(context: context, font: font, textColor: textColor, baseTextStyle: baseTextStyle);
    if (_labelStyles.length >= 2) _labelStyles.removeAt(0);
    _labelStyles.add((key, style));
    return style;
  }

  final _labelStyles = <(Object, TextStyle)>[];

  /// The padding is a fixed inset, so a width an application pins below the
  /// preferred one leaves the label a viewport narrower than the text and it
  /// renders ellipsized. Applications size a button from their own font through
  /// GC.textExtent, while the label is painted in the (wider) theme font, so
  /// that pinned width is routinely a few pixels short. The label keeps its
  /// full extent and the padding absorbs the deficit, down to none at all.
  ///
  /// [horizontalRoom] is what the pinned width leaves the content; null where
  /// nothing is pinned.
  EdgeInsets _fitContentPadding(
    EdgeInsets padding, {
    double? horizontalRoom,
  }) {
    if (horizontalRoom == null || horizontalRoom >= padding.horizontal) {
      return padding;
    }
    final double half = horizontalRoom > 0 ? horizontalRoom / 2 : 0;
    return padding.copyWith(left: half, right: half);
  }

  Widget _buildToggleButton(
    BuildContext context,
    ButtonThemeExtension widgetTheme,
    bool enabled,
    String? text,
  ) {
    final isSelected = state.selection ?? false;

    final backgroundColor = getButtonBackgroundColor(
      state,
      widgetTheme,
      isSelected
          ? widgetTheme.selectableButtonColor
          : widgetTheme.toggleButtonColor,
      enabled: enabled,
    );

    final margin = _bezelMargin;
    final constraints = _surfaceConstraints(margin);
    final hasValidBounds = hasBounds(state.bounds);

    Widget button = Material(
      color: backgroundColor,
      borderRadius: BorderRadius.circular(widgetTheme.pushButtonBorderRadius),
      child: InkWell(
        onTap: _onPressed,
        focusNode: _focusNode,
        onFocusChange: _onFocusChange,
        borderRadius: BorderRadius.circular(widgetTheme.pushButtonBorderRadius),
        splashColor: widgetTheme.splashColor,
        highlightColor: widgetTheme.highlightColor,
        child: Container(
          constraints: constraints,
          decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(
              widgetTheme.pushButtonBorderRadius,
            ),
            border: Border.all(
              color: widgetTheme.toggleButtonBorderColor,
              width: widgetTheme.pushButtonBorderWidth,
            ),
          ),
          child: _buildButtonContent(
            context,
            text,
            widgetTheme,
            enabled,
            enabled
                ? widgetTheme.pushButtonTextColor
                : widgetTheme.disabledForegroundColor,
            widgetTheme.pushButtonFontStyle,
            hasBounds: hasValidBounds,
          ),
        ),
      ),
    );

    return wrap(_wrapWithSwtBackground(
        context, _insideBezelMargin(widgetTheme, button, margin)));
  }

  Widget _buildCheckBox(
    BuildContext context,
    ButtonThemeExtension widgetTheme,
    bool enabled,
    String? text,
  ) {
    final checkboxBackgroundColor = getButtonBackgroundColor(
      state,
      widgetTheme,
      widgetTheme.checkboxColor,
      enabled: enabled,
    );

    final constraints = getConstraintsFromBounds(state.bounds);
    final hasValidBounds = hasBounds(state.bounds);
    final textStyle = getTextStyle(
      context: context,
      font: state.font,
      textColor: enabled
          ? widgetTheme.checkboxTextColor
          : widgetTheme.disabledForegroundColor,
      baseTextStyle: widgetTheme.checkboxFontStyle,
    );

    final checkboxSize = getCheckboxSize(
      state,
      widgetTheme,
      textLineHeight: textStyle.height ?? 14.0,
      hasText: state.text?.isNotEmpty == true,
      hasImage: state.image != null && state.image?.imageData != null,
    );

    // checkboxCheckmarkColor is picked to contrast with the selected fill; on the
    // light disabled fill it would be invisible, so a disabled tick uses the
    // disabled foreground instead — as the radio's inner dot already does.
    final checkmarkColor = enabled
        ? widgetTheme.checkboxCheckmarkColor
        : widgetTheme.disabledForegroundColor;
    final checkboxWidget = _HoverableCheckboxRadio(
      enabled: enabled,
      selection: _selectionTicks,
      box: (hovering) {
        final isChecked = state.selection ?? false;
        // As on every native platform, grayed only shows on a checked box; grayed but unchecked
        // draws as an empty box.
        final isGrayed = isChecked && (state.grayed ?? false);
        return _CheckboxRadioBox(
          size: checkboxSize,
          enabled: enabled,
          hovering: hovering,
          isSelected: isChecked,
          isGrayed: isGrayed,
          baseColor: enabled
              ? (isChecked
                    ? widgetTheme.checkboxSelectedColor
                    : checkboxBackgroundColor)
              : widgetTheme.disabledBackgroundColor,
          hoverColor: widgetTheme.checkboxHoverColor,
          borderColor: enabled
              ? (isChecked
                    ? widgetTheme.checkboxSelectedColor
                    : widgetTheme.checkboxBorderColor)
              : widgetTheme.disabledForegroundColor,
          borderRadius: widgetTheme.checkboxBorderRadius,
          borderWidth: widgetTheme.checkboxBorderWidth,
          tick: _tickOf(checkboxSize * widgetTheme.checkboxCheckmarkSizeMultiplier, checkmarkColor),
          checkmarkColor: checkmarkColor,
          grayedMargin: checkboxSize * widgetTheme.checkboxGrayedMarginMultiplier,
          grayedBorderRadius: widgetTheme.checkboxGrayedBorderRadius,
          duration: widgetTheme.buttonPressDelay,
        );
      },
      onTap: enabled ? _onPressed : null,
      focusNode: _focusNode,
      onFocusChange: _onFocusChange,
    );

    Widget button = GestureDetector(
      onTap: enabled ? _onPressed : null,
      child: _pressedBySpace(Container(
        constraints: constraints,
        child: _buildButtonContent(
          context,
          text,
          widgetTheme,
          enabled,
          enabled
              ? widgetTheme.checkboxTextColor
              : widgetTheme.disabledForegroundColor,
          widgetTheme.checkboxFontStyle,
          hasBounds: hasValidBounds,
          leadingWidget: checkboxWidget,
          leadingSpacing: widgetTheme.radioButtonTextSpacing,
        ),
      )),
    );

    return wrap(_wrapWithSwtBackground(context, button));
  }

  Widget _buildRadioButton(
    BuildContext context,
    ButtonThemeExtension widgetTheme,
    bool enabled,
    String? text,
  ) {
    final radioBackgroundColor = getButtonBackgroundColor(
      state,
      widgetTheme,
      widgetTheme.dropdownButtonColor,
      enabled: enabled,
    );

    final constraints = getConstraintsFromBounds(state.bounds);
    final hasValidBounds = hasBounds(state.bounds);
    final textStyle = getTextStyle(
      context: context,
      font: state.font,
      textColor: enabled
          ? widgetTheme.radioButtonTextColor
          : widgetTheme.disabledForegroundColor,
      baseTextStyle: widgetTheme.radioButtonFontStyle,
    );
    final radioButtonSize = getRadioButtonSize(
      state,
      widgetTheme,
      textLineHeight: textStyle.height ?? 14.0,
      hasText: state.text?.isNotEmpty == true,
      hasImage: state.image != null && state.image?.imageData != null,
    );
    final radioButtonInnerSize = widgetTheme.radioButtonInnerSize;
    final radioButtonBorderRadius = widgetTheme.radioButtonBorderRadius;

    final radioWidget = _HoverableCheckboxRadio(
      enabled: enabled,
      selection: _selectionTicks,
      box: (hovering) {
        final isSelected = state.selection ?? false;
        return _CheckboxRadioBox(
          size: radioButtonSize,
          enabled: enabled,
          hovering: hovering,
          isSelected: isSelected,
          isGrayed: false,
          baseColor: enabled
              ? (isSelected
                    ? widgetTheme.radioButtonSelectedColor
                    : radioBackgroundColor)
              : widgetTheme.disabledBackgroundColor,
          hoverColor: widgetTheme.radioButtonHoverColor,
          selectedHoverColor: widgetTheme.radioButtonSelectedHoverColor,
          borderColor: enabled
              ? (isSelected
                    ? widgetTheme.radioButtonSelectedColor
                    : widgetTheme.radioButtonBorderColor)
              : widgetTheme.disabledForegroundColor,
          borderRadius: radioButtonBorderRadius,
          borderWidth: isSelected
              ? widgetTheme.radioButtonSelectedBorderWidth
              : widgetTheme.radioButtonBorderWidth,
          isCircle: true,
          innerSize: isSelected ? radioButtonInnerSize : null,
          innerColor: enabled
              ? radioBackgroundColor
              : widgetTheme.disabledForegroundColor,
          duration: widgetTheme.buttonPressDelay,
        );
      },
      onTap: enabled ? _onPressed : null,
      focusNode: _focusNode,
      onFocusChange: _onFocusChange,
    );

    Widget button = GestureDetector(
      onTap: enabled ? _onPressed : null,
      child: _pressedBySpace(Container(
        constraints: constraints,
        child: _buildButtonContent(
          context,
          text,
          widgetTheme,
          enabled,
          enabled
              ? widgetTheme.radioButtonTextColor
              : widgetTheme.disabledForegroundColor,
          widgetTheme.radioButtonFontStyle,
          hasBounds: hasValidBounds,
          leadingWidget: radioWidget,
          leadingSpacing: widgetTheme.radioButtonTextSpacing,
        ),
      )),
    );

    return wrap(_wrapWithSwtBackground(context, button));
  }

  Widget _buildArrowButton(
    BuildContext context,
    ButtonThemeExtension widgetTheme,
    bool enabled,
    String? text,
  ) {
    final alignment = state.alignment ?? SWT.CENTER;
    final arrowIcon = _getArrowIcon(alignment);

    final constraints = getConstraintsFromBounds(state.bounds);
    final hasValidBounds = hasBounds(state.bounds);

    Widget button = Material(
      borderRadius: BorderRadius.circular(widgetTheme.pushButtonBorderRadius),
      child: InkWell(
        onTap: _onPressed,
        focusNode: _focusNode,
        // An arrow button is NO_FOCUS in SWT: never focused, and no tab stop.
        canRequestFocus: false,
        onFocusChange: _onFocusChange,
        borderRadius: BorderRadius.circular(widgetTheme.pushButtonBorderRadius),
        child: QuietAnimatedContainer(
          duration: widgetTheme.buttonPressDelay,
          constraints: constraints,
          decoration: BoxDecoration(
            color: enabled
                ? widgetTheme.pushButtonColor
                : widgetTheme.pushButtonDisabledColor,
            borderRadius: BorderRadius.circular(
              widgetTheme.pushButtonBorderRadius,
            ),
            border: Border.all(
              color: widgetTheme.pushButtonBorderColor,
              width: widgetTheme.pushButtonBorderWidth,
            ),
          ),
          child: Icon(
            arrowIcon,
            size: widgetTheme.dropdownButtonIconSize,
            color: enabled
                ? getForegroundColor(
                    foreground: _ownForeground,
                    defaultColor: widgetTheme.pushButtonTextColor,
                    context: context,
                    minContrast: widgetTheme.minTextContrast,
                  )
                : widgetTheme.disabledForegroundColor,
          ),
        ),
      ),
    );

    return wrap(_wrapWithSwtBackground(context, button));
  }

  Widget _buildButtonContent(
    BuildContext context,
    String? text,
    ButtonThemeExtension widgetTheme,
    bool enabled,
    Color defaultTextColor,
    TextStyle? baseTextStyle, {
    bool hasBounds = false,
    Widget? leadingWidget,
    double? leadingSpacing,
    Widget? trailingWidget,
    bool includeImage = true,
  }) {
    final bool hasText = text?.isNotEmpty == true;
    final swtAlignment = state.alignment ?? SWT.CENTER;

    Widget? imageWidget;
    if (includeImage && state.image != null && state.image?.imageData != null) {
      final double imageWidth = state.image!.imageData!.width?.toDouble() ?? 0;
      final double imageHeight =
          state.image!.imageData!.height?.toDouble() ?? 0;

      if (imageWidth > 0 && imageHeight > 0) {
        final builtImage = ImageUtils.buildVImage(
          state.image,
          width: imageWidth,
          height: imageHeight,
          enabled: enabled,
          constraints: null,
          useBinaryImage: true,
          renderAsIcon: true,
        );

        imageWidget = SizedBox(
          width: imageWidth,
          height: imageHeight,
          child: builtImage,
        );
      }
    }

    final alignment = (imageWidget != null)
        ? MainAxisAlignment.start
        : swtAlignment == SWT.LEFT
        ? MainAxisAlignment.start
        : swtAlignment == SWT.RIGHT
        ? MainAxisAlignment.end
        : MainAxisAlignment.center;

    final textColor = getForegroundColor(
      foreground: _ownForeground,
      defaultColor: defaultTextColor,
      context: context,
      minContrast: widgetTheme.minTextContrast,
    );

    final textStyle = _labelStyle(context, textColor, baseTextStyle);

    final shouldWrap = shouldWrapText(
      style: state.style,
      hasValidBounds: hasBounds,
      text: text ?? '',
    );

    Widget rowContent = Row(
      mainAxisSize: hasBounds ? MainAxisSize.max : MainAxisSize.min,
      mainAxisAlignment: alignment,
      crossAxisAlignment: CrossAxisAlignment.center,
      children: [
        if (leadingWidget != null) ...[
          leadingWidget,
          // The gap between a check box or radio and its label is part of the button, as on GTK.
          if (hasText || imageWidget != null)
            MetaData(
              behavior: HitTestBehavior.opaque,
              child: SizedBox(width: leadingSpacing, height: double.infinity),
            ),
        ],
        if (imageWidget != null) imageWidget,

        if (hasText)
          Flexible(
            child: MnemonicText(
              state.text,
              style: textStyle,
              overflow: shouldWrap
                  ? TextOverflow.visible
                  : TextOverflow.ellipsis,
              maxLines: shouldWrap ? null : 1,
            ),
          ),

        if (trailingWidget != null) ...[
          if (hasText || imageWidget != null)
            SizedBox(width: widgetTheme.imageTextSpacing),
          trailingWidget,
        ],
      ],
    );

    if (leadingWidget != null) {
      return _RowBox(onBox: (box) => _indicatorRow = box, child: IntrinsicHeight(child: rowContent));
    }

    return rowContent;
  }

  /// Kept on the InkWell whatever the enablement: [wrap] already keeps pointer and focus from a
  /// disabled button, and an InkWell whose enablement flips sets its own state in the middle of the
  /// build, which makes the build owner re-sort every widget waiting to rebuild.
  ///
  /// The button's own focus nodes stay focusable for the same reason: [wrap]'s node, set before the
  /// build, keeps focus from them while the button is disabled. One that followed the enablement
  /// would change during the build and rebuild in a frame of its own.
  void _onPressed() {
    _selectedThisPress = true;
    if (!(state.enabled ?? false)) return;
    final isRadio = hasStyle(state.style, SWT.RADIO);
    if (isRadio ||
        hasStyle(state.style, SWT.CHECK) ||
        hasStyle(state.style, SWT.TOGGLE)) {
      setState(() {
        state.selection = !(state.selection ?? false);
      });
    }
    // A radio selecting itself must immediately deselect the other radios in its
    // group, so the UI never shows more than one selected even before Java's
    // selectRadio round-trip lands. Java remains the source of
    // truth and confirms the same result.
    if (isRadio && (state.selection ?? false)) {
      _deselectSiblingRadios();
    }
    widget.sendSelectionSelection(state, null);
  }

  /// Deselect the other RADIO buttons in the same group. The group is the
  /// nearest enclosing Composite subtree (SWT groups radios by parent); nested
  /// composites are independent groups. Falls back to the whole tree if no
  /// composite ancestor is found.
  void _deselectSiblingRadios() {
    Element? scope;
    context.visitAncestorElements((el) {
      if (el is StatefulElement && el.state is CompositeImpl) {
        scope = el;
        return false; // stop at the nearest enclosing composite
      }
      return true;
    });
    final Element? root = scope ?? WidgetsBinding.instance.rootElement;
    if (root == null) return;

    void visit(Element el) {
      if (el is StatefulElement) {
        final st = el.state;
        // Don't descend into a nested composite — that's a separate group.
        if (st is CompositeImpl && !identical(el, root)) return;
        if (st is ButtonImpl &&
            !identical(st, this) &&
            hasStyle(st.state.style, SWT.RADIO)) {
          st._deselectByRadioGroup();
          return;
        }
      }
      el.visitChildren(visit);
    }

    root.visitChildren(visit);
  }

  void _deselectByRadioGroup() {
    if (mounted && (state.selection ?? false)) {
      setState(() => state.selection = false);
    }
  }

  /// Space presses a focused check box or radio button, as it does a push button (whose InkWell
  /// already answers it).
  Widget _pressedBySpace(Widget child) => Actions(
        actions: <Type, Action<Intent>>{
          ActivateIntent: CallbackAction<ActivateIntent>(onInvoke: (_) {
            _onPressed();
            return null;
          }),
        },
        child: child,
      );

  /// Called on every change of a focus node's properties, such as its enablement, and not only of
  /// its focus.
  void _onFocusChange(bool hasFocus) {
    if (_hasFocus == hasFocus) return;
    if (mounted) {
      setState(() => _hasFocus = hasFocus);
    } else {
      _hasFocus = hasFocus;
    }
    if (hasFocus) {
      widget.sendFocusFocusIn(state, null);
    } else {
      widget.sendFocusFocusOut(state, null);
    }
  }
}

/// What a push button draws of its state, as of its last build.
///
/// [keepsInk] says whether the InkWell built last can be kept: [surface] rebuilds by itself.
typedef _PushLook = ({
  Color base,
  Color hover,
  Color pressed,
  bool isPressed,
  bool enabled,
  bool keepsInk,
  Widget surface,
});

/// A push button's surface and what it reacts to. It reads what it draws from the [ButtonImpl]
/// above it, which marks it when that changes, so that the widgets between them stay the same.
class _HoverableButton extends StatefulWidget {
  final ButtonThemeExtension widgetTheme;

  const _HoverableButton({required this.widgetTheme});

  @override
  State<_HoverableButton> createState() => _HoverableButtonState();
}

class _HoverableButtonState extends State<_HoverableButton> {
  bool _isHovering = false;

  late final ButtonImpl _host = context.findAncestorStateOfType<ButtonImpl>()!;

  @override
  void dispose() {
    _host._hoverableRebuild.disposed(context);
    super.dispose();
  }

  /// The Material and the InkWell in it, kept while [_HoverableButton.keepsInk] and what they are
  /// built from stay the same. The fill that changes with the enablement, a hover or a press is
  /// [_ShapeFill]'s, under them, so that none of those changes rebuilds them.
  Widget? _ink;
  Object? _inkKey;

  Widget _material(_PushLook look) {
    final kept = _ink;
    if (kept != null && look.keepsInk && identical(widget.widgetTheme, _inkKey)) return kept;
    _inkKey = widget.widgetTheme;
    final radius = BorderRadius.circular(widget.widgetTheme.pushButtonBorderRadius);
    return _ink = Material(
      type: MaterialType.transparency,
      borderRadius: radius,
      child: InkWell(
        onTap: _host._onPressed,
        focusNode: _host._focusNode,
        onFocusChange: _host._onFocusChange,
        borderRadius: radius,
        splashColor: widget.widgetTheme.splashColor,
        highlightColor: widget.widgetTheme.highlightColor,
        child: look.surface,
      ),
    );
  }

  void _entered(PointerEnterEvent _) {
    setState(() => _isHovering = true);
  }

  void _exited(PointerExitEvent _) {
    setState(() => _isHovering = false);
  }

  @override
  Widget build(BuildContext context) {
    _host._hoverableRebuild.builds(context);
    final look = _host._pushLook!;
    final backgroundColor = look.enabled
        ? (look.isPressed
              ? look.pressed
              : (_isHovering
                    ? look.hover
                    : look.base))
        : widget.widgetTheme.pushButtonDisabledColor;

    return MouseRegion(
      onEnter: _entered,
      onExit: _exited,
      child: _ShapeFill(
        color: backgroundColor,
        radius: widget.widgetTheme.pushButtonBorderRadius,
        child: _material(look),
      ),
    );
  }
}

/// Fills a rounded rectangle under its child, as a canvas [Material] of that colour and radius fills
/// its shape under the ink and the content it holds: the same path, drawn the same way, and taking
/// pointers only inside it.
class _ShapeFill extends SingleChildRenderObjectWidget {
  const _ShapeFill({required this.color, required this.radius, super.child});

  final Color color;
  final double radius;

  @override
  _RenderShapeFill createRenderObject(BuildContext context) =>
      _RenderShapeFill(color, radius, Directionality.maybeOf(context));

  @override
  void updateRenderObject(BuildContext context, _RenderShapeFill renderObject) {
    renderObject
      ..color = color
      ..radius = radius
      ..textDirection = Directionality.maybeOf(context);
  }
}

class _RenderShapeFill extends RenderProxyBox {
  _RenderShapeFill(this._color, this._radius, this._textDirection);

  Color _color;
  set color(Color value) {
    if (value == _color) return;
    _color = value;
    markNeedsPaint();
  }

  double _radius;
  set radius(double value) {
    if (value == _radius) return;
    _radius = value;
    _clip = null;
    markNeedsPaint();
  }

  TextDirection? _textDirection;
  set textDirection(TextDirection? value) {
    if (value == _textDirection) return;
    _textDirection = value;
    _clip = null;
    markNeedsPaint();
  }

  Path? _clip;
  Size? _clipSize;

  Path _shape() {
    if (_clip == null || _clipSize != size) {
      _clipSize = size;
      _clip = ShapeBorderClipper(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(_radius)),
        textDirection: _textDirection,
      ).getClip(size);
    }
    return _clip!;
  }

  @override
  void paint(PaintingContext context, Offset offset) {
    if (child == null) return;
    context.canvas.drawPath(_shape().shift(offset), Paint()..color = _color);
    super.paint(context, offset);
  }

  @override
  bool hitTest(BoxHitTestResult result, {required Offset position}) {
    if (!_shape().contains(position)) return false;
    return super.hitTest(result, position: position);
  }
}

/// The check box or radio's box: what it reacts to, around [box], which draws it and alone follows
/// [selection].
class _HoverableCheckboxRadio extends StatefulWidget {
  final bool enabled;
  final Listenable selection;
  final _CheckboxRadioBox Function(bool hovering) box;
  final VoidCallback? onTap;
  final FocusNode focusNode;
  final ValueChanged<bool> onFocusChange;

  const _HoverableCheckboxRadio({
    required this.enabled,
    required this.selection,
    required this.box,
    this.onTap,
    required this.focusNode,
    required this.onFocusChange,
  });

  @override
  State<_HoverableCheckboxRadio> createState() =>
      _HoverableCheckboxRadioState();
}

class _HoverableCheckboxRadioState extends State<_HoverableCheckboxRadio> {
  bool _isHovering = false;

  @override
  Widget build(BuildContext context) {
    return MouseRegion(
      onEnter: (_) {
        setState(() => _isHovering = true);
      },
      onExit: (_) {
        setState(() => _isHovering = false);
      },
      child: GestureDetector(
        onTap: widget.onTap,
        // The button's only Focus, so it is one tab stop: a Focus nested in another would hear an
        // enablement change during the build (see ControlImpl.blockWhenDisabled).
        child: Focus(
          focusNode: widget.focusNode,
          onFocusChange: widget.onFocusChange,
          // The box animates on every selection or hover change; the label beside it need not
          // repaint with each step.
          child: RepaintBoundary(
            child: ListenableBuilder(
              listenable: widget.selection,
              builder: (context, _) => widget.box(_isHovering),
            ),
          ),
        ),
      ),
    );
  }
}

class _CheckboxRadioBox extends StatelessWidget {
  final double size;
  final bool enabled;
  final bool hovering;
  final bool isSelected;
  final bool isGrayed;
  final Color baseColor;
  final Color hoverColor;
  final Color? selectedHoverColor;
  final Color borderColor;
  final double borderRadius;
  final double borderWidth;
  final Widget? tick;
  final Color? checkmarkColor;
  final double? grayedMargin;
  final double? grayedBorderRadius;
  final bool isCircle;
  final double? innerSize;
  final Color? innerColor;
  final Duration duration;

  const _CheckboxRadioBox({
    required this.size,
    required this.enabled,
    required this.hovering,
    required this.isSelected,
    required this.isGrayed,
    required this.baseColor,
    required this.hoverColor,
    this.selectedHoverColor,
    required this.borderColor,
    required this.borderRadius,
    required this.borderWidth,
    this.tick,
    this.checkmarkColor,
    this.grayedMargin,
    this.grayedBorderRadius,
    this.isCircle = false,
    this.innerSize,
    this.innerColor,
    required this.duration,
  });

  @override
  Widget build(BuildContext context) {
    Color backgroundColor;
    if (enabled && hovering) {
      if (isSelected && selectedHoverColor != null) {
        backgroundColor = selectedHoverColor!;
      } else if (!isSelected) {
        backgroundColor = hoverColor;
      } else {
        backgroundColor = baseColor;
      }
    } else {
      backgroundColor = baseColor;
    }

    if (isCircle) {
      return QuietAnimatedContainer(
        duration: duration,
        width: size,
        height: size,
        decoration: BoxDecoration(
          color: backgroundColor,
          shape: BoxShape.circle,
          border: Border.all(
            color: borderColor,
            width: borderWidth,
          ),
        ),
        child: isSelected && innerSize != null
            ? Center(
                child: Container(
                  width: innerSize,
                  height: innerSize,
                  decoration: BoxDecoration(
                    color: innerColor,
                    shape: BoxShape.circle,
                  ),
                ),
              )
            : null,
      );
    }
    return QuietAnimatedContainer(
      duration: duration,
      width: size,
      height: size,
      decoration: BoxDecoration(
        color: backgroundColor,
        borderRadius: BorderRadius.circular(borderRadius),
        border: Border.all(
          color: borderColor,
          width: borderWidth,
        ),
      ),
      // Grayed outranks checked: the partial mark is all that separates a partially-checked
      // box from a fully-checked one.
      child: isGrayed
          ? Container(
              margin: EdgeInsets.all(grayedMargin ?? 0),
              decoration: BoxDecoration(
                color: checkmarkColor,
                borderRadius: BorderRadius.circular(
                  grayedBorderRadius ?? 0,
                ),
              ),
            )
          // The mark stays mounted while hidden: a check toggles it, and mounting it shapes its glyph.
          // Offstage, not Opacity: an Opacity going to or from zero changes what is composited.
          : Offstage(offstage: !isSelected, child: tick),
    );
  }
}

/// Hands [onBox] the render box its child is laid out in, so its owner can read where it is drawn.
class _RowBox extends SingleChildRenderObjectWidget {
  const _RowBox({required this.onBox, required super.child});

  final void Function(RenderBox box) onBox;

  @override
  RenderProxyBox createRenderObject(BuildContext context) {
    final box = RenderProxyBox();
    onBox(box);
    return box;
  }

  @override
  void updateRenderObject(BuildContext context, RenderProxyBox renderObject) => onBox(renderObject);
}
