package org.example.utils;

import com.microsoft.playwright.*;
import java.util.Arrays;

public class PlaywrightManager {

    private static Playwright playwright;
    private static Browser browser;
    private static ThreadLocal<Page> page = new ThreadLocal<>();

    public static void initBrowser() {
        if (playwright == null) {
            playwright = Playwright.create();
            boolean headless = Boolean.parseBoolean(System.getProperty("headless", "false"));
            browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(headless));
        }
    }

    public static void createPage() {
        closePage();

        BrowserContext context = browser.newContext(
                new Browser.NewContextOptions()
                        .setViewportSize(1920, 1080)
                        .setPermissions(Arrays.asList("clipboard-read", "clipboard-write"))
        );

        page.set(context.newPage());
    }

    // Метод для создания страницы с геолокацией
    public static void createPageWithGeolocation(double latitude, double longitude) {
        closePage();

        BrowserContext context = browser.newContext(
                new Browser.NewContextOptions()
                        .setViewportSize(1920, 1080)
                        .setPermissions(Arrays.asList("clipboard-read", "clipboard-write", "geolocation"))
                        .setGeolocation(latitude, longitude)
        );

        page.set(context.newPage());
    }

    // Метод для создания страницы без геолокации (для теста с запретом)
    public static void createPageWithoutGeolocation() {
        closePage();

        BrowserContext context = browser.newContext(
                new Browser.NewContextOptions()
                        .setViewportSize(1920, 1080)
                        .setPermissions(Arrays.asList("clipboard-read", "clipboard-write"))
        );

        page.set(context.newPage());
    }

    public static Page getPage() {
        return page.get();
    }

    public static void closePage() {
        if (page.get() != null) {
            page.get().context().close();
            page.remove();
        }
    }

    public static void closeBrowser() {
        if (browser != null) {
            browser.close();
            playwright.close();
        }
    }
}