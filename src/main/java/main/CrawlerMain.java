package main;

import db.ProductDB;
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
    public static String searchItem = "louie";

    public static void main(String[] args)
    {
        try
        {
            Set<ProductModel> data = new HashSet<>();
            for (String site : sites)
                data.addAll(new URLFecter(new Page(site, searchItem)).URLParser());
            // db
            writeDB(data, searchItem);

            sites.forEach((site)->{
                ProductModel p = getCheapest(searchItem, site);
                logger.info(site + " : ¥" + p.getProductPrice() + " " + p.getProductName());
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        System.exit(1);
    }

    public static void writeDB(Set<ProductModel> data, String searchItem)
    {
        logger.info("writing in database : " + searchItem);
        ProductDB db = ProductDB.getInstance();

        db.writeProducts(data, searchItem);

        db.killInstance();
        logger.info("done written");
    }

    public static ProductModel getCheapest(String searchItem, String site)
    {
        ProductDB db = ProductDB.getInstance();

        ProductModel product = db.getCheapest(searchItem, site);

        db.killInstance();

        return product;
    }
}
