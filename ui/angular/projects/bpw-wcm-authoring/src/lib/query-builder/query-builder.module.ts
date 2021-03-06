import { NgModule } from "@angular/core";
import { RouterModule } from "@angular/router";
import { CommonModule } from "@angular/common";
import { FormsModule, ReactiveFormsModule } from "@angular/forms";
import { FlexLayoutModule } from "@angular/flex-layout";
import { ScrollingModule } from "@angular/cdk/scrolling";
import { CdkTableModule } from "@angular/cdk/table";
import { CdkTreeModule } from "@angular/cdk/tree";
import { MatButtonModule } from "@angular/material/button";
import { MatCardModule } from "@angular/material/card";
import { MatCheckboxModule } from "@angular/material/checkbox";
import { MatDatepickerModule } from "@angular/material/datepicker";
import { MatProgressSpinnerModule } from "@angular/material/progress-spinner";
import { MatRippleModule } from "@angular/material/core";
import { MatDialogModule } from "@angular/material/dialog";
import { MatDividerModule } from "@angular/material/divider";
import { MatExpansionModule } from "@angular/material/expansion";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatGridListModule } from "@angular/material/grid-list";
import { MatIconModule } from "@angular/material/icon";
import { MatInputModule } from "@angular/material/input";
import { MatListModule } from "@angular/material/list";
import { MatMenuModule } from "@angular/material/menu";
import { MatPaginatorModule } from "@angular/material/paginator";
import { MatSelectModule } from "@angular/material/select";
import { MatSortModule } from "@angular/material/sort";
import { MatToolbarModule } from "@angular/material/toolbar";
import { MatTableModule } from "@angular/material/table";
import { MatTabsModule } from "@angular/material/tabs";
import { MatTreeModule } from "@angular/material/tree";
import { MatNativeDateModule } from "@angular/material/core";
import { MatRadioModule } from "@angular/material/radio";
import { MatChipsModule } from "@angular/material/chips";
import { MatSnackBarModule } from "@angular/material/snack-bar";

import { TranslateModule } from "@ngx-translate/core";
import { PerfectScrollbarModule } from "ngx-perfect-scrollbar";
import { SharedUIModule, SidebarModule } from "bpw-common";
import { QueryListComponent } from "./query-list/query-list.component";
import { QueryEditorComponent } from "./query-editor/query-editor.component";
import { SearchCriteriaComponent } from "./search-criteria/search-criteria.component";

import {
  QueryBuilderComponent,
  QueryInputDirective,
  QueryFieldDirective,
  QueryEntityDirective,
  QueryOperatorDirective,
  QueryButtonGroupDirective,
  QuerySwitchGroupDirective,
  QueryRemoveButtonDirective,
  QueryEmptyWarningDirective,
  QueryArrowIconDirective,
} from "./query-builder";
import { QueryTreeComponent } from "./query-tree/query-tree.component";
import { QueryStatementComponent } from "./query-statement/query-statement.component";
import { JsonSchemaFormModule } from "@bpw/ajsf-core";
import { MaterialDesignFrameworkModule } from "@bpw/ajsf-material";
import { QueryHistoryComponent } from "./query-history/query-history.component";
import { QueryPermissionsComponent } from "./query-permissions/query-permissions.component";
import { ComponentModule } from "../components/component.module";
import { AclModule } from "../components/acl/acl.module";
import { HistoryModule } from "../components/history/history.module";
import { WcmFormWidgetModule } from "../wcm-form-widget/wcm-form-widget.module";

@NgModule({
  declarations: [
    QueryListComponent,
    QueryEditorComponent,
    SearchCriteriaComponent,

    QueryBuilderComponent,
    QueryInputDirective,
    QueryOperatorDirective,
    QueryFieldDirective,
    QueryEntityDirective,
    QueryButtonGroupDirective,
    QuerySwitchGroupDirective,
    QueryRemoveButtonDirective,
    QueryEmptyWarningDirective,
    QueryArrowIconDirective,
    QueryTreeComponent,
    QueryStatementComponent,
    QueryHistoryComponent,
    QueryPermissionsComponent,
  ],
  imports: [
    RouterModule,
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    FlexLayoutModule,
    ScrollingModule,
    CdkTableModule,
    CdkTreeModule,
    MatChipsModule,
    MatButtonModule,
    MatCardModule,
    MatCheckboxModule,
    MatDatepickerModule,
    MatDialogModule,
    MatDividerModule,
    MatExpansionModule,
    MatFormFieldModule,
    MatGridListModule,
    MatIconModule,
    MatInputModule,
    MatListModule,
    MatMenuModule,
    MatPaginatorModule,
    MatProgressSpinnerModule,
    MatRippleModule,
    MatSelectModule,
    MatSortModule,
    MatTabsModule,
    MatTableModule,
    MatToolbarModule,
    MatTreeModule,
    MatNativeDateModule,
    MatRadioModule,
    MatSnackBarModule,
    PerfectScrollbarModule,
    TranslateModule.forChild({
      extend: true,
    }),

    SharedUIModule,
    SidebarModule,
    JsonSchemaFormModule,
    MaterialDesignFrameworkModule,
    ComponentModule,
    AclModule,
    HistoryModule,
    WcmFormWidgetModule,
  ],
  exports: [
    QueryListComponent,
    QueryEditorComponent,
    SearchCriteriaComponent,

    QueryBuilderComponent,
    QueryInputDirective,
    QueryOperatorDirective,
    QueryFieldDirective,
    QueryEntityDirective,
    QueryButtonGroupDirective,
    QuerySwitchGroupDirective,
    QueryRemoveButtonDirective,
    QueryEmptyWarningDirective,
    QueryArrowIconDirective,
    QueryTreeComponent,
  ],
})
export class QueryBuilderModule {}
