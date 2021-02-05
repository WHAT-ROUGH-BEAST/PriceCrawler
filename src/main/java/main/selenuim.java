package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import util.ConfigUtils;

public class selenuim
{
    public static void main(String[] args)
    {
        // TODO Auto-generated method stub

        //setting the driver executable
        System.setProperty("webdriver.chrome.driver", "D:\\IntelliJ IDEA\\chromedriver_win32\\chromedriver.exe");

        //Initiating your chromedriver
        WebDriver driver=new ChromeDriver();

        //Applied wait time
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        //maximize window
        driver.manage().window().maximize();

        //cookie
        ConfigUtils.setCurrentSource("taobao");
        String cookie = ConfigUtils.getElement("cookie");
        String[] cs = cookie.split(";");
        ArrayList<String> csl = new ArrayList<>();
        for (String c : cs)
            csl.addAll(Arrays.asList(c.split("=")));

        for (int i = 0; i < csl.size(); i = i + 2)
            driver.manage().addCookie(new Cookie(csl.get(i), csl.get(i + 1)));

        System.out.println(csl);

        //open browser with desried URL
        driver.get(ConfigUtils.getElement("website"));

        //get html

        //closing the browser
        driver.close();
    }
}
