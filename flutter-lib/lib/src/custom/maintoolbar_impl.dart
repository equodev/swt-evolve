import 'package:flutter/material.dart';

import '../gen/composite.dart';
import '../gen/toolitem.dart';
import '../gen/widgets.dart';

import 'dart:io';
import 'package:flutter_svg/flutter_svg.dart';
import '../impl/widget_config.dart';
import '../impl/composite_evolve.dart';
import '../impl/scrollable_evolve.dart';
import '../gen/toolbar.dart';

class MainToolbarSwt<V extends VComposite> extends CompositeSwt<V> {
  const MainToolbarSwt({super.key, required super.value});

  @override
  State createState() =>
      MainToolbarImpl<MainToolbarSwt<VComposite>, VComposite>();
}

class MainToolbarImpl<T extends MainToolbarSwt, V extends VComposite>
    extends CompositeImpl<T, V> {

  final bool useDarkTheme = getCurrentTheme();

  @override
  Widget build(BuildContext context) {
    // String parentPath = Directory.current.parent.path;
    // String basePath = '$parentPath/Eclipse_SVG';
    String basePath = 'assets/svg';

    final String saveSvg = useDarkTheme ? '$basePath/save.svg' : '$basePath/save_bright.svg';
    final String eclipseSvg = useDarkTheme ? '$basePath/Eclipse.svg' : '$basePath/Eclipse_bright.svg';
    final String gitSvg = useDarkTheme ? '$basePath/git.svg' : '$basePath/git_bright.svg';
    final String selfHealingSvg = useDarkTheme ? '$basePath/self-healing.svg' : '$basePath/self-healing_bright.svg';
    final String studioAssistSvg = useDarkTheme ? '$basePath/StudioAssist.svg' : '$basePath/StudioAssist_bright.svg';
    final String consoleSvg = useDarkTheme ? '$basePath/console.svg' : '$basePath/console_bright.svg';
    final String recordWebSvg = useDarkTheme ? '$basePath/record-web.svg' : '$basePath/record-web_bright.svg';
    final String spyWebSvg = useDarkTheme ? '$basePath/spy-web.svg' : '$basePath/spy-web_bright.svg';
    final String playSvg = '$basePath/play.svg';
    final String stopSvg = '$basePath/stop.svg';
    final String bugSvg = useDarkTheme ? '$basePath/bug.svg' : '$basePath/bug_bright.svg';
    final String notificationSvg = useDarkTheme ? '$basePath/notification.svg' : '$basePath/notification_bright.svg';
    final String userSvg = useDarkTheme ? '$basePath/user.svg' : '$basePath/user_bright.svg';

    final Color backgroundColor = useDarkTheme ? const Color(0xFF2A2A2A) : const Color(0xFFF5F5F5);
    final Color separatorColor = useDarkTheme ? const Color(0xFF3B3B3B) : const Color(0xFFDDDDDD);
    final Color keywordDebugBgColor = useDarkTheme ? const Color(0xFF1F1F1F) : const Color(0xFFE8E8E8);
    final Color debugTextColor = useDarkTheme ? const Color(0xFF6D6D6D) : const Color(0xFF888888);
    final Color keywordButtonBgColor = const Color(0xFF5C64FF);
    final Color keywordButtonTextColor = Colors.white;

    final Color newTestCaseBgColor = const Color(0xFF5C64FF);
    final Color newTestCaseTextColor = Colors.white;

    final Color lightThemeIconBorder = const Color(0xFFE0E0E0);

    return super.wrap(
      Container(
        height: 40,
        color: backgroundColor,
        child: Row(
          children: [
            // ElevatedButton(
            //   onPressed: () => print('Test button works!'),
            //   child: Text('Test'),
            // ),
            _buildSvgButtonWithDropdown(saveSvg, border: true, separatorColor: separatorColor, identifier: "Save"),
            _buildSvgButton(eclipseSvg, border: false, identifier: "qTest integration"),
            _buildSvgButton(gitSvg, border: false, identifier: "Git"),
            _buildSvgButton(selfHealingSvg, border: false, identifier: "Self Healing"),
            _buildSvgButton(studioAssistSvg, border: true, separatorColor: separatorColor, identifier: "StudioAssist"),

            _buildNewTestCaseButton(
              backgroundColor: backgroundColor,
              newTestCaseBgColor: newTestCaseBgColor,
              newTestCaseTextColor: newTestCaseTextColor,
            ),

            const Spacer(),

            _buildSvgButton(consoleSvg, border: true, separatorColor: separatorColor, identifier: "Build CMD"),
            _buildSvgButtonWithDropdown(recordWebSvg, border: false, identifier: "Record Web"),
            _buildSvgButtonWithDropdown(spyWebSvg, border: false, identifier: "Spy Web"),

            Container(
              height: 22,
              width: 1.5,
              margin: const EdgeInsets.symmetric(vertical: 9),
              color: separatorColor,
            ),

            _buildSvgButtonWithDropdown(playSvg, border: false, identifier: "Run"),
            _buildSvgButton(stopSvg, border: false, identifier: "Stop"),
            _buildSvgButtonWithDropdown(bugSvg, border: true, separatorColor: separatorColor, identifier: "Debug"),

            _buildKeywordDebugButton(
              keywordDebugBgColor: keywordDebugBgColor,
              debugTextColor: debugTextColor,
              keywordButtonBgColor: keywordButtonBgColor,
              keywordButtonTextColor: keywordButtonTextColor,
              lightThemeIconBorder: lightThemeIconBorder,
            ),

            Container(
              height: 22,
              width: 1.5,
              margin: const EdgeInsets.symmetric(horizontal: 2, vertical: 9),
              color: separatorColor,
            ),

            _buildSvgButton(notificationSvg, border: false, identifier: "Eclipse Platform integration"),
            _buildSvgButtonWithDropdown(userSvg, border: false, identifier: "User"),
          ],
        ),
      ),
    );
  }

  // Helper method to find state by tooltip or text
  VToolItem? _findStateByIdentifier(String identifier) {
    var r = _findStateByIdentifierComposite(identifier, state);
    print("${r != null ? "Found" : "Not Found"} identifier!!! ${identifier}");
    return r;
  }

  VToolItem? _findStateByIdentifierComposite(String identifier, VComposite comp) {
    if (comp.children == null) return null;
    for (var toolbar in comp.children!) {
      if (toolbar is VToolBar) {
        final toolbarValue = toolbar;
        if (toolbarValue.items != null) {
          for (var toolItem in toolbar.items!) {
            if (toolItem.toolTipText?.contains(identifier) == true ||
                toolItem.text?.contains(identifier) == true ||
                (identifier == "Self Healing" && toolItem.toolTipText?.contains("healing") == true) ||
                (identifier == "Record Web" && toolItem.toolTipText?.contains("record") == true) ||
                (identifier == "Spy Web" && toolItem.toolTipText?.contains("spy") == true) ||
                (identifier == "User" && toolItem.toolTipText?.isEmpty == true && toolItem.text?.isEmpty == true)) {
              return toolItem;
            }
          }
        }
      }
      else if (toolbar is VComposite) {
        var r = _findStateByIdentifierComposite(identifier, toolbar);
        if (r != null) return r;
      }
    }
    return null;
  }

  Widget _buildSvgButton(String svgPath, {bool border = false, Color? separatorColor, String? identifier}) {
    final VToolItem? toolItemState = identifier != null ? _findStateByIdentifier(identifier) : null;
    final bool isEnabled = toolItemState?.enabled ?? true;

    final Widget svgImage = SvgPicture.asset(
      svgPath,
      width: 20,
      height: 20,
      colorFilter: isEnabled
          ? null
          : ColorFilter.mode(Colors.grey.shade400, BlendMode.srcIn),
    );

    Widget svgBtn = Focus(
      onFocusChange: (hasFocus) {
        if (toolItemState != null) {
          if (hasFocus) {
            handleFocusIn_(toolItemState);
          } else {
            handleFocusOut_(toolItemState);
          }
        }
      },
      child: Material(
        color: Colors.transparent,
        child: InkWell(
          onTap: () {
            print('Button enabled: $isEnabled, State found $identifier: ${toolItemState != null}');
            if (isEnabled && toolItemState != null) {
              onPressed_(toolItemState);
            }
          },
          hoverColor: useDarkTheme ? const Color(0xFF3D3D3D) : const Color(0xFFE0E0E0),
          borderRadius: BorderRadius.circular(4),
          child: Container(
            padding: const EdgeInsets.all(8),
            constraints: const BoxConstraints(minHeight: 28, minWidth: 36),
            child: Center(child: svgImage),
          ),
        ),
      ),
    );

    if (border) {
      return Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          svgBtn,
          Container(
            height: 22,
            width: 1.5,
            margin: const EdgeInsets.symmetric(vertical: 9),
            color: separatorColor ?? const Color(0xFF3B3B3B),
          ),
        ],
      );
    }

    return svgBtn;
  }

  Widget _buildSvgButtonWithDropdown(String svgPath, {bool border = false, Color? separatorColor, String? identifier}) {
    final VToolItem? toolItemState = identifier != null ? _findStateByIdentifier(identifier) : null;
    final bool isEnabled = toolItemState?.enabled ?? true;

    final Widget svgImage = SvgPicture.asset(svgPath,
      width: 20,
      height: 20,
      colorFilter: isEnabled
          ? null
          : ColorFilter.mode(Colors.grey.shade400, BlendMode.srcIn),
    );

    final Widget dropdownIcon = Icon(
      Icons.expand_more,
      color: useDarkTheme ? Colors.white : const Color(0xFF444444),
      size: 12,
    );

    Widget buttonContent = Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        svgImage,
        Padding(
          padding: const EdgeInsets.only(left: 1),
          child: dropdownIcon,
        ),
      ],
    );

    Widget clickableBtn = Focus(
      onFocusChange: (hasFocus) {
        if (toolItemState != null) {
          if (hasFocus) {
            handleFocusIn_(toolItemState);
          } else {
            handleFocusOut_(toolItemState);
          }
        }
      },
      child: Material(
        color: Colors.transparent,
        child: InkWell(
          onTap: () {
            print('Button enabled: $isEnabled, State found $identifier: ${toolItemState != null}');
            if (isEnabled && toolItemState != null) {
              onPressed_(toolItemState);
            }
          },
          hoverColor: useDarkTheme ? const Color(0xFF3D3D3D) : const Color(0xFFE0E0E0),
          borderRadius: BorderRadius.circular(4),
          child: Container(
            padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 8),
            child: buttonContent,
          ),
        ),
      ),
    );

    if (border) {
      return Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          clickableBtn,
          Container(
            height: 22,
            width: 1.5,
            margin: const EdgeInsets.symmetric(vertical: 9),
            color: separatorColor ?? const Color(0xFF3B3B3B),
          ),
        ],
      );
    }

    return clickableBtn;
  }

  Widget _buildNewTestCaseButton({
    required Color backgroundColor,
    required Color newTestCaseBgColor,
    required Color newTestCaseTextColor,
  }) {
    final VToolItem? toolItemState = _findStateByIdentifier("Create new Test Case");
    final bool isEnabled = toolItemState?.enabled ?? true;

    return Focus(
      onFocusChange: (hasFocus) {
        if (toolItemState != null) {
          if (hasFocus) {
            handleFocusIn_(toolItemState);
          } else {
            handleFocusOut_(toolItemState);
          }
        }
      },
      child: Material(
        color: Colors.transparent,
        child: InkWell(
          onTap: () {
            print('Button enabled: $isEnabled, State found: ${toolItemState != null}');
            if (isEnabled && toolItemState != null) {
              onPressed_(toolItemState);
            }
          },
          hoverColor: Colors.black12,
          borderRadius: BorderRadius.circular(5),
          child: Container(
            height: 24,
            margin: const EdgeInsets.symmetric(horizontal: 8, vertical: 8),
            decoration: BoxDecoration(
              color: isEnabled ? newTestCaseBgColor : Colors.grey.shade400,
              borderRadius: BorderRadius.circular(5),
            ),
            child: Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 6.0),
                  child: Text(
                    'New Test Case',
                    style: TextStyle(
                      color: newTestCaseTextColor,
                      fontSize: 12.5,
                      fontFamily: 'Segoe UI',
                      fontWeight: FontWeight.w400,
                    ),
                  ),
                ),
                Container(
                  height: 24,
                  width: 1,
                  color: backgroundColor,
                ),
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 4),
                  child: Icon(
                    Icons.keyboard_arrow_down,
                    color: newTestCaseTextColor,
                    size: 14,
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildKeywordDebugButton({
    required Color keywordDebugBgColor,
    required Color debugTextColor,
    required Color keywordButtonBgColor,
    required Color keywordButtonTextColor,
    required Color lightThemeIconBorder,
  }) {
    final VToolItem? toolItemState = _findStateByIdentifier("StudioAssist");
    final bool isEnabled = toolItemState?.enabled ?? true;

    return Focus(
      onFocusChange: (hasFocus) {
        if (toolItemState != null) {
          if (hasFocus) {
            handleFocusIn_(toolItemState);
          } else {
            handleFocusOut_(toolItemState);
          }
        }
      },
      child: Material(
        color: Colors.transparent,
        child: InkWell(
          onTap: () {
            print('Button enabled: $isEnabled, State found: ${toolItemState != null}');
            if (isEnabled && toolItemState != null) {
              onPressed_(toolItemState);
            }
          },
          hoverColor: useDarkTheme ? const Color(0xFF3D3D3D) : const Color(0xFFE0E0E0),
          borderRadius: BorderRadius.circular(4),
          child: Container(
            height: 22,
            padding: const EdgeInsets.symmetric(horizontal: 1, vertical: 1),
            margin: const EdgeInsets.symmetric(horizontal: 4, vertical: 9),
            decoration: BoxDecoration(
              color: keywordDebugBgColor,
              borderRadius: BorderRadius.circular(4),
              border: !useDarkTheme ? Border.all(color: lightThemeIconBorder, width: 1) : null,
            ),
            child: Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                // Keyword part
                Container(
                  height: 19,
                  width: 56,
                  padding: const EdgeInsets.symmetric(horizontal: 2, vertical: 0),
                  decoration: BoxDecoration(
                    color: isEnabled ? keywordButtonBgColor : Colors.grey.shade400,
                    borderRadius: BorderRadius.circular(4),
                  ),
                  child: Center(
                    child: FittedBox(
                      fit: BoxFit.fill,
                      child: Text(
                        'Keyword',
                        style: TextStyle(
                          color: keywordButtonTextColor,
                          fontSize: 11.5,
                          fontFamily: 'Segoe UI',
                          fontWeight: FontWeight.w400,
                        ),
                      ),
                    ),
                  ),
                ),

                // Debug part
                Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 6),
                  child: Text(
                    'Debug',
                    style: TextStyle(
                      color: isEnabled ? debugTextColor : Colors.grey.shade400,
                      fontSize: 11.5,
                      fontFamily: 'Segoe UI',
                      fontWeight: FontWeight.w500,
                    ),
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  void onPressed_(VToolItem toolItemState) {
    ToolItemSwt(value: toolItemState).sendSelectionSelection(toolItemState, null);
  }

  void handleFocusIn_(VToolItem toolItemState) {
    widget.sendFocusFocusIn(state, null);
  }

  void handleFocusOut_(VToolItem toolItemState) {
    widget.sendFocusFocusOut(state, null);
  }
}
