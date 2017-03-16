package web.src.models;

public class News {

    private String id;// 标记文档的 id
    private String Title;// 文档的标题
    private String Article;// 新闻正文内容
    private String Summary;// 用于搜索结果的显示
    private String URL;// 新闻 url

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
        if (this.getArticle().length() > 200) {
            // 防止 <span> 标签被中途截断
            int position = this.Article.lastIndexOf(new String("</span>"), 200);

            if (position == -1) {
                // 如果找不到 "</span>" 直接显示 200 个
                return this.getArticle().substring(0, 200) + "...";
            } else if (position < 200) {
                // 如果找到了, 但是位置在 200 之前, 则再次查找 250 之前的 </span> 标签
                position = this.Article.lastIndexOf(new String("</span>"), 250) + new String("</span>").length();
                return this.getArticle().substring(0, position) + "...";
            } else {
                position = position + new String("</span>").length();
                return this.getArticle().substring(0, position) + "...";
            }
        } else {
            return this.getArticle();
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
