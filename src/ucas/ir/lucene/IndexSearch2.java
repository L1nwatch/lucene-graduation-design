package ucas.ir.lucene;

import java.io.File;
import java.io.IOException;

import javax.print.Doc;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class IndexSearch2 {

    public static void main(String[] args) {
        Directory directory = null;
        try {
            File indexpath = new File("/Users/yp/Documents/workspace/UCASIR/WebContent/index");
            if (indexpath.exists() != true) {
                indexpath.mkdirs();
            }
            //设置要查询的索引目录
            directory = FSDirectory.open(indexpath);
            //创建indexSearcher
            DirectoryReader dReader = DirectoryReader.open(directory);
            IndexSearcher searcher = new IndexSearcher(dReader);
            //设置分词方式
            Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_43);

            //设置查询域
            QueryParser parser = new QueryParser(Version.LUCENE_43, "news_title", analyzer);
            // 查询字符串
            Query query = parser.parse("阿法狗");
            
            String[] fields={"news_title","news_summary"};
            
            MultiFieldQueryParser parser2=new MultiFieldQueryParser(Version.LUCENE_43, fields, analyzer);
            Query query2=parser2.parse("凤凰网编辑");
            System.out.println("query:"+query.toString());
            // 返回前10条
            TopDocs topDocs = searcher.search(query2, 10);
            if (topDocs != null) {
                System.out.println("符合条件第文档总数：" + topDocs.totalHits);

                for (int i = 0; i < topDocs.scoreDocs.length; i++) {
                    Document doc = searcher.doc(topDocs.scoreDocs[i].doc);
                    System.out.println("news_title= " + doc.get("news_title"));
                    System.out.println("news_abstract=" + doc.get("news_summary"));
                }
            }

            directory.close(); 
            dReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}