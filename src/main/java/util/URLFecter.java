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
    private static HttpClient httpClient;
    private static WebDriver driver;

    static
    {
        httpClient = HttpClientBuilder.create().build();
        initChrome();
    }

    private static void initChrome()
    {
        //setting the driver executable
        System.setProperty("webdriver.chrome.driver", ConfigUtils.getDriverAdd());
        ChromeOptions options = new ChromeOptions();
        //设置代理IP
//        String proxyHttpUrl = "127.2.2.2:8080";
//        options.addArguments("--proxy-server=http://" + proxyHttpUrl);
        // 不加载图片
//        options.addArguments("blink-settings=imagesEnabled=false");
        //关闭自动测试状态显示
        options.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));

        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        driver.manage().window().maximize();
        // 设置driver undefined
//        Map<String, Object> param = new HashMap<>();
//
//        ((RemoteWebDriver) driver).getExecuteMethod().execute("executeCdpCommand",
//                ImmutableMap.of(
//                        "cmd", "Page.addScriptToEvaluateOnNewDocument",
//                        "params", param));
    }

    public static List<BookModel> URLParser(Page page) throws Exception
    {
        String siteName = page.getSiteName();
        if (siteName.equals("jd") || siteName.equals("tmall"))
            return SeleniumURLParser(page);
        else
            return HttpClientURLParser(page);
    }

    private static List<BookModel> HttpClientURLParser(Page page) throws Exception
    {
        List<BookModel> data = new ArrayList<>();
        HttpResponse response = HTTPUtils.getRawHtml(httpClient, page);

        int StatusCode = response.getStatusLine().getStatusCode();

        if (StatusCode == 200)
        {
            String entity = EntityUtils.toString(response.getEntity(), "utf-8");
            data = Parse.getData(page.getSiteName(), entity);
        }

        EntityUtils.consume(response.getEntity());

        return data;
    }

    private static List<BookModel> SeleniumURLParser(Page page)
    {
        List<BookModel> data = new ArrayList<>();

        //Applied wait time
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        //maximize window
        driver.manage().window().maximize();

        //login
        if (page.getSiteName().equals("tmall"))
            login(page);

        //逐页爬取
        while (pageCraw(data, page)){}

        //closing the browser
        driver.close();

        return data;
    }

    private static void login(Page page)
    {
        driver.get(page.getUrl());
        // 进入登陆界面
        WebElement loginlabel = driver.findElement(By.className("sn-login"));
        loginlabel.click();

        sleep(20000);

        WebElement usr = driver.findElement(By.id("fm-login-id"));
        WebElement pw = driver.findElement(By.id("fm-login-password"));
        WebElement button = driver.findElement(By.className("fm-button fm-submit password-login"));
        usr.clear(); pw.clear();
        usr.sendKeys("1806991194@qq.com");
        pw.sendKeys("transformers!13");
        button.click();

        sleep(2000);

        // 扫码
        page.setUrl(driver.getCurrentUrl());
    }

    private static void sleep(int milis)
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

    private static boolean pageCraw(final List<BookModel> data, Page page)
    {
        driver.get(page.getUrl());

        List<BookModel> pageData;
        if (page.getSiteName().equals("jd"))
            pageData = scrollPage(page);
        else
        {
            // 滚动页面
//            String js1 = "window.scrollTo(0,document.body.scrollHeight)";
//            ((JavascriptExecutor) driver).executeScript(js1);
            String entity = driver.getPageSource();
            pageData = Parse.getData(page.getSiteName(), entity);
        }

        data.addAll(pageData);
        // 翻页
        try
        {
            nextPage(page);
        }
        catch (Exception e)
        {
            CrawlerMain.logger.info(e.getMessage() + "no more page / anti-craw");
        }

        return !(pageData.size() < 60);

    }

    private static void nextPage(Page page) throws Exception
    {
        String siteName = page.getSiteName();
        if (siteName.equals("jd"))
        {
            WebElement nextButton = driver.
                    findElement(By.className("fp-next"));
            nextButton.click();
            page.setUrl(driver.getCurrentUrl());
        }
        else if (siteName.equals("tmall"))
        {
            page.setPage(page.getPage() + 1);
            driver.findElement(By.cssSelector("input[name='jumpto']")).
                    sendKeys("\b" + page.getPage() + Keys.ENTER);
            page.setUrl(driver.getCurrentUrl());
        }
    }

    private static List<BookModel> scrollPage(Page page)
    {
        // 滚动条到最下方加载所有商品
        String js1 = "window.scrollTo(0,document.body.scrollHeight)";
        ((JavascriptExecutor) driver).executeScript(js1);

        // 避免网页没有刷新完 以及避免最后一页不足60死循环
        List<BookModel> pageData = new ArrayList<>();
        int loop = 0;
        while ((pageData.size() == 30 || pageData.size() == 0) && loop < 3)
        {
            // 等待网页刷新
            try
            {
                Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            // 得到并处理网页源码
            String entity = driver.getPageSource();
            pageData = Parse.getData(page.getSiteName(), entity);
            loop ++;
        }

        return pageData;
    }
}
