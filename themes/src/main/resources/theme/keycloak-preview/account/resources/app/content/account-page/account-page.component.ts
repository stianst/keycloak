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
import {Component, OnInit} from '@angular/core';
import {Http, Response} from '@angular/http';

import {AbstractContent} from '../abstract-content/abstract.content';
import {KeycloakService} from '../../keycloak-service/keycloak.service';

@Component({
    selector: 'app-account-page',
    templateUrl: './account-page.component.html',
    styleUrls: ['./account-page.component.css']
})
export class AccountPageComponent extends AbstractContent implements OnInit {
    
    private account: Account = {};

    constructor(protected http: Http, protected kcSvc: KeycloakService) {
        super(http, kcSvc);
        super.doGetRequest("/");
    }
    
    public saveAccount() {
        console.log("posting: " + JSON.stringify(this.account));
        super.doPostRequest("/", this.account);
    }
    
    protected handleGetResponse(res: Response) {
      this.account = res.json();
      console.log('**** response from account REST API ***');
      console.log(JSON.stringify(this.account));
      console.log('***************************************');
    }
    
    protected handlePostResponse(res: Response) {
      console.log('**** response from account POST ***');
      console.log(JSON.stringify(res));
      console.log('***************************************');
    }

    ngOnInit() {
    }
    
}

class Account {
    constructor(username: string, 
                emailVerified: boolean,
                firstName?: string, 
                lastName?: string, 
                email?: string, 
                attributes?: Object){
    }
}