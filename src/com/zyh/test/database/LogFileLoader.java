/**
 * 
 */
package com.zyh.test.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;

/**
 * Load log files to mariadb.
 * 
 * @author zhyhang
 *
 */
public class LogFileLoader {

	private final static int THREAD_NUM = 10;

	private final static String RETREIVE_SQL = "select * from v6log_seg1";

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
					"jdbc:mysql://192.168.1xx.xx:3306/xxxdb?useUnicode=true&characterEncoding=GBK", "xxxUser",
					"xxxPass");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void main(String[] args) throws Exception {
		Connection conn=connPool.take();
		try(Statement stmt = conn.createStatement()){
			ResultSet resultSet = stmt.executeQuery(RETREIVE_SQL);
			ResultSetMetaData metaData = resultSet.getMetaData();
			for (int i = 0; i <metaData.getColumnCount(); i++) {
				System.out.format("name[%s],label[%s],class[%s],type[%s]\n", metaData.getColumnName(i+1),
						metaData.getColumnLabel(i+1),metaData.getColumnClassName(i+1),metaData.getColumnTypeName(i+1));
			}
			resultSet.close();
		}
		es.shutdown();
		es.awaitTermination(2, TimeUnit.HOURS);
	}

}
