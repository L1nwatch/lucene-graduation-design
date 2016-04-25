package ucas.ir.action;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.cjk.CJKAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.apache.lucene.search.highlight.TokenSources;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;
import org.wltea.analyzer.lucene.IKAnalyzer;

import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import ucas.ir.pojo.News;

public class SearchServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public SearchServlet() {
		super();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);

	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String query = request.getParameter("query");
		query = new String(query.getBytes("iso8859-1"), "UTF-8");
		if (query != null && "".equals(query) != true) {
			request.setAttribute("query", query);
			ArrayList<News> newsList = getTopDoc(query);
			System.out.println("servlet newslist length:" + newsList.size());
			request.setAttribute("newslist", newsList);
			request.setAttribute("queryback", query);
		}

		request.getRequestDispatcher("result.jsp").forward(request, response);
	}

	public static ArrayList<News> getTopDoc(String key) {
		ArrayList<News> newsList = new ArrayList<News>();

		Directory directory = null;
		try {
			File indexpath = new File("/Users/yp/Documents/workspace/UCASIR/WebContent/index");
			if (indexpath.exists() != true) {
				indexpath.mkdirs();
			}
			// 设置要查询的索引目录
			directory = FSDirectory.open(indexpath);
			// 创建indexSearcher
			DirectoryReader dReader = DirectoryReader.open(directory);
			IndexSearcher searcher = new IndexSearcher(dReader);

			String[] fields = { "news_title", "news_summary" };
			// 设置分词方式
			Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_43);// 标准分词
			MultiFieldQueryParser parser2 = new MultiFieldQueryParser(Version.LUCENE_43, fields, analyzer);
			Query query2 = parser2.parse(key);
			TermQuery tq=new TermQuery(new Term("name", key));
            PrefixQuery prefixQuery=new PrefixQuery(new Term("name",key));
            PhraseQuery phraseQuery=new PhraseQuery();
            phraseQuery.setSlop(3);
            phraseQuery.add(new Term("field", "key1"));
            phraseQuery.add(new Term("field", "key2"));
            WildcardQuery wildcardQuery=new WildcardQuery(new Term("field", "基于?"));
           
            
            BooleanQuery bQuery=new BooleanQuery();
            bQuery.add(new TermQuery(new Term("title", "lucene")), Occur.MUST);
            bQuery.add(new TermQuery(new Term("content", "基于")), Occur.SHOULD);
            bQuery.add(new TermQuery(new Term("name", "java")), Occur.MUST_NOT);
            
            Query q = NumericRangeQuery.newFloatRange("weight", 0.03f, 0.10f, true, true);
            QueryScorer scorer = new QueryScorer(query2, fields[0]);
			SimpleHTMLFormatter fors = new SimpleHTMLFormatter("<span style=\"color:red;\">", "</span>");
			Highlighter highlighter = new Highlighter(fors, scorer);
			// 返回前10条
			TopDocs topDocs = searcher.search(query2, 10);
			if (topDocs != null) {
				System.out.println("符合条件第文档总数：" + topDocs.totalHits);

				for (int i = 0; i < topDocs.scoreDocs.length; i++) {
					Document doc = searcher.doc(topDocs.scoreDocs[i].doc);

					TokenStream tokenStream = TokenSources.getAnyTokenStream(searcher.getIndexReader(),
							topDocs.scoreDocs[i].doc, fields[0], analyzer);
					Fragmenter fragment = new SimpleSpanFragmenter(scorer);
					highlighter.setTextFragmenter(fragment);
					
					String hl_title=highlighter.getBestFragment(tokenStream, doc.get("news_title"));

					
					tokenStream = TokenSources.getAnyTokenStream(searcher.getIndexReader(),
							topDocs.scoreDocs[i].doc, fields[1], analyzer);
					String hl_summary=highlighter.getBestFragment(tokenStream, doc.get("news_summary"));
					News news = new News(hl_title!=null?hl_title:doc.get("news_title"), doc.get("news_cat"), doc.get("news_source"),
							doc.get("news_url"), hl_summary!=null?hl_summary:doc.get("news_summary"), doc.get("news_keyword"));
					newsList.add(news);

				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return newsList;
	}

}
