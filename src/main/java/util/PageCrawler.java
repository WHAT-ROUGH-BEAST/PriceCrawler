package util;

import main.CrawlerMain;
import model.ProductModel;
import model.Page;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import parse.Parse;

import javax.swing.text.Element;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class PageCrawler
{
    private final ChromeDriver driver;
    private final Page page;
    private final Set<ProductModel> data;

    public PageCrawler(ChromeDriver driver, Page page, final Set<ProductModel> data)
    {
        this.driver = driver;
        this.page = page;
        this.data = data;

        driver.get(page.getUrl());

        // 处理滑块
        if (driver.getTitle().equals("验证码拦截"))
            slide();

        // 得到最大页数
        readMaxPage(page);
    }

    private void readMaxPage(Page page)
    {
        int maxPage = 0;
        String siteName = page.getSiteName();

        try
        {
            switch (siteName)
            {
                case "tmall" -> maxPage = Integer.parseInt(driver.findElement(By.cssSelector("input[name='totalPage']")).getAttribute("value"));
                case "taobao" -> maxPage = Integer.parseInt(driver.findElement(By.cssSelector("input[aria-label='页码输入框']")).getAttribute("max"));
                case "jd" -> maxPage = Integer.parseInt(driver.findElement(By.cssSelector("span[class='p-skip']")).findElement(By.tagName("b")).getText());
            }
        }
        catch (Exception e)
        {
            maxPage = 1;
            CrawlerMain.logger.info("only one page");
        }

        page.setMaxPage(maxPage);
    }

    public boolean Craw()
    {
        if (page.getSiteName().equals("jd"))
            data.addAll(scrollPage(page));
        else
        {
            // 通过刷新的方式让taobao源码中出现本页的json
            if (page.getSiteName().equals("taobao"))
            {
                driver.navigate().refresh();
                URLFecter.sleep(1);
            }

            String entity = driver.getPageSource();
            List<ProductModel> pdata = Parse.getData(page.getSiteName(), entity);
            data.addAll(pdata);
        }

        if (page.getPage() < page.getMaxPage())
        {
            nextPage(page);
            return true;
        }
        else
            return false;
    }

    private void nextPage(Page page)
    {
        // 在每一页停留随机时长(s)
        // 一分钟以内
//        int sleepTime = Math.abs(new Random(47).nextInt()) % 15;
//        URLFecter.sleep(sleepTime * 1000);
        page.setPage(page.getPage() + 1);

        String siteName = page.getSiteName();
        switch (siteName)
        {
            case "jd" -> {
                URLFecter.sleep(1);
                driver.findElement(By.className("fp-next")).click();
                page.setUrl(driver.getCurrentUrl());
            }
            case "tmall" -> {
                driver.findElement(By.cssSelector("a[class='ui-page-next']")).click();
                page.setUrl(driver.getCurrentUrl());

                if (driver.getTitle().equals("验证码拦截"))
                    slide();
            }
            case "taobao" -> {
                driver.findElement(By.cssSelector("a[trace='srp_bottom_pagedown']")).click();
                // 避免因为过快导致未翻页就刷新
                URLFecter.sleep(1);

                // 滑块页
                if (driver.getTitle().equals("验证码拦截"))
                    slide();
                // 页面嵌入滑块
                WebElement thisPage = driver.findElement(By.cssSelector("li[class='item active']"));
                int thisPageNum = Integer.parseInt(thisPage.findElement(By.className("num")).getText());
                if (thisPageNum != page.getPage())
                {
                    // slide
                    CrawlerMain.logger.info("anti-craw");
                }

                page.setUrl(driver.getCurrentUrl());
            }
        }
    }

    // 人工滑动滑块
    private void slide()
    {
        // 1s加载页面
        URLFecter.sleep(1);
//        String js = "var element = document.getElementById('nc_1_n1z');" +
//                "element.style = 'width: 300px;';" +
//                "var element = document.getElementById('nc_1__bg');" +
//                "element.style = 'width: 300px;';";
//        driver.executeScript(js);

        while (driver.getTitle().equals("验证码拦截"))
        {
            CrawlerMain.logger.info("请滑动滑块");
            URLFecter.sleep(5);
        }

        // 防止页面还没加载好就读数据
        URLFecter.sleep(5);
    }

    private List<ProductModel> scrollPage(Page page)
    {
        // 滚动条到最下方加载所有商品
        String js1 = "window.scrollTo(0,document.body.scrollHeight)";
        ((JavascriptExecutor) driver).executeScript(js1);

        // 避免网页没有刷新完 以及避免最后一页不足60死循环
        List<ProductModel> pageData = new ArrayList<>();
        int loop = 0;
        while ((pageData.size() == 30 || pageData.size() == 0) && loop < 3)
        {
            // 等待网页刷新
            URLFecter.sleep(1);
            // 得到并处理网页源码
            String entity = driver.getPageSource();
            pageData = Parse.getData(page.getSiteName(), entity);
            loop ++;
        }

        return pageData;
    }
}
