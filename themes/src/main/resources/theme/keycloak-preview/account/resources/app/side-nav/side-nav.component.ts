import { Component, OnInit } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { SideNavItem } from '../page/side-nav-item';
import { Icon } from '../page/icon';

@Component({
  selector: 'app-side-nav',
  templateUrl: './side-nav.component.html',
  styleUrls: ['./side-nav.component.css']
})
export class SideNavComponent implements OnInit {
    
  public navItems: SideNavItem[];

  constructor(private router: Router) {
      this.navItems = [
        new SideNavItem("Account", "account", "Account", new Icon("pficon", "user"), "active"),
        new SideNavItem("Password", "password", "Password", new Icon("pficon", "key")),
        new SideNavItem("Authenticator", "authenticator", "Authenticator", new Icon("pficon", "cloud-security")),
        new SideNavItem("Sessions", "sessions", "Sessions", new Icon("fa", "clock-o")),
        new SideNavItem("Applications", "applications", "Applications", new Icon("fa", "th")),
      ];

      this.router.events.subscribe( value => {
          if (value instanceof NavigationEnd) {
              const navEnd = value as NavigationEnd;
              this.setActive(navEnd.url);
          }
      });
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
