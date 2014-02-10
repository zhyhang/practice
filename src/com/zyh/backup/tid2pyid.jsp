<%@page import="java.io.PrintWriter"%>
<%@page import="java.io.StringReader"%>
<%@page import="java.io.File"%>
<%@page import="com.ipinyou.iredis.IPooledRedis"%>
<%@page import="org.springframework.web.context.WebApplicationContext"%>
<%@page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%@page session="false"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="content-type" content="text/html;charset=utf-8" />
<title>TID2PYID</title>
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
		IPooledRedis redis= wac.getBean("idmIRedis", IPooledRedis.class);
		String srcFile="/home/weiwei.wang/tid2pyid.tids";
		String outFile="/home/weiwei.wang/tid2pyid.pyids";
		String platForm=request.getParameter("p");
		platForm=null==platForm?"MIAOZHEN":platForm;
		String stid=request.getParameter("tid");
		%>
	<div>
	<table>
		<thead>
			<tr><td>No.</td><td>tid</td><td>pyid</td><td>platform</td></tr>
		</thead>
		<tbody>
			<%
			Scanner scanner = null;
			PrintWriter pw= null;
			if(null!=stid && !stid.isEmpty()){
				scanner=new Scanner(new StringReader(stid));
			}else{
				scanner=new Scanner(new File(srcFile));
				pw=new PrintWriter(new File(outFile));
			}
			int i=1;
			while(scanner.hasNextLine()){
				String tid=scanner.nextLine();
				tid=null==tid?null:tid.trim();
				if(tid==null || tid.isEmpty()){
					continue;
				}
				String pyid=null;
				try{
					pyid=redis.get(platForm+":"+tid);
					if(null!=pw){
						pw.print(tid);
						pw.print("\t");
						pw.println(pyid);
					}
				}catch(Exception e){
					pyid=e.getMessage();
				}
				if(i<501){
					out.println("<tr>");
					out.println("<td>"+i+"</td>");
					out.println("<td>"+tid+"</td>");
					out.println("<td>"+pyid+"</td>");
					out.println("<td>"+platForm+"</td>");
					out.println("</tr>");
				}
				i++;
			}
			scanner.close();
			if(pw!=null){ pw.close(); }
			%>
		</tbody>
	</table>
	</div>
</body>
</html>
