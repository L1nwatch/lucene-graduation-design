package web.src.views;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
import pagerank.MyPageRank;
import net.paoding.analysis.analyzer.PaodingAnalyzer;
import sqlite.interactive.SQLInteractive;


public class SearchServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static int totalNews = 0;
    private static final int perPageCount = 20; // 设置每页显示多少条结果
    private static SQLInteractive sqlInteractive = new SQLInteractive();    // 用于数据库连接

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
    protected ArrayList<News> getOutPages(String domainId, SQLInteractive dbOperator) {
        ArrayList<News> resultList = new ArrayList<>();
        // 查询该 id 是否存在于表 linkindoc 中
        if (dbOperator.checkDomainIdInLinkInDoc(domainId)) {
            // 存在则把该页面指向的所有 ID 拿出来
            ArrayList<String> allIDList = dbOperator.getAllOutFromLinkInDoc(domainId);

            // 然后到 domainid2url 这个表中把所有链接拿出来
            allIDList.forEach(eachId -> {
                ArrayList<String> allURLList = dbOperator.getURLFromDomainID2URL(eachId);

                allURLList.forEach(each_url -> {
                    String content = String.format("这是一个由搜索结果 %s 指向的页面", domainId);
                    // 创建一个 news, 添加进 resultList 中
                    News newPage = new News(each_url, content,
                            String.format("xx-%s", eachId), "某个被搜索结果指向的页面", content.length());
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
        if (dbOperator.checkDomainIdInLinkInDoc(pageId)) {
            // 存在则把所有指向该页面的 ID 拿出来
            ArrayList<String> allIDList = dbOperator.getAllInFromLinkInDoc(pageId);

            // 然后到 domainid2url 这个表中把所有链接拿出来
            allIDList.forEach(eachId -> {
                ArrayList<String> allURLList = dbOperator.getURLFromDomainID2URL(eachId);

                allURLList.forEach(each_url -> {
                    // 创建一个 news, 添加进 resultList 中
                    String content = String.format("这是一个指向搜索结果 %s 的页面", pageId);
                    News newPage = new News(each_url, content,
                            String.format("xx-%s", eachId), "某个指向搜索结果的页面", content.length());
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
        SQLInteractive dbOperator = sqlInteractive;
        dbOperator.startConnection();

        // 遍历每一个页面
        rawNewsList.forEach(each_new -> {
            // 把该页面指向的所有页面都加入进来
            ArrayList<News> outPages = getOutPages(each_new.getDomainID(), dbOperator);
            outPages.forEach(each_page -> {
                if (!expandPagesSet.contains(each_page.getURL().toLowerCase())) {
                    expandPagesSet.add(each_page.getURL().toLowerCase());
                    resultList.add(each_page);
                }
            });

            // 把所有指向该页面的页面中摘选至多 d 个加进来, 文献给出的 d = 50
            // TODO: d 的值不合适
            ArrayList<News> inPages = getInPages(each_new.getDomainID(), dbOperator);
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
            System.out.println("[*] 接下来要对 200 个网页进行扩展");
            return expandSet((ArrayList<News>) resultList.subList(0, 200), pagesSet);
        } else {
            System.out.println(String.format("[*] 接下来要对 %d 个网页进行扩展", resultList.size()));
            return expandSet(resultList, pagesSet);
        }
    }

    /**
     * 判断用户的查询词是否存在于基本集之中
     *
     * @param answerDomain 用户的查询词对应的标准答案域名
     * @param baseSet      基本集
     * @return true or false
     */
    protected boolean is_in_base_set(String answerDomain, ArrayList<News> baseSet) {
        for (News each_new : baseSet) {
            if (each_new.getDomainID().equals(answerDomain)) {
                System.out.println(each_new.getArticle());
                return true;
            }
        }

        return false;
    }

    /**
     * 获取网页链接关系, 以矩阵表示
     *
     * @param pagesList
     * @param delete_inside_href true 表示要删除内联链接
     * @return
     */
    protected boolean[][] getLinkMatrix(ArrayList<News> pagesList, boolean delete_inside_href) {
        boolean[][] linkMatrix = new boolean[pagesList.size()][pagesList.size()];
        SQLInteractive dbOperator = sqlInteractive;

        dbOperator.startConnection();

        for (int x = 0; x < pagesList.size(); ++x) {
            for (int y = x; y < pagesList.size(); ++y) {
                News page_x, page_y;
                page_x = pagesList.get(x);
                page_y = pagesList.get(y);
                int checkResult = dbOperator.checkPagesLinkRelationShip(page_x, page_y);
                if (delete_inside_href && page_x.getDomainID().equals(page_y.getDomainID())) {
                    // 内联链接
                    linkMatrix[x][y] = false;
                    linkMatrix[y][x] = false;
                } else if (checkResult == 1) {
                    // 表示 pagesList[x] 指向了 pagesList[y]
                    linkMatrix[x][y] = true;
                    linkMatrix[y][x] = false;
                } else if (checkResult == 2) {
                    // 表示 pagesList[y] 指向了 pagesList[x]
                    linkMatrix[x][y] = false;
                    linkMatrix[y][x] = true;
                } else if (checkResult == 3) {
                    // 表示 pagesList[x] 与 pagesList[y] 互相指向
                    linkMatrix[x][y] = true;
                    linkMatrix[y][x] = true;
                } else {
                    // 表示两者无链接关系
                    linkMatrix[x][y] = false;
                    linkMatrix[y][x] = false;
                }
            }
        }

        dbOperator.closeConnection();

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

            // 这里得到的是原始的 N 个网页
            ArrayList<News> rawNewsList = getTopDoc(query, indexPathStr);

            // 1. 拿出网页数量(大于 200 个拿 200 个, 小于 200 个拿全部)
            long my_func_start = System.currentTimeMillis();    // 用来统计局部函数耗时的
            ArrayList<News> baseSetForHITS = getBaseSet(rawNewsList);
            ArrayList<News> baseSetForPageRank = new ArrayList<>(baseSetForHITS);
            long my_func_end = System.currentTimeMillis();      // 用来统计局部函数耗时的
            System.out.println(String.format("[*] 扩展根集为基本集的操作完成, 耗时 %d ms", my_func_end - my_func_start));

            // 评测用: 判断用户的查询词对应的答案是否存在于基本集之中
            if (is_in_base_set("69713306c0bb3300", baseSetForHITS)) {
                System.out.println(String.format("[*] 查询词 %s 对应的标准答案【存在】于基本集中", query));
            } else {
                System.out.println(String.format("[-] 查询词 %s 对应的标准答案【不存在】于基本集中", query));
            }


            // 2. 获取网页链接关系
            my_func_start = System.currentTimeMillis();    // 用来统计局部函数耗时的
            boolean[][] linkMatrixForHITS = getLinkMatrix(baseSetForHITS, true);
            boolean[][] linkMatrixForPageRank = getLinkMatrix(baseSetForHITS, false);
            my_func_end = System.currentTimeMillis();      // 用来统计局部函数耗时的
            System.out.println(String.format("[*] 获取网页间的链接关系操作完成, 耗时 %d ms", my_func_end - my_func_start));

            // 3. 按 HITS 排序或者 PageRank 排序
            // HITS 排序
            my_func_start = System.currentTimeMillis();    // 用来统计局部函数耗时的
            MyHITS hits = new MyHITS(baseSetForHITS);
            ArrayList<News> auth_sort_result = new ArrayList<>(hits.hitsSort(linkMatrixForHITS, "Authority"));
            // 插入 HITS 排序过后的权威页面
            int half_length = perPageCount / 2;
            List<News> hitsList = auth_sort_result.subList(0, auth_sort_result.size() >= half_length ? half_length : auth_sort_result.size());

            ArrayList<News> hub_sort_result = new ArrayList<>(hits.sortByHITSResult("Hub"));
            // 插入 HITS 排序过后的中心页面
            hitsList.addAll(hub_sort_result.subList(0, auth_sort_result.size() >= half_length ? half_length : auth_sort_result.size()));
            my_func_end = System.currentTimeMillis();      // 用来统计局部函数耗时的

            // 设置分页
            System.out.println(String.format("[*] HITS 排序完毕, 耗时 %d ms", my_func_end - my_func_start));
            System.out.println(String.format("[*] 要展示到前端的 hitsList 长度为: %s", hitsList.size()));

            // PageRank 排序
            my_func_start = System.currentTimeMillis();    // 用来统计局部函数耗时的
            MyPageRank pageRank = new MyPageRank(baseSetForPageRank);
            ArrayList<News> pageRankArrayList = pageRank.pageRankSort(pageRank.doubleMatrix(linkMatrixForPageRank));
            my_func_end = System.currentTimeMillis();      // 用来统计局部函数耗时的
            System.out.println(String.format("[*] PageRank 排序完毕, 耗时: %d ms", my_func_end - my_func_start));

            // 设置 PageRank 要展示到前端的页面
            // 修正 BUG, 这里如果超出索引值, 参考: http://stackoverflow.com/questions/12099721/how-to-use-sublist
            List<News> pageRankList = pageRankArrayList.subList(perPageCount * (p - 1),
                    perPageCount * p > pageRankArrayList.size() ? pageRankArrayList.size() : perPageCount * p);
            // 设置分页
            System.out.println(String.format("[*] 要展示到前端的 pageRankList 长度为: %s", pageRankList.size()));

            // jsp 相关设定
            request.setAttribute("query", query);

            request.setAttribute("hitsList", hitsList);
            request.setAttribute("pageRankList", pageRankList);

            request.setAttribute("queryBack", query);
            request.setAttribute("totalNews", totalNews);
            request.setAttribute("perPageCount", perPageCount);

            // 计算搜索耗时
            long endTime = System.currentTimeMillis();// end time
            long Time = endTime - starTime;
            request.setAttribute("time", (double) Time / 1000);

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

            // 返回前 200 条
            TopDocs topDocs = searcher.search(query2, 200);
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

                System.out.println(String.format("[*] 计算得到的 newsList 长度为 %s", newsList.size()));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return newsList;
    }

}
