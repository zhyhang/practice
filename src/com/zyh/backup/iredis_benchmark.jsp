<%@page import="java.util.concurrent.TimeUnit"%>
<%@page import="java.io.IOException"%>
<%@page import="java.io.PrintWriter"%>
<%@page
	import="com.ipinyou.iredis.strategy.factory.AutoShardingStrategyFactory"%>
<%@page import="com.ipinyou.iredis.IRedisPool"%>
<%@page import="com.ipinyou.iredis.IRedisTemplate"%>
<%@page import="java.util.concurrent.atomic.AtomicLong"%>
<%@page import="java.nio.file.Files"%>
<%@page import="java.io.File"%>
<%@page import="com.ipinyou.iredis.IPooledRedis"%>
<%@page import="org.springframework.web.context.WebApplicationContext"%>
<%@page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%@page session="false"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="content-type" content="text/html;charset=utf-8" />
<title>Benchmark</title>
</head>
<%!static AtomicLong running = new AtomicLong(-1);
	static AtomicLong timecost = new AtomicLong(0);
	static AtomicLong errors = new AtomicLong(0);
	static long startTime = 0;
	static boolean stop = true;
	static IRedisTemplate iredis = new IRedisTemplate(new IRedisPool(new AutoShardingStrategyFactory(
			"file:///home/weiwei.wang/iredis/iredis.xml")));%>

<%!void printInfo(JspWriter out) throws IOException {
		out.print("<span><font color='blue'>程序正在执行，已经处理");
		out.print(running.get());
		out.print("次请求，平均QPS：");
		out.print(running.longValue() * 1000 / (System.currentTimeMillis() - startTime));
		out.print("，平均延时：");
		out.print(TimeUnit.NANOSECONDS.toMicros(timecost.longValue() / running.longValue()));
		out.print("微秒，错误次数：");
		out.print(errors.longValue());
		out.println("。</font></span>");
	}%>
<body>
	<%
		String cmd = request.getParameter("cmd");
		int printCount = 0;
	%>
	<div>
		<%
			if ("start".equalsIgnoreCase(cmd)) {
				if (!stop) {
					printInfo(out);
					return;
				}
				stop = false;
				startTime = System.currentTimeMillis();
				running.set(-1);
				timecost.set(0);
				errors.set(0);
			} else if ("stop".equalsIgnoreCase(cmd)) {
				if (!stop) {
					stop = true;
					printInfo(out);
				}
				return;
			} else {
				printInfo(out);
				return;
			}
		%>
		<table>
			<thead>
				<tr>
					<td><font color='blue'><b>程序正在运行，可刷新页面查看处理情况。</b></font></td>
				</tr>
			</thead>
			<tbody>
				<%
					while (!stop) {
						long tsb = System.nanoTime();
						try {
							iredis.zrevrange("test1", 0, 199);
						} catch (Exception ignore) {
							errors.incrementAndGet();
							return;
						}
						timecost.addAndGet(System.nanoTime() - tsb);
						running.incrementAndGet();
					}
				%>
			</tbody>
		</table>
	</div>
</body>
</html>
