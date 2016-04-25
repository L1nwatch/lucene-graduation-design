package ucas.ir.lucene;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import ucas.ir.pojo.News;

public class Test {
	public static void main(String[] args) {
		File dataPth=new File("./WebContent/data");
		if (dataPth.exists()) {
			System.out.println("目录存在");
		}
		
		
		ArrayList<News> arrlist=new ArrayList<News>();
		News n1=new News("title1", "ct1", "source2", "url1", "summary1", "keyword1");
		News n2=new News("title2", "ct1", "source2", "url1", "summary1", "keyword1");
		News n3=new News("title3", "ct1", "source2", "url1", "summary1", "keyword1");
		arrlist.add(n1);
		arrlist.add(n2);
		arrlist.add(n3);
		Iterator<News> iter=arrlist.iterator();
		while(iter.hasNext()){
			System.out.println(((News)iter.next()).getTitle());
		}
	}
}
