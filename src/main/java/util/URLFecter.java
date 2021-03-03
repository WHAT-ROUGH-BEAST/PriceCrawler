package util;

import main.CrawlerMain;
import model.ProductModel;
import model.Page;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class URLFecter
{
    private final Page page;
    private ChromeDriver driver;

    public URLFecter(Page page)
    {
        this.page = page;
        initChrome();
    }

    private void initChrome()
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

    public Set<ProductModel> URLParser()
    {
        HashSet<ProductModel> data = new HashSet<>();

        //login
        if (page.getSiteName().equals("tmall") ||
                page.getSiteName().equals("taobao"))
        {
            login();
            sleep(5);
        }

        //逐页爬取
        PageCrawler crawler = new PageCrawler(driver, page, data);
        //等待滑块出现
        sleep(2);
        while (crawler.Craw()){}

        //closing the browser
        driver.close();

        log(data);

        return data;
    }

    private void login()
    {
        driver.get(ConfigUtils.getLoginUrl());
        sleep(1);

        WebElement usr = driver.findElement(By.id("fm-login-id"));
        WebElement pw = driver.findElement(By.id("fm-login-password"));
        usr.clear(); pw.clear();
        usr.sendKeys(ConfigUtils.getUser());
        pw.sendKeys(ConfigUtils.getPw() + Keys.ENTER);
    }

    private void log(Set<ProductModel> data)
    {
        CrawlerMain.logger.info("read from: " + page.getSiteName() + "\n" +
                "search " + page.getSearchItem() + "\n" +
                "count " + data.size() + "\n" +
                "page " + page.getPage() + " of " + page.getMaxPage());

        for (ProductModel da : data)
            CrawlerMain.logger.info("bookID:"+da.getProductId()+"\t\t"+"bookPrice:"+ da.getProductPrice() +
                    "\t\t"+"bookName:"+da.getProductName());
    }

    static void sleep(double s)
    {
        try
        {
            Thread.sleep((int)s * 1000);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
