/**
 * 版权所有：美创科技
 * 项目名称:OggErrorHandle
 * 创建者: sandy
 * 创建日期: 2016年2月18日
 * 文件说明:
 * 最近修改者：sandy
 * 最近修改日期：2016年2月18日
 */
package com.mchz.errorhandle.dao;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.log4j.Logger;
import com.mchz.errorhandle.util.PropertiesConfig;


/**
 * 打开行迁移、增加表空间、修改表空间
 * 
 * @author sandy
 */
public class ExecuteSQL {

	private static final Logger	logger	= Logger.getLogger(ExecuteSQL.class);

	/**
	 * 打开行迁移、增加表空间、修改表空间
	 * 
	 * @param tableName
	 * @return
	 */
	public boolean executeSql(String sql) {
		Connection conn = null;
		Statement stmt = null;
		try {
			Class.forName(PropertiesConfig.driverClass);
			conn = DriverManager.getConnection(PropertiesConfig.jdbcUrl, PropertiesConfig.user,
					PropertiesConfig.password);
			logger.info("ExecuteSQL:" + sql);
			stmt = conn.createStatement();
			stmt.execute(sql);
			conn.commit();
		} catch (Exception e) {
			logger.error(e.getMessage());
			return false;
		} finally {
			try {
				if (stmt != null)
					stmt.close();
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return true;
	}
}
