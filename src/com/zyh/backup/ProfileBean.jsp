<%@page import="com.ipinyou.optimus.kernel.reptile.cache.ReptileRuleCache"%>
<%@page import="com.ipinyou.optimus.kernel.reptile.model.ReptileRuleType"%>
<%@page import="com.ipinyou.optimus.kernel.reptile.model.ReptileRule"%>
<%@page import="org.springframework.web.context.WebApplicationContext"%>
<%@page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%@page session="false"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="content-type" content="text/html;charset=utf-8" />
<title>Click Rate Editor</title>
<style>
html {
	font-size: 10px;
}

div.d {
	float: left;
	background-color: #000080;
	margin: 3px;
	clear: both;
}

td {
	background-color: white;
	padding: 2px;
	text-align: right;
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
		WebApplicationContext wac = (WebApplicationContext) application
				.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
		ReptileRuleCache prc= wac.getBean("reptileRuleCache", ReptileRuleCache.class);
		HashMap<ReptileRuleType, HashSet<ReptileRule>> rules=prc.getAllRule();
	%>
	<div>
	<table>
		<thead>
			<tr><td>No.</td><td>id</td><td>type</td><td>rule</td><td>lastModified</td><td>removed</td></tr>
		</thead>
		<tbody>
			<%
			int i=1;
			for(Map.Entry<ReptileRuleType, HashSet<ReptileRule>> entry:rules.entrySet()){
				for(ReptileRule rule:entry.getValue()){
					out.println("<tr>");
					out.println("<td>"+i+"</td>");
					out.println("<td>"+rule.getId()+"</td>");
					out.println("<td>"+rule.getType().name()+"</td>");
					out.println("<td>"+rule.getRule()+"</td>");
					out.println("<td>"+rule.getLastModified()+"</td>");
					out.println("<td>"+rule.isRemoved()+"</td>");
					out.println("</tr>");
					i++;
				}
			}
			%>
		</tbody>
	</table>
	</div>

</body>
</html>
