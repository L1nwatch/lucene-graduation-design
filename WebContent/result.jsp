<%@page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" import="ucas.ir.pojo.*,java.util.*"%>
<%@taglib prefix="s" uri="/struts-tags"%>
<%
	ArrayList<News> arrlist = (ArrayList<News>) request.getAttribute("newslist");
	String queryback = (String) request.getAttribute("queryback");
	//out.print(arrlist.size());
%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>UCAS IR</title>
<link rel="stylesheet" style="text/css" href="css/main.css">
<link rel="stylesheet" style="text/css" href="css/bootstrap.min.css">
<script type="text/javascript">
	window.onload = function() {
		document.getElementById("query").value ="<%=queryback%>";
	}
</script>
</head>
<body>
	<div class="nav">
		<div class="nav_left">
			<a href="index.jsp"><img alt="logo" src="images/LOGO.png"></a>
		</div>
		<div class="nav_right">
			<div class="nav_form">
				<form action="search" method="get">
					<input id="query" type="text" name="query" value=" "> <input
						type="submit" value="搜索"><br />

				</form>

			</div>
		</div>
	</div>

	<div class="newmain">
		<h4>

			共搜到<span class="newsnum"><%=arrlist.size()%></span>条结果
		</h4>
		<%
			if (arrlist.size() > 0) {
				Iterator<News> iter = arrlist.iterator();
				News news;
				while (iter.hasNext()) {
					news = iter.next();
		%>

		<div class="item">
			<h4>
				<a href="<%=news.getUrl()%> " target="_blank"><%=news.getTitle()%></a>
			</h4>
			<p>
				<a href=""><%=news.getSource()%></a>
			</p>
			<p><%=news.getSummary()%></br><a href=""><%=news.getUrl()%></a></p>
		</div>
		<%
			}
			}
		%>







	</div>
</body>
</html>