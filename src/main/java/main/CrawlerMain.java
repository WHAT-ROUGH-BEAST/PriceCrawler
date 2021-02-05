package main;

import model.BookModel;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;
import util.ConfigUtils;
import util.URLFecter;

import java.util.*;

public class CrawlerMain
{
    static Logger logger = Logger.getLogger(CrawlerMain.class);

    public static void main(String[] args) throws Exception
    {
        HttpClient httpClient = HttpClientBuilder.create().build();

        // 网站
        ConfigUtils.setCurrentSource("taobao");
        String url = ConfigUtils.getElement("website");

        List<BookModel> jdData = URLFecter.URLParser(httpClient, url);

        logger.info("read from: " + ConfigUtils.getCurrentSource() + "\n");

        for (BookModel jd:jdData)
        {
            logger.info("bookID:"+jd.getBookId()+"\t\t"+"bookPrice:"+jd.getBookPrice()+"\t\t"+"bookName:"+jd.getBookName());
        }

        // db
    }
}
