import 'package:flutter/material.dart';
import 'package:material_design_icons_flutter/material_design_icons_flutter.dart';
import 'package:provider/provider.dart';
import 'package:flutter_i18n/flutter_i18n.dart';
import '../../../utils/SizeConfig.dart';
import '../../../theme/AppTheme.dart';
import '../../../app_context/AppContextNotifier.dart';

class RegistrationScreen extends StatefulWidget {
  const RegistrationScreen({Key key}) : super(key: key);
  @override
  _RegistrationScreenState createState() => _RegistrationScreenState();
}

class _RegistrationScreenState extends State<RegistrationScreen> {
  ThemeData themeData;
  // NestedNavigatorsBloc _bloc;
  bool _passwordVisible = false;

  @override
  Widget build(BuildContext context) {
    themeData = Theme.of(context);
    return Consumer<AppContextNotifier>(
      builder: (BuildContext context, AppContextNotifier value, Widget child) {
        return Scaffold(
            backgroundColor: themeData.scaffoldBackgroundColor,
            appBar: AppBar(
              title: Text(
                FlutterI18n.translate(context, "lang.text_register"),
              ),
              leading: GestureDetector(
                onTap: () {
                  // NestedNavigatorsBlocProvider.of(context).actionWithScaffold(
                  //     (scaffoldState) => scaffoldState.openDrawer(),
                  //   );
                  Scaffold.of(context).openDrawer();
                },
                child: Icon(
                  Icons.menu, // add custom icons also
                ),
              ),
              // backgroundColor: Colors.blue,
            ),
            body: Container(
              padding:
                  EdgeInsets.only(left: MySize.size24, right: MySize.size24),
              child: Center(
                child: ListView(
                  shrinkWrap: true,
                  children: <Widget>[
                    Container(
                      child: Text(
                        FlutterI18n.translate(
                            context, "lang.text_Create_an_Account"),
                        style: AppTheme.getTextStyle(
                            themeData.textTheme.headline5,
                            fontWeight: 600,
                            letterSpacing: 0),
                      ),
                    ),
                    Container(
                      margin: EdgeInsets.only(top: MySize.size32),
                      child: TextFormField(
                        style: AppTheme.getTextStyle(
                            themeData.textTheme.bodyText1,
                            letterSpacing: 0.1,
                            color: themeData.colorScheme.onBackground,
                            fontWeight: 500),
                        decoration: InputDecoration(
                          hintText: FlutterI18n.translate(
                              context, "lang.text_username"),
                          hintStyle: AppTheme.getTextStyle(
                              themeData.textTheme.bodyText1,
                              letterSpacing: 0.1,
                              color: themeData.colorScheme.onBackground,
                              fontWeight: 500),
                          border: OutlineInputBorder(
                              borderRadius: BorderRadius.all(
                                Radius.circular(MySize.size8),
                              ),
                              borderSide: BorderSide(
                                  color: themeData.colorScheme.surface,
                                  width: 1.2)),
                          enabledBorder: OutlineInputBorder(
                              borderRadius: BorderRadius.all(
                                Radius.circular(MySize.size8),
                              ),
                              borderSide: BorderSide(
                                  color: themeData.colorScheme.surface,
                                  width: 1.2)),
                          focusedBorder: OutlineInputBorder(
                              borderRadius: BorderRadius.all(
                                Radius.circular(MySize.size8),
                              ),
                              borderSide: BorderSide(
                                  color: themeData.colorScheme.surface,
                                  width: 1.2)),
                          prefixIcon: Icon(
                            MdiIcons.accountOutline,
                            size: MySize.size22,
                          ),
                          isDense: true,
                          contentPadding: EdgeInsets.all(0),
                        ),
                        textCapitalization: TextCapitalization.sentences,
                      ),
                    ),
                    Container(
                      margin: EdgeInsets.only(top: MySize.size16),
                      child: TextFormField(
                        style: AppTheme.getTextStyle(
                            themeData.textTheme.bodyText1,
                            letterSpacing: 0.1,
                            color: themeData.colorScheme.onBackground,
                            fontWeight: 500),
                        decoration: InputDecoration(
                          hintText: FlutterI18n.translate(
                              context, "lang.text_email_address"),
                          hintStyle: AppTheme.getTextStyle(
                              themeData.textTheme.subtitle2,
                              letterSpacing: 0.1,
                              color: themeData.colorScheme.onBackground,
                              fontWeight: 500),
                          border: OutlineInputBorder(
                              borderRadius: BorderRadius.all(
                                Radius.circular(MySize.size8),
                              ),
                              borderSide: BorderSide(
                                  color: themeData.colorScheme.surface,
                                  width: 1.2)),
                          enabledBorder: OutlineInputBorder(
                              borderRadius: BorderRadius.all(
                                Radius.circular(MySize.size8),
                              ),
                              borderSide: BorderSide(
                                  color: themeData.colorScheme.surface,
                                  width: 1.2)),
                          focusedBorder: OutlineInputBorder(
                              borderRadius: BorderRadius.all(
                                Radius.circular(MySize.size8),
                              ),
                              borderSide: BorderSide(
                                  color: themeData.colorScheme.surface,
                                  width: 1.2)),
                          prefixIcon: Icon(
                            MdiIcons.emailOutline,
                            size: MySize.size22,
                          ),
                          isDense: true,
                          contentPadding: EdgeInsets.all(0),
                        ),
                        keyboardType: TextInputType.emailAddress,
                        textCapitalization: TextCapitalization.sentences,
                      ),
                    ),
                    Container(
                      margin: EdgeInsets.only(top: MySize.size16),
                      child: TextFormField(
                        obscureText: _passwordVisible,
                        style: AppTheme.getTextStyle(
                            themeData.textTheme.bodyText1,
                            letterSpacing: 0.1,
                            color: themeData.colorScheme.onBackground,
                            fontWeight: 500),
                        decoration: InputDecoration(
                          hintStyle: AppTheme.getTextStyle(
                              themeData.textTheme.subtitle2,
                              letterSpacing: 0.1,
                              color: themeData.colorScheme.onBackground,
                              fontWeight: 500),
                          hintText: FlutterI18n.translate(
                              context, "lang.text_password"),
                          border: OutlineInputBorder(
                              borderRadius: BorderRadius.all(
                                Radius.circular(MySize.size8),
                              ),
                              borderSide: BorderSide(
                                  color: themeData.colorScheme.surface,
                                  width: 1.2)),
                          enabledBorder: OutlineInputBorder(
                              borderRadius: BorderRadius.all(
                                Radius.circular(MySize.size8),
                              ),
                              borderSide: BorderSide(
                                  color: themeData.colorScheme.surface,
                                  width: 1.2)),
                          focusedBorder: OutlineInputBorder(
                              borderRadius: BorderRadius.all(
                                Radius.circular(MySize.size8),
                              ),
                              borderSide: BorderSide(
                                  color: themeData.colorScheme.surface,
                                  width: 1.2)),
                          prefixIcon: Icon(
                            MdiIcons.lockOutline,
                            size: MySize.size22,
                          ),
                          suffixIcon: InkWell(
                            onTap: () {
                              setState(() {
                                _passwordVisible = !_passwordVisible;
                              });
                            },
                            child: Icon(
                              _passwordVisible
                                  ? MdiIcons.eyeOutline
                                  : MdiIcons.eyeOffOutline,
                              size: MySize.size22,
                            ),
                          ),
                          isDense: true,
                          contentPadding: EdgeInsets.all(0),
                        ),
                        textCapitalization: TextCapitalization.sentences,
                      ),
                    ),
                    Container(
                      margin: EdgeInsets.only(top: MySize.size24),
                      child: Row(
                        children: <Widget>[
                          Expanded(
                            child: Container(
                              decoration: BoxDecoration(
                                borderRadius: BorderRadius.all(
                                    Radius.circular(MySize.size28)),
                                boxShadow: [
                                  BoxShadow(
                                    color: themeData.cardTheme.shadowColor
                                        .withAlpha(18),
                                    blurRadius: 4,
                                    offset: Offset(0, 3),
                                  ),
                                ],
                              ),
                              child: FlatButton(
                                shape: RoundedRectangleBorder(
                                    borderRadius:
                                        BorderRadius.circular(MySize.size28)),
                                color: themeData.backgroundColor,
                                splashColor: themeData.colorScheme.primary,
                                highlightColor: themeData.backgroundColor,
                                onPressed: () {
                                  // TODO: update userRepository and add loggedIn event
                                  Navigator.of(context)
                                      .pushReplacementNamed('/mysite/home');
                                },
                                child: Row(
                                  mainAxisSize: MainAxisSize.min,
                                  children: <Widget>[
                                    Text(
                                      FlutterI18n.translate(
                                              context, "lang.text_create")
                                          .toUpperCase(),
                                      style: AppTheme.getTextStyle(
                                          themeData.textTheme.bodyText2,
                                          fontWeight: 600,
                                          color: themeData.colorScheme.primary,
                                          letterSpacing: 0.5),
                                    ),
                                    Container(
                                      margin:
                                          EdgeInsets.only(left: MySize.size16),
                                      child: Icon(
                                        MdiIcons.chevronRight,
                                        color: themeData.colorScheme.primary,
                                        size: MySize.size18,
                                      ),
                                    )
                                  ],
                                ),
                                padding: EdgeInsets.only(
                                    top: MySize.size12, bottom: MySize.size12),
                              ),
                            ),
                          ),
                          Container(
                            margin: EdgeInsets.only(left: MySize.size32),
                            child: ClipOval(
                              child: Material(
                                color: Color(0xffe33239),
                                child: InkWell(
                                  splashColor: Colors.white.withAlpha(100),
                                  highlightColor: themeData.colorScheme.primary,
                                  child: SizedBox(
                                      width: MySize.size36,
                                      height: MySize.size36,
                                      child: Icon(MdiIcons.google,
                                          color: Colors.white,
                                          size: MySize.size20)),
                                  onTap: () {},
                                ),
                              ),
                            ),
                          ),
                          Container(
                            margin: EdgeInsets.only(left: MySize.size16),
                            child: ClipOval(
                              child: Material(
                                color: Color(0xff335994),
                                child: InkWell(
                                  splashColor: Colors.white.withAlpha(100),
                                  highlightColor: themeData.colorScheme.primary,
                                  child: SizedBox(
                                      width: MySize.size36,
                                      height: MySize.size36,
                                      child: Center(
                                          child: Text(
                                        "F",
                                        style: AppTheme.getTextStyle(
                                            themeData.textTheme.headline6,
                                            fontSize: MySize.size20,
                                            color: Colors.white,
                                            fontWeight: 600,
                                            letterSpacing: 0),
                                      ))),
                                  onTap: () {},
                                ),
                              ),
                            ),
                          ),
                        ],
                      ),
                    ),
                    Container(
                      margin: EdgeInsets.only(top: MySize.size16),
                      child: Center(
                        child: GestureDetector(
                          onTap: () {
                            Navigator.of(context)
                                .pushReplacementNamed('/auth/login');
                            // _bloc.select('/auth/login');
                          },
                          child: Text(
                            FlutterI18n.translate(
                                context, "lang.text_i_have_already_account"),
                            style: AppTheme.getTextStyle(
                                themeData.textTheme.bodyText2,
                                decoration: TextDecoration.underline),
                          ),
                        ),
                      ),
                    ),
                  ],
                ),
              ),
            ));
      },
    );
  }
}
