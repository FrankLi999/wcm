import 'package:flutter/widgets.dart';
import '../../bpw_renderer.dart';
import '../utils.dart';

class PlaceholderWidgetParser extends WidgetParser {
  @override
  Widget parse(Map<String, dynamic> map, BuildContext buildContext,
      ClickListener listener) {
    return Placeholder(
      color: map.containsKey('color')
          ? parseHexColor(map['color'])
          : const Color(0xFF455A64),
      strokeWidth: map.containsKey('strokeWidth') ? map['strokeWidth'] : 2.0,
      fallbackWidth:
          map.containsKey('fallbackWidth') ? map['fallbackWidth'] : 400.0,
      fallbackHeight:
          map.containsKey('fallbackHeight') ? map['fallbackHeight'] : 400.0,
    );
  }

  @override
  String get widgetName => "Placeholder";
}
