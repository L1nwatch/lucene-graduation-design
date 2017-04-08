<!DOCTYPE html>
<%@page language="java" contentType="text/html; charset=UTF-8"
        pageEncoding="UTF-8" import="java.util.*" %>
<%@ page import="web.src.models.News" %>
<%@taglib prefix="s" uri="/struts-tags" %>
<%
    List<News> hitsList = (List<News>) request.getAttribute("hitsList");    // hits 排序结果
    List<News> pageRankList = (List<News>) request.getAttribute("pageRankList");    // pageRank 排序结果
    String queryBack = (String) request.getAttribute("queryBack");
    int totalNews = (Integer) request.getAttribute("totalNews");
    double time = Double.parseDouble(request.getAttribute("time").toString());
%>

<html lang="zh-cn">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>HITS-毕业设计</title>
    <link rel="stylesheet" type="text/css" href="css/main.css">
    <link rel="stylesheet" type="text/css" href="css/bootstrap.min.css">
    <script type="text/javascript">
        window.onload = function () {
            document.getElementById("query").value = "<%=queryBack%>";
        }

    </script>
</head>
<body>
<div class="nav">
    <div class="nav_left">
        <a href="index.jsp"><img alt="logo" src="images/logo.png"></a>
    </div>

    <div class="nav_right">
        <div class="nav_form">
            <form action="search" method="get">
                <input id="query" type="text" name="query" value=" ">
                <input type="submit" value="搜索"><br/>
            </form>

        </div>
    </div>
</div>

<div class="newmain">
    <h4 style="margin-left:31.5%">
        共搜到<span class="newsnum"><%=totalNews%></span>条结果
        <% if (totalNews > 0) { %>| 用时<span class="newsnum"><%=time%></span>秒<% } %>
    </h4>

    <div class="searchResult">
        <div class="hitsResult">
            <h4 style="text-align: center"> HITS 排序</h4>

            <%
                if (hitsList.size() > 0) {
                    Iterator<News> hitsIter = hitsList.iterator();
                    News hitsNews;
                    while (hitsIter.hasNext()) {
                        hitsNews = hitsIter.next();
            %>

            <div class="item" style="border-right: 1px solid #cccccc;">
                <h4>
                    <a id="id_hits_result" href="<%=hitsNews.getURL()%> " target="_blank"><%=hitsNews.getTitle()%>
                    </a>
                </h4>
                <p>
                    <%=hitsNews.getSummary()%>
                    <br/>
                    <a href="<%=hitsNews.getURL()%>"><%=hitsNews.getURL()%>
                    </a>
                </p>
            </div>
            <%
                    }
                }
            %>
        </div>
        <div class="pageRankResult">
            <h4 style="text-align: center"> PageRank 排序</h4>

            <%
                if (pageRankList.size() > 0) {
                    Iterator<News> pageRankIter = pageRankList.iterator();
                    News pageRankNews;
                    while (pageRankIter.hasNext()) {
                        pageRankNews = pageRankIter.next();
            %>

            <div class="item">
                <h4>
                    <a id="id_page_rank_result" href="<%=pageRankNews.getURL()%> " target="_blank"><%=pageRankNews.getTitle()%>
                    </a>
                </h4>
                <p>
                    <%=pageRankNews.getSummary()%>
                    <br/>
                    <a href="<%=pageRankNews.getURL()%>"><%=pageRankNews.getURL()%>
                    </a>
                </p>
            </div>
            <%
                    }
                }
            %>
        </div>
    </div>

    <hr>
</div>

<div class="footerinfo">
    <p>2017-西电-网信院-林丰-13030110024-毕业设计-designed by <a href="http://watch0.top">w@tch</a></p>
</div>

</body>
</html>