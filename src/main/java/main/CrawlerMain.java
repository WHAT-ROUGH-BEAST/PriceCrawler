package main;

import model.ProductModel;
import model.Page;
import org.apache.log4j.Logger;
import util.URLFecter;

import java.util.*;

public class CrawlerMain
{
    public static Logger logger = Logger.getLogger(CrawlerMain.class);
    public static List<String> sites = new ArrayList<>(Arrays.asList(
            "taobao", "jd", "tmall"));
    public static String searchItem = "三体";

    public static void main(String[] args)
    {
        HashMap<String, ProductModel> mins = new HashMap<>();
        try
        {
//            for (String site : sites)
//            {
//                Set<ProductModel> data = new URLFecter(new Page(site, searchItem)).URLParser();
//                mins.put(site, Collections.min(data));
//            }

            // test taobao
            Set<ProductModel> data = new URLFecter(new Page("taobao", searchItem)).URLParser();
            mins.put("jd", Collections.min(data));

            mins.forEach((s, v)->{
                logger.info(s + " : " + v.getProductPrice() + " " + v.getProductName());
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        System.exit(1);
    }
}
