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
import {Response} from '@angular/http';

import {AccountServiceClient} from '../../account-service/account.service';

@Component({
    selector: 'app-sessions-page',
    templateUrl: './sessions-page.component.html',
    styleUrls: ['./sessions-page.component.css']
})
export class SessionsPageComponent implements OnInit {

    private response: any[] = [];
    
    constructor(private accountSvc: AccountServiceClient ) {
        accountSvc.doGetRequest("/sessions", (res: Response) => this.handleGetResponse(res));
    }

    private handleGetResponse(res: Response) {
      console.log('**** response from account REST API ***');
      console.log(JSON.stringify(res));
      console.log('*** res.json() ***');
      console.log(JSON.stringify(res.json()));
      console.log('***************************************');
      this.response = res.json();
    }
    
    private logoutAllSessions() {
        this.accountSvc.doDelete("/sessions", 
                                (res: Response) => this.handleLogoutResponse(res), 
                                {params: {current: true}},
                                "Logging out all sessions.");
    }
    
    private handleLogoutResponse(res: Response) {
      console.log('**** response from account DELETE ***');
      console.log(JSON.stringify(res));
      console.log('***************************************');
    }
    
    ngOnInit() {
    }

}
