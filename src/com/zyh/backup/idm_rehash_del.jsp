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
<title>IDM re-hash key clear</title>
</head>
<%!static AtomicLong running = new AtomicLong(-1);
	static long lastComplete = 0;%>
<body>
	<%
		String keyfilename = request.getParameter("kfname");
		String keyFile = "/home/weiwei.wang/idm.clear/" + keyfilename;
		String cmd = request.getParameter("cmd");
		String reservehash=request.getParameter("hash");
		int hash=Integer.valueOf(reservehash);
		String redisxmlname=null;
		switch(hash){
		case 0:
			redisxmlname="idm.144.54.6379.xml";
			break;
		case 1:
			redisxmlname="idm.144.54.6380.xml";
			break;
		case 2:
			redisxmlname="idm.144.54.6381.xml";
			break;
		case 3:
			redisxmlname="idm.144.54.6382.xml";
			break;
		case 4:
			redisxmlname="idm.144.24.6379.xml";
			break;
		case 5:
			redisxmlname="idm.144.24.6380.xml";
			break;
		case 6:
			redisxmlname="idm.144.24.6381.xml";
			break;
		case 7:
			redisxmlname="idm.144.24.6382.xml";
			break;
		case 8:
			redisxmlname="idm.152.13.6379.xml";
			break;
		case 9:
			redisxmlname="idm.152.13.6380.xml";
			break;			
		case 10:
			redisxmlname="idm.152.13.6381.xml";
			break;
		case 11:
			redisxmlname="idm.152.13.6382.xml";
			break;
		case 12:
			redisxmlname="idm.152.11.6379.xml";
			break;
		case 13:
			redisxmlname="idm.152.12.6380.xml";
			break;
		case 14:
			redisxmlname="idm.144.56.6387.xml";
			break;
		case 15:
			redisxmlname="idm.144.57.6387.xml";
			break;			
		}
		IRedisTemplate iredis = new IRedisTemplate(new IRedisPool(new AutoShardingStrategyFactory(
				"file:///home/weiwei.wang/idm.clear/"+redisxmlname)));
		int nodenumber=16;
		int printCount=0;
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
			}else if (running.get() == -1) {
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
					<td><font color='blue'><b>程序正在运行，可刷新页面查看处理了多少个Key。</b></font></td>
				</tr>
			</thead>
			<tbody>
				<%
					try {
						Scanner scanner = new Scanner(new File(keyFile));
						while (scanner.hasNextLine()) {
							String entry = scanner.nextLine();
							if (entry == null || entry.isEmpty()) {
								continue;
							}
							String key = entry.split("\":\"")[0].substring(1);
							if(Math.abs(key.hashCode()) % 16 == hash){
								continue;
							}
							if(printCount++<=100){
								out.print(key+"<br>");
							}else{
								out.flush();
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
