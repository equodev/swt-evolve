import 'dart:ui' show Brightness, PlatformDispatcher;

import 'package:flutter/material.dart' show ThemeMode;

/// The light/dark appearance of the system this client runs on, read once at launch: the browser's
/// on web, the OS's on desktop. The theme follows it when the application defines none, and Java is
/// told it in ClientReady so the system colors it hands out match.
final Brightness launchSystemBrightness = PlatformDispatcher.instance.platformBrightness;

ThemeMode get systemThemeMode =>
    launchSystemBrightness == Brightness.dark ? ThemeMode.dark : ThemeMode.light;

String get systemAppearance => launchSystemBrightness == Brightness.dark ? 'dark' : 'light';
