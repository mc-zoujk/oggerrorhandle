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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import com.mchz.errorhandle.util.PropertiesConfig;


/**
 * 获取表空间路径
 * 
 * @author sandy
 */
public class QueryTableSpace {

	private static final Logger	logger	= Logger.getLogger(QueryTableSpace.class);

	/**
	 * 表空间不存在情况下查找其他表空间路径
	 * 
	 * @return
	 */
	public List<String> getTableSpace() {
		List<String> list = new ArrayList<String>();
		Connection conn = null;
		PreparedStatement pstmt = null;
		String sql = "select t.file_name from dba_data_files t";
		try {
			Class.forName(PropertiesConfig.driverClass);
			conn = DriverManager.getConnection(PropertiesConfig.jdbcUrl, PropertiesConfig.user,
					PropertiesConfig.password);
			pstmt = conn.prepareStatement(sql);
			logger.info(sql);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				list.add(rs.getString(1));
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			try {
				if (pstmt != null)
					pstmt.close();
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return list;
	}

	/**
	 * 表空间满情况下表空间路径
	 * 
	 * @param user
	 * @return
	 */
	public List<String> getTableSpaceByName(String spaceName) {
		List<String> list = new ArrayList<String>();
		Connection conn = null;
		PreparedStatement pstmt = null;
		String sql = "select t.file_name from dba_data_files t where t.tablespace_name = ?";
		try {
			Class.forName(PropertiesConfig.driverClass);
			conn = DriverManager.getConnection(PropertiesConfig.jdbcUrl, PropertiesConfig.user,
					PropertiesConfig.password);
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, spaceName.toUpperCase());
			ResultSet rs = pstmt.executeQuery();
			logger.info(sql);
			while (rs.next()) {
				list.add(rs.getString(1));
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			try {
				if (pstmt != null)
					pstmt.close();
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return list;
	}
}
