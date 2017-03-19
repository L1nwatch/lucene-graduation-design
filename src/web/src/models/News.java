package web.src.models;

public class News {

    private String id;// 标记文档的 id
    private String Title;// 文档的标题
    private String Article;// 新闻正文内容
    private String Summary;// 用于搜索结果的显示
    private Integer ArticleLength;// 原来正文内容的长度
    private String URL;// 新闻 url
    private double authority;   // 权威值

    public double getAuthority() {
        return authority;
    }

    public void setAuthority(double authority) {
        this.authority = authority;
    }

    public Integer getArticleLength() {
        return ArticleLength;
    }

    public void setArticleLength(Integer articleLength) {
        ArticleLength = articleLength;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getArticle() {
        return Article;
    }

    public String getSummary() {
        if (ArticleLength == this.getArticle().length()) {
            return this.getArticle();
        } else {
            return this.getArticle() + "...";
        }
    }

    public void setSummary(String summary) {
        Summary = summary;
    }

    public void setArticle(String article) {
        Article = article;
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String uRL) {
        URL = uRL;
    }

    public News() {
        super();
    }

    // 用于创建显示搜索页面
    public News(String url, String content, String doc_number, String content_title, int articleLength) {
        super();
        this.id = doc_number;
        Title = content_title;
        Article = content;
        URL = url;
        ArticleLength = articleLength;
    }


    // 用于索引创建
    public News(String url, String content, String doc_number, String content_title) {
        super();
        this.id = doc_number;
        Title = content_title;
        Article = content;
        URL = url;
    }

    @Override
    public String toString() {
        return "News [id=" + id + ", Title=" + Title + ", Article=" + Article + ", URL=" + URL + "]";
    }
}
