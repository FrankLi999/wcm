import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
@Component({
  selector: 'resource-viewer',
  templateUrl: './resource-viewer.component.html',
  styleUrls: ['./resource-viewer.component.scss']
})
export class ResourceViewerComponent implements OnInit {
  @Output() resourceViewerRemoved = new EventEmitter<number>();
  // @Input() viewer: Viewer;
  @Input() viewerIndex: number;
  @Input() renderTemplate: string;
  constructor() { }

  ngOnInit() {
  }

  // public addRenderTemplate() {
  //   this.viewer.renderTemplates.push("a_render_tmplate");
  // }
  
  public removeSideViewer(viewerIndex: number) {
    this.resourceViewerRemoved.emit(viewerIndex);
    return false;
  }
}