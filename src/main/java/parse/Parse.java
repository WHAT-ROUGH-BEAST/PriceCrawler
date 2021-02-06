package parse;

import model.BookModel;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class Parse
{
    public static List<BookModel> getData(String sc, String html)
    {
        Document doc = Jsoup.parse(html);

        if (sc.equals("jd"))
            return Jdparse(doc);
        else if (sc.equals("tmall"))
            return Tmallparse(doc);
        else if (sc.equals("taobao"))
            return Taobaoparse(doc);

        throw new RuntimeException("no such source");
    }

    private static List<BookModel> Jdparse(Document doc)
    {
        List<BookModel> data = new ArrayList<>();

        Elements elements = doc.select("ul[class=gl-warp clearfix]").select("li[class=gl-item]");
        for (Element elem : elements)
        {
            String bookID = elem.attr("data-sku");
            String bookPrice = elem.select("div[class=p-price]").select("strong").select("i").text();
            String bookName = elem.select("div[class=p-name]").select("em").text();

            BookModel bookModel = new BookModel();

            bookModel.setBookId(bookID);
            bookModel.setBookName(bookName);
            bookModel.setBookPrice(bookPrice);

            data.add(bookModel);
        }

        return data;
    }

    private static List<BookModel> Tmallparse(Document doc)
    {
        List<BookModel> data = new ArrayList<>();

        Elements ulList = doc.select("div[class='view grid-nosku']");
        Elements liList = ulList.select("div[class='product']");
        // 循环liList的数据（具体获取的数据值还得看doc的页面源代码来获取，可能稍有变动）
        for (Element item : liList)
        {
            // 商品ID
            String bookID = item.select("div[class='product']").select("p[class='productStatus']").select("span[class='ww-light ww-small m_wangwang J_WangWang']").attr("data-item");
            // 商品名称
            String bookName = item.select("p[class='productTitle']").select("a").attr("title");
            // 商品价格
            String bookPrice = item.select("p[class='productPrice']").select("em").attr("title");

            BookModel bookModel = new BookModel();

            bookModel.setBookId(bookID);
            bookModel.setBookName(bookName);
            bookModel.setBookPrice(bookPrice);

            data.add(bookModel);
        }

        return data;
    }

    private static List<BookModel> Taobaoparse(Document doc)
    {
        List<BookModel> data = new ArrayList<>();

        Elements scripts = doc.select("script");

        String jsonStr = scripts.get(7).childNode(0).toString();

        jsonStr = jsonStr.split("=")[1].
            split("g_srp_loadCss();")[0];

        // 分析javascript
        JSONObject jsonObj = new JSONObject(jsonStr);
        JSONArray jsonArr = jsonObj.getJSONObject("mods").
                getJSONObject("itemlist").
                getJSONObject("data").
                getJSONArray("auctions");

        for(int i = 0;i < jsonArr.length(); i ++)
        {
            JSONObject tagValue = jsonArr.getJSONObject(i);
            // 商品ID
            String bookID = tagValue.getString("nid");
            // 商品名称
            String bookName = tagValue.getString("raw_title");
            // 商品价格
            String bookPrice = tagValue.getString("view_price");

            BookModel bookModel = new BookModel();

            bookModel.setBookId(bookID);
            bookModel.setBookName(bookName);
            bookModel.setBookPrice(bookPrice);

            data.add(bookModel);
        }

        return data;
    }
}
