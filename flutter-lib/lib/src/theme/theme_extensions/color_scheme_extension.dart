import 'package:flutter/material.dart';
import 'package:theme_tailor_annotation/theme_tailor_annotation.dart';
import 'package:json_annotation/json_annotation.dart';
import 'json_converters.dart';

part 'color_scheme_extension.tailor.dart';
part 'color_scheme_extension.g.dart';

@TailorMixin()
@JsonSerializable(explicitToJson: true)
@ColorConverter()
class ColorSchemeExtension extends ThemeExtension<ColorSchemeExtension> with _$ColorSchemeExtensionTailorMixin {
  // Primary colors adicionales
  final Color primaryHovered; // sys.color.primary.hovered #1E30CC
  final Color primaryBorder; // sys.color.on_primary.border.default #1B2AB2
  final Color primaryBorderDisabled; // sys.color.on_primary.border.disabled #17181933
  final Color onPrimaryVariantDisabled; // sys.color.on_primary.variant.disabled #17181980
  
  // Secondary colors adicionales
  final Color secondaryPressed; // sys.color.secondary.pressed #C9CDF0
  final Color secondaryBold; // sys.color.secondary_bold #E1E3F5
  final Color secondaryBorder; // sys.color.on_secondary.border.default #C9CDF0
  final Color secondaryBorderDisabled; // sys.color.on_secondary.border.disabled #17181933
  final Color onSecondaryVariantDisabled; // sys.color.on_secondary.variant.disabled #17181980
  
  // Tertiary colors adicionales
  final Color tertiaryPressed; // sys.color.tertiary.pressed #17181933
  final Color tertiaryHovered; // sys.color.tertiary.hovered #1718191A
  final Color tertiaryBorder; // sys.color.on_tertiary.border.default #FFFFFF00
  final Color tertiaryBorderDisabled; // sys.color.on_tertiary.border.disabled #FFFFFF00
  final Color onTertiaryVariantDisabled; // sys.color.on_tertiary.variant.disabled #17181980
  
  // Surface colors adicionales
  final Color surfaceFocused; // sys.color.surface_highest.focused #F2F3FA
  final Color surfaceBorderEnabled; // sys.color.on_surface.border.enabled #1718191A
  final Color surfaceBorderHovered; // sys.color.on_surface.border.hovered #1718194D
  final Color surfaceBorderFocused; // sys.color.on_surface.border.focused #0F1866
  final Color surfaceBorderDisabled; // sys.color.on_surface.border.disabled #17181933
  final Color surfaceBorderBoldEnabled; // sys.color.on_surface.border_bold.enabled #17181980
  final Color surfaceBorderDataDisplay; // sys.color.on_surface.border_data_display #FFFFFF
  final Color surfacePlaceholder; // sys.color.on_surface.placeholder #797B7F
  final Color onSurfaceVariantError; // sys.color.on_surface.variant.error #DC0A56
  final Color onSurfaceVariantWarning; // sys.color.on_surface.variant.warning #DE7A2D
  final Color onSurfaceVariantDisabled; // sys.color.on_surface.variant.disabled #17181980
  final Color onSurfaceVariantSmallError; // sys.color.on_surface.variant_small.error #99073C
  
  // Error colors adicionales
  final Color errorHovered; // sys.color.error.hovered #B20846
  final Color errorBorder; // sys.color.on_error.border.default #99073C
  final Color errorBorderDisabled; // sys.color.on_error.border.disabled #17181933
  final Color onErrorVariantDisabled; // sys.color.on_error.variant.disabled #17181980
  final Color errorContainerPressed; // sys.color.error_container.pressed #F0C9D4
  final Color errorContainerHovered; // sys.color.error_container.hovered #F5E1E6
  final Color onErrorContainerVariantError; // sys.color.on_error_container.variant.error #99073C
  final Color onErrorContainerVariantDisabled; // sys.color.on_error_container.variant.disabled #17181980
  final Color onErrorContainerBorder; // sys.color.on_error_container.border.default #F0C9D4
  final Color onErrorContainerBorderDisabled; // sys.color.on_error_container.border.disabled #17181933
  final Color onErrorContainerBorderHovered; // sys.color.on_error_container.border.hovered #DC0A56
  final Color onErrorContainerBorderEnabled; // sys.color.on_error_container.border.enabled #F0C9D4
  final Color onErrorContainerBorderFocused; // sys.color.on_error_container.border.focused #59040D
  final Color onErrorContainerPlaceholder; // sys.color.on_error_container.placeholder #DC0A56
  final Color errorTextEnabled; // sys.color.error_text.enabled #FFFFFF00
  final Color errorTextHovered; // sys.color.error_text.hovered #FAF2F4
  final Color errorTextPressed; // sys.color.error_text.pressed #F5E1E6
  final Color onErrorTextVariant; // sys.color.on_error_text.variant.default #59040D
  final Color onErrorTextVariantDisabled; // sys.color.on_error_text.variant.disabled #17181980
  
