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

import {ToastNotifier, ToastNotification} from '../../top-nav/toast.notifier';
import {KeycloakService} from '../../keycloak-service/keycloak.service';

export abstract class AbstractContent {
    private accountUrl: string;

    constructor(protected http: Http,
        protected kcSvc: KeycloakService,
        protected notifier: ToastNotifier) {
        this.accountUrl = kcSvc.authServerUrl() + '/realms/' + kcSvc.realm() + '/account';
    }

    protected doGetRequest(endpoint: string) {
        this.http.get(this.accountUrl + endpoint)
            .subscribe((res: Response) => this.handleGetResponse(res),
            (error: Response) => this.handleServiceError(error));
    }

    protected abstract handleGetResponse(res: Response): void;

    protected doPostRequest(endpoint: string, body: any) {
        this.http.post(this.accountUrl + endpoint, body)
            .subscribe((res: Response) => this.doPostSuccess(res),
            (error: Response) => this.handleServiceError(error));
    }

    private doPostSuccess(res: Response): void {
        this.notifier.emit(new ToastNotification("Your account has been updated.", "success"));
        this.handlePostResponse(res);
    }

    protected abstract handlePostResponse(res: Response): void;

    private handleServiceError(response: Response) {
        console.log('**** ERROR!!!! ***');
        console.log(JSON.stringify(response));
        console.log('***************************************')
        
        if (response.status === 401) {
            this.kcSvc.logout();
            return;
        }

        if (response.status === 403) {
            // TODO: go to a forbidden page?
        }

        if (response.status === 404) {
            // TODO: route to PageNotFoundComponent
        }

        let message: string = response.status + " " + response.statusText;

        // Unfortunately, errors can be sent back in the response body as
        // 'errorMessage' or 'error_description'
        if (response.json().hasOwnProperty('errorMessage')) {
            message = response.json().errorMessage;
        }

        if (response.json().hasOwnProperty('error_description')) {
            message = response.json().error_description;
        }

        this.notifier.emit(new ToastNotification(message, "error"));
    }
}
