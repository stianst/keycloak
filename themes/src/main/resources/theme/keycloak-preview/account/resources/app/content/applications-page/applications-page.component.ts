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

import {TranslateUtil} from '../../ngx-translate/translate.util';
import {AccountServiceClient} from '../../account-service/account.service';

declare const resourceUrl: string;

type ApplicationView = "LargeCards" | "SmallCards" | "List";

@Component({
    moduleId: module.id, // need this for styleUrls path to work properly with Systemjs
    selector: 'app-applications-page',
    templateUrl: 'applications-page.component.html',
    styleUrls: ['applications-page.component.css']
})
export class ApplicationsPageComponent implements OnInit {
    private activeView: ApplicationView = "LargeCards";
    
    private resourceUrl: string = resourceUrl;
    private applications: any[] = [];
    private sessions: any[] = [];

    constructor(accountSvc: AccountServiceClient, private translateUtil: TranslateUtil ) {
        accountSvc.doGetRequest("/applications", (res: Response) => this.handleGetAppsResponse(res));
        accountSvc.doGetRequest("/sessions", (res: Response) => this.handleGetSessionsResponse(res));
    }

    private handleGetAppsResponse(res: Response) {
      console.log('**** response from apps REST API ***');
      console.log(JSON.stringify(res));
      console.log('*** apps res.json() ***');
      console.log(JSON.stringify(res.json().applications));
      console.log('***************************************');
      this.applications = res.json().applications;
      
      for (let app of this.applications) {
          this.setIcon(app);
      }
    }
    
    private handleGetSessionsResponse(res: Response) {
      console.log('**** response from sessions REST API ***');
      console.log(JSON.stringify(res));
      console.log('*** sessions res.json() ***');
      console.log(JSON.stringify(res.json()));
      console.log('***************************************');
      this.sessions = res.json();
    }
    
    private getName(application: any): string {
        if (application.hasOwnProperty('name')) {
            return this.translateUtil.translate(application.name);
        }
        
        return application.clientId;
    }
    
    private getDescription (application: any): string {
        if (!application.hasOwnProperty('description')) return null;
        
        let desc: string = application.description;
        
        if (desc.indexOf("//icon") > -1) {
            desc = desc.substring(0, desc.indexOf("//icon"));
        }
        
        return desc;
    }
    
    private isSessionActive(application: any) : boolean {
        for (let session of this.sessions) {
            for (let client of session.clients) {
                if (application.clientId === client.clientId) return true;
            }
        }
        return false;
    }
    
    private setIcon(application: any) {
        application.icon = "pficon-key";
        
        if (!application.hasOwnProperty('description')) {
            return;
        }
        
        let desc: string = application.description;
        const iconIndex: number = desc.indexOf("//icon=");
        if (iconIndex > -1) {
            application.icon = desc.substring(iconIndex + 7, desc.length);
            return;
        }
    }
    
    private changeView(activeView: ApplicationView) {
        this.activeView = activeView;
    }

    ngOnInit() {
    }

}
