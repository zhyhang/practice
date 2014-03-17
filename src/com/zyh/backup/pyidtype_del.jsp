<%@page import="com.ipinyou.iredis.strategy.factory.AutoShardingStrategyFactory"%>
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
<title>PYID_Category_Clear</title>
</head>
<%!static AtomicLong running = new AtomicLong(-1);
	static long lastComplete = 0;%>
<body>
	<%
		IRedisTemplate iredis = new IRedisTemplate(new IRedisPool(new AutoShardingStrategyFactory(
			"http://192.168.144.10/iredis/config/test-audience-write.xml")));;
		String keyfileno = request.getParameter("kfno");
		String typeFile = "/home/weiwei.wang/pyidtype.redis.del";
		String keyFile = "/home/weiwei.wang/pyidtype.redis.keys" + "." + keyfileno;
		String cmd = request.getParameter("cmd");
	%>
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
				<tr>
					<td><font color='blue'><b>程序正在运行，可刷新页面查看处理了多个个Key。</b></font></td>
				</tr>
			</thead>
			<tbody>
				<%
					try {
						Scanner scanner = new Scanner(new File(typeFile));
						Set<String> delTypes = new HashSet<String>();
						while (scanner.hasNextLine()) {
							String type = scanner.nextLine();
							if (null != type && type.trim().length() > 0) {
								delTypes.add(type.trim());
							}
						}
						scanner.close();
						scanner = new Scanner(new File(keyFile));
						while (scanner.hasNextLine()) {
							String key = scanner.nextLine();
							if (key == null || key.isEmpty()) {
								continue;
							}
							String type = key.split(":")[0];
							if (!delTypes.contains(type)) {
								continue;
							}
							try {
								if (iredis.del(key) == 1) {
									running.incrementAndGet();
								}
							} catch (Exception e) {
								//ignore
							}
						}
						scanner.close();
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
