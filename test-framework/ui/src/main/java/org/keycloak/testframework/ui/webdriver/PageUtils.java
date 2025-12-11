package org.keycloak.testframework.ui.webdriver;

import java.lang.reflect.Constructor;

import org.keycloak.testframework.ui.page.AbstractPage;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;

public class PageUtils {

    private final ManagedWebDriver managed;

    PageUtils(ManagedWebDriver managed) {
        this.managed = managed;
    }

    public <S extends AbstractPage> S createPage(Class<S> valueType) {
        try {
            Constructor<S> constructor = valueType.getDeclaredConstructor(ManagedWebDriver.class);
            S page = constructor.newInstance(managed);
            PageFactory.initElements(managed.driver(), page);
            return page;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getCurrentPageId() {
        return managed.findElement(By.xpath("//body")).getAttribute("data-page-id");
    }

    public String getTitle() {
        return managed.driver().getTitle();
    }

    public String getPageSource() {
        return managed.driver().getPageSource();
    }

    public void clickButton(WebElement element) {
        if (managed.getBrowserType().equals(BrowserType.CHROME)) {
            element.sendKeys(Keys.ENTER);
        } else {
            element.click();
        }
    }

}
