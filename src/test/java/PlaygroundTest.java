import com.microsoft.playwright.*;

import com.microsoft.playwright.options.BoundingBox;
import org.example.utils.PlaywrightManager;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class PlaygroundTest {

    //private static final String TEST_PDF_PATH = "src/test/resources/test-file.pdf";

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
        var dynamicLocatorID = page.locator("//button[@class='btn btn-primary' and @type='button']").getAttribute("id");
        page.locator("//button[@class='btn btn-primary' and @type='button']").click();
        page.reload();
        assertThat(page.locator("//button[@id='" + dynamicLocatorID + "']")).hasCount(0);
    }

    @Test
    void classAttribute() {
        Page page = PlaywrightManager.getPage();
        page.navigate("http://uitestingplayground.com/classattr");
        var button = page.locator(".btn-primary");
        button.click();
        page.onDialog(Dialog::accept);
        assertTrue(button.isVisible());
    }

    @Test
    void hiddenLayers() {
        Page page = PlaywrightManager.getPage();
        Locator greenButton = page.locator("#greenButton");
        Locator blueButton = page.locator("#blueButton");
        page.navigate("http://uitestingplayground.com/hiddenlayers");
        greenButton.click();
        assertTrue(blueButton.isVisible());
        assertThrows(PlaywrightException.class, () -> {
            page.locator("#greenButton").click();
        });   }

    @Test
    void loadDelays() {
        Page page = PlaywrightManager.getPage();
        page.navigate("http://uitestingplayground.com");
        page.locator("//a[text()='Load Delay']").click();
        page.locator(".btn-primary").waitFor();
        page.locator(".btn-primary").click();
        assertTrue(page.locator(".btn-primary").isEnabled());
    }

    @Test
    void ajax() {
        Page page = PlaywrightManager.getPage();
        page.navigate("http://uitestingplayground.com/ajax");
        Locator ajaxButton = page.locator("//button[@id='ajaxButton']");
        Locator success = page.locator(".bg-success");
        ajaxButton.click();
        success.waitFor(new Locator.WaitForOptions()
                .setTimeout(20000)); //Playwright сам ждет 30000ms, поэтому доп ожидания прописывать
        // не нужно, но мы сделаем допущение
        assertTrue(success.isVisible());
    }

    @Test
    void clientDelay() {
        Page page = PlaywrightManager.getPage();
        page.navigate("http://uitestingplayground.com/clientdelay");
        Locator success = page.locator(".bg-success");
        Locator button = page.locator("//button[@id='ajaxButton']");
        button.click();
        success.waitFor(new Locator.WaitForOptions()
                .setTimeout(20000)); //То же самое
        assertTrue(success.isVisible());
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
        assertTrue(button.isEnabled());
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
        assertTrue(element.isVisible());
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
        int result = Integer.parseInt(finalProgress);
        assertTrue(result>=75);
    }

    @Test
    void visibility() {
        Page page = PlaywrightManager.getPage();
        page.navigate("http://uitestingplayground.com/visibility");
        page.locator("#hideButton").click();
        page.waitForTimeout(1000);

        assertFalse(page.locator("#zeroWidthButton").isVisible());
        assertFalse(page.locator("#notdisplayedButton").isVisible());
        assertFalse(page.locator("#invisibleButton").isVisible());

        Locator transparent = page.locator("#transparentButton");
        String opacity = transparent.evaluate("el => getComputedStyle(el).opacity").toString();
        assertEquals("0", opacity);

        assertTrue(page.locator("#removedButton").count() == 0);
        assertTrue(page.locator("#hideButton").isVisible());
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


    @Test
    void nbsp() {
        Page page = PlaywrightManager.getPage();
        page.navigate("http://uitestingplayground.com/nbsp");

        Locator button1 = page.locator("//button[text()='My\u00A0Button']");
        assertTrue(button1.isVisible());
        button1.click();
    }

    @Test
    void overlapped() {
        Page page = PlaywrightManager.getPage();
        page.navigate("http://uitestingplayground.com/overlapped");

        Locator scrollContainer = page.locator("#name").locator("..");
        scrollContainer.evaluate("el => el.scrollTop = 50");
        Locator nameField = page.locator("#name");
        String expectedText = "Олег";
        nameField.fill(expectedText);
        String actualText = nameField.inputValue();

        assertEquals(expectedText, actualText);
    }


    @Test
    void shadowDom() {
        Page page = PlaywrightManager.getPage();
        page.navigate("http://uitestingplayground.com/shadowdom");

        //Жесть я чуть не померла пока искала это решение для буфера
        page.evaluate("""
        window._copiedText = null;
        navigator.clipboard = {
            writeText: (text) => {
                window._copiedText = text;
                return Promise.resolve();
            },
            readText: () => {
                return Promise.resolve(window._copiedText);
            }
        };
    """);

        page.locator("#buttonGenerate").click();
        page.waitForTimeout(1000);

        Locator editField = page.locator("#editField");
        String generatedCode = editField.inputValue();
        assertNotNull(generatedCode);

        page.click("#buttonCopy");
        page.waitForTimeout(500);

        String copiedText = (String) page.evaluate("() => navigator.clipboard.readText()");

        assertEquals(generatedCode, copiedText);
    }

    //Я хз какой assert тут использовать
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
            assertTrue(uploadInfo.isVisible());

            Locator errorMessage = frame.locator(".error-message");
            assertFalse(errorMessage.isVisible());

            Locator fileName = frame.locator(".file-name");
            if (fileName.count() > 0) {
                assertTrue(fileName.textContent().contains("test-upload"));
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
