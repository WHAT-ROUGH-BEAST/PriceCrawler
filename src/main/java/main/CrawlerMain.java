package main;

import model.ProductModel;
import model.Page;
import org.apache.log4j.Logger;
import org.openqa.selenium.chrome.ChromeDriver;
import util.ConfigUtils;
import util.URLFecter;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CrawlerMain
{
    public static Logger logger = Logger.getLogger(CrawlerMain.class);
    public static List<String> sites = new ArrayList<>(Arrays.asList(
            "taobao", "jd", "tmall"));
    public static String searchItem = "louie";

    public static void main(String[] args)
    {
        HashMap<String, ProductModel> mins = new HashMap<>();
        try
        {
            for (String site : sites)
            {
                Set<ProductModel> data = new URLFecter(new Page(site, searchItem)).URLParser();
                mins.put(site, Collections.min(data));
            }

            mins.forEach((s, v)->{
                logger.info(s + " : " + v.getBookPrice() + " " + v.getBookName());
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        System.exit(1);
    }
}
