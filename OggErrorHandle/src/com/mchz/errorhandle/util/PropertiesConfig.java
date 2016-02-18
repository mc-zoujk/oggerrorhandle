/**
 * 版权所有：美创科技
 * 项目名称:OggErrorHandle
 * 创建者: sandy
 * 创建日期: 2016年2月15日
 * 文件说明:
 * 最近修改者：sandy
 * 最近修改日期：2016年2月15日
 */
package com.mchz.errorhandle.util;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


/**
 * 取properties文件相关参数
 * 
 * @author sandy
 */
public class PropertiesConfig {

	private static final Logger	logger			= Logger.getLogger(PropertiesConfig.class);
	public static String		jdbcUrl			= null;
	public static String		user			= null;
	public static String		password		= null;
	public static String		driverClass		= null;
	public static String		hostIp			= null;
	public static int			oggPort			= 0;
	public static int			sshPort			= 0;
	public static String		tablespaceDir	= null;
	public static int			checkTimer		= 0;
	public static String		osUser			= null;
	public static String		osPassword		= null;

	public PropertiesConfig() {
	}

	/**
	 * 数据库相关参数
	 */
	public void getParameter() {
		jdbcUrl = readValueByPassword("jdbc.jdbcUrl");
		logger.debug("jdbcUrl:" + jdbcUrl);
		user = readValueByPassword("jdbc.user");
		logger.debug("user:" + user);
		password = readValueByPassword("jdbc.password");
		logger.debug("password:" + password);
		hostIp = readValueByPassword("host.ip");
		driverClass = "oracle.jdbc.driver.OracleDriver";
		String oggPortStr = readValueByPassword("ogg.port");
		if (StringUtils.isEmpty(oggPortStr))
			logger.error("oggPort not exists in config.properties");
		else
			oggPort = Integer.parseInt(oggPortStr);
		logger.debug("oggPort:" + oggPort);
		String sshPortStr = readValueByPassword("ssh.port");
		if (StringUtils.isEmpty(sshPortStr))
			sshPort = 22;
		else
			sshPort = Integer.parseInt(sshPortStr);
		logger.debug("sshPort:" + sshPort);
		tablespaceDir = readValueByPassword("tablespace.dir");
		logger.debug("tablespaceDir:" + tablespaceDir);
		String checkTimerStr = readValueByPassword("check.timer");
		if (StringUtils.isEmpty(checkTimerStr))
			checkTimer = 60;
		else
			checkTimer = Integer.parseInt(checkTimerStr);
		logger.debug("checkTimer:" + checkTimer);
		osUser = readValueByPassword("os.user");
		logger.debug("osUser:" + osUser);
		osPassword = readValueByPassword("os.password");
		logger.debug("osPassword:" + osPassword);
	}

	/**
	 * @param key
	 * @return
	 */
	public String readValueByPassword(String key) {
		try {
			String filename = "config.properties";
			Properties properties = new Properties();
			InputStream in = PropertiesConfig.class.getClassLoader().getResourceAsStream(filename);
			properties.load(in);
			in.close();
			String value = properties.getProperty(key);
			if (value == null) {
				logger.info("PropertiesConfig.readJdbcByPassword():" + key + " not exists in config.properties!");
				return null;
			}
			return value;
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
			logger.error("PropertiesConfig.readValueByPassword():" + ex.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("PropertiesConfig.readValueByPassword():" + e.getMessage());
		}
		return null;
	}
}
