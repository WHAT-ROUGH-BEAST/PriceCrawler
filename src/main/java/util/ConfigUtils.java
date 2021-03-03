package util;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.*;

public class ConfigUtils
{
    private static Map<String, Map<String, String>> attrs;
    private static String driverAdd, user, pw, loginUrl;
    private static Document doc;
    private static Map<String, String> dbInfo;

    // 初始化attrs
    static
    {
        readXML();
        initAttr();
        initDBInfo();
    }

    private static void readXML()
    {
        try
        {
            DocumentBuilderFactory dFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = dFactory.newDocumentBuilder();
            InputStream is = ConfigUtils.class.getClassLoader().getResourceAsStream("config.xml");
            doc = builder.parse(is);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private static void initDBInfo()
    {
        Node node = doc.getElementsByTagName("database").item(0);

        dbInfo = new HashMap<>();
        for (int j = 1; j < node.getChildNodes().getLength(); j = j + 2)
        {
            Node n = node.getChildNodes().item(j);
            dbInfo.put(n.getNodeName(), n.getTextContent());
        }

        return;
    }

    private static void initAttr()
    {
        driverAdd = doc.getElementsByTagName("driveradd").item(0).getTextContent();
        loginUrl = doc.getElementsByTagName("login").item(0).getTextContent();
        user = doc.getElementsByTagName("user").item(0).getTextContent();
        pw = doc.getElementsByTagName("pw").item(0).getTextContent();

        NodeList nl = doc.getElementsByTagName("source");

        Map<String, Map<String, String>> attrmap = new HashMap<>();
        for (int i = 0; i < nl.getLength(); i ++)
        {
            Node node = nl.item(i);
            HashMap<String, String> map = new HashMap<>();

            for (int j = 1; j < node.getChildNodes().getLength(); j = j + 2)
            {
                Node n = node.getChildNodes().item(j);
                map.put(n.getNodeName(), n.getTextContent());
            }

            attrmap.put(node.getAttributes().getNamedItem("name").getNodeValue(),
                    Collections.unmodifiableMap(map));
        }

        attrs = Collections.unmodifiableMap(attrmap);
    }

    public static String getElement(String source, String type)
    {
        return attrs.get(source).get(type);
    }

    public static String getDriverAdd()
    {
        return driverAdd;
    }

    public static String getLoginUrl()
    {
        return loginUrl;
    }

    public static String getUser()
    {
        return user;
    }

    public static String getPw()
    {
        return pw;
    }

    public static String getDBElement(String element)
    {
        return dbInfo.get(element);
    }
}
