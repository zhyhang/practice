/**
 * 
 */
package com.zyh.test.httpclient;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LongSummaryStatistics;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;

/**
 * @author zhyhang
 *
 */
public class ApacheHttpClientLoop {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String url = "https://cn.bing.com";
		url = args != null && args.length > 0 ? args[0] : url;
		int loopCount = 100;
		loopCount = args != null && args.length > 1 ? Integer.parseInt(args[1]) : loopCount;
		ConcurrentRequest(url, loopCount, args!=null && args.length>2?args[2]:"GET");
	}

	private static void ConcurrentRequest(String url, int loopCount,String httpMethod) {
		LongSummaryStatistics stat = new LongSummaryStatistics();
		System.out.println("Begin_Request");
		long tsb = System.currentTimeMillis();
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		cm.setMaxTotal(400);
		cm.setDefaultMaxPerRoute(200);// indicate create how many connections per host
		try (CloseableHttpClient client = HttpClients
				.custom()
				.setConnectionManager(cm)
				.setDefaultRequestConfig(
						RequestConfig.custom().setConnectionRequestTimeout(1500).setConnectTimeout(500)
								.setSocketTimeout(1000).build()).build();) {
			Thread[] ths = new Thread[100];
			Arrays.setAll(ths, i -> {
				return new Thread(() -> {
					for (int j = 0; j < loopCount; j++) {
						long tsbj = System.currentTimeMillis();
						HttpUriRequest req = null;
						if("POST".equalsIgnoreCase(httpMethod)){
							req = new HttpPost(url);
							String entityString="{\"a\":768,\"datas\":[{\"cu\":true,\"p\":{\"000000000125481024\":0,\"000000000120914187\":0,\"000000000108316568\":0,\"000000000123465366\":0,\"000000000102430242\":0,\"000000000125780599\":0,\"000000000102295657\":0,\"000000000108394043\":0,\"000000000128307206\":0,\"000000000124552749\":0,\"000000000125523691\":0,\"000000000124960768\":0,\"000000000106361757\":0,\"000000000108567504\":0,\"000000000127190489\":0,\"000000000125782280\":0,\"000000000124691412\":0,\"000000000125781947\":0,\"000000000127935507\":0,\"000000000125800917\":0,\"000000000126944229\":0,\"000000000106247922\":0,\"000000000125359836\":0,\"000000000106245525\":0,\"000000000126965017\":0,\"000000000126280056\":0,\"000000000123129015\":0,\"000000000123129118\":0,\"000000000125038010\":0,\"000000000126019770\":0,\"000000000123129094\":0,\"000000000127585552\":0,\"000000000123125892\":0,\"000000000127820433\":0,\"000000000127840893\":0,\"000000000125692961\":0,\"000000000125555342\":0,\"000000000126582986\":0,\"000000000128693309\":0,\"000000000126291578\":0,\"000000000127841404\":0,\"000000000127840894\":0,\"000000000128874709\":0,\"000000000125523508\":0,\"000000000126015284\":0,\"000000000128983445\":0,\"000000000126451156\":0,\"000000000127424697\":0,\"000000000127840194\":0,\"000000000104607279\":0,\"000000000127841743\":0,\"000000000128974805\":0,\"000000000125523263\":0,\"000000000125522947\":0,\"000000000125584192\":0,\"000000000127838278\":1,\"000000000125523207\":2,\"000000000127841613\":3,\"000000000129160002\":4,\"000000000125839089\":5,\"000000000126000531\":6,\"000000000125361452\":7,\"000000000127841909\":8,\"000000000127833972\":9,\"000000000127841152\":10,\"000000000129160068\":11,\"000000000126554580\":12,\"000000000127840898\":13,\"00000000000000null\":14,\"000000000127841337\":15},\"rts\":0,\"tid\":\"143456426607465711\",\"ur\":15}],\"p\":\"Suning\"}";
							((HttpPost) req).setEntity(new StringEntity(entityString, StandardCharsets.UTF_8));
						}else{
							req = new HttpGet(url);
						}
						HttpContext context = HttpClientContext.create();
						try(CloseableHttpResponse resp = client.execute(req, context);) {
							// it's important,must be executed before response.close()
							resp.getEntity().getContent().close();
						} catch (Exception e) {
							e.printStackTrace();
						}
						stat.accept(System.currentTimeMillis() - tsbj);
					}
				});
			});
			Arrays.stream(ths).forEach(t -> t.start());
			Arrays.stream(ths).forEach(t -> {
				try {
					t.join();
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.printf("time_cost(ms): %d.\n", System.currentTimeMillis() - tsb);
		System.out.printf("total request: %d, max: %d(ms), min: %d(ms), average: %.2f(ms). \n", stat.getCount(),
				stat.getMax(), stat.getMin(), stat.getAverage());
	}
}
