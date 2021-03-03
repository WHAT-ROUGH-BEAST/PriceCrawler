package db;

import model.ProductModel;
import util.ConfigUtils;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class ProductDB
{
    // 驱动路径
    private static final String DBDRIVER;
    // 数据库地址
    private static final String DBURL;
    // 数据库登录用户名
    private static final String DBUSER;
    // 数据库用户密码
    private static final String DBPASSWORD;
    // 数据库连接
    private static Connection conn = null;
    private static ProductDB instance = null;
    private static Statement stmt = null;

    static
    {
        DBDRIVER = ConfigUtils.getDBElement("driver");
        DBURL = ConfigUtils.getDBElement("url");
        DBUSER = ConfigUtils.getDBElement("user");
        DBPASSWORD = ConfigUtils.getDBElement("pw");
    }

    private ProductDB()
    {
    }

    synchronized public static ProductDB getInstance()
    {
        if (null == instance)
        {
            instance = new ProductDB();

            // 保证不会重复连接
            try
            {
                conn = instance.getConnection();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            throw new RuntimeException("数据库正在使用");
        }
        return instance;
    }

    public void killInstance()
    {
        closeStmt(stmt);
        stmt = null;
        closeConnection(conn);
        conn = null;

        instance = null;
    }

    private Connection getConnection() throws Exception
    {
        //加载驱动程序
        Class.forName(DBDRIVER);
        //连接数据库
        conn = DriverManager.getConnection(DBURL, DBUSER, DBPASSWORD);

        return conn;
    }

    private void closeConnection(Connection con)
    {
        try
        {
            if (con != null)
                con.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void closeStmt(Statement stmt)
    {
        try
        {
            if (stmt != null)
                stmt.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void closeResultSet(ResultSet rs)
    {
        try
        {
            if (rs != null)
                rs.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void executeUpdateStmt(String sql)
    {
        closeStmt(stmt); // 关闭上次stmt
        stmt = null;
        try
        {
            stmt = conn.createStatement();
            stmt.executeUpdate(sql);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            System.out.println(sql);
        }
    }

    private ResultSet executeQueryStmt(String sql)
    {
        closeStmt(stmt); // 关闭上次stmt
        stmt = null;
        ResultSet rs = null;
        try
        {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            return rs;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    // 所有的结果放在统一的搜索关键词下
    private void createTable(String searchItem)
    {
        // 以关键词作为表名
        String tableName = searchItem;

        String sql = " USE PriceCrawler\n" +
                "IF EXISTS(Select 1 From Sysobjects Where Name='"+ tableName +"')\n" +
                "DROP table " + tableName + "\n" +
                " CREATE TABLE " + tableName + "\n" +
                " (\n" +
                "\tPSite char(10) NOT NULL,\n" +
                "\tPName varchar(255) NOT NULL,\n" +
                "\tPId\t varchar(50) Primary Key NOT NULL,\n" +
                "\tPPrice float NOT NULL\n" +
                " );";

        executeUpdateStmt(sql);
    }

    public void writeProducts(Set<ProductModel> products, String searchItem)
    {
        createTable(searchItem);

        for (ProductModel p : products)
        {
            String tableName = searchItem;
            String sql = "insert into " + tableName +" values(" +
                    "'" + p.getSite() + "', " +
                    // 避免名字中的'影响sql
                    "'" + p.getProductName().replace("'", "‘").replace("\n", " ") + "', " +
                    "'" + p.getProductId() + "', " +
                    p.getProductPrice() + ");";

            executeUpdateStmt(sql);
        }
    }

    public Set<ProductModel> readProducts(String searchItem, String site)
    {
        String tableName = searchItem;
        String sql = "SELECT * FROM " + tableName + " " +
                "WHERE PSite='" + site + "';";

        ResultSet rs = executeQueryStmt(sql);

        Set<ProductModel> products = new HashSet<>();
        try
        {
            rs.next();
            while (!rs.isAfterLast())
            {
                ProductModel p = new ProductModel(
                        rs.getString("PSite").trim(),
                        rs.getString("PName").trim(),
                        rs.getString("PId").trim(),
                        rs.getString("PPrice").trim()
                );

                products.add(p);

                rs.next();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            closeResultSet(rs);
        }

        return products;
    }

    public ProductModel getCheapest(String searchItem, String site)
    {
        String tableName = searchItem;

        String sql = "SELECT * FROM " + tableName + "\n" +
                "WHERE (PPrice IN\n" +
                "(SELECT MIN(PPrice)\n" +
                "FROM louie WHERE PSite = '" + site + "'))\n" +
                "AND PSite = '" + site + "'";

        ResultSet rs = executeQueryStmt(sql);

        try
        {
            rs.next();
            ProductModel p = new ProductModel(
                    rs.getString("PSite").trim(),
                    rs.getString("PName").trim(),
                    rs.getString("PId").trim(),
                    rs.getString("PPrice").trim()
            );

            rs.next();
            if (rs.isAfterLast())
                return p;
            else
                throw new Exception("sql error: not only cheapest product");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            closeResultSet(rs);
        }

        return null;
    }

    public static void main(String[] args)
    {
        ProductModel p1 = new ProductModel("jd", "三体1", "123456", "10");
        ProductModel p2 = new ProductModel("jd", "三体2", "123457", "12");

        Set<ProductModel> products = new HashSet<>();
        products.add(p1);
        products.add(p2);

        ProductDB db = ProductDB.getInstance();

        db.writeProducts(products, "三体");
        Set<ProductModel> productModels = db.readProducts("三体", "jd");
        System.out.println(productModels);
        System.out.println(db.getCheapest("三体", "jd"));

        db.killInstance();
    }
}
