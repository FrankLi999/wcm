import { Component, OnInit, OnDestroy, ViewEncapsulation } from "@angular/core";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { Router } from "@angular/router";
import { select, Store } from "@ngrx/store";
import {
  authLayoutConfig,
  UIConfigService,
  wcmAnimations,
  UiService,
} from "bpw-common";
import * as authStore from "bpw-common";
import { takeUntil, filter } from "rxjs/operators";
import { Subject } from "rxjs";
import { ApiConfigService } from "bpw-rest-client";

import { appConfig } from "bpw-common";
// declare var appConfig: any;
@Component({
  selector: "login",
  templateUrl: "./login.component.html",
  styleUrls: ["./login.component.scss"],
  encapsulation: ViewEncapsulation.None,
  animations: wcmAnimations,
})
export class LoginComponent implements OnInit, OnDestroy {
  loginForm: FormGroup;
  googleLogo = "assets/images/social-login/google-logo.png";
  googleLogin = "";
  fbLogo = "assets/images/social-login/fb-logo.png";
  facebookLogin = "";
  githubLogo = "assets/images/social-login/github-logo.png";
  githubLogin: string = "";
  loginError: string = "";
  private unsubscribeAll: Subject<any>;

  /**
   * @param _uiConfigService
   * @param _formBuilder
   * @param apiConfigService
   * @param ApiConfigService
   * @param router
   * @param store
   */
  constructor(
    private apiConfigService: ApiConfigService,
    private _uiConfigService: UIConfigService,
    private _formBuilder: FormBuilder,

    //private http: HttpClient,
    private router: Router,
    private store: Store<authStore.AuthState>,
    private uiService: UiService
  ) {
    this.unsubscribeAll = new Subject<any>();
    // Configure the layout
    this._uiConfigService.config = {
      ...authLayoutConfig,
    };
  }

  // -----------------------------------------------------------------------------------------------------
  // @ Lifecycle hooks
  // -----------------------------------------------------------------------------------------------------

  /**
   * On init
   */
  ngOnInit(): void {
    this.googleLogin = this.apiConfigService.apiConfig.googleAuthUrl;
    this.facebookLogin = this.apiConfigService.apiConfig.facebookAuthUrl;
    this.githubLogin = this.apiConfigService.apiConfig.githubAuthUrl;

    this.loginForm = this._formBuilder.group({
      email: ["", [Validators.required, Validators.email]],
      password: ["", Validators.required],
    });

    this.store
      .pipe(
        select(authStore.getLoginError),
        filter((loginError) => !!loginError),
        takeUntil(this.unsubscribeAll)
      )
      .subscribe((loginError: any) => {
        this.loginError = this.uiService.getErrorMessage(loginError);
      });
    this.store
      .pipe(
        select(authStore.isLoggedIn),
        filter((loggedIn) => loggedIn),
        takeUntil(this.unsubscribeAll)
      )
      // flatMap(loggedIn => this.store.select(getRouteSnapshot)))
      .subscribe(
        (loggedIn) => {
          this.router.navigateByUrl(
            appConfig && appConfig.defaultUrl
              ? appConfig.defaultUrl
              : "/oauth2/profile"
          );
        }
        // (routeSnapshotOrError: RouteSnapshot) => {
        //     let redirectUrl = (routeSnapshotOrError as RouteSnapshot).paramMap['url'];
        //     redirectUrl = redirectUrl || '/oauth2/profile';
        //     this.router.navigateByUrl(redirectUrl);
        // }
      );
  }

  /**
   * On destroy
   */
  ngOnDestroy(): void {
    this.unsubscribeAll.next();
    this.unsubscribeAll.complete();
    this.loginError &&
      this.store.dispatch(new authStore.LoginClearErrorAction());
  }

  handleLogin() {
    this.store.dispatch(
      new authStore.LoginAction({
        email: this.loginForm.value.email,
        password: this.loginForm.value.password,
      })
    );
  }
}
