package util;

import main.CrawlerMain;
import model.BookModel;
import model.Page;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.Mouse;
import org.openqa.selenium.interactions.PointerInput;
import parse.Parse;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PageCrawler
{
    private ChromeDriver driver;
    private Page page;
    private List<BookModel> data;

    public PageCrawler(ChromeDriver driver, Page page, final List<BookModel> data)
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
            if (siteName.equals("tmall"))
                maxPage = Integer.parseInt(driver.findElement(By.cssSelector("input[name='totalPage']")).getAttribute("value"));
            else if (siteName.equals("taobao"))
                maxPage = Integer.parseInt(driver.findElement(By.cssSelector("input[aria-label='页码输入框']")).getAttribute("max"));
            else if (siteName.equals("jd"))
                maxPage = Integer.parseInt(driver.findElement(By.cssSelector("span[class='p-skip']")).findElement(By.tagName("b")).getText());
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
            // 通过刷新的方式让源码中出现本页的json
            driver.navigate().refresh();
            URLFecter.sleep(1000);

            String entity = driver.getPageSource();
            List<BookModel> pdata = Parse.getData(page.getSiteName(), entity);
            data.addAll(pdata);
        }

        // 翻页
        nextPage(page);

        return page.getPage() <= page.getMaxPage();
    }

    private void nextPage(Page page)
    {
        page.setPage(page.getPage() + 1);

        if (page.getPage() == page.getMaxPage())
            return;

        String siteName = page.getSiteName();
        if (siteName.equals("jd"))
        {
            driver.findElement(By.className("fp-next")).click();
            page.setUrl(driver.getCurrentUrl());
        }
        else if (siteName.equals("tmall"))
        {
            driver.findElement(By.cssSelector("a[class='ui-page-next']")).click();
            page.setUrl(driver.getCurrentUrl());

            // 处理滑块
            if (driver.getTitle().equals("验证码拦截"))
                slide();
        }
        else if (siteName.equals("taobao"))
        {
            try
            {
                driver.findElement(By.cssSelector("a[trace='srp_bottom_pagedown']")).click();
            }
            catch (Exception e)
            {
                CrawlerMain.logger.info("anti-crawler found");
                page.setPage(page.getPage() - 1);
            }

            page.setUrl(driver.getCurrentUrl());

            URLFecter.sleep(1000);

            // 处理滑块 TODO
            if (driver.getTitle().equals("验证码拦截"))
            {
                slide();
            }
        }
    }

    // TODO
    private void slide()
    {
        String js = "var element = document.getElementById('nc_1_n1z');" +
                "element.style = 'width: 300px;';" +
                "var element = document.getElementById('nc_1__bg');" +
                "element.style = 'width: 300px;';";
        driver.executeScript(js);

        WebElement slider = driver.findElement(By.cssSelector("span[id='nc_1_n1z']"));
        Actions move = new Actions(driver);
        move.dragAndDropBy(slider, 1, 0).perform();

        if (driver.getTitle().equals("验证码拦截"))
        {
            driver.navigate().refresh();
            URLFecter.sleep(1000);
            slide();
        }
    }
//
//    private List<Integer> randSlideSteps(int px, int step)
//    {
//        List<Integer> steps = new ArrayList<>();
//        int add = 0;
//        for (int i = 0; i < px / step; i ++)
//        {
//            int rand = Math.abs(new Random(47).nextInt()) % (step) + 1;
//            add += rand;
//            steps.add(rand);
//        }
//
//        steps.add(px - add);
//
//        return steps;
//    }

    private List<BookModel> scrollPage(Page page)
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
            URLFecter.sleep(500);
            // 得到并处理网页源码
            String entity = driver.getPageSource();
            pageData = Parse.getData(page.getSiteName(), entity);
            loop ++;
        }

        return pageData;
    }
}
