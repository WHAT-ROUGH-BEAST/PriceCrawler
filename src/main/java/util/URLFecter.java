package util;

import model.BookModel;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.util.EntityUtils;
import parse.Parse;

import java.io.File;
import java.io.PrintWriter;
import java.util.*;

public class URLFecter
{
    public static List<BookModel> URLParser (HttpClient client, String url) throws Exception
    {
        List<BookModel> data = new ArrayList<>();
        HttpResponse response = HTTPUtils.getRawHtml(client, url);

        int StatusCode = response.getStatusLine().getStatusCode();

        if (StatusCode == 200)
        {
            String entity = EntityUtils.toString(response.getEntity(), "utf-8");

            // DEBUG : cookie
//            PrintWriter writer = new PrintWriter(new File("cookie.html"));
//            writer.println(entity);

            data = Parse.getData(ConfigUtils.getCurrentSource(), entity);
        }

        EntityUtils.consume(response.getEntity());

        return data;
    }
}