  // Warning colors (no están en ColorScheme estándar)
  final Color warning; // sys.color.warning.default #DE7A2D
  final Color warningPressed; // sys.color.warning.pressed #99541F
  final Color warningHovered; // sys.color.warning.hovered #B26224
  final Color onWarning; // sys.color.on_warning.variant.default #FFFFFF
  final Color onWarningVariantDisabled; // sys.color.on_warning.variant.disabled #17181980
  final Color warningBorder; // sys.color.on_warning.border.default #99541F
  final Color warningBorderDisabled; // sys.color.on_warning.border.disabled #17181933
  final Color warningTextEnabled; // sys.color.warning_text.enabled #FFFFFF00
  final Color warningTextPressed; // sys.color.warning_text.pressed #F5EAE1
  final Color warningTextHovered; // sys.color.warning_text.hovered #FAF3ED
  final Color onWarningTextVariant; // sys.color.on_warning_text.variant.default #4D2100
  final Color onWarningTextVariantDisabled; // sys.color.on_warning_text.variant.disabled #17181980
  final Color warningContainer; // sys.color.warning_container.enabled #FAF3ED
  final Color warningContainerPressed; // sys.color.warning_container.pressed #F0DAC9
  final Color warningContainerHovered; // sys.color.warning_container.hovered #F5EAE1
  final Color onWarningContainerVariant; // sys.color.on_warning_container.variant.default #59040D
  final Color onWarningContainerVariantDisabled; // sys.color.on_warning_container.variant.disabled #17181980
  final Color onWarningContainerBorder; // sys.color.on_warning_container.border.default #F0DAC9
  final Color onWarningContainerBorderDisabled; // sys.color.on_warning_container.border.disabled #17181933
  
  // Success colors (no están en ColorScheme estándar)
  final Color success; // sys.color.success.enabled #1BBB77
  final Color successPressed; // sys.color.success.pressed #128051
  final Color successHovered; // sys.color.success.hovered #169961
  final Color onSuccess; // sys.color.on_success.variant.default #FFFFFF
  final Color onSuccessVariantDisabled; // sys.color.on_success.variant.disabled #17181980
  final Color successBorder; // sys.color.on_success.border.default #128051
  final Color successBorderDisabled; // sys.color.on_success.border.disabled #17181933
  final Color successContainer; // sys.color.success_container.enabled #E6F2ED
  final Color successContainerPressed; // sys.color.success_container.pressed #C1E5D6
  final Color successContainerHovered; // sys.color.success_container.hovered #DAEDE5
  final Color onSuccessContainerVariant; // sys.color.on_success_container.variant.default #0C3322
  final Color onSuccessContainerVariantDisabled; // sys.color.on_success_container.variant.disabled #17181980
  final Color onSuccessContainerBorder; // sys.color.on_success_container.border.default #C1E5D6
  final Color onSuccessContainerBorderDisabled; // sys.color.on_success_container.border.disabled #17181933
  
  // Status colors
  final Color statusDefault; // sys.color.status.default #606266
  final Color statusOnDefault; // sys.color.status.on_default #FFFFFF
  final Color statusDefaultContainer; // sys.color.status.default_container #E9EBF0
  final Color statusOnDefaultContainer; // sys.color.status.on_default_container #2F3033
  final Color statusInform; // sys.color.status.inform #2236E5
  final Color statusOnInform; // sys.color.status.on_inform #FFFFFF
  final Color statusInformContainer; // sys.color.status.inform_container #F2F3FA
  final Color statusOnInformContainer; // sys.color.status.on_inform_container #0F1866
  final Color statusOnErrorContainer; // sys.color.status.on_error_container #59040D
  final Color statusSuccess; // sys.color.status.success #1BBB77
  final Color statusOnSuccessContainer; // sys.color.status.on_success_container #0C3322
  final Color statusWarning; // sys.color.status.warning #DE7A2D
  final Color statusWarningContainer; // sys.color.status.warning_container #FAF3ED
  final Color statusOnWarningContainer; // sys.color.status.on_warning_container #4D2100
  final Color statusCaution; // sys.color.status.caution #D4DB2D
  final Color statusOnCaution; // sys.color.status.on_caution #4A4D00
  final Color statusCautionContainer; // sys.color.status.caution_container #F9FAED
  final Color statusOnCautionContainer; // sys.color.status.on_caution_container #4A4D00
  
  // Neutral colors
  final Color neutral; // sys.color.neutral #F0F2F5
  final Color onNeutralBorder; // sys.color.on_neutral.border #FFFFFF
  final Color onNeutralVariant; // sys.color.on_neutral.variant #171819
  
