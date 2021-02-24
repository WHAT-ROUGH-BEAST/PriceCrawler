package parse;

import model.ProductModel;
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
    public static List<ProductModel> getData(String sc, String html)
    {
        Document doc = Jsoup.parse(html);

        switch (sc)
        {
            case "jd":
                return Jdparse(doc);
            case "tmall":
                return Tmallparse(doc);
            case "taobao":
                return Taobaoparse(doc);
        }

        throw new RuntimeException("no such source");
    }

    private static List<ProductModel> Jdparse(Document doc)
    {
        List<ProductModel> data = new ArrayList<>();

        Elements elements = doc.select("ul[class=gl-warp clearfix]").select("li[class=gl-item]");
        for (Element elem : elements)
        {
            String site = "jd";
            String productID = elem.attr("data-sku");
            String productPrice = elem.select("div[class=p-price]").select("strong").select("i").text();
            String productName = elem.select("div[class=p-name]").select("em").text();
            if (productName.isEmpty())
                productName = elem.select("div[class=p-name p-name-type-2]").select("em").text();

            ProductModel productModel = new ProductModel();

            productModel.setProductId(productID);
            productModel.setProductName(productName);
            productModel.setProductPrice(productPrice);
            productModel.setSite(site);

            data.add(productModel);
        }

        return data;
    }

    private static List<ProductModel> Tmallparse(Document doc)
    {
        List<ProductModel> data = new ArrayList<>();

        Elements ulList = doc.select("div[class='view grid-nosku']");
        Elements liList = ulList.select("div[class='product']");
        // 循环liList的数据（具体获取的数据值还得看doc的页面源代码来获取，可能稍有变动）
        for (Element item : liList)
        {
            String site = "tmall";
            // 商品ID
            String productID = item.select("div[class='product']").select("p[class='productStatus']").select("span[class='ww-light ww-small m_wangwang J_WangWang']").attr("data-item");
            // 商品名称
            String productName = item.select("p[class='productTitle']").select("a").attr("title");
            // 商品价格
            String productPrice = item.select("p[class='productPrice']").select("em").attr("title");

            ProductModel productModel = new ProductModel();

            productModel.setProductId(productID);
            productModel.setProductName(productName);
            productModel.setProductPrice(productPrice);
            productModel.setSite(site);

            data.add(productModel);
        }

        return data;
    }

    private static List<ProductModel> Taobaoparse(Document doc)
    {
        List<ProductModel> data = new ArrayList<>();

        // 得到相关的js内容
        String jsonStr = "";
        Elements scripts = doc.select("script");
//        jsonStr = scripts.get(39).childNode(0).toString();
        for (Element s : scripts)
            if (s.childNodeSize() > 0 && s.childNode(0).toString().contains("g_page_config"))
                jsonStr = s.childNode(0).toString();

        jsonStr = jsonStr.split("=")[1].
            split("g_srp_loadCss();")[0];

        // 分析javascript
        JSONObject jsonObj = new JSONObject(jsonStr);
        JSONArray jsonArr = jsonObj.getJSONObject("mods").
                getJSONObject("itemlist").
                getJSONObject("data").
                getJSONArray("auctions");

        for(int i = 0; i < jsonArr.length(); i ++)
        {
            JSONObject tagValue = jsonArr.getJSONObject(i);

            String site = "taobao";
            // 商品ID
            String productID = tagValue.getString("nid");
            // 商品名称
            String productName = tagValue.getString("raw_title");
            // 商品价格
            String productPrice = tagValue.getString("view_price");

            ProductModel productModel = new ProductModel();

            productModel.setProductId(productID);
            productModel.setProductName(productName);
            productModel.setProductPrice(productPrice);
            productModel.setSite(site);

            data.add(productModel);
        }

        return data;
    }
}
