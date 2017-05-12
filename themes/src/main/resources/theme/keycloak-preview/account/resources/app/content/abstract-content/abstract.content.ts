/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import {Http, Response} from '@angular/http';
 
import {KeycloakService} from '../../keycloak-service/keycloak.service';
 
 export abstract class AbstractContent {
    private accountUrl: string;
     
    constructor(protected http: Http, protected kcSvc: KeycloakService) {
        this.accountUrl = kcSvc.authServerUrl() + '/realms/' + kcSvc.realm() + '/account';
    }
    
    protected request(endpoint: string) {
        this.http.get(this.accountUrl + endpoint)
            .subscribe((res: Response) => this.handleResponse(res),
                       (error: Response) => this.handleServiceError(error));
    }
    
    protected abstract handleResponse(res: Response): void;
    
    private handleServiceError(error: Response) {
      console.log('**** ERROR!!!! ***');
      console.log(JSON.stringify(error));
      console.log('***************************************')
    }
 }