  // State colors
  final Color stateDefaultEnabled; // sys.state.default.enabled #FFFFFF00
  final Color stateDefaultHovered; // sys.state.default.hovered rgba(0, 0, 0, 0.12)
  final Color stateDefaultPressed; // sys.state.default.pressed rgba(0, 0, 0, 0.2)
  final Color stateOnContainerEnabled; // sys.state.on_container.enabled #FFFFFF00
  final Color stateOnContainerHovered; // sys.state.on_container.hovered rgba(0, 0, 0, 0.04)
  final Color stateOnContainerPressed; // sys.state.on_container.pressed rgba(0, 0, 0, 0.08)
  
  // Label/Input colors
  final Color labelInputDefault; // sys.color.label.input.default #47494D
  final Color labelInputDisabled; // sys.color.label.input.disabled #47494D

  const ColorSchemeExtension({
    required this.primaryHovered,
    required this.primaryBorder,
    required this.primaryBorderDisabled,
    required this.onPrimaryVariantDisabled,
    required this.secondaryPressed,
    required this.secondaryBold,
    required this.secondaryBorder,
    required this.secondaryBorderDisabled,
    required this.onSecondaryVariantDisabled,
    required this.tertiaryPressed,
    required this.tertiaryHovered,
    required this.tertiaryBorder,
    required this.tertiaryBorderDisabled,
    required this.onTertiaryVariantDisabled,
    required this.surfaceFocused,
    required this.surfaceBorderEnabled,
    required this.surfaceBorderHovered,
    required this.surfaceBorderFocused,
    required this.surfaceBorderDisabled,
    required this.surfaceBorderBoldEnabled,
    required this.surfaceBorderDataDisplay,
    required this.surfacePlaceholder,
    required this.onSurfaceVariantError,
    required this.onSurfaceVariantWarning,
    required this.onSurfaceVariantDisabled,
    required this.onSurfaceVariantSmallError,
    required this.errorHovered,
    required this.errorBorder,
    required this.errorBorderDisabled,
    required this.onErrorVariantDisabled,
    required this.errorContainerPressed,
    required this.errorContainerHovered,
    required this.onErrorContainerVariantError,
    required this.onErrorContainerVariantDisabled,
    required this.onErrorContainerBorder,
    required this.onErrorContainerBorderDisabled,
    required this.onErrorContainerBorderHovered,
    required this.onErrorContainerBorderEnabled,
    required this.onErrorContainerBorderFocused,
    required this.onErrorContainerPlaceholder,
    required this.errorTextEnabled,
    required this.errorTextHovered,
    required this.errorTextPressed,
    required this.onErrorTextVariant,
    required this.onErrorTextVariantDisabled,
    required this.warning,
    required this.warningPressed,
    required this.warningHovered,
    required this.onWarning,
    required this.onWarningVariantDisabled,
    required this.warningBorder,
    required this.warningBorderDisabled,
    required this.warningTextEnabled,
    required this.warningTextPressed,
    required this.warningTextHovered,
    required this.onWarningTextVariant,
    required this.onWarningTextVariantDisabled,
    required this.warningContainer,
    required this.warningContainerPressed,
    required this.warningContainerHovered,
    required this.onWarningContainerVariant,
    required this.onWarningContainerVariantDisabled,
    required this.onWarningContainerBorder,
    required this.onWarningContainerBorderDisabled,
    required this.success,
    required this.successPressed,
    required this.successHovered,
    required this.onSuccess,
    required this.onSuccessVariantDisabled,
    required this.successBorder,
    required this.successBorderDisabled,
    required this.successContainer,
    required this.successContainerPressed,
    required this.successContainerHovered,
    required this.onSuccessContainerVariant,
    required this.onSuccessContainerVariantDisabled,
    required this.onSuccessContainerBorder,
    required this.onSuccessContainerBorderDisabled,
    required this.statusDefault,
    required this.statusOnDefault,
    required this.statusDefaultContainer,
    required this.statusOnDefaultContainer,
    required this.statusInform,
    required this.statusOnInform,
    required this.statusInformContainer,
    required this.statusOnInformContainer,
    required this.statusOnErrorContainer,
    required this.statusSuccess,
    required this.statusOnSuccessContainer,
    required this.statusWarning,
    required this.statusWarningContainer,
    required this.statusOnWarningContainer,
    required this.statusCaution,
    required this.statusOnCaution,
    required this.statusCautionContainer,
    required this.statusOnCautionContainer,
    required this.neutral,
    required this.onNeutralBorder,
    required this.onNeutralVariant,
    required this.stateDefaultEnabled,
    required this.stateDefaultHovered,
    required this.stateDefaultPressed,
    required this.stateOnContainerEnabled,
    required this.stateOnContainerHovered,
    required this.stateOnContainerPressed,
    required this.labelInputDefault,
    required this.labelInputDisabled,
  });

  factory ColorSchemeExtension.fromJson(Map<String, dynamic> json) =>
      _$ColorSchemeExtensionFromJson(json);

  Map<String, dynamic> toJson() => _$ColorSchemeExtensionToJson(this);
}
