package web.src.models;

public class News {

    private String id;//news id
    private String Title;//news title
    private String Keyword;//key words of news
    private String Time;//news post time
    private String Source;//news source
    private String Article;//news content
    private String Summary;
    private String Total;//total number of people
    private String URL;//news url
    private String Reply;//reply number
    private String Show;//review number

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

    public String getKeyword() {
        return Keyword;
    }

    public void setKeyword(String keyword) {
        Keyword = keyword;
    }

    public String getTime() {
        return Time;
    }

    public void setTime(String time) {
        Time = time;
    }

    public String getSource() {
        return Source;
    }

    public void setSource(String source) {
        Source = source;
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

    public String getTotal() {
        return Total;
    }

    public void setTotal(String total) {
        Total = total;
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String uRL) {
        URL = uRL;
    }

    public String getReply() {
        return Reply;
    }

    public void setReply(String reply) {
        Reply = reply;
    }

    public String getShow() {
        return Show;
    }

    public void setShow(String show) {
        Show = show;
    }

    public News() {
        super();
        // TODO Auto-generated constructor stub
    }

    public News(String id, String title, String keyword, String time, String source, String artical, String total,
                String uRL, String reply, String show) {
        super();
        this.id = id;
        Title = title;
        Keyword = keyword;
        Time = time;
        Source = source;
        Article = artical;
        Total = total;
        URL = uRL;
        Reply = reply;
        Show = show;
    }

    @Override
    public String toString() {
        return "News [id=" + id + ", Title=" + Title + ", Keyword=" + Keyword + ", Time=" + Time + ", Source=" + Source
                + ", Article=" + Article + ", Total=" + Total + ", URL=" + URL + ", Reply=" + Reply + ", Show=" + Show
                + "]";
    }


}
