import 'package:flutter/material.dart';
import '../../bpw_renderer.dart';
import '../utils.dart';

class RaisedButtonParser extends WidgetParser {
  @override
  Widget parse(Map<String, dynamic> map, BuildContext buildContext,
      ClickListener listener) {
    String clickEvent =
        map.containsKey("click_event") ? map['click_event'] : "";

    var raisedButton = RaisedButton(
      color: map.containsKey('color') ? parseHexColor(map['color']) : null,
      disabledColor: map.containsKey('disabledColor')
          ? parseHexColor(map['disabledColor'])
          : null,
      disabledElevation:
          map.containsKey('disabledElevation') ? map['disabledElevation'] : 0.0,
      disabledTextColor: map.containsKey('disabledTextColor')
          ? parseHexColor(map['disabledTextColor'])
          : null,
      elevation: map.containsKey('elevation') ? map['elevation'] : 0.0,
      padding: map.containsKey('padding')
          ? parseEdgeInsetsGeometry(map['padding'])
          : null,
      splashColor: map.containsKey('splashColor')
          ? parseHexColor(map['splashColor'])
          : null,
      textColor:
          map.containsKey('textColor') ? parseHexColor(map['textColor']) : null,
      child: DynamicWidgetBuilder.buildFromMap(
          map['child'], buildContext, listener),
      onPressed: () {
        listener.onClicked(clickEvent);
      },
    );

    return raisedButton;
  }

  @override
  String get widgetName => "RaisedButton";
}
