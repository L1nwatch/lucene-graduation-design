package web.src.views;

import java.util.Comparator;

import web.src.models.News;

class SortByTime implements Comparator{

	@Override
	public int compare(Object o1, Object o2) {
		News n1=(News) o1;
		News n2=(News) o2;
		
		return n2.getTime().compareTo(n1.getTime());
	}
	
}
