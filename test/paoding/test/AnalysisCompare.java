/**
 *
 */
package paoding.test;

import java.io.IOException;
import java.io.StringReader;

import net.paoding.analysis.analyzer.PaodingAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleFragmenter;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.util.Version;
import org.apache.lucene.search.Query;

/**
 * @author ZhenQin
 */
public class AnalysisCompare {

    /**
     *
     */
    public AnalysisCompare() {

    }


    public static void parse(Analyzer analyzer, String text) throws Exception {
        TokenStream ts = analyzer.tokenStream("aaa", new StringReader(text));
        ts.reset();
        // 添加工具类 注意：以下这些与之前lucene2.x版本不同的地方
        CharTermAttribute offAtt = ts.addAttribute(CharTermAttribute.class);
        // 循环打印出分词的结果，及分词出现的位置

        try {
            QueryParser queryParse = new QueryParser(Version.LUCENE_43, "aaa", analyzer);
            Query query = queryParse.parse("吃饭");
            SimpleHTMLFormatter simpleHTMLFormatter = new SimpleHTMLFormatter("<b><font color='red'>", "</font></b>");
            Highlighter highlighter = new Highlighter(simpleHTMLFormatter, new QueryScorer(query));
            highlighter.setTextFragmenter(new SimpleFragmenter(Integer.MAX_VALUE));
            Analyzer paodingAnalyzer = new PaodingAnalyzer();
            TokenStream tokenStream = paodingAnalyzer.tokenStream("aaa", new StringReader(text));
            String str = highlighter.getBestFragment(tokenStream, text);
            System.out.println(str);


        } catch (Exception e) {
            throw e;
        }


        while (ts.incrementToken()) {
            System.out.print(offAtt.toString() + "\t");
        }
        System.out.println();
        ts.close();
    }


    public static void main(String[] args) throws Exception {
        Analyzer paodingAnalyzer = new PaodingAnalyzer();

        String text = "你吃饭了吗";

        parse(paodingAnalyzer, text);
//        parse(paodingAnalyzer, text);
//        parse(paodingAnalyzer, text);
//        parse(paodingAnalyzer, text);
//        parse(paodingAnalyzer, text);
    }

}
