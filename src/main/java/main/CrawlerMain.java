package main;

import model.BookModel;
import model.Page;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;
import util.ConfigUtils;
import util.URLFecter;

import java.util.*;

public class CrawlerMain
{
    static Logger logger = Logger.getLogger(CrawlerMain.class);

    public static void main(String[] args)
    {
        Page jd = new Page("tmall", "三体");
        craw(jd);
    }

    private static void craw(Page page)
    {
        List<BookModel> data = null;
        try
        {
            data = URLFecter.URLParser(page);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        logger.info("read from: " + page.getSiteName() + "\n" + "count " + data.size());

        for (BookModel da : data)
            CrawlerMain.logger.info("bookID:"+da.getBookId()+"\t\t"+"bookPrice:"+da.getBookPrice()+
                    "\t\t"+"bookName:"+da.getBookName());

        // db
    }
}
