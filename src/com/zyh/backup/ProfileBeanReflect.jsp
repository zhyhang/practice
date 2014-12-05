<%@page import="com.ipinyou.optimus.kernel.model.TimeObject"%>
<%@page import="java.lang.reflect.Field"%>
<%@page import="com.ipinyou.optimus.kernel.cache.redis.RedisMemProductCache"%>
<%@page import="org.springframework.web.context.WebApplicationContext"%>
<%@page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%@page session="false"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="content-type" content="text/html;charset=utf-8" />
<title>Bean Profile By Reflect</title>
<style>
html {
	font-size: 16px;
}

div.d {
	float: left;
	background-color: #000080;
	margin: 3px;
	clear: both;
}
table{
	border-collapse:collapse;
}

tr{
	border: 0px none;
}

td {
	background-color: white;
	padding: 2px;
	text-align: right;
	border:2px solid blue;
}

a {
	color: blue;
	cursor: pointer;
	text-decoration: none;
}
</style>
</head>
<body>
	<%
		Class<RedisMemProductCache> clazz=RedisMemProductCache.class;
		WebApplicationContext wac = (WebApplicationContext) application
				.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
		RedisMemProductCache bean= wac.getBean("productCache", clazz);
	%>
	<div>
	<table>
		<caption><b><%=clazz.getCanonicalName()%></b></caption>
		<thead>
			<tr><td style="background-color:Aquamarine;text-align:center;">Field/Method</td><td style="background-color:Aquamarine;text-align:center;">Value</td></tr>
		</thead>
		<tbody>
			<%
			out.println("<tr>");
			out.println("<td>smallMapGetCount</td>");
			out.println("<td>"+bean.getSmallMapGetCount()+"</td>");
			out.println("</tr>");
			out.println("<tr>");
			Field field=clazz.getDeclaredField("smallMap");
			field.setAccessible(true);
			Map<String, TimeObject<String>> timemap=(Map<String, TimeObject<String>>)field.get(bean);
			out.println("<td>smallMap.size()</td>");
			out.println("<td>"+timemap.size()+"</td>");
			out.println("</tr>");
			field=clazz.getDeclaredField("absentMapGetCount");
			field.setAccessible(true);
			int count=(int)field.getInt(bean);
			out.println("<td>absentMapGetCount</td>");
			out.println("<td>"+count+"</td>");
			out.println("</tr>");
			field=clazz.getDeclaredField("absentMap");
			field.setAccessible(true);
			timemap=(Map<String, TimeObject<String>>)field.get(bean);
			out.println("<td>absentMap.size()</td>");
			out.println("<td>"+timemap.size()+"</td>");
			out.println("</tr>");
			field=clazz.getDeclaredField("recommendProductNosMapGetCount");
			field.setAccessible(true);
			count=(int)field.getInt(bean);
			out.println("<td>recommendProductNosMapGetCount</td>");
			out.println("<td>"+count+"</td>");
			out.println("</tr>");
			field=clazz.getDeclaredField("recommendProductNosMap");
			field.setAccessible(true);
			Map<String, TimeObject<String[]>> timemaps=(Map<String, TimeObject<String[]>>)field.get(bean);
			out.println("<td>recommendProductNosMap.size()</td>");
			out.println("<td>"+timemaps.size()+"</td>");
			out.println("</tr>");
			field=clazz.getDeclaredField("productAlgoExtendInfoMapGetCount");
			field.setAccessible(true);
			count=(int)field.getInt(bean);
			out.println("<td>productAlgoExtendInfoMapGetCount</td>");
			out.println("<td>"+count+"</td>");
			out.println("</tr>");
			field=clazz.getDeclaredField("productAlgoExtendInfoMap");
			field.setAccessible(true);
			Map<String, TimeObject<Map<String, String>>> timemapmap=(Map<String, TimeObject<Map<String, String>>>)field.get(bean);
			out.println("<td>productAlgoExtendInfoMap.size()</td>");
			out.println("<td>"+timemapmap.size()+"</td>");
			out.println("</tr>");
			%>
		</tbody>
	</table>
	</div>
</body>
</html>
