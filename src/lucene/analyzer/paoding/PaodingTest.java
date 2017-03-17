package lucene.analyzer.paoding;

/**
 * Created by L1n on 17/3/17.
 * 参考：http://blog.csdn.net/zhu_tianwei/article/details/46607489
 */

import java.io.StringReader;

import net.paoding.analysis.analyzer.PaodingAnalyzer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

public class PaodingTest {


    public static void main(String[] args) throws Exception {
        String text = "生成analyzer实例  将项目中的dic复制到工程的classpath下，默认配置";
        testSplitChinese(text);
        System.out.println("==============");
        testDemo(text);
    }

    /**
     * 分词测试
     */
    public static void testSplitChinese(String text) throws Exception {
        // 生成analyzer实例 将项目中的dic复制到工程的根下，若修改paoding.dic.home，更换位置
        Analyzer analyzer = new PaodingAnalyzer();
        // 取得Token流
        TokenStream tokenizer = analyzer.tokenStream("text", new StringReader(text));
        tokenizer.reset();
        // 添加工具类 注意：以下这些与之前lucene2.x版本不同的地方
        CharTermAttribute offAtt = (CharTermAttribute) tokenizer.addAttribute(CharTermAttribute.class);
        // 循环打印出分词的结果，及分词出现的位置
        while (tokenizer.incrementToken()) {
            System.out.print(offAtt.toString() + "\t");
        }
        tokenizer.close();
    }

    private static Document createDocument(String title, String content) {
        Document doc = new Document();
        doc.add(new TextField("title", title, Store.YES));
        doc.add(new TextField("content", content, Store.YES));
        return doc;
    }

    /**
     * lucene简单实例
     */
    public static void testDemo(String text) throws Exception {
        Analyzer analyzer = new PaodingAnalyzer();
        Directory idx = new RAMDirectory();
        IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_43, analyzer);
        IndexWriter writer = new IndexWriter(idx, iwc);
        writer.addDocument(createDocument("维基百科:关于中文维基百科", "维基百科:关于中文维基百科"));
        writer.commit();
        IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(idx));
        System.out.println("命中个数:" + searcher.search(new QueryParser(Version.LUCENE_43,
                "title", analyzer).parse("title:'维基'"), 10).totalHits);
        writer.close();
    }
}
