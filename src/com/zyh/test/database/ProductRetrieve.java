/**
 * 
 */
package com.zyh.test.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Concurrent retrieve data testing.
 * 
 * @author zhyhang
 *
 */
public class ProductRetrieve {

	private final static int THREAD_NUM = 10;

	private final static String RETREIVE_SQL = "select p.id,unix_timestamp(p.last_modified) last_modified,p.removed,p.active, p.product_no,p.name,p.advertiser_id,p.category,p.category_id,p.brand,p.brand_id,p.price,p.orig_price,p.short_desc, ifnull(p.pic_url01,'') pic_url01,ifnull(p.pic_url02,'') pic_url02,ifnull(p.pic_url03,'') pic_url03,ifnull(p.pic_url04,'') pic_url04,ifnull(p.pic_url05,'') pic_url05, ifnull(p.pic_url06,'') pic_url06, p.product_url,p.logo_target_url,p.relative_content,p.relative_url,p.promotion,p.category_content,p.category_url,p.activity_content,p.activity_url,p.relative_product_ids, p.extend,ifnull(p.weight,0) weight,ifnull(p.daily_avg_pv,0) daily_avg_pv,ifnull(p.discount,1) discount, p.padding_top,p.padding_left,p.padding_bottom,p.padding_right,p.forbid_platforms,p.algo_extend,p.pic_ratio,d.ios_url,d.android_url,d.mobile_url,COLUMN_JSON(d.dynamic_fields) as dynamic_fields,d.store from product_info as p left join product_destination as d on p.advertiser_id=d.advertiser_id and p.product_no=d.product_no where p.last_modified >= ? and p.last_modified < ? order by p.advertiser_id asc,p.product_no asc limit 0,1000000";

	private final static LocalDateTime START_DT = LocalDateTime.of(2015, 10, 23, 6, 40);

	private static ExecutorService es = Executors.newFixedThreadPool(THREAD_NUM);

	private static LinkedTransferQueue<Connection> connPool = new LinkedTransferQueue<>();

	static {
		for (int i = 0; i < THREAD_NUM; ++i) {
			connPool.add(connect());
		}
	}

	private static Connection connect() {
		try {
			return DriverManager.getConnection(
					"jdbc:mysql://192.168.144.17:3306/XXXDB?useUnicode=true&characterEncoding=GBK", "XXXUSER",
					"XXXPASS");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static String timeTo(LocalDateTime ldt) {
		return ldt.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
	}

	public static void main(String[] args) throws Exception {
		AtomicLong totalCount = new AtomicLong(0);
		long tsb = System.currentTimeMillis();
		for (int i = 1; i <= 60; i++) {
			final String startDtStr = timeTo(START_DT.plusMinutes(i - 1));
			final String endDtStr = timeTo(START_DT.plusMinutes(i));
			es.execute(() -> {
				long tsbi = System.currentTimeMillis();
				int rowc = 0;
				Connection usingConn = null;
				System.out.format("product-retrieve from[%s]to[%s] query-start.\n", startDtStr, endDtStr);
				try {
					usingConn = connPool.take();
					PreparedStatement stmt = usingConn.prepareStatement(RETREIVE_SQL);
					stmt.setString(1, startDtStr);
					stmt.setString(2, endDtStr);
					ResultSet resultSet = stmt.executeQuery();
					while (resultSet.next()) {
						rowc++;
					}
					totalCount.addAndGet(rowc);
				} catch (Exception re) {
					System.out.println(re);
				} finally {
					connPool.add(usingConn);
					System.out.format(
							"product-retrieve from[%s]to[%s] query-end, time cost[%d]secords, retrieve rows[%d].\n",
							startDtStr, endDtStr, TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - tsbi),
							rowc);
				}
			});
		}
		es.shutdown();
		es.awaitTermination(2, TimeUnit.HOURS);
		System.out.format("product-retrieve , total time cost[%d]secords, total retrieve rows[%d].\n",
				TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - tsb), totalCount.get());
	}

}
