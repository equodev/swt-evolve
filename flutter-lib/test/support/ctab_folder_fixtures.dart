// A CTabFolder whose one tab shows an icon, as Java serializes it.

import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:swtflutter/main.dart';
import 'package:swtflutter/src/gen/ctabfolder.dart';
import 'package:swtflutter/src/gen/ctabitem.dart';
import 'package:swtflutter/src/gen/image.dart';
import 'package:swtflutter/src/gen/imagedata.dart';
import 'package:swtflutter/src/gen/rectangle.dart';
import 'package:swtflutter/src/gen/swt.dart';
import 'package:swtflutter/src/impl/ctabfolder_evolve.dart';

/// A 16x16 image with the PNG bytes [png].
VImage iconImage(Uint8List png) => VImage()
  ..imageData = (VImageData()
    ..width = 16
    ..height = 16
    ..data = png);

/// A light app showing a 300x200 CTabFolder with one selected tab titled [tabText], its icon [png].
Widget ctabFolderWithIcon(Uint8List png, {required String tabText}) => EvolveApp(
      theme: ThemeMode.light,
      contentWidget: Align(
        alignment: Alignment.topLeft,
        child: SizedBox(
          width: 300,
          height: 200,
          child: CTabFolderSwt<VCTabFolder>(
            value: VCTabFolder()
              ..id = 1
              ..style = SWT.NONE
              ..enabled = true
              ..tabPosition = SWT.TOP
              ..selection = 0
              ..bounds = (VRectangle()
                ..x = 0
                ..y = 0
                ..width = 300
                ..height = 200)
              ..items = [
                VCTabItem()
                  ..id = 2
                  ..text = tabText
                  ..showing = true
                  ..image = iconImage(png),
              ],
          ),
        ),
      ),
    );

Finder get ctabItem => find.byType(CTabItemSwt<VCTabItem>);

/// The icon drawn in [ctabItem].
Finder get ctabIcon => find.descendant(of: ctabItem, matching: find.byType(Image));
