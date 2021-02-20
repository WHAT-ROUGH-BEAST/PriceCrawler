package main;

import model.BookModel;
import model.Page;
import org.apache.http.impl.client.HttpClientBuilder;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import parse.Parse;
import util.ConfigUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class Test
{
    private static WebDriver driver;

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
        //关闭自动测试状态显示
        options.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
        //添加useragent
//        options.addArguments("user-agent=\"" + "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36" + "\"");

        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        driver.manage().window().maximize();
    }

    public static void main(String[] args)
    {
        List<BookModel> data = new ArrayList<>();
        Page page = new Page("tmall", "三体");

        // 初始登录
        login();

        // TODO webdriver undefined
//        ((JavascriptExecutor) driver).executeScript("Object.defineProperties(navigator, {webdriver:{get:()=>undefined}});");
        sleep(1500);

        // 爬需要的界面
        driver.get(page.getUrl());
        while (pageCraw(data, page)){}

        // 输出结果
        CrawlerMain.logger.info("read from: " + page.getSiteName() + "\n" + "count " + data.size());

        for (BookModel da : data)
            CrawlerMain.logger.info("bookID:"+da.getBookId()+"\t\t"+"bookPrice:"+da.getBookPrice()+
                    "\t\t"+"bookName:"+da.getBookName());

        // 关闭webdriver
        driver.close();
    }

    private static void login()
    {
        driver.get("https://login.taobao.com/member/login.jhtml?tpl_redirect_url=https%3A%2F%2Fwww.tmall.com%2F&style=miniall&enup=true&newMini2=true&full_redirect=true&sub=true&from=tmall&allp=assets_css%3D3.0.10/login_pc.css&pms=1613635752037");
        sleep(1000);
        ((JavascriptExecutor) driver).executeScript("Object.defineProperties(navigator, {webdriver:{get:()=>undefined}});");

        WebElement usr = driver.findElement(By.id("fm-login-id"));
        WebElement pw = driver.findElement(By.id("fm-login-password"));
        usr.clear(); pw.clear();
        usr.sendKeys("1806991194@qq.com");
        pw.sendKeys("transformers!13" + Keys.ENTER);
    }

    private static boolean pageCraw(final List<BookModel> data, Page page)
    {
        // TODO webdriver undefined
//        ((JavascriptExecutor) driver).executeScript("Object.defineProperties(navigator, {webdriver:{get:()=>undefined}});");

        List<BookModel> pageData = new ArrayList<>();
        if (page.getSiteName().equals("jd")) {}
//            pageData = scrollPage(page);
        else
        {
            String entity = driver.getPageSource();
            pageData = Parse.getData(page.getSiteName(), entity);
        }

        data.addAll(pageData);
        // 翻页
        nextPage(page);

//        return !(pageData.size() < 60);
        return !(pageData.size() < 48);
    }

    private static void nextPage(Page page)
    {
        String siteName = page.getSiteName();

        if (siteName.equals("jd")) {}
        else if (siteName.equals("tmall"))
        {
            page.setPage(page.getPage() + 1);
            driver.findElement(By.cssSelector("a[class='ui-page-next']")).click();

            page.setUrl(driver.getCurrentUrl());
            ((JavascriptExecutor) driver).executeScript("Object.defineProperties(navigator, {webdriver:{get:()=>undefined}});");
            ((JavascriptExecutor) driver).executeScript("window.navigator.chrome = { runtime: {},  };");
            ((JavascriptExecutor) driver).executeScript("Object.defineProperty(navigator, 'languages', { get: () => ['en-US', 'en'] });");
            ((JavascriptExecutor) driver).executeScript("Object.defineProperty(navigator, 'plugins', { get: () => [1, 2, 3, 4, 5,6], });");

            // 防止滑块
            try
            {
                WebElement slider = driver.findElement(By.cssSelector("span[id='nc_1_n1z']"));
//                slide();
                sleep(10000);
            }
            catch (Exception e)
            {
                System.err.println(e.getMessage() + " no slider");
            }
        }
        else if (siteName.equals("taobao"))
        {
            page.setPage(page.getPage() + 1);
            String src = driver.getPageSource();
            driver.findElement(By.cssSelector("a[trace='srp_bottom_pagedown']")).click();
            page.setUrl(driver.getCurrentUrl());

            // 防止滑块
            try
            {
                slide();
//                sleep(10000);
            }
            catch (Exception e)
            {
                System.err.println(e.getMessage() + " no slider");
            }
        }
    }

    private static void slide()
    {
        // TODO ADD KEEP THIS ONE
       WebElement slider = driver.findElement(By.cssSelector("span[id='nc_1_n1z']"));

        Actions move = new Actions(driver);
        move.clickAndHold(slider).perform();
        for (int i = 0; i < 100; i ++)
            move.moveByOffset(i, 0);
        move.pause(1).release().perform();
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
}
