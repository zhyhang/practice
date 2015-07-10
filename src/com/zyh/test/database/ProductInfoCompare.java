/**
 * 
 */
package com.zyh.test.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Compare product info between master and slave server
 * 
 * @author zhyhang
 *
 */
public class ProductInfoCompare {

	private static Set<Integer> IGNORE_COLUMNS;

	private final static Set<String> IGNORE_COLUMNS_STR = new LinkedHashSet<>(
			Arrays.asList("relative_product_ids", "info_change_time", "last_modified", "discount", "daily_avg_pv","version","creation"));

	private static String[] COLUMNS;

	private static long[] COLUMNS_DIFF;

	private final static ScheduledExecutorService SES = Executors.newScheduledThreadPool(1);

	/**
	 * @param args
	 * @throws SQLException
	 */
	public static void main(String[] args) {
		try (Connection connMaster = DriverManager.getConnection(
				"jdbc:mysql://192.168.144.17:3306/xxxdb?useUnicode=true&characterEncoding=GBK", "itest",
				"");
				Connection connSlave = DriverManager.getConnection(
						"jdbc:mysql://192.168.144.12:3306/xxxdb?useUnicode=true&characterEncoding=GBK", "itest",
						"");) {
			String sqlMasterPrefix = "select * from product_info where advertiser_id=5535 and last_modified=info_change_time and info_change_time>='2015-07-10 09:00:00' order by id limit ";
//			String sqlSlavePrefix = "select * from product_info where advertiser_id=5535 and info_change_time <'2015-07-10 00:00:00' and id in ";
			String sqlSlavePrefix = "select * from product_info where advertiser_id=5535 and id in ";
			int pageSize = 100000;
			int paged = 0;
			final AtomicLong totalCount = new AtomicLong(0);
			final AtomicLong noCampareCount = new AtomicLong(0);
			final AtomicLong diffCount = new AtomicLong(0);

			SES.scheduleAtFixedRate(() -> {
				StringBuilder log = new StringBuilder();
				log.append("totalCount").append('\t').append(totalCount.get()).append('\n');
				log.append("diffCount").append('\t').append(diffCount.get()).append('\n');
				log.append("noCampareCount").append('\t').append(noCampareCount.get()).append('\n');
				log.append("columnNames").append('\t').append(Arrays.deepToString(COLUMNS)).append('\n');
				log.append("columnDiffs").append('\t').append(Arrays.toString(COLUMNS_DIFF)).append('\n');
				log.append("ignore_columns").append('\t')
						.append(Arrays.deepToString(IGNORE_COLUMNS.toArray(new Integer[IGNORE_COLUMNS.size()])))
						.append('\n');
				System.out.println(log.toString());
			} , 1, 1, TimeUnit.MINUTES);
			
			final RsListComparator comparator = new RsListComparator(COLUMNS_DIFF,IGNORE_COLUMNS);
			try (Statement stmtMaster = connMaster.createStatement();
					Statement stmtSlave = connSlave.createStatement();) {
				while (true) {
					// read from master
					ResultSet resultMaster = stmtMaster
							.executeQuery(sqlMasterPrefix + paged * pageSize + "," + pageSize);
					// find column name
					if (IGNORE_COLUMNS == null) {
						IGNORE_COLUMNS = new LinkedHashSet<>();
						int cc = resultMaster.getMetaData().getColumnCount();
						COLUMNS = new String[cc];
						COLUMNS_DIFF = new long[cc];
						comparator.setColumnInfo(COLUMNS_DIFF,IGNORE_COLUMNS);
						for (int i = 1; i <= cc; i++) {
							String cn = resultMaster.getMetaData().getColumnName(i);
							COLUMNS[i - 1] = cn;
							if (IGNORE_COLUMNS_STR.contains(cn == null ? "" : cn.toLowerCase())) {
								IGNORE_COLUMNS.add(Integer.valueOf(i - 1));
							}
						}
					}
					Set<Long> pids = new HashSet<>();
					Map<Long, List<Object>> rowsMaster = new HashMap<>();
					while (resultMaster.next()) {
						totalCount.incrementAndGet();
						Long id = resultMaster.getLong("id");
						pids.add(id);
						int cc = resultMaster.getMetaData().getColumnCount();
						List<Object> row = new ArrayList<>();
						for (int i = 1; i <= cc; i++) {
							row.add(resultMaster.getObject(i));
						}
						rowsMaster.put(id, row);
					}
					paged++;
					resultMaster.close();
					if (pids.isEmpty()) {
						break;
					}
					// read from master
					ResultSet resultSlave = stmtSlave
							.executeQuery(sqlSlavePrefix + getInSql(pids.toArray(new Long[pids.size()])));
					Map<Long, List<Object>> rowsSlave = new HashMap<>();
					while (resultSlave.next()) {
						int cc = resultSlave.getMetaData().getColumnCount();
						List<Object> row = new ArrayList<>();
						for (int i = 1; i <= cc; i++) {
							row.add(resultSlave.getObject(i));
						}
						rowsSlave.put(resultSlave.getLong("id"), row);
					}
					resultSlave.close();
					// compare master slave
					rowsMaster.forEach((k, v) -> {
						List<Object> row = rowsSlave.get(k);
						if (row == null) {
							noCampareCount.incrementAndGet();
						} else if(!comparator.isEqual(v, row)){
							diffCount.incrementAndGet();
						}
					});
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static String getInSql(Long... ids) {
		StringBuilder buf = new StringBuilder("(");
		for (int i = 0; i < ids.length - 1; i++) {
			buf.append(ids[i]).append(",");
		}
		buf.append(ids[ids.length - 1]);
		buf.append(")");
		return buf.toString();
	}
	
	 public static class RsListComparator {

		 	private long[] columnSize;
	        private Set<Integer> ingnoreColumns;
	        
	        public void setColumnInfo(long[] columnSize, Set<Integer> ingnoreColumns){
	        	this.columnSize = columnSize;
	            this.ingnoreColumns = ingnoreColumns;
	        }

	        public RsListComparator(long[] columnSize, Set<Integer> ingnoreColumns) {
	        	this.columnSize = columnSize;
	            this.ingnoreColumns = ingnoreColumns;
	        }

	        public boolean isEqual(List<Object> o1, List<Object> o2) {
	        	AtomicInteger pos=new AtomicInteger(-1);
	        	AtomicBoolean equals=new AtomicBoolean(true);
	        	o1.forEach(e->{
	        		pos.incrementAndGet();
	        		if(!ingnoreColumns.contains(Integer.valueOf(pos.get()))){
	        		    if(!Objects.equals(e, o2.get(pos.get()))) {
	                    	columnSize[pos.get()]++;
	                    	equals.set(false);
	                    }
	        		}
	        	});
	        	return equals.get();
	        }
	    }
}
