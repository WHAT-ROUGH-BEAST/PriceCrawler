package model;

import util.ConfigUtils;

public class Page
{
    private String siteName;
    private String url;
    private String searchItem;
    private String cookie;
    private String agent;
    private int page = 1;

    public Page(String siteName, String searchItem)
    {
        this(siteName, searchItem, 1);
    }

    public Page(String siteName, String searchItem, int page)
    {
        setSiteName(siteName);
        setUrl(ConfigUtils.getElement(siteName, "website"));
        setSearchItem(searchItem);
        setPage(page);
        setAgent(ConfigUtils.getElement(siteName, "agent"));
        setCookie(ConfigUtils.getElement(siteName, "cookie"));
    }

    public int getPage()
    {
        return page;
    }

    public void setPage(int page)
    {
        this.page = page;
//        if (siteName.equals("jd"))
//        {
//            url = url.replaceFirst("#PAGE#", String.valueOf(page));
//            url = url.replaceFirst("page=(\\d*)", "page=" + String.valueOf(page));
//        }
    }

    public String getSiteName()
    {
        return siteName;
    }

    public String getUrl()
    {
        return url;
    }

    public String getSearchItem()
    {
        return searchItem;
    }

    public String getCookie()
    {
        return cookie;
    }

    public String getAgent()
    {
        return agent;
    }

    public void setSiteName(String siteName)
    {
        this.siteName = siteName;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public void setSearchItem(String searchItem)
    {
        this.searchItem = searchItem;
        url = url.replaceFirst("#SEARCHITEM#", searchItem);
    }

    public void setCookie(String cookie)
    {
        this.cookie = cookie;
    }

    public void setAgent(String agent)
    {
        this.agent = agent;
    }
}
