package web.src.views;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
//import org.apache.lucene.analysis.standard.StandardAnalyzer;  // 单字分词, 已弃用
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.apache.lucene.search.highlight.TokenSources;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

// 自定义
import web.src.models.News;
import web.src.models.Page;
import hits.MyHITS;
import net.paoding.analysis.analyzer.PaodingAnalyzer;
import sqlite.interactive.SQLInteractive;


public class SearchServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static int totalNews = 0;
    private static final int perPageCount = 5;

    public SearchServlet() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);

    }

    /*
     * 通过访问数据库获取指定页面指向的所有页面
     */
    protected ArrayList<News> getOutPages(String pageId, SQLInteractive dbOperator) {
        ArrayList<News> resultList = new ArrayList<>();
        // 查询该 id 是否存在于表 linkindoc 中
        if (dbOperator.checkPageIdInLinkInDoc(pageId)) {
            // 存在则把该页面指向的所有 ID 拿出来
            ArrayList<String> allIDList = dbOperator.getAllOutFromLinkInDoc(pageId);

            // 然后到 domainid2url 这个表中把所有链接拿出来
            allIDList.forEach(eachId -> {
                ArrayList<String> allURLList = dbOperator.getURLFromDomainID2URL(eachId);

                allURLList.forEach(each_url -> {
                    String content = String.format("这是一个由搜索结果 %s 指向的页面", pageId);
                    // 创建一个 news, 添加进 resultList 中
                    News newPage = new News(each_url, content, "null", "某个被搜索结果指向的页面", content.length());
                    resultList.add(newPage);
                });
            });
        }

        return resultList;
    }

    /*
     * 通过访问数据库获取所有指向给定页面的页面
     */
    protected ArrayList<News> getInPages(String pageId, SQLInteractive dbOperator) {
        ArrayList<News> resultList = new ArrayList<>();
        // 查询该 id 是否存在于表 linkindoc 中
        if (dbOperator.checkPageIdInLinkInDoc(pageId)) {
            // 存在则把所有指向该页面的 ID 拿出来
            ArrayList<String> allIDList = dbOperator.getAllInFromLinkInDoc(pageId);

            // 然后到 domainid2url 这个表中把所有链接拿出来
            allIDList.forEach(eachId -> {
                ArrayList<String> allURLList = dbOperator.getURLFromDomainID2URL(eachId);

                allURLList.forEach(each_url -> {
                    // 创建一个 news, 添加进 resultList 中
                    String content = String.format("这是一个指向搜索结果 %s 指向的页面", pageId);
                    News newPage = new News(each_url, content, "null", "某个指向搜索结果的页面", content.length());
                    resultList.add(newPage);
                });
            });
        }

        return resultList;
    }

    /*
     * 通过链接关系库扩展根集变为基本集
     */
    protected ArrayList<News> expandSet(ArrayList<News> rawNewsList, Set<String> pagesSet) {
        ArrayList<News> resultList = new ArrayList<>(rawNewsList);
        Set<String> expandPagesSet = new HashSet<>(pagesSet);
        SQLInteractive dbOperator = new SQLInteractive();
        dbOperator.startConnection();

        // 遍历每一个页面
        rawNewsList.forEach(each_new -> {
            // 把该页面指向的所有页面都加入进来
            ArrayList<News> outPages = getOutPages(each_new.getId().split("-")[1], dbOperator);
            outPages.forEach(each_page -> {
                if (!expandPagesSet.contains(each_page.getURL().toLowerCase())) {
                    expandPagesSet.add(each_page.getURL().toLowerCase());
                    resultList.add(each_page);
                }
            });

            // 把所有指向该页面的页面中摘选至多 d 个加进来, 文献给出的 d = 50
            // TODO: d 的值不合适
            ArrayList<News> inPages = getInPages(each_new.getId().split("-")[1], dbOperator);
            int limit = 50; // 至多加进 50 个页面
            int counts = 0;
            for (News each_page : inPages) {
                if (counts < limit && !expandPagesSet.contains(each_page.getURL().toLowerCase())) {
                    expandPagesSet.add(each_page.getURL().toLowerCase());
                    resultList.add(each_page);
                    counts += 1;
                } else if (counts >= limit) {
                    break;
                }
            }
        });

        dbOperator.closeConnection();
        return resultList;
    }

    /*
     * 获取 HITS 算法所需的基本集
     */
    protected ArrayList<News> getBaseSet(ArrayList<News> rawNewsList) {
        ArrayList<News> resultList = new ArrayList<>();
        Set<String> pagesSet = new HashSet<>();

        // java 的 for each 方式
        rawNewsList.forEach(each_new -> {
            if (!pagesSet.contains(each_new.getURL().toLowerCase())) {
                pagesSet.add(each_new.getURL().toLowerCase());
                resultList.add(each_new);
            }
        });

        if (resultList.size() > 200) {
            return expandSet((ArrayList<News>) resultList.subList(0, 200), pagesSet);
        } else {
            return expandSet(resultList, pagesSet);
        }
    }

    /*
     * 获取网页链接关系, 以矩阵表示
     */
    protected boolean[][] getLinkMatrix(ArrayList<News> pagesList) {
        boolean[][] linkMatrix = new boolean[pagesList.size()][pagesList.size()];

        // TODO: 这里还没有网页链接的数据库, 先伪造数据
        for (int x = 0; x < pagesList.size(); ++x) {
            for (int y = 0; y < pagesList.size(); ++y) {
                linkMatrix[x][y] = Math.random() < 0.5; // 随机构造链接关系;
            }
        }

        return linkMatrix;
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 表单参数处理
        String query = request.getParameter("query");
        query = new String(query.getBytes("iso8859-1"), "UTF-8");

        // 计算查询时间
        long starTime = System.currentTimeMillis();// start time

        // 存放 index 的路径
        String indexPathStr = request.getSession().getServletContext().getRealPath("/new_index");   // paoding 分词
//        String indexPathStr = request.getSession().getServletContext().getRealPath("/full_index");   // 单字分词

        // 开始查询
        if (query != null && " ".equals(query) != true) {
            // 要显示第几页
            String pageNum = request.getParameter("p");
            int p = (pageNum == null) ? 1 : Integer.parseInt(pageNum);
            System.out.println(String.format("[*] 用户要显示第 %s 页, 实际显示第 %d 页", pageNum, p));

            // 获取排序方式
            String sortMethod = request.getParameter("sortnews");
            sortMethod = sortMethod == null ? "HITS" : sortMethod;
            System.out.println(String.format("[*] 用户希望使用的排序方式是: %s", sortMethod));

            // 这里得到的是原始的 N 个网页
            ArrayList<News> rawNewsList = getTopDoc(query, indexPathStr);

            // 1. 拿出网页数量(大于 200 个拿 200 个, 小于 200 个拿全部)
            ArrayList<News> baseSet = getBaseSet(rawNewsList);
            totalNews = baseSet.size();
            // 2. 获取网页链接关系
            boolean[][] linkMatrix = getLinkMatrix(baseSet);

            // 3. 按 HITS 排序或者 PageRank 排序
            ArrayList<News> sortedPagesList = new ArrayList<>();
            if ("HITS".equals(sortMethod)) {
                // HITS 排序插入到这里
                MyHITS hits = new MyHITS(baseSet);
                sortedPagesList = hits.hitsSort(linkMatrix);
            } else if ("PageRank".equals(sortMethod)) {
                // TODO: PageRank 排序插入到这里
            } else {
                throw new ServletException(String.format("[-] 用户选择了不支持的排序算法: %s", sortMethod));
            }

            // 获取要展示到前端的页面
            Page page = new Page(p, sortedPagesList.size() / perPageCount + 1, perPageCount, sortedPagesList.size(),
                    perPageCount * (p - 1), perPageCount * p, true, p == 1 ? false : true);
            System.out.println(page.toString());
            // 修正 BUG, 这里如果超出索引值, 参考: http://stackoverflow.com/questions/12099721/how-to-use-sublist
            List<News> pageList = sortedPagesList.subList(perPageCount * (p - 1),
                    perPageCount * p > sortedPagesList.size() ? sortedPagesList.size() : perPageCount * p);

            // 设置分页
            System.out.println(String.format("[*] 要展示到前端的 sortedPagesList 长度为: %s", sortedPagesList.size()));

            // jsp 相关设定
            request.setAttribute("query", query);
            request.setAttribute("newslist", pageList);
            request.setAttribute("queryback", query);
            request.setAttribute("totaln", totalNews);
            request.setAttribute("perPageCount", perPageCount);

            // 计算搜索耗时
            long endTime = System.currentTimeMillis();// end time
            long Time = endTime - starTime;
            request.setAttribute("time", (double) Time / 1000);

            //
            request.setAttribute("page", page);
            request.getRequestDispatcher("result.jsp").forward(request, response);
        } else {
            // 重定向到错误页面
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
//             Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_43);// 标准分词
            Analyzer paodingAnalyzer = new PaodingAnalyzer();   // paoding 分词

            MultiFieldQueryParser parser2 = new MultiFieldQueryParser(Version.LUCENE_43, fields, paodingAnalyzer);
//            MultiFieldQueryParser parser2 = new MultiFieldQueryParser(Version.LUCENE_43, fields, analyzer);
            Query query2 = parser2.parse(key);

            QueryScorer scorer = new QueryScorer(query2, fields[0]);
            SimpleHTMLFormatter formatter = new SimpleHTMLFormatter("<span style=\"color:red;\">", "</span>");
            Highlighter highLighter = new Highlighter(formatter, scorer);

            // 返回前 500 条?
            TopDocs topDocs = searcher.search(query2, 500);
            if (topDocs != null) {
                totalNews = topDocs.totalHits;
                System.out.println("[*] 符合条件第文档总数：" + totalNews);
                for (int i = 0; i < topDocs.scoreDocs.length; i++) {
                    Document doc = searcher.doc(topDocs.scoreDocs[i].doc);

                    // 设置高亮
                    highLighter.setTextFragmenter(new SimpleSpanFragmenter(scorer));

                    // 设置 title 高亮
                    TokenStream tokenStream = TokenSources.getAnyTokenStream(searcher.getIndexReader(), topDocs.scoreDocs[i].doc, fields[0], new PaodingAnalyzer());
//                    TokenStream tokenStream = TokenSources.getAnyTokenStream(searcher.getIndexReader(),topDocs.scoreDocs[i].doc, fields[0], analyzer);
                    String highTitle = highLighter.getBestFragment(tokenStream, doc.get(fields[0]));

                    // 设置 content 高亮
                    tokenStream = TokenSources.getAnyTokenStream(searcher.getIndexReader(), topDocs.scoreDocs[i].doc, fields[1], new PaodingAnalyzer());
//                    tokenStream = TokenSources.getAnyTokenStream(searcher.getIndexReader(), topDocs.scoreDocs[i].doc,fields[1], analyzer);
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
                System.out.println(String.format("[*] 计算得到的 newsList 长度为 %s", newsList.size()));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return newsList;
    }

}
