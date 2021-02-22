package util;

import com.google.common.collect.ImmutableMap;
import main.CrawlerMain;
import model.BookModel;
import model.Page;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import parse.Parse;

import javax.swing.text.Element;
import java.io.File;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class URLFecter
{
    private static ChromeDriver driver;

    static
    {
        initChrome();
    }

    private static void initChrome()
    {
        //setting the driver executable
        System.setProperty("webdriver.chrome.driver", ConfigUtils.getDriverAdd());
        ChromeOptions options = new ChromeOptions();
        // 不加载图片
        options.addArguments("blink-settings=imagesEnabled=false");
        // 关闭自动测试状态显示
        options.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
//        options.addArguments("--headless");
        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        driver.manage().window().maximize();

        // 防止被淘宝检测 + ChromeDriver内部改字符串
        driver.executeCdpCommand("Page.addScriptToEvaluateOnNewDocument", new HashMap<>(){
            {
                put("source", "Object.defineProperties(navigator, {webdriver:{get:()=>undefined}});" +
                        "window.navigator.chrome = { runtime: {},  };" +
                        "Object.defineProperty(navigator, 'languages', { get: () => ['en-US', 'en'] });" +
                        "Object.defineProperty(navigator, 'language', { get: () => 'zh-CN' });" +
                        "Object.defineProperty(navigator, 'plugins', { get: () => [1, 2, 3, 4, 5, 6] });");
            }
        });
    }

    public static List<BookModel> URLParser(Page page) throws Exception
    {
        List<BookModel> data = new ArrayList<>();

        //login
        if (page.getSiteName().equals("tmall") ||
                page.getSiteName().equals("taobao"))
        {
            login(page);
            sleep(1500);
        }

        //逐页爬取
        PageCrawler crawler = new PageCrawler(driver, page, data);
        while (crawler.Craw()){}

        //closing the browser
        driver.close();

        return data;
    }

    private static void login(Page page)
    {
        driver.get(ConfigUtils.getLoginUrl());
        sleep(1000);

        WebElement usr = driver.findElement(By.id("fm-login-id"));
        WebElement pw = driver.findElement(By.id("fm-login-password"));
        usr.clear(); pw.clear();
        usr.sendKeys(ConfigUtils.getUser());
        pw.sendKeys(ConfigUtils.getPw() + Keys.ENTER);
    }

    static void sleep(int milis)
    {
        try
        {
            Thread.sleep(milis);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
