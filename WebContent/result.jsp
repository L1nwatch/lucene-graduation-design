<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" import="ucas.ir.pojo.*"%>
<%
	News newslist = (News) request.getAttribute("news");
%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>果壳搜索</title>
<link rel="stylesheet" style="text/css" href="css/main.css">
<link rel="stylesheet" style="text/css" href="css/bootstrap.min.css">
</head>
<body>

	<div class="nav">
		<div class="nav_left">
			<a href="index.jsp"><img alt="logo" src="images/LOGO.png"></a>
		</div>
		<div class="nav_right">
			<div class="nav_form">
				<form action="search" method="get">
					<input type="text" name="query"> <input type="submit" value="搜索">
				</form>
			</div>
		</div>
	</div>

	<div class="newmain">
		<div class="item">
			<h4>
				<a href="<%=newslist.getUrl()%>"><%=newslist.getTitle()%> </a>
			</h4>
			<p>
				<a href="">tech.163.com › 网易科技 2016</a>
			</p>
			<p>
				<%=newslist.getSummary()%>
			</p>
		</div>
	</div>
</body>
</html>