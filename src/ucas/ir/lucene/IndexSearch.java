package ucas.ir.lucene;

import java.io.File;
import java.io.IOException;

import javax.print.Doc;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
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
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.apache.lucene.search.highlight.TokenSources;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.wltea.analyzer.lucene.IKAnalyzer;

public class IndexSearch {

	public static void main(String[] args) {
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
			// 设置分词方式
			Analyzer analyze2 = new StandardAnalyzer(Version.LUCENE_43);// 标准分词
			Analyzer analyzer = new IKAnalyzer();

			// 设置查询域
			String field="news_title";
			String[] fields={"news_title","news_summady"};
			String key="阿法狗";
			//单域查询
			QueryParser parser = new QueryParser(Version.LUCENE_43, field, analyzer);
			// 查询字符串
			Query query = parser.parse(key);

			//多域查询
			MultiFieldQueryParser parser2 =new MultiFieldQueryParser(Version.LUCENE_43, fields, analyzer);
			Query query2=parser2.parse(key);
			
			QueryScorer scorer=new QueryScorer(query2,field);
			SimpleHTMLFormatter fors=new SimpleHTMLFormatter("<span style=\"color:red;\">", "</span>");
			Highlighter highlighter=new Highlighter(fors, scorer);
			  
			System.out.println("query:" + query.toString());
			// 返回前10条
			TopDocs topDocs = searcher.search(query, 5);
			if (topDocs != null) {
				System.out.println("符合条件第文档总数：" + topDocs.totalHits);

				for (int i = 0; i < topDocs.scoreDocs.length; i++) {
					Document doc = searcher.doc(topDocs.scoreDocs[i].doc);
					TokenStream tokenStream=TokenSources.getAnyTokenStream(searcher.getIndexReader(), topDocs.scoreDocs[i].doc, field, analyzer);
					Fragmenter  fragment=new SimpleSpanFragmenter(scorer);
			        highlighter.setTextFragmenter(fragment); 
			        //高亮news_title域
			        String str=highlighter.getBestFragment(tokenStream, doc.get("news_title"));//获取高亮的片段，可以对其数量进行限制  
					System.out.println("高亮title："+str);
					tokenStream=TokenSources.getAnyTokenStream(searcher.getIndexReader(), topDocs.scoreDocs[i].doc, "news_summary", analyzer);
					str=highlighter.getBestFragment(tokenStream, doc.get("news_summary"));//获取高亮的片段，可以对其数量进行限制  
					System.out.println("高亮summary："+str);
				}
			}
			directory.close();
			dReader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
