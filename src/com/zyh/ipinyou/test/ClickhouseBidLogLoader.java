/**
 * 
 */
package com.zyh.ipinyou.test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.yandex.clickhouse.settings.ClickHouseConnectionSettings;
import ru.yandex.clickhouse.settings.ClickHouseQueryParam;

/**
 * @author zhyhang
 *
 */
public class ClickhouseBidLogLoader {

	private static final int THREAD_NUM = 8;
	
	private static final int LOAD_BATCH_LINES=1000;
	
	private static final String SOCKET_READ_TIMEOUT_MILLIS="180000";
	
	private static final String MAX_QUERY_SIZE="25165824";//24MB

	private static final String TABLE_NAME = "bid_log_mt";

	private static final String SQL_TYPE_QUERY = "select name,type from system.columns where table='bid_log_mt'";
	
	private static final int SEGMENT_COUNT = 15;

	// field number of every segment
	private static final int[] SEGMENT_FIELDS=new int[]{
			1,
			18,
			13,
			14,
			14,
			14,
			25,
			36,
			12,
			21,
			14,
			4,
			5,
			1,
			1
			};

	private static final Logger LOGGER = LoggerFactory.getLogger(ClickhouseBidLogLoader.class);
	
	private String sqlInsert;

	private final List<String> columnNames = new ArrayList<>();
	
	private final List<String> columnTypes = new ArrayList<>();

	private final LinkedTransferQueue<Connection> connPool = new LinkedTransferQueue<>();

	{
		for (int i = 0; i < THREAD_NUM; ++i) {
			connPool.add(connect());
		}
		initTableMeta();
	}

	private Connection connect() {
		try {
			Properties pps=new Properties();
			pps.setProperty(ClickHouseQueryParam.MAX_QUERY_SIZE.getKey(), MAX_QUERY_SIZE);
			pps.setProperty(ClickHouseConnectionSettings.SOCKET_TIMEOUT.getKey(), SOCKET_READ_TIMEOUT_MILLIS);
			Connection connection = DriverManager.getConnection("jdbc:clickhouse://192.168.144.58:8123/default",pps);
			connection.setAutoCommit(false);
			return connection;
		} catch (Exception e) {
			LOGGER.error("create connection error", e);
			return null;
		}
	}

	private void shutdown() {
		while (connPool.size() > 0) {
			try {
				connPool.take().close();
			} catch (Exception e) {
			}
		}
	}

	private void initTableMeta() {
		Connection connection = null;
		try {
			connection = connPool.take();
			try (Statement stmt = connection.createStatement();
					ResultSet resultSet = stmt.executeQuery(SQL_TYPE_QUERY);) {
				resultSet.next();// skip the first date field
				while (resultSet.next()) {
					columnNames.add(resultSet.getString(1));
					columnTypes.add(resultSet.getString(2));
				}
			}
			connection.commit();
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (connection != null) {
				connPool.put(connection);
			}
		}
		String[] paras = new String[IntStream.of(SEGMENT_FIELDS).sum()];
		Arrays.fill(paras, "?");
		sqlInsert = "insert into " + TABLE_NAME + "(" + String.join(",", columnNames) + ") values("
				+ String.join(",", paras) + ")";
	}

	private void loadRow(List<String> lines) {
		if (lines == null || lines.size() == 0) {
			return;
		}
		Connection conn = null;
		AtomicReference<String> processLine = new AtomicReference<String>(null);
		for (int r = 0; r < 5; r++) { // retry 5 times
			try {
				conn = connPool.poll(30, TimeUnit.SECONDS);
				if (!conn.isValid(60)) {
					conn.close();
					conn = connect();
				}
				conn.setAutoCommit(false);
				try (PreparedStatement pstmt = conn.prepareStatement(sqlInsert);) {
					lines.stream().forEach(line -> {
						try {
							processLine.set(line);
							String[] segments = StringUtils.splitByWholeSeparatorPreserveAllTokens(line, "\001");
							if (segments.length != SEGMENT_COUNT) {
								throw new RuntimeException(
										"error-segment-count:" + segments.length + ", should be: " + SEGMENT_COUNT);
							}
							int parameterIndex = 0;
							for (int i = 0; i < segments.length; i++) {
								String[] fields = StringUtils.splitByWholeSeparatorPreserveAllTokens(segments[i],
										"\002");
								int fieldNumCap = SEGMENT_FIELDS[i];
								if (fields.length < fieldNumCap) {
									fields = Arrays.copyOf(fields, fieldNumCap);
								}
								for (int j = 0; j < fieldNumCap; j++) {
									parameterIndex++;

									prepareInsertValue(fields[j], columnTypes.get(parameterIndex - 1), pstmt,
											parameterIndex);

								}
							}
							pstmt.addBatch();
						} catch (Exception e) {
							throw new RuntimeException(e);
						}
					});
					pstmt.executeBatch();
				}
				conn.commit();
				break;
			} catch (Exception e) {
				LOGGER.error("error when loading one row", e);
				LOGGER.error("::::error_row::::{}", processLine.get());
			} finally {
				if (conn != null) {
					try {
						conn.rollback();
					} catch (Exception ex) {
						LOGGER.error("error when rollback connection", ex);
					}
					connPool.put(conn);
				}
			}
		}
	}

