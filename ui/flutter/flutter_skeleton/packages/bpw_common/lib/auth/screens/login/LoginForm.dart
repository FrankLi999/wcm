import 'package:flutter/material.dart';
import 'package:material_design_icons_flutter/material_design_icons_flutter.dart';
import 'package:flutter_i18n/flutter_i18n.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../../../theme/AppTheme.dart';
import '../../../utils/SizeConfig.dart';
import '../../bloc/login/login_bloc.dart';

class LoginForm extends StatefulWidget {
  @override
  _LoginFormState createState() => _LoginFormState();
}

class _LoginFormState extends State<LoginForm> {
  ThemeData themeData;
  bool _passwordVisible = false;
  final _usernameController = TextEditingController();
  final _passwordController = TextEditingController();

  @override
  Widget build(BuildContext context) {
    themeData = Theme.of(context);
    _onLoginButtonPressed() {
      BlocProvider.of<LoginBloc>(context).add(LoginButtonPressed(
        username: _usernameController.text,
        password: _passwordController.text,
      ));
    }

    return BlocListener<LoginBloc, LoginState>(listener: (context, state) {
      if (state is LoginFaliure) {
        Scaffold.of(context).showSnackBar(SnackBar(
          content: Text('${state.error}'),
          backgroundColor: Colors.red,
        ));
      }
    }, child: BlocBuilder<LoginBloc, LoginState>(builder: (context, state) {
      return Scaffold(
          backgroundColor: themeData.scaffoldBackgroundColor,
          appBar: AppBar(
            title: Text(
              FlutterI18n.translate(context, "lang.text_login"),
              // "Login", //lang.text_login
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
          ),
          body: Container(
            padding: EdgeInsets.only(left: 24, right: 24),
            child: Form(
              child: Padding(
                // shrinkWrap: true,
                padding: EdgeInsets.all(40.0),
                child: Column(children: <Widget>[
                  Container(
                    child: Text(
                      FlutterI18n.translate(context, "lang.text_Log_In"),
                      style: AppTheme.getTextStyle(
                          themeData.textTheme.headline5,
                          fontWeight: 600,
                          letterSpacing: 0),
                    ),
                  ),
                  Container(
                    margin: EdgeInsets.only(top: MySize.size24),
                    child: TextFormField(
                      controller: _usernameController,
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
                          size: 22,
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
                      controller: _passwordController,
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
                    margin: EdgeInsets.only(top: MySize.size16),
                    alignment: Alignment.centerRight,
                    child: GestureDetector(
                      onTap: () {
                        Navigator.of(context)
                            .pushReplacementNamed('/auth/reset_password');
                        // _bloc.select('/auth/reset_password');
                      },
                      child: Text(
                        FlutterI18n.translate(
                                context, "lang.text_forgot_password") +
                            " ?",
                        style: AppTheme.getTextStyle(
                            themeData.textTheme.caption,
                            fontWeight: 500),
                      ),
                    ),
                  ),
                  Container(
                    margin: EdgeInsets.only(top: MySize.size24),
                    child: Row(
                      children: <Widget>[
                        Expanded(
                          child: Container(
                            decoration: BoxDecoration(
                              borderRadius:
                                  BorderRadius.all(Radius.circular(28)),
                              boxShadow: [
                                BoxShadow(
                                  color: themeData.cardTheme.shadowColor
                                      .withAlpha(18),
                                  blurRadius: 4,
                                  offset: Offset(
                                      0, 3), // changes position of shadow
                                ),
                              ],
                            ),
                            child: FlatButton(
                              shape: RoundedRectangleBorder(
                                  borderRadius: BorderRadius.circular(28)),
                              color: themeData.backgroundColor,
                              splashColor: themeData.colorScheme.primary,
                              highlightColor: themeData.backgroundColor,
                              onPressed: state is! LoginLoading
                                  ? _onLoginButtonPressed
                                  : null,
                              child: Row(
                                mainAxisSize: MainAxisSize.min,
                                children: <Widget>[
                                  Text(
                                    FlutterI18n.translate(
                                            context, "lang.text_login")
                                        .toUpperCase(),
                                    style: AppTheme.getTextStyle(
                                        themeData.textTheme.caption,
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
                                      size: 18,
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
                              .pushReplacementNamed('/auth/registration');
                          // _bloc.select('/auth/registration');
                        },
                        child: Text(
                          FlutterI18n.translate(
                              context, "lang.text_i_have_not_an_account"),
                          style: AppTheme.getTextStyle(
                              themeData.textTheme.bodyText2,
                              decoration: TextDecoration.underline),
                        ),
                      ),
                    ),
                  ),
                ]),
              ),
            ),
          ));
    }));
  }
}
