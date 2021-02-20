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

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test
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
        //关闭自动测试状态显示
        options.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
        //添加useragent
//        options.addArguments("user-agent=\"" + "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36" + "\"");

        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        driver.manage().window().maximize();

        // 防止被淘宝检测 + ChromeDriver内部改字符串
        driver.executeCdpCommand("Page.addScriptToEvaluateOnNewDocument", new HashMap<>(){
            {
                put("source", "Object.defineProperties(navigator, {webdriver:{get:()=>undefined}});" +
                        "window.navigator.chrome = { runtime: {},  };" +
                        "Object.defineProperty(navigator, 'languages', { get: () => ['en-US', 'en'] });" +
                        "Object.defineProperty(navigator, 'plugins', { get: () => [1, 2, 3, 4, 5,6], });");
            }
        });
    }

    public static void main(String[] args)
    {
        List<BookModel> data = new ArrayList<>();
        Page page = new Page("tmall", "三体");

        // 初始登录
        login();

        sleep(1500);
        driver.get(page.getUrl());

        // 得到最大页数
        ReadMaxPage(page);

        // 爬需要的界面
        while (pageCraw(data, page)){}

        // 输出结果
        CrawlerMain.logger.info("read from: " + page.getSiteName() + "\n" + "count " + data.size());

        for (BookModel da : data)
            CrawlerMain.logger.info("bookID:"+da.getBookId()+"\t\t"+"bookPrice:"+da.getBookPrice()+
                    "\t\t"+"bookName:"+da.getBookName());

        // 关闭webdriver
        driver.close();
    }

    private static void ReadMaxPage(Page page)
    {
        int maxPage = 0;
        String siteName = page.getSiteName();
        if (siteName.equals("tmall"))
        {
            maxPage = Integer.parseInt(driver.findElement(By.cssSelector("input[name='totalPage']")).getAttribute("value"));
        }
        else if (siteName.equals("taobao"))
        {
            maxPage = Integer.parseInt(driver.findElement(By.cssSelector("input[aria-label='页码输入框']")).getAttribute("max"));
        }
        else if (siteName.equals("jd"))
        {
            maxPage = Integer.parseInt(driver.findElement(By.cssSelector("span[class='p-skip']")).findElement(By.tagName("b")).getText());
        }

        page.setMaxPage(maxPage);
    }

    private static void login()
    {
        driver.get("https://login.taobao.com/member/login.jhtml?tpl_redirect_url=https%3A%2F%2Fwww.tmall.com%2F&style=miniall&enup=true&newMini2=true&full_redirect=true&sub=true&from=tmall&allp=assets_css%3D3.0.10/login_pc.css&pms=1613635752037");
        sleep(1000);

        WebElement usr = driver.findElement(By.id("fm-login-id"));
        WebElement pw = driver.findElement(By.id("fm-login-password"));
        usr.clear(); pw.clear();
        usr.sendKeys("1806991194@qq.com");
        pw.sendKeys("transformers!13" + Keys.ENTER);
    }

    private static boolean pageCraw(final List<BookModel> data, Page page)
    {
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

        return page.getPage() < page.getMaxPage();
    }

    private static void nextPage(Page page)
    {
        page.setPage(page.getPage() + 1);

        String siteName = page.getSiteName();
        if (siteName.equals("jd")) {}
        else if (siteName.equals("tmall"))
        {
            driver.findElement(By.cssSelector("a[class='ui-page-next']")).click();

            page.setUrl(driver.getCurrentUrl());

            // 防止滑块 TODO
            if (driver.getTitle().equals("验证码拦截"))
                slide();
        }
        else if (siteName.equals("taobao"))
        {
            String src = driver.getPageSource();
            driver.findElement(By.cssSelector("a[trace='srp_bottom_pagedown']")).click();
            page.setUrl(driver.getCurrentUrl());

            // 防止滑块 TODO
            if (driver.getTitle().equals("验证码拦截"))
                slide();
        }
    }

    private static void slide()
    {
        WebElement slider = driver.findElement(By.cssSelector("span[id='nc_1_n1z']"));
        Actions move = new Actions(driver);
        move.clickAndHold(slider).perform();
        move.moveByOffset(500, 0);
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
