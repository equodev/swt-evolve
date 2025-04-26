import 'package:flutter/material.dart';

final Map<String, IconData> materialIconMap = {
  // Basic UI Icons
  'home': Icons.home,
  'menu': Icons.menu,
  'search': Icons.search,
  'settings': Icons.settings,
  'person': Icons.person,

  // Navigation Icons
  'arrow_back': Icons.arrow_back,
  'arrow_forward': Icons.arrow_forward,
  'close': Icons.close,
  'more_vert': Icons.more_vert,
  'more_horiz': Icons.more_horiz,
  'check': Icons.check,
  'done': Icons.done,
  'navigate_next': Icons.navigate_next,
  'navigate_before': Icons.navigate_before,

  // Actions Icons
  'add': Icons.add,
  'remove': Icons.remove,
  'edit': Icons.edit,
  'delete': Icons.delete,
  'favorite': Icons.favorite,
  'favorite_border': Icons.favorite_border,
  'share': Icons.share,
  'bookmark': Icons.bookmark,
  'bookmark_border': Icons.bookmark_border,

  // Communication Icons
  'email': Icons.email,
  'phone': Icons.phone,
  'chat': Icons.chat,
  'message': Icons.message,
  'notifications': Icons.notifications,
  'notifications_none': Icons.notifications_none,

  // Media Icons
  'play_arrow': Icons.play_arrow,
  'pause': Icons.pause,
  'stop': Icons.stop,
  'skip_next': Icons.skip_next,
  'skip_previous': Icons.skip_previous,
  'volume_up': Icons.volume_up,
  'volume_off': Icons.volume_off,
  'image': Icons.image,
  'video_library': Icons.video_library,

  // Content Icons
  'attach_file': Icons.attach_file,
  'copy': Icons.copy,
  'text_fields': Icons.text_fields,
  'format_list_bulleted': Icons.format_list_bulleted,
  'folder': Icons.folder,
  'file_present': Icons.file_present,

  // Device Icons
  'bluetooth': Icons.bluetooth,
  'wifi': Icons.wifi,
  'battery_full': Icons.battery_full,
  'brightness_high': Icons.brightness_high,
  'location_on': Icons.location_on,

  // Social Icons
  'thumb_up': Icons.thumb_up,
  'thumb_down': Icons.thumb_down,
  'star': Icons.star,
  'star_border': Icons.star_border,
  'group': Icons.group,

  // Shopping Icons
  'shopping_cart': Icons.shopping_cart,
  'local_offer': Icons.local_offer,
  'credit_card': Icons.credit_card,
  'store': Icons.store,

  // Alert Icons
  'error': Icons.error,
  'warning': Icons.warning,
  'info': Icons.info,
  'help': Icons.help,
};

IconData? getMaterialIconByName(String name) {
  return materialIconMap[name];
}