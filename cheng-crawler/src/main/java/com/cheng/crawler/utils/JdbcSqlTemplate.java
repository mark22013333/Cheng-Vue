package com.cheng.crawler.utils;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

/**
 * @author cheng
 * @since 2022/4/19 17:10
 **/
@Getter
@Slf4j
@Component
public class JdbcSqlTemplate {

    private final DataSource dataSource;
    private final DatabaseType databaseType;

    @Autowired
    public JdbcSqlTemplate(@Qualifier("masterDataSource") DataSource dataSource) {
        this.dataSource = dataSource;
        this.databaseType = detectDatabaseType();
    }

    /**
     * 偵測資料庫類型
     */
    private DatabaseType detectDatabaseType() {
        try (Connection conn = dataSource.getConnection()) {
            String productName = conn.getMetaData().getDatabaseProductName().toLowerCase();
            log.info("偵測到資料庫類型: {}", productName);

            if (productName.contains("mysql")) {
                return DatabaseType.MYSQL;
            } else if (productName.contains("microsoft") || productName.contains("sql server")) {
                return DatabaseType.MSSQL;
            } else if (productName.contains("oracle")) {
                return DatabaseType.ORACLE;
            } else if (productName.contains("postgresql")) {
                return DatabaseType.POSTGRESQL;
            } else {
                log.warn("未知的資料庫類型: {}, 預設使用 MYSQL", productName);
                return DatabaseType.MYSQL;
            }
        } catch (SQLException e) {
            log.error("偵測資料庫類型失敗，預設使用 MYSQL", e);
            return DatabaseType.MYSQL;
        }
    }

    /**
     * 取得當前時間的 SQL 函數
     *
     * @return 對應資料庫的當前時間函數
     */
    public String getCurrentTimestampFunction() {
        return switch (databaseType) {
            case MYSQL -> "NOW()";
            case MSSQL -> "GETDATE()";
            case ORACLE -> "SYSDATE";
            case POSTGRESQL -> "CURRENT_TIMESTAMP";
        };
    }

    /**
     * 資料庫類型列舉
     */
    public enum DatabaseType {
        MYSQL, MSSQL, ORACLE, POSTGRESQL
    }


