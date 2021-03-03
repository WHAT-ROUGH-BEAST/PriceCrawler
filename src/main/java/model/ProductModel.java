package model;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ProductModel implements Comparable<ProductModel>
{
    private String site;
    private String productName;
    private String productId;
    private String productPrice;

    public ProductModel() {}

    public ProductModel(String site, String productName, String productId, String productPrice)
    {
        this.site = site;
        this.productName = productName;
        this.productId = productId;
        this.productPrice = productPrice;
    }

    public String getSite()
    {
        return site;
    }

    public void setSite(String site)
    {
        this.site = site;
    }

    public String getProductName()
    {
        return productName;
    }

    public String getProductId()
    {
        return productId;
    }

    public String getProductPrice()
    {
        return productPrice;
    }

    public void setProductName(String productName)
    {
        this.productName = productName;
    }

    public void setProductId(String productId)
    {
        this.productId = productId;
    }

    public void setProductPrice(String productPrice)
    {
        this.productPrice = productPrice;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ProductModel that = (ProductModel) o;
        return productId.equals(that.productId);
    }

    @Override
    public String toString()
    {
        return "Product: " + site + " " + productId + " " + productName + " ¥" + productPrice;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(productId);
    }

    @Override
    public int compareTo(@NotNull ProductModel o)
    {
        double thisPrice = Double.parseDouble(this.productPrice),
                oPrice = Double.parseDouble(o.productPrice);

        if (thisPrice > oPrice)
            return 1;
        else if (thisPrice == oPrice)
            return 0;
        else
            return -1;
    }
}
