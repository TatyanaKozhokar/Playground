import com.microsoft.playwright.*;

import org.example.utils.PlaywrightManager;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


import static org.junit.jupiter.api.Assertions.*;

public class PlaygroundTest {

    private static final String TEST_PDF_PATH = "src/test/resources/test-file.pdf";

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
        page.onceDialog(Dialog::accept);
        page.locator("#alertButton").click();
    }

    @Test
    void hiddenLayers() {
        Page page = PlaywrightManager.getPage();
        page.navigate("http://uitestingplayground.com/hiddenlayers");
        page.locator("//button[@id='greenButton']").click();
        page.locator("//button[@id='greenButton']").click();
    }

    @Test
    void loadDelays() {
        Page page = PlaywrightManager.getPage();
        page.navigate("http://uitestingplayground.com");
        page.locator("//a[text()='Load Delay']").click();
        page.locator("//html/body/section/div/button").waitFor();
        page.locator("//html/body/section/div/button").click();
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

    @Test
    void overlapped() {
        Page page = PlaywrightManager.getPage();
        page.navigate("http://uitestingplayground.com/overlapped");

        Locator name = page.locator("//input[@id='name']");
        //name.scrollIntoViewIfNeeded(); - тест выполняется и без этого метода, т.к. Playwright, как правило,
        // сам скроллит до элемента, но если что можно использовать его
        name.fill("AAA");
    }


    //Замучила бедный дипсик, но так и не получилось до конца выполнить тест - clipboardText возвращает пустую строку, разберусь позже (надеюсь)
    @Test
    void shadowDom() {
        Page page = PlaywrightManager.getPage();
        page.navigate("http://uitestingplayground.com/shadowdom");

        Locator shadowHost = page.locator("guid-generator");

        // Кликаем Generate
        shadowHost.locator("#buttonGenerate").click();
        page.waitForTimeout(1000);

        String generatedGuid = shadowHost.locator("#editField").getAttribute("value");

        // Кликаем Copy
        shadowHost.locator("#buttonCopy").click();
        page.waitForTimeout(1000);

        // Используем наш эмулированный clipboard
        String clipboardText = page.evaluate("() => window.getClipboard()").toString();

        assertEquals(generatedGuid, clipboardText);
    }

    @Test
    void alerts() {
        Page page = PlaywrightManager.getPage();
        page.navigate("http://uitestingplayground.com/alerts");

        page.onceDialog(Dialog::accept);
        page.locator("#alertButton").click();

        page.onceDialog(Dialog::accept);
        page.locator("#confirmButton").click();

        String customAnswer = "Hello";
        page.onceDialog(dialog -> {
            dialog.accept(customAnswer);
        });
        page.locator("#promptButton").click();
    }

    //Навайбкодила
    @Test
    void upload() throws IOException {
        Page page = PlaywrightManager.getPage();
        page.navigate("http://uitestingplayground.com/upload");

        FrameLocator frame = page.frameLocator("iframe[src='/static/upload.html']");

        Locator fileInput = frame.locator("#browse");

        Path tempFile = Files.createTempFile("test-upload", ".txt");
        Files.write(tempFile, "Test content for upload".getBytes());

        try {
            fileInput.setInputFiles(tempFile);

            frame.locator("#browse").dispatchEvent("change");

            page.waitForTimeout(500);

            Locator uploadInfo = frame.locator(".upload-info");
            assertTrue(uploadInfo.isVisible(), "Upload info should be visible");

            Locator errorMessage = frame.locator(".error-message");
            assertFalse(errorMessage.isVisible(), "Error message should not be visible");

            Locator fileName = frame.locator(".file-name");
            if (fileName.count() > 0) {
                assertTrue(fileName.textContent().contains("test-upload"),
                        "File name should be displayed");
            }

        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void animation()  {
        Page page = PlaywrightManager.getPage();
        page.navigate("http://uitestingplayground.com/animation");
        page.locator("#animationButton").click();
        page.waitForFunction(
                "document.querySelector('#movingTarget') && " +
                        "!document.querySelector('#movingTarget').classList.contains('spin')",
                null
        );
        page.locator("#movingTarget").click();
        String buttonClass = page.locator("#movingTarget").getAttribute("class");

        assertFalse(buttonClass.contains("spin"));
    }

    @Test
    void disabledInput()  {
        Page page = PlaywrightManager.getPage();
        page.navigate("http://uitestingplayground.com/disabledinput");
        page.click("#enableButton");
        page.locator("#inputField:enabled").waitFor(new Locator.WaitForOptions()
                .setTimeout(10000));
        String text = "Test";
        page.fill("#inputField", text);

        assertEquals(text, page.inputValue("#inputField"));
    }

    @Test
    void autoWait()  {
        Page page = PlaywrightManager.getPage();
        page.navigate("http://uitestingplayground.com/autowait");
        page.selectOption("#element-type", "input");
        page.uncheck("#visible");
        page.click("#applyButton3");
        Locator target = page.locator("#target");

        assertTrue(target.isHidden(), "Element should be hidden after applying settings");

        String testText = "Чикиряу";
        target.fill(testText);

        assertEquals(testText, target.inputValue());
    }

    @Test
    @DisplayName("Тест: Frames")
    void testFrames() {
        Page page = PlaywrightManager.getPage();
        page.navigate("http://uitestingplayground.com/frames");

        FrameLocator outer = page.frameLocator("#frame-outer");

        outer.locator("[data-action='edit']").click();
        outer.locator("text=Submit").click();
        outer.locator("[name='my-button']").click();
        outer.locator("//button[@class='btn-class']").click();

        FrameLocator inner = outer.frameLocator("#frame-inner");

        inner.locator("[data-action='edit']").click();
        inner.locator("text=Submit").click();
        inner.locator("[name='my-button']").click();
        inner.locator("//button[@class='btn-class']").click();

        String result = inner.locator("#result").textContent();
        assertTrue(result.contains("Primary"), "Last click should show Primary. Result: " + result);
    }

    //Вручную тест не проходит нигде
    @Test
    void testGeolocationAllow() {
        PlaywrightManager.createPageWithGeolocation(55, 37.0060);
        Page page = PlaywrightManager.getPage();

        page.navigate("http://uitestingplayground.com/geolocation");
        page.click("#requestLocation");
        page.waitForTimeout(2000);

        String location = page.locator("#location").textContent();

        Assertions.assertFalse(location.contains("Not requested"),
                "Location should be requested");
        Assertions.assertTrue(location.matches(".*\\d+\\.\\d+.*"));
    }

}
