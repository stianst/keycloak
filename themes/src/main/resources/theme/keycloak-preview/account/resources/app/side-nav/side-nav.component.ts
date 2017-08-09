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
 
import { Component, OnInit } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import {TranslateUtil} from '../ngx-translate/translate.util';
import { SideNavItem, Active } from '../page/side-nav-item';
import { Icon } from '../page/icon';

@Component({
  selector: 'app-side-nav',
  templateUrl: './side-nav.component.html',
  styleUrls: ['./side-nav.component.css']
})
export class SideNavComponent implements OnInit {
    
  public navItems: SideNavItem[];

  constructor(private router: Router, private translateUtil: TranslateUtil) {
      this.navItems = [
          this.makeSideNavItem("account", new Icon("pficon", "user"), "active"),
          this.makeSideNavItem("password", new Icon("pficon", "key")),
          this.makeSideNavItem("authenticator", new Icon("pficon", "cloud-security")),
          this.makeSideNavItem("sessions", new Icon("fa", "clock-o")),
          this.makeSideNavItem("applications", new Icon("fa", "th"))
      ];

      this.router.events.subscribe( value => {
          if (value instanceof NavigationEnd) {
              const navEnd = value as NavigationEnd;
              this.setActive(navEnd.url);
          }
      });
  }
  
  // use itemName for translate key, link, and tooltip
  private makeSideNavItem(itemName: string, icon: Icon, active?: Active): SideNavItem {
      const localizedName: string = this.translateUtil.translate(itemName);
      
      return new SideNavItem(localizedName, itemName, localizedName, icon, active);
  }

  setActive(url: string) {
      for (let navItem of this.navItems) {
          if (("/" + navItem.link) === url) {
              navItem.setActive("active");
          } else {
              navItem.setActive("");
          }
      }

      if ("/" === url) {
          this.navItems[0].setActive("active");
      }
  }

  ngOnInit() {

  }

}
