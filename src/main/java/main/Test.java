package main;

import model.Page;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test
{
    public static void main(String[] args)
    {
        Page jd = new Page("jd", "三体");
        jd.setPage(3);
        System.out.println(jd.getPage());
        System.out.println(jd.getUrl());

        String url = jd.getUrl();
        System.out.println(url.replaceFirst("page=(\\d*)", "page"));


        String str = new String("Site is BeginnersBook.com");

        System.out.print("String after replacing com with net :" );
        System.out.println(str.replaceFirst("com", "net"));

        System.out.print("String after replacing Site name:" );
        System.out.println(str.replaceFirst("Beginners(.*)", "XYZ.com"));
    }
}
