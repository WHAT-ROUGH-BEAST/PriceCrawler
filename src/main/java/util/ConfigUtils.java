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

    private static String driverAdd;

    // 初始化attrs
    static
    {
        readAttr();
    }

    public static void readAttr()
    {
        DocumentBuilderFactory dFactory = DocumentBuilderFactory.newInstance();

        try
        {
            DocumentBuilder builder = dFactory.newDocumentBuilder();
            InputStream is = ConfigUtils.class.getClassLoader().getResourceAsStream("config.xml");
            Document doc = builder.parse(is);

            driverAdd = doc.getElementsByTagName("driveradd").item(0).getTextContent();

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
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static String getElement(String source, String type)
    {
        return attrs.get(source).get(type);
    }

    public static String getDriverAdd()
    {
        return driverAdd;
    }
}
