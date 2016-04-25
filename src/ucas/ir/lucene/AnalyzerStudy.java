package ucas.ir.lucene;

import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.cjk.CJKAnalyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.wltea.analyzer.lucene.IKAnalyzer;

public class AnalyzerStudy {
	public static void print(String str, Analyzer analyzer) {
		StringReader reader = new StringReader(str);
		try {
			TokenStream toStream = analyzer.tokenStream("", reader);
			toStream.reset();
			CharTermAttribute term = toStream.getAttribute(CharTermAttribute.class);
			System.out.println("分词技术" + analyzer.getClass());
			while (toStream.incrementToken()) {
				System.out.print(term.toString() + "|");
			}
			System.out.println("\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		
		Analyzer analyzer=null;
		String text = "IK Analyzer是一个结合词典分词和文法分词的中文分词开源工具包。它使用了全新的正向迭代最细粒度切分算法。";
       
		
		//标准分词
		analyzer=new StandardAnalyzer(Version.LUCENE_43);
		print(text, analyzer);
		
		//中文分词
		analyzer=new IKAnalyzer();//最细粒度切分
		print(text, analyzer);
		
		analyzer=new IKAnalyzer(true);//智能分词
		print(text, analyzer);
		
		
		//空格分词
		analyzer=new WhitespaceAnalyzer(Version.LUCENE_43);
		print(text, analyzer);
		
		//简单分词
		analyzer=new SimpleAnalyzer(Version.LUCENE_43);
		print(text, analyzer);
		
		//二分法
		analyzer=new CJKAnalyzer(Version.LUCENE_43);
		print(text, analyzer);
		
		//关键字分词
		analyzer=new KeywordAnalyzer();
		print(text, analyzer);
		
		//忽略词分词器
		analyzer=new StopAnalyzer(Version.LUCENE_43);
		print(text, analyzer);
		
		
		
		
		
		
		/*
		// Lucene Document域名
		String fieldName = "text";
		// 检索内容
		String text = "IK Analyzer是一个结合词典分词和文法分词的中文分词开源工具包。它使用了全新的正向迭代最细粒度切分算法。";
		// 实例化IKAnalyzer分词器
		Analyzer analyzer = new IKAnalyzer();

		Directory directory = null;
		IndexWriter indexWriter = null;
		IndexReader indexReader = null;
		IndexSearcher indexSearcher = null;

		// 建立内存索引对象
		directory = new RAMDirectory();

		IndexWriterConfig writerConfig = new IndexWriterConfig(Version.LUCENE_43, analyzer);
		writerConfig.setOpenMode(OpenMode.CREATE_OR_APPEND);
		try {
			indexWriter = new IndexWriter(directory, writerConfig);

			// 写入索引
			Document doc1 = new Document();
			doc1.add(new IntField("id", 100, Store.YES));
			doc1.add(new TextField(fieldName, text, Store.YES));

			indexWriter.addDocument(doc1);
			indexWriter.close();

			// 搜索过程

			indexReader = IndexReader.open(directory);
			indexSearcher = new IndexSearcher(indexReader);
			String keyword = "中文分词工具包";
			QueryParser queryParser = new QueryParser(Version.LUCENE_43, fieldName, analyzer);
			queryParser.setDefaultOperator(queryParser.AND_OPERATOR);
			Query query = queryParser.parse(keyword);
			TopDocs topDocs = indexSearcher.search(query, 10);
			for (int i = 0; i < topDocs.scoreDocs.length; i++) {
				Document doc = indexSearcher.doc(topDocs.scoreDocs[i].doc);
				System.out.println("内容：" + doc.get(fieldName));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
*/
	}
}
