package util;

import model.Page;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.message.BasicHttpResponse;

import java.io.IOException;

public class HTTPUtils
{
    public static HttpResponse getRawHtml(HttpClient client, Page page)
    {
        HttpGet getMethod = new HttpGet(page.getUrl());

        // 登陆情况
        getMethod.setHeader("User-Agent", page.getAgent());
        getMethod.addHeader("Cookie", page.getCookie());

        HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1,
                HttpStatus.SC_OK, "OK");

        try
        {
            //执行get方法
            response = client.execute(getMethod);
        }
        catch (IOException e)
        {
            e.printStackTrace();

        }

        return response;
    }
}
