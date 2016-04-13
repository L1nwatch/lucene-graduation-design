package ucas.ir.pojo;

public class News {
   
	private String title;
	private String cat;
	private String source;
	private String url;
	private String summary;
	private String keyword;
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getCat() {
		return cat;
	}
	public void setCat(String cat) {
		this.cat = cat;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getSummary() {
		return summary;
	}
	public void setSummary(String summary) {
		this.summary = summary;
	}
	public String getKeyword() {
		return keyword;
	}
	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
	@Override
	public String toString() {
		return "News [title=" + title + ", cat=" + cat + ", source=" + source + ", url=" + url + ", summary=" + summary
				+ ", keyword=" + keyword + "]";
	}
	
	
	public News() {
		super();
	}
	public News(String title, String cat, String source, String url, String summary, String keyword) {
		super();
		this.title = title;
		this.cat = cat;
		this.source = source;
		this.url = url;
		this.summary = summary;
		this.keyword = keyword;
	}
	
	
	
}
