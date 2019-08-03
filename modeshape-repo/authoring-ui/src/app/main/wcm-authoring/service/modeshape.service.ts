import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { Observable } from 'rxjs';

import {
  RestClient, 
  Client, 
  Path, 
  Get,
  Post,
  Delete,
  Timeout,
  Body,
  Query, 
  Produces, 
  MediaType
} from '../../rest-client';

import {API_BASE_URL} from '../../authentication/constants';

import { 
  RestRepositories,
  RestWorkspaces,
  RestItem
} from '../model';
@Injectable()
@Client({
  serviceId: 'modeshape-service',
  baseUrl: `${API_BASE_URL}/modeshape/api/rest`,
  headers: {
      'content-type': 'application/json'
  }
})
export class ModeshapeService extends RestClient {

  constructor(httpClient: HttpClient){
    super(httpClient);
  }

  @Get("/repo")
  @Timeout(2000) //In milliseconds
  @Produces(MediaType.JSON)
  public getRepositories(): Observable<RestRepositories> { return null; };

  @Get("/{repositoryName}")
  @Timeout(2000) //In milliseconds
  @Produces(MediaType.JSON)
  public getWorkspaces(@Path("repositoryName") repositoryName: string): Observable<RestWorkspaces> { return null; };

  @Get("/{repositoryName}/{workspaceName}/items/{path}")
  @Timeout(20000) //In milliseconds
  @Produces(MediaType.JSON)
  public getItems(
      @Path("repositoryName") repositoryName: string, 
      @Path("workspaceName") workspaceName: string,
      @Path("path") path: string,
      @Query("depth") depth?: number): Observable<RestItem> { return null; };
  
  @Post("/{repositoryName}/{workspaceName}/items/{path}")
  @Timeout(20000) //In milliseconds
  public postItems(
      @Path("repositoryName") repositoryName: string, 
      @Path("workspaceName") workspaceName: string,
      @Path("path") path: string,
      @Body itemContent: any): Observable<any> { return null; };

  @Delete("/{repositoryName}/{workspaceName}/items/{path}")
  @Timeout(20000) //In milliseconds
  public deleteItem(
      @Path("repositoryName") repositoryName: string, 
      @Path("workspaceName") workspaceName: string,
      @Path("path") path: string): Observable<any> { return null; };
}