    /**
     * 使用JDBC方式寫入大量資料（帶交易管理）
     * <p>
     * <b>交易行為：</b>
     * <ul>
     *   <li>關閉自動提交（auto-commit = false）</li>
     *   <li>批次執行成功後手動提交（commit）</li>
     *   <li>發生異常時自動回滾（rollback）</li>
     * </ul>
     *
     * @param insertSql  要寫入Table的sql語法 ex:"INSERT INTO BCS_USER_EVENT_SET (ACTION, CONTENT, MID, MODIFY_USER, REFERENCE_ID, SET_TIME, TARGET) VALUES (?,?,?,?,?,?,?);"
     * @param resultList 要寫入Table的值 (字串陣列要依照表的欄位順序排，否則寫入的資料和欄位會不一致)
     * @return boolean 是否成功
     **/
    public boolean insertBatchSql(String insertSql, List<String[]> resultList) {
        boolean executeStatus;
        Connection connection = null;
        PreparedStatement ps = null;
        String errStr = "";

        try {
            long start = System.currentTimeMillis();
            connection = dataSource.getConnection();

            // 關閉自動提交，啟用交易管理
            connection.setAutoCommit(false);

            ps = connection.prepareStatement(insertSql);

            for (String[] data : resultList) {
                errStr = Arrays.toString(data);
                for (int j = 0; j < data.length; j++) {
                    ps.setObject(j + 1, data[j]);
                }
                ps.addBatch();
            }

            // 執行批次
            ps.executeBatch();

            // 手動提交交易
            connection.commit();

            long end = System.currentTimeMillis();
            log.info("批次寫入成功: 筆數={}, 耗時={}ms", resultList.size(), (end - start));
            executeStatus = true;

        } catch (Exception e) {
            log.error("批次寫入失敗，執行 rollback", e);
            log.error("錯誤資料: {}", errStr);

            // 回滾交易
            if (connection != null) {
                try {
                    connection.rollback();
                    log.info("交易已回滾");
                } catch (SQLException rollbackEx) {
                    log.error("回滾失敗", rollbackEx);
                }
            }

            throw new RuntimeException("Database batch operation failed", e);

        } finally {
            // 恢復自動提交模式（避免影響連線池中的其他使用者）
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException e) {
                    log.warn("恢復自動提交模式失敗", e);
                }
            }
            releaseConn(ps, connection);
        }

        return executeStatus;
    }

    /**
     * 執行SQL查詢並返回結果列表
     *
     * @param sql SQL查詢語句
     * @return 結果列表，每個元素為一個包含列名和值的Map
     */
    public List<Map<String, Object>> queryForList(String sql) {
        return queryForList(sql, null);
    }

    /**
     * 執行 SQL 查詢並將結果以列表形式返回，每個元素為一個包含欄位名稱與值的映射。
     *
     * @param sql    SQL 查詢語句
     * @param params 查詢參數，可為 null
     * @return 結果列表，每個元素為一個包含欄位名稱與值的 Map
     * @throws RuntimeException 若資料庫查詢失敗
     */
    public List<Map<String, Object>> queryForList(String sql, Object[] params) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            long start = System.currentTimeMillis();
            connection = dataSource.getConnection();
            ps = connection.prepareStatement(sql);

            // 設定查詢參數
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    ps.setObject(i + 1, params[i]);
                }
            }

            rs = ps.executeQuery();
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            // 處理查詢結果
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    Object value = rs.getObject(i);
                    row.put(columnName, value);
                }
                resultList.add(row);
            }

            long end = System.currentTimeMillis();
            log.debug("Query executed, results: {}, Time: {} ms", resultList.size(), (end - start));
        } catch (Exception e) {
            log.error("查詢執行錯誤: ", e);
            throw new RuntimeException("Database query failed", e);
        } finally {
            releaseResources(rs, ps, connection);
        }

        return resultList;
    }

    /**
     * 執行SQL查詢並返回單個結果
     *
     * @param sql SQL查詢語句
     * @return 單個結果，包含列名和值的Map
     */
    public Map<String, Object> queryForMap(String sql) {
        return queryForMap(sql, null);
    }

    /**
     * 執行參數化SQL查詢並返回單個結果
     *
     * @param sql    SQL查詢語句
     * @param params 查詢參數，可為null
     * @return 單個結果，包含列名和值的Map，若無結果則返回null
     */
    public Map<String, Object> queryForMap(String sql, Object[] params) {
        List<Map<String, Object>> resultList = queryForList(sql, params);
        if (resultList.isEmpty()) {
            return null;
        }
        return resultList.get(0);
    }

    /**
     * 執行SQL查詢並返回單個值
     *
     * @param sql          SQL查詢語句
     * @param requiredType 返回值的類型
     * @return 單個值，若無結果則返回null
     */
    public <T> T queryForObject(String sql, Class<T> requiredType) {
        return queryForObject(sql, null, requiredType);
    }

    /**
     * 執行參數化SQL查詢並返回單個值
     *
     * @param sql          SQL查詢語句
     * @param params       查詢參數，可為null
     * @param requiredType 返回值的類型
     * @return 單個值，若無結果則返回null
     */
    @SuppressWarnings("unchecked")
    public <T> T queryForObject(String sql, Object[] params, Class<T> requiredType) {
        Map<String, Object> result = queryForMap(sql, params);
        if (result == null || result.isEmpty()) {
            return null;
        }
        Object value = result.values().iterator().next();
        if (value == null) {
            return null;
        }
        if (requiredType.isInstance(value)) {
            return (T) value;
        }
        // 進行類型轉換
        if (requiredType == Integer.class && value instanceof Number) {
            return (T) Integer.valueOf(((Number) value).intValue());
        } else if (requiredType == Long.class && value instanceof Number) {
            return (T) Long.valueOf(((Number) value).longValue());
        } else if (requiredType == Double.class && value instanceof Number) {
            return (T) Double.valueOf(((Number) value).doubleValue());
        } else if (requiredType == Boolean.class) {
            if (value instanceof Boolean) {
                return (T) value;
            } else if (value instanceof Number) {
                return (T) Boolean.valueOf(((Number) value).intValue() != 0);
            } else if (value instanceof String) {
                return (T) Boolean.valueOf("true".equalsIgnoreCase((String) value) ||
                        "1".equals(value) ||
                        "yes".equalsIgnoreCase((String) value));
            }
        } else if (requiredType == String.class) {
            return (T) value.toString();
        }
        throw new ClassCastException("Cannot convert value of type " + value.getClass().getName() + " to required type " + requiredType.getName());
    }

    /**
     * 執行更新SQL語句（包含 INSERT、UPDATE、DELETE）
     *
     * @param sql SQL更新語句
     * @return 影響的行數
     */
    public int update(String sql) {
        return update(sql, null);
    }

    /**
     * 執行參數化更新SQL語句（包含 INSERT、UPDATE、DELETE）
     *
     * @param sql    SQL更新語句
     * @param params 查詢參數，可為null
     * @return 影響的行數
     */
    public int update(String sql, Object[] params) {
        Connection connection = null;
        PreparedStatement ps = null;
        int affectedRows = 0;

        try {
            connection = dataSource.getConnection();
            ps = connection.prepareStatement(sql);

            // 設置查詢參數
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    ps.setObject(i + 1, params[i]);
                }
            }

            affectedRows = ps.executeUpdate();
            log.debug("更新執行完成，影響行數: {}", affectedRows);
        } catch (Exception e) {
            log.error("更新執行錯誤: ", e);
            throw new RuntimeException("Database update failed", e);
        } finally {
            releaseConn(ps, connection);
        }

        return affectedRows;
    }

    private void releaseResources(ResultSet rs, PreparedStatement ps, Connection connection) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                log.info("FAIL rs.close()", e);
            }
        }
        releaseConn(ps, connection);
    }

    public boolean executeBatch(String sql, List<String[]> objects) {
        boolean executeStatus = false;
        Connection connection = null;
        PreparedStatement ps = null;
        String errStr = "";
        int[] result = null;
        try {
            long start = System.currentTimeMillis();
            connection = dataSource.getConnection();
            ps = connection.prepareStatement(sql);
            for (String[] obj : objects) {
                errStr = Arrays.toString(obj);
                for (int j = 0; j < obj.length; j++) {
                    ps.setObject(j + 1, obj[j]);
                }
                ps.addBatch();
            }
            result = ps.executeBatch();
            long end = System.currentTimeMillis();
            log.info("executeBatch, objects.size():{}, duration:{}", objects.size(), (end - start));
            executeStatus = true;
        } catch (Exception e) {
            log.error("executeBatch, error, errStr:{}, result:{}", errStr, result, e);
        } finally {
            releaseConn(ps, connection);
        }
        return executeStatus;
    }

    public int executeUpdate(String sql, List<String[]> objects) {
        int rowsAffected = -1;
        Connection connection = null;
        PreparedStatement ps = null;
        String errStr = "";
        try {
            long start = System.currentTimeMillis();
            connection = dataSource.getConnection();
            ps = connection.prepareStatement(sql);
            for (String[] obj : objects) {
                errStr = Arrays.toString(obj);
                for (int j = 0; j < obj.length; j++) {
                    ps.setObject(j + 1, obj[j]);
                }
                rowsAffected += ps.executeUpdate();
            }
            long end = System.currentTimeMillis();
            log.info("executeUpdate(sql, objects), rowsAffected:{}, duration:{}", rowsAffected, (end - start));
        } catch (Exception e) {
            log.error("executeUpdate(sql, objects), error, errStr:{}", errStr, e);
            throw new RuntimeException("Database operation failed", e);
        } finally {
            releaseConn(ps, connection);
        }
        return rowsAffected;
    }

    public int executeUpdate(String sql) {
        int rowsAffected = -1;
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            long start = System.currentTimeMillis();
            connection = dataSource.getConnection();
            ps = connection.prepareStatement(sql);
            rowsAffected = ps.executeUpdate();
            long end = System.currentTimeMillis();
            log.info("executeUpdate(sql), rowsAffected:{}, duration:{}", rowsAffected, (end - start));
        } catch (Exception e) {
            log.error("executeUpdate(sql), error", e);
            throw new RuntimeException("Database operation failed", e);
        } finally {
            releaseConn(ps, connection);
        }
        return rowsAffected;
    }

    private void releaseConn(PreparedStatement ps, Connection connection) {
        if (ps != null) {
            try {
                ps.close();
            } catch (SQLException e) {
                log.info("FAIL ps.close()", e);
            }
        }

        if (connection != null) {
            try {
                connection.setAutoCommit(true);
                connection.close();
            } catch (SQLException e) {
                log.info("FAIL connection.close()", e);
            }
        }
    }
}
