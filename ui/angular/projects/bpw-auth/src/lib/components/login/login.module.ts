import { NgModule } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule, ReactiveFormsModule } from "@angular/forms";
import { FlexLayoutModule } from "@angular/flex-layout";
import { RouterModule } from "@angular/router";
import { MatButtonModule } from "@angular/material/button";
import { MatCheckboxModule } from "@angular/material/checkbox";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatIconModule } from "@angular/material/icon";
import { MatInputModule } from "@angular/material/input";
import { PerfectScrollbarModule } from "ngx-perfect-scrollbar";
import { AuthStoreModule, SharedUIModule } from "bpw-common";
import { RestClientModule } from "bpw-rest-client";
import { LoginComponent } from "./login/login.component";

// const routes = [
//   {
//     path: "login",
//     component: LoginComponent,
//   },
// ];

@NgModule({
  declarations: [LoginComponent],
  imports: [
    RestClientModule,
    RouterModule, //.forChild(routes),
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatCheckboxModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    SharedUIModule,
    FlexLayoutModule,
    AuthStoreModule,
    PerfectScrollbarModule,
  ],
  exports: [LoginComponent],
})
export class LoginModule {}
