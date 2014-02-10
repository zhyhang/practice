<%@page import="java.util.concurrent.TimeUnit"%>
<%@page import="java.util.concurrent.atomic.AtomicLong"%>
<%@page import="java.nio.file.StandardCopyOption"%>
<%@page import="java.nio.file.CopyOption"%>
<%@page import="java.nio.file.Paths"%>
<%@page import="java.nio.file.LinkOption"%>
<%@page import="java.nio.file.Files"%>
<%@page import="java.nio.file.attribute.FileTime"%>
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
<%!static AtomicLong running = new AtomicLong(-1);
	static long lastComplete = 0;%>
<body>
	<%
		WebApplicationContext wac = (WebApplicationContext) application
				.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
		IPooledRedis redis = wac.getBean("idmIRedis", IPooledRedis.class);
		String srcFile = "/home/weiwei.wang/tid2pyid.tids";
		String outFile = "/home/weiwei.wang/tid2pyid.pyids";
		String platForm = request.getParameter("p");
		platForm = null == platForm ? "MIAOZHEN" : platForm;
		String stid = request.getParameter("tid");
		String cmd = request.getParameter("cmd");
	%>
	<div><font color="red"><b>usage:</b></font></div>
	<div><font color="red">url?cmd=start(query)&amp;p=PlatForm(ie:Adx)&amp;tid=xxx</font></div>
	<div><font color="red">cmd=start: retrieve pyid from redis by tid</font></div>
	<div><font color="red">cmd!=start: query retrieving status</font></div>
	<div><font color="red">tid!=null: only retrieve the tid</font></div>
	<div><font color="red">tid==null: retrieve the tids in file <%=srcFile %>, then to file <%=outFile %></font></div>
	<div>
		<%
			if ("start".equalsIgnoreCase(cmd)) {
				if (!running.compareAndSet(-1, 0)) {
					out.print("<span><font color='blue'>相同的程序正在执行，已经处理");
					out.print(running.get());
					out.println("个Key，请稍后重试...</font></span>");
					return;
				}
			} else if (running.get() == -1) {
				out.print("<span><font color='blue'>前一次运行已经完成，共计处理");
				out.print(lastComplete);
				out.println("个Key。</font></span>");
				return;
			} else {
				out.print("<span><font color='blue'>前一次正在运行，已经处理");
				out.print(running.get());
				out.println("个Key。</font></span>");
				return;
			}
		%>
		<table>
			<thead>
				<b>Max display 500 rows</b>
				<tr>
					<td>No.</td>
					<td>tid</td>
					<td>pyid</td>
					<td>platform</td>
				</tr>
			</thead>
			<tbody>
				<%
					try {
						Scanner scanner = null;
						PrintWriter pw = null;
						if (null != stid && !stid.isEmpty()) {
							scanner = new Scanner(new StringReader(stid));
						} else {
							scanner = new Scanner(new File(srcFile));
							File outf = new File(outFile);
							if (outf.exists()) {
								FileTime ft = Files.getLastModifiedTime(outf.toPath(), LinkOption.NOFOLLOW_LINKS);
								Files.copy(outf.toPath(), Paths.get(outFile + "." + ft.toMillis()),
										StandardCopyOption.REPLACE_EXISTING);
							}
							pw = new PrintWriter(outf);
						}
						while (scanner.hasNextLine()) {
							String tid = scanner.nextLine();
							tid = null == tid ? null : tid.trim();
							if (tid == null || tid.isEmpty()) {
								continue;
							}
							String pyid = null;
							try {
								pyid = redis.get(platForm + ":" + tid);
								if (null != pw) {
									pw.print(tid);
									pw.print("\t");
									pw.println(pyid);
								}
							} catch (Exception e) {
								pyid = e.getMessage();
							}
							if (running.incrementAndGet() < 501) {
								out.println("<tr>");
								out.println("<td>" + running.get() + "</td>");
								out.println("<td>" + tid + "</td>");
								out.println("<td>" + pyid + "</td>");
								out.println("<td>" + platForm + "</td>");
								out.println("</tr>");
							}
						}
						scanner.close();
						if (pw != null) {
							pw.close();
						}
						out.flush();
					} finally {
						lastComplete = running.get();
						running.set(-1);
					}
				%>
			</tbody>
		</table>
	</div>
</body>
</html>
