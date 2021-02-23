package model;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ProductModel implements Comparable<ProductModel>
{
    private String bookName;
    private String bookId;
    private String bookPrice;

    public String getBookName()
    {
        return bookName;
    }

    public String getBookId()
    {
        return bookId;
    }

    public String getBookPrice()
    {
        return bookPrice;
    }

    public void setBookName(String bookName)
    {
        this.bookName = bookName;
    }

    public void setBookId(String bookId)
    {
        this.bookId = bookId;
    }

    public void setBookPrice(String bookPrice)
    {
        this.bookPrice = bookPrice;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ProductModel that = (ProductModel) o;
        return bookId.equals(that.bookId);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(bookId);
    }

    @Override
    public int compareTo(@NotNull ProductModel o)
    {
        double thisPrice = Double.parseDouble(this.bookPrice),
                oPrice = Double.parseDouble(o.bookPrice);

        if (thisPrice > oPrice)
            return 1;
        else if (thisPrice == oPrice)
            return 0;
        else
            return -1;
    }
}
