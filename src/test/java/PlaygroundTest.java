import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import org.example.utils.PlaywrightManager;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class PlaygroundTest {

    @BeforeAll
    static void setUpClass() {
        PlaywrightManager.initBrowser();
    }

    @BeforeEach
    void setUp() {
        PlaywrightManager.createPage();
    }

    @AfterEach
    void tearDown() {
        PlaywrightManager.closePage();
    }

    @AfterAll
    static void tearDownClass() {
        PlaywrightManager.closeBrowser();
    }

    @Test
    void dynamicID() {
        Page page = PlaywrightManager.getPage();
        page.navigate("http://uitestingplayground.com/dynamicid");
        page.locator("//button[@class='btn btn-primary' and @type='button']").click();
    }

    @Test
    void classAttribute() {
        Page page = PlaywrightManager.getPage();
        page.navigate("http://uitestingplayground.com/classattr");
        page.locator("//button[contains(concat(' ', normalize-space(@class), ' '), ' class3 ')]").click();
        page.locator("//button[contains(text(), 'OK')]").click();
    }

    @Test
    void hiddenLayers() {
        Page page = PlaywrightManager.getPage();
        page.navigate("http://uitestingplayground.com/hiddenlayers");
        page.locator("//button[@id='greenButton']").click();
        page.locator("//button[@id='greenButton']").click();
    }

    //Тест не проходит - не видит синюю кнопку, хотя локатор верный
    @Test
    void loadDelays() {
        Page page = PlaywrightManager.getPage();
        page.navigate("http://uitestingplayground.com");
        page.locator("//a[text()='Load Delay']").click();
        page.locator("//ya-tr-span[contains(@data-value, 'Button Appearing')]").waitFor(); //знаю что в Playwright ожидание прописано под капотом,
        // но у меня не работало и я прописала на всякий случай (работать не стало)
        page.locator("//ya-tr-span[contains(@data-value, 'Button Appearing')]").click();
    }

    @Test
    void ajax() {
        Page page = PlaywrightManager.getPage();
        page.navigate("http://uitestingplayground.com/ajax");
        page.locator("//button[@id='ajaxButton']").click();
        page.locator("//div[@id='content']/p[@class='bg-success']").waitFor(new Locator.WaitForOptions()
                .setTimeout(20000)); //Playwright сам ждет 30000ms, поэтому доп ожидания прописывать
        // не нужно, но мы сделаем допущение
    }

    @Test
    void clientDelay() {
        Page page = PlaywrightManager.getPage();
        page.navigate("http://uitestingplayground.com/clientdelay");
        page.locator("//button[@id='ajaxButton']").click();
        page.locator("//p[@class='bg-success']").waitFor(new Locator.WaitForOptions()
                .setTimeout(20000)); //То же самое
        page.locator("//p[@class='bg-success']").click();
    }

    @Test
    void click() {
        Page page = PlaywrightManager.getPage();

        page.navigate("http://uitestingplayground.com/click");
        Locator button = page.locator("//button[@id='badButton']");
        String beforeClass = button.getAttribute("class");
        button.click(new Locator.ClickOptions()
                .setForce(true)
                .setPosition(5, 5));
        page.waitForTimeout(1000);
        String newClass = button.getAttribute("class");

        assertNotEquals(beforeClass, newClass);
    }

    @Test
    void textInput() {
        Page page = PlaywrightManager.getPage();
        page.navigate("http://uitestingplayground.com/textinput");
        String newName = "AAA";
        Locator input = page.locator("//input[@id='newButtonName']");
        input.click();
        input.pressSequentially(newName, new Locator.PressSequentiallyOptions()
                .setDelay(100));
        Locator button = page.locator("//button[@id='updatingButton']");
        button.click(new Locator.ClickOptions()
                .setForce(true));
        String newButton = button.innerText();

        assertEquals(newName, newButton);

    }

    @Test
    void scrollbars() {
        Page page = PlaywrightManager.getPage();
        page.navigate("http://uitestingplayground.com/scrollbars");
        Locator button = page.locator("//button[@id='hidingButton']");

        button.scrollIntoViewIfNeeded();
        button.click();

    }

    @Test
    void dynamicTable() {
        Page page = PlaywrightManager.getPage();
        page.navigate("http://uitestingplayground.com/dynamictable");
        int cpuColumnIndex = -1;
        Locator headers = page.locator("[role='columnheader']");
        for (int i = 0; i < headers.count(); i++) {
            if (headers.nth(i).textContent().equals("CPU")) {
                cpuColumnIndex = i;
                break;
            }
        }

        String cpuValue = null;
        Locator rows = page.locator("[role='row']");

        // Пропускаем первую строку (заголовки)
        for (int i = 1; i < rows.count(); i++) {
            Locator row = rows.nth(i);
            Locator nameCell = row.locator("[role='cell']").first();

            if (nameCell.textContent().equals("Chrome")) {
                cpuValue = row.locator("[role='cell']").nth(cpuColumnIndex).textContent();
                break;
            }
        }
        assertNotNull(cpuValue, "CPU value for Chrome not found");
        String yellowLabelValue = page.locator("//p[@class='bg-warning']").innerText().replaceAll("[^0-9.]", "");
        assertEquals(
                cpuValue.replace("%", "").trim(),
                yellowLabelValue.replace("%", "").trim()
        );
    }

    @Test
    void verifyText() {
        Page page = PlaywrightManager.getPage();
        page.navigate("http://uitestingplayground.com/verifytext");
        Locator element = page.locator("//span[normalize-space()='Welcome UserName!']");
        element.isVisible();
    }

    @Test
    void progressbar() {
        Page page = PlaywrightManager.getPage();
        page.navigate("http://uitestingplayground.com/progressbar");
        page.locator("//button[@id='startButton']").click();
        Locator progressBar = page.locator("//div[@id='progressBar']");

        while (true) {
            String progressValue = progressBar.getAttribute("aria-valuenow");
            int progress = Integer.parseInt(progressValue);

            if (progress >= 75) {
                break;
            }
            page.waitForTimeout(100);
        }

        page.locator("//button[@id='stopButton']").click();

        String finalProgress = progressBar.getAttribute("aria-valuenow");
        System.out.println("Прогресс остановлен на " + finalProgress + "%");
    }

    @Test
    void visibility() {
        Page page = PlaywrightManager.getPage();
        page.navigate("http://uitestingplayground.com/visibility");
        String[][] buttons = {
                {"hideButton"},
                {"removedButton"},
                {"zeroWidthButton"},
                {"overlappedButton"},
                {"transparentButton"},
                {"invisibleButton"},
                {"notdisplayedButton"},
                {"offscreenButton"}
        };

        page.locator("//button[@id='hideButton']").click();
        page.waitForTimeout(1000);

        for (String[] button : buttons) {
            String id = button[0];
            String xpath = "//button[@id='" + id + "']";

            page.locator(xpath).isVisible();

        }
    }

    @Test
    void sampleApp() {
        Page page = PlaywrightManager.getPage();
        page.navigate("http://uitestingplayground.com/sampleapp");
        page.locator("//input[@name='UserName']").fill("TestUser");
        page.locator("//input[@name='Password']").fill("pwd");
        page.locator("//button[@id='login']").click();
        Locator welcome = page.locator("//label[@id='loginstatus']");
        welcome.waitFor();
        assertEquals("Welcome, TestUser!", welcome.innerText());
    }

    @Test
    void mouseover() {
        Page page = PlaywrightManager.getPage();
        page.navigate("http://uitestingplayground.com/mouseover");

        Locator firstLink = page.locator("//a[text()='Click me']");
        Locator firstCounter = page.locator("//span[@id='clickCount']");
        firstLink.click();
        firstLink.click();
        assertEquals("2", firstCounter.innerText());

        Locator secondLink = page.locator("//a[text()='Link Button']");
        Locator secondCounter = page.locator("//span[@id='clickButtonCount']");
        secondLink.click();
        secondLink.click();
        assertEquals("2", secondCounter.innerText());

    }


    //ААААААА
    @Test
    void nbsp() {
        Page page = PlaywrightManager.getPage();
        page.navigate("http://uitestingplayground.com/nbsp");

        Locator button1 = page.locator("//button[normalize-space(.)='My Button']");
        button1.click();

    }
}
