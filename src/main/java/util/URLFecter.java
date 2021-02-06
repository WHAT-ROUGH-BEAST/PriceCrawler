package util;

import main.CrawlerMain;
import model.BookModel;
import model.Page;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import parse.Parse;

import java.io.File;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class URLFecter
{
    static
    {
        //setting the driver executable
        System.setProperty("webdriver.chrome.driver", ConfigUtils.getDriverAdd());
        httpClient = HttpClientBuilder.create().build();
    }

    // 唯一httpclient
    private static HttpClient httpClient;

    public static List<BookModel> URLParser(Page page) throws Exception
    {
        if (page.getSiteName().equals("jd"))
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

            // DEBUG : cookie
//            PrintWriter writer = new PrintWriter(new File("cookie.html"));
//            writer.println(entity);

            data = Parse.getData(page.getSiteName(), entity);
        }

        EntityUtils.consume(response.getEntity());

        return data;
    }

    private static WebDriver driver;

    private static List<BookModel> SeleniumURLParser(Page page)
    {
        //Initiating your chromedriver
        driver = new ChromeDriver();
        List<BookModel> data = new ArrayList<>();

        //Applied wait time
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        //maximize window
        driver.manage().window().maximize();

        //逐页爬取
        while (pageCraw(data, page)){}

        //closing the browser
        driver.close();

        return data;
    }

    private static boolean pageCraw(final List<BookModel> data, Page page)
    {
        // 滚动条到最下方加载所有商品
        driver.get(page.getUrl());
        String js1 = "window.scrollTo(0,document.body.scrollHeight)";
        ((JavascriptExecutor) driver).executeScript(js1);

        // 避免网页没有刷新完 以及避免最后一页不足60死循环
        List<BookModel> pageData = new ArrayList<>();
        int loop = 0;
        while ((pageData.size() == 30 || pageData.size() == 0) && loop < 3) // TODO
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
        data.addAll(pageData);

        // 翻页
        page.setPage(page.getPage() + 2);

        return !(pageData.size() < 60);
    }
}
