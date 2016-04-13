package ucas.ir.action;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Stack;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import ucas.ir.pojo.News;

/**
 * Servlet implementation class SearchServlet
 */
public class SearchServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public SearchServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);

	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String query=request.getParameter("query");
		query=new String(query.getBytes("iso8859-1"), "UTF-8");
		String path1 =getServletContext().getRealPath("data/news01.json");
		String path2 =getServletContext().getRealPath("data/news02.json");
				
		if(query.equals("任志强")){
			System.out.println(query);
			request.setAttribute("news", getNews(path2));
		}else{
			request.setAttribute("news", getNews(path1));
			System.out.println(query);	
		}

		request.getRequestDispatcher("result.jsp").forward(request, response);
	}

	public News getNews(String path) {
		News news = new News();
		try {
			JsonParser jParser = new JsonParser();
			JsonObject jObject = (JsonObject) jParser.parse(new FileReader(path));
			String title = jObject.get("title").getAsString();
			String cat = jObject.get("cat").getAsString();
			String source = jObject.get("source").getAsString();
			String url = jObject.get("url").getAsString();
			String summary = jObject.get("summary").getAsString();
			String keyword = jObject.get("keyword").getAsString();
			news = new News(title, cat, source, url, summary, keyword);

		} catch (JsonIOException e) {
			e.printStackTrace();
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return news;

	}

}
