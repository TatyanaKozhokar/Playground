package org.example.utils;

import com.microsoft.playwright.*;

public class PlaywrightManager {

    private static Playwright playwright;
    private static Browser browser;
    private static ThreadLocal<Page> page = new ThreadLocal<>();

    // Инициализация браузера
    public static void initBrowser() {
        if (playwright == null) {
            playwright = Playwright.create();

            boolean headless = Boolean.parseBoolean(System.getProperty("headless", "false"));

            BrowserType.LaunchOptions options = new BrowserType.LaunchOptions()
                    .setHeadless(headless);

            browser = playwright.chromium().launch(options);
        }
    }

    // Создание новой страницы для теста
    public static void createPage() {
        closePage(); // Закрываем старую страницу если была

        BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                .setViewportSize(1920, 1080));
        Page newPage = context.newPage();
        page.set(newPage);
    }

    // Получение текущей страницы
    public static Page getPage() {
        return page.get();
    }

    // Закрытие страницы и контекста
    public static void closePage() {
        if (page.get() != null) {
            page.get().context().close();
            page.remove();
        }
    }

    // Закрытие браузера
    public static void closeBrowser() {
        if (browser != null) {
            browser.close();
            playwright.close();
        }
    }
}
