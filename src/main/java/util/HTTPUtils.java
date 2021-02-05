package util;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.message.BasicHttpResponse;

import java.io.IOException;

public class HTTPUtils
{
    public static HttpResponse getRawHtml(HttpClient client, String personalUrl)
    {
        HttpGet getMethod = new HttpGet(personalUrl);

        // 登陆情况
        getMethod.setHeader("User-Agent", ConfigUtils.getElement("agent"));
        getMethod.addHeader("Cookie", ConfigUtils.getElement("cookie"));

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
