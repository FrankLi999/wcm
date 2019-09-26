import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { FlexLayoutModule } from '@angular/flex-layout';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatRippleModule } from '@angular/material/core';
import { MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatMenuModule } from '@angular/material/menu';
import { MatSelectModule } from '@angular/material/select';
import { MatToolbarModule } from '@angular/material/toolbar';

import { MatChipsModule } from '@angular/material/chips';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatSortModule } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { MatTabsModule } from '@angular/material/tabs';

import { TranslateModule } from '@ngx-translate/core';

import { FuseSharedModule, FuseSidebarModule } from 'bpw-components';

import { RenderTemplateComponent } from './render-template/render-template.component';
import { AceEditorModule } from 'ng2-ace-editor';
import { SafePipe } from './pipes/safe.pipe';
import { RenderTemplatesComponent } from './render-templates/render-templates.component';
import * as fromGuards from '../store/guards';
import { AuthGuard } from 'bpw-auth';

const routes: Routes = [
  {
      path       : 'render-template/edit',
      component  : RenderTemplateComponent,
      canActivate: [AuthGuard, fromGuards.ResolveGuard]
  },
  {
      path       : 'render-template/list',
      component  : RenderTemplatesComponent,
      canActivate: [AuthGuard, fromGuards.ResolveGuard]
  }
];


@NgModule({
  declarations: [
    RenderTemplateComponent,
    SafePipe,
    RenderTemplatesComponent
  ],
  imports: [
    RouterModule.forChild(routes),
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    FlexLayoutModule,
    AceEditorModule,
    MatButtonModule,
    MatCheckboxModule,
    MatDialogModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatMenuModule,
    MatRippleModule,
    MatSelectModule,
    MatToolbarModule,
    MatChipsModule,
    MatExpansionModule,
    MatPaginatorModule,
    MatSortModule,
    MatSnackBarModule,
    MatTableModule,
    MatTabsModule,
    TranslateModule,
    FuseSharedModule,
    FuseSidebarModule
  ]
})
export class RenderTemplateModule { }