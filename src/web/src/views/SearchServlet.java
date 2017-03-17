package web.src.views;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
//import org.apache.lucene.analysis.standard.StandardAnalyzer;  // 单字分词, 已弃用
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.apache.lucene.search.highlight.TokenSources;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import web.src.models.News;
import web.src.models.Page;
import net.paoding.analysis.analyzer.PaodingAnalyzer;

public class SearchServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static int totalnews = 0;
    private static final int perPageCount = 5;

    public SearchServlet() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);

    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 表单参数处理
        String query = request.getParameter("query");
        query = new String(query.getBytes("iso8859-1"), "UTF-8");

        // 计算查询时间
        long starTime = System.currentTimeMillis();// start time
        String indexPathStr = request.getSession().getServletContext().getRealPath("/new_index");
        if (query != null && " ".equals(query) != true) {

            String pagenum = request.getParameter("p");
            System.out.println("pagenum:" + pagenum);
            int p = pagenum == null ? 1 : Integer.parseInt(pagenum);
            System.out.println("p:" + p);

            // 获取排序方式
            String sortmethod = request.getParameter("sortnews");
            System.out.println("排序方式:" + sortmethod);
            ArrayList<News> newsList = getTopDoc(query, indexPathStr);

            Page page = new Page(p, newsList.size() / perPageCount + 1, perPageCount, newsList.size(),
                    perPageCount * (p - 1), perPageCount * p, true, p == 1 ? false : true);
            System.out.println(page.toString());
            if ("byTime".equals(sortmethod)) {
//                Collections.sort(newsList, new SortByTime());
            }

            // 修正 BUG, 这里如果超出索引值, 参考: http://stackoverflow.com/questions/12099721/how-to-use-sublist
            List<News> pageList = newsList.subList(perPageCount * (p - 1),
                    perPageCount * p > newsList.size() ? newsList.size() : perPageCount * p);
            // 设置分页
            System.out.println("servlet newslist length:" + newsList.size());
            request.setAttribute("query", query);
            request.setAttribute("newslist", pageList);
            request.setAttribute("queryback", query);
            request.setAttribute("totaln", totalnews);
            request.setAttribute("perPageCount", perPageCount);
            long endTime = System.currentTimeMillis();// end time
            long Time = endTime - starTime;
            request.setAttribute("time", (double) Time / 1000);
            request.setAttribute("page", page);
            request.getRequestDispatcher("result.jsp").forward(request, response);
        } else {
            request.getRequestDispatcher("error.jsp").forward(request, response);
        }

    }

    public static ArrayList<News> getTopDoc(String key, String indexPathStr) {
        ArrayList<News> newsList = new ArrayList<News>();

        Directory directory = null;
        try {
            File indexPath = new File(indexPathStr);
            if (indexPath.exists() != true) {
                indexPath.mkdirs();
            }
            // 设置要查询的索引目录
            directory = FSDirectory.open(indexPath);
            // 创建indexSearcher
            DirectoryReader dReader = DirectoryReader.open(directory);
            IndexSearcher searcher = new IndexSearcher(dReader);

            String[] fields = {"news_title", "news_article"};
            // 设置分词方式
            // Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_43);// 标准分词
            Analyzer paodingAnalyzer = new PaodingAnalyzer();   // paoding 分词

            MultiFieldQueryParser parser2 = new MultiFieldQueryParser(Version.LUCENE_43, fields, paodingAnalyzer);
            Query query2 = parser2.parse(key);

            QueryScorer scorer = new QueryScorer(query2, fields[0]);
            SimpleHTMLFormatter formatter = new SimpleHTMLFormatter("<span style=\"color:red;\">", "</span>");
            Highlighter highLighter = new Highlighter(formatter, scorer);

            // 返回前10条
            TopDocs topDocs = searcher.search(query2, 500);
            if (topDocs != null) {
                totalnews = topDocs.totalHits;
                System.out.println("符合条件第文档总数：" + totalnews);
                for (int i = 0; i < topDocs.scoreDocs.length; i++) {
                    Document doc = searcher.doc(topDocs.scoreDocs[i].doc);

                    // 设置高亮
                    highLighter.setTextFragmenter(new SimpleSpanFragmenter(scorer));

                    // 设置 title 高亮
                    TokenStream tokenStream = TokenSources.getAnyTokenStream(searcher.getIndexReader(),
                            topDocs.scoreDocs[i].doc, fields[0], new PaodingAnalyzer());
                    String highTitle = highLighter.getBestFragment(tokenStream, doc.get(fields[0]));

                    // 设置 content 高亮
                    tokenStream = TokenSources.getAnyTokenStream(searcher.getIndexReader(), topDocs.scoreDocs[i].doc,
                            fields[1], new PaodingAnalyzer());
                    String fragmentSeparator = "...";
                    int maxNumFragmentsRequired = 1;
                    String highContent = highLighter.getBestFragments(tokenStream, doc.get(fields[1]),
                            maxNumFragmentsRequired, fragmentSeparator);

                    // 原始的 article 长度
                    int raw_article_length = doc.get("news_article").length();

                    News news = new News(doc.get("news_url"),
                            highContent != null ? highContent : doc.get("news_article"),
                            doc.get("news_id"),
                            highTitle != null ? highTitle : doc.get("news_title"),
                            raw_article_length);
                    newsList.add(news);

                }

                // TODO: HITS 算法插入位置
                newsList = newsList;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return newsList;
    }

}
