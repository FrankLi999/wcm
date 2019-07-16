import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ScrollingModule } from '@angular/cdk/scrolling';
import { CdkTableModule } from '@angular/cdk/table';
import { CdkTreeModule } from '@angular/cdk/tree';
import { NgxDnDModule } from '@swimlane/ngx-dnd';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatRippleModule } from '@angular/material/core';
import { MatDialogModule } from '@angular/material/dialog';
import { MatDividerModule } from '@angular/material/divider';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatGridListModule } from '@angular/material/grid-list';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatListModule } from '@angular/material/list';
import { MatMenuModule } from '@angular/material/menu';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { MatSortModule } from '@angular/material/sort';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatTableModule } from '@angular/material/table';
import { MatTabsModule } from '@angular/material/tabs';
import { MatTreeModule } from '@angular/material/tree';
import { TranslateModule } from '@ngx-translate/core';

import {DragDropModule} from '@angular/cdk/drag-drop';

import { FuseSharedModule } from '@fuse/shared.module';
import { FuseSidebarModule } from '@fuse/components';

import { ResourceTypeListComponent } from './resource-type-list/resource-type-list.component';
import { ResourceTypeEditorComponent } from './resource-type-editor/resource-type-editor.component';
import { 
  ResourceTypeLayoutComponent, 
  ResourceTypeDialog, 
  ResourceFieldDialog,
  StepEditorDialog,
  TabEditorDialog } from './resource-type-layout/resource-type-layout.component';

const routes: Routes = [
    {
        path       : 'resource-type/edit',
        component  : ResourceTypeEditorComponent
    },
    {
        path       : 'resource-type/list',
        component  : ResourceTypeListComponent
    }
];
@NgModule({
  declarations: [
    ResourceTypeListComponent,
    ResourceTypeEditorComponent,
    ResourceTypeLayoutComponent,
    ResourceTypeDialog,
    ResourceFieldDialog,
    StepEditorDialog,
    TabEditorDialog
  ],
  imports: [
    RouterModule.forChild(routes),
    ScrollingModule,
    CdkTableModule,
    CdkTreeModule,
    NgxDnDModule,
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
    MatRippleModule,
    MatSelectModule,
    MatSortModule,
    MatTabsModule,
    MatTableModule,
    MatToolbarModule,
    MatTreeModule,
    
    DragDropModule,
    
    TranslateModule,

    FuseSharedModule,
    FuseSidebarModule
  ],
  entryComponents: [
    ResourceTypeDialog,
    ResourceFieldDialog,
    StepEditorDialog,
    TabEditorDialog
  ]
})
export class ResourceTypeModule { }