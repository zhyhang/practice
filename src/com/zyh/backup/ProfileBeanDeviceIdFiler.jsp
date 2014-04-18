<%@page import="java.lang.reflect.Field"%>
<%@page import="com.ipinyou.optimus.kernel.adpicking.filter.strategy.DeviceIdFilter"%>
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
<title>Bean property profiler</title>
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
	DeviceIdFilter bean= wac.getBean("deviceIdFilter", DeviceIdFilter.class);
	Field f=DeviceIdFilter.class.getDeclaredField("pool");
	f.setAccessible(true);
	Map<String,String> pool=(Map<String,String>)f.get(bean);
	%>
	<div>
	<table>
		<thead>
			<tr><td>Size.</td></tr>
		</thead>
		<tbody>
			<tr><td><%=pool.size() %></td></tr>
		</tbody>
	</table>
	</div>

</body>
</html>
