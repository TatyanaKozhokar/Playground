import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import org.example.utils.PlaywrightManager;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

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
}