	private void prepareInsertValue(String value, String fieldClass, PreparedStatement pstmt, int parameterIndex)
			throws SQLException {
		if ("Nullable(String)".equalsIgnoreCase(fieldClass) || "String".equalsIgnoreCase(fieldClass)) {
			if (null == value) {
				pstmt.setString(parameterIndex, value);
			} else {
				pstmt.setString(parameterIndex, value);
			}
		} else if ("Nullable(Int64)".equalsIgnoreCase(fieldClass) || "Int64".equalsIgnoreCase(fieldClass)) {
			if (null == value || value.trim().length() == 0) {
				pstmt.setLong(parameterIndex, 0);
			} else {
				pstmt.setLong(parameterIndex, Long.parseLong(value));
			}
		} else if ("Nullable(Int32)".equalsIgnoreCase(fieldClass)) {
			if (null == value || value.trim().length() == 0) {
				pstmt.setInt(parameterIndex, 0);
			} else {
				pstmt.setInt(parameterIndex, Integer.parseInt(value));
			}
		} else if ("Nullable(Float32)".equalsIgnoreCase(fieldClass)) {
			if (null == value || value.trim().length() == 0) {
				pstmt.setFloat(parameterIndex, 0);
			} else {
				pstmt.setFloat(parameterIndex, Float.parseFloat(value));
			}
		} else if ("Nullable(Float64)".equalsIgnoreCase(fieldClass)) {
			if (null == value || value.trim().length() == 0) {
				pstmt.setDouble(parameterIndex, 0);
			} else {
				pstmt.setDouble(parameterIndex, Double.parseDouble(value));
			}
		} else if ("Date".equalsIgnoreCase(fieldClass)) {
			if (null == value || value.trim().length() == 0) {
				pstmt.setDate(parameterIndex, null);
			} else {
				pstmt.setDate(parameterIndex, new Date(Long.parseLong(value)));
			}
		} else {
			throw new RuntimeException("not support data type" + fieldClass);
		}
	}

	private void loadFile(String logFileName) throws IOException {
		Stream<String> lines = Files.lines(Paths.get(logFileName), StandardCharsets.UTF_8);
		List<String> batchLines = new ArrayList<>();
		AtomicInteger batchSize = new AtomicInteger(LOAD_BATCH_LINES);
		lines.forEach(line -> {
			batchLines.add(line);
			if (batchSize.decrementAndGet() == 0) {
				loadRow(batchLines);
				batchLines.clear();
				batchSize.set(LOAD_BATCH_LINES);
			}
		});
		if (batchLines.size() > 0) {
			loadRow(batchLines);
		}
		lines.close();
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		if (args.length < 1) {
			System.out.println("\nPlease specify the loading log file or directory in argument.\n");
			System.exit(-1);
		}
		System.out.format("begin load data from [%s].\n", args[0]);
		LOGGER.info("begin load data from [{}].", args[0]);
		ClickhouseBidLogLoader loader = new ClickhouseBidLogLoader();
		File file = new File(args[0]);
		if (file.isFile()) {
			loader.loadFile(args[0]);
		} else if (file.isDirectory()) {
			ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_NUM);
			for (File f : file.listFiles()) {
				if (f.isFile()) {
					threadPool.execute(() -> {
						try {
							loader.loadFile(f.getCanonicalPath());
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					});
				}
			}
			threadPool.shutdown();
			threadPool.awaitTermination(10, TimeUnit.DAYS);
		}
		System.out.format("load data completed. Error loading row, please see the logs file. \n");
		LOGGER.info("load data completed.");
		loader.shutdown();
		System.exit(0);
	}

}
