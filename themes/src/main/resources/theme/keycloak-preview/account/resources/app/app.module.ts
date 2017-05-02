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
import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpModule } from '@angular/http';

import { KeycloakService } from './keycloak-service/keycloak.service';
import { KeycloakHttp,KEYCLOAK_HTTP_PROVIDER } from './keycloak-service/keycloak.http';

import { AppComponent } from './app.component';
import { TopNavComponent } from './top-nav/top-nav.component';
import { SideNavComponent } from './side-nav/side-nav.component';
import { AccountPageComponent } from './content/account-page/account-page.component';
import { PasswordPageComponent } from './content/password-page/password-page.component';
import { PageNotFoundComponent } from './content/page-not-found/page-not-found.component';

import { RouterModule, Routes } from '@angular/router';
import { LocationStrategy, HashLocationStrategy } from '@angular/common';
import { AuthenticatorPageComponent } from './content/authenticator-page/authenticator-page.component';
import { SessionsPageComponent } from './content/sessions-page/sessions-page.component';
import { ApplicationsPageComponent } from './content/applications-page/applications-page.component';

const routes: Routes = [
    { path: 'account', component: AccountPageComponent },
    { path: 'password', component: PasswordPageComponent },
    { path: 'authenticator', component: AuthenticatorPageComponent },
    { path: 'sessions', component: SessionsPageComponent },
    { path: 'applications', component: ApplicationsPageComponent },
    { path: '', redirectTo: '/account', pathMatch: 'full' },
    { path: '**', component: PageNotFoundComponent}
];

const decs = [
    AppComponent,
    TopNavComponent,
    SideNavComponent,
    AccountPageComponent,
    PasswordPageComponent,
    PageNotFoundComponent,
    AuthenticatorPageComponent,
    SessionsPageComponent,
    ApplicationsPageComponent
];

@NgModule({
  declarations: decs,
  imports: [
    BrowserModule,
    FormsModule,
    HttpModule,
    RouterModule.forRoot(routes)
  ],
  providers: [
    KeycloakService,
    KEYCLOAK_HTTP_PROVIDER,
    { provide: LocationStrategy, useClass: HashLocationStrategy }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
