import '../widget_config.dart';

/// On desktop the locale is the JVM's, which Java sends in the config flags.
String decimalSeparator() => getConfigFlags().decimal_separator ?? '.';
