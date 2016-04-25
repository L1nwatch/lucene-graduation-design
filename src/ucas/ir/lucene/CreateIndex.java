package ucas.ir.lucene;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.*;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import ucas.ir.pojo.News;

import javax.servlet.*;

public class CreateIndex {

	public static void main(String[] args) {
		// 第一步：创建分词器
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_43);
		// 第二步：创建indexWriter配置信息
		IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_43, analyzer);
		// 第三步：设置索引的打开方式
		indexWriterConfig.setOpenMode(OpenMode.CREATE);
		// 第四步：设置索引第路径
		Directory directory = null;
		// 第五步:创建indexWriter,用于索引第增删改.
		IndexWriter indexWriter = null;

		try {
			File indexpath = new File("/Users/yp/Documents/workspace/UCASIR/WebContent/index");
			if (indexpath.exists() != true) {
				indexpath.mkdirs();
			}
			directory = FSDirectory.open(indexpath);
			if (indexWriter.isLocked(directory)) {
				indexWriter.unlock(directory);
			}
			indexWriter = new IndexWriter(directory, indexWriterConfig);

		} catch (IOException e) {
			e.printStackTrace();
		}

		// 循环创建索引

		ArrayList<String> filenamelist = getfileName();
		Iterator<String> iter = filenamelist.iterator();

		while (iter.hasNext()) {
			// System.out.println(iter.next());
			News news = getNews("./WebContent/data/" + iter.next());
			System.out.println(news.getTitle());
			Document doc = new Document();
			doc.add(new TextField("news_title", news.getTitle(), Store.YES));
			doc.add(new TextField("news_cat", news.getCat(), Store.YES));
			doc.add(new TextField("news_source", news.getSource(), Store.YES));
			doc.add(new TextField("news_url", news.getUrl(), Store.YES));
			doc.add(new TextField("news_summary", news.getSummary(), Store.YES));
			doc.add(new TextField("news_keyword", news.getKeyword(), Store.YES));

			try {
				indexWriter.addDocument(doc);
				indexWriter.commit();

			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		try {
			indexWriter.close();
			directory.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("index create success!");

	}

	// 获取data目录下所有json文件的文件名,返回文件名数组
	public static ArrayList<String> getfileName() {
		ArrayList<String> arrlist = new ArrayList<String>();
		File dataPth = new File("./WebContent/data");
		if (dataPth.exists()) {

			File[] allFiles = dataPth.listFiles();
			for (int i = 0; i < allFiles.length; i++) {
				arrlist.add(allFiles[i].getName().toString());
			}
		}

		System.out.println(arrlist.size());
		return arrlist;
	}

	// 把json文件解析为News对象,返回值为News对象

	public static News getNews(String path) {
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
