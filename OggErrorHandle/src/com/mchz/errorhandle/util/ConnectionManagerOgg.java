/**
 * 版权所有：美创科技
 * 项目名称:OggErrorHandle
 * 创建者: sandy
 * 创建日期: 2016年2月17日
 * 文件说明:
 * 最近修改者：sandy
 * 最近修改日期：2016年2月17日
 */
package com.mchz.errorhandle.util;


import java.util.HashMap;
import org.apache.log4j.Logger;
import com.goldengate.gdsc.server.net.ggsci.ConnectException;
import com.goldengate.gdsc.server.net.ggsci.Connection;
import com.goldengate.gdsc.server.net.services.ResourcePool;


/**
 * ogg连接管理
 * 
 * @author sandy
 */
public class ConnectionManagerOgg {

	private static final Logger						logger		= Logger.getLogger(ConnectionManagerOgg.class);
	private static ConnectionManagerOgg				instance;
	private static HashMap<String, ResourcePool>	pools;
	private static int								POOL_SIZE	= 1;

	private ConnectionManagerOgg() {
		pools = new HashMap<String, ResourcePool>();
	}

	/**
	 * 获取连接
	 * 
	 * @param host
	 * @param port
	 * @return
	 * @throws ConnectException
	 */
	public Connection getConnection() throws ConnectException {
		String host = PropertiesConfig.hostIp;
		int port = PropertiesConfig.oggPort;
		String key = host + ":" + String.valueOf(port);
		ResourcePool pool = null;
		Connection con = null;
		logger.debug("get ogg connect " + host + ":" + port);
		if (pools.containsKey(key)) {
			pool = (ResourcePool) pools.get(key);
			con = (Connection) pool.getResource();
			return con;
		}
		synchronized (this) {
			pool = (ResourcePool) pools.get(key);
			if (pool != null)
				return (Connection) pool.getResource();
			pool = new ResourcePool();
			for (int i = 0; i < POOL_SIZE; i++) {
				try {
					con = new Connection(host, port);
				} catch (ConnectException e) {
					throw e;
				}
				pool.addResource(con);
			}
			con = (Connection) pool.getResource();
			pools.put(key, pool);
			return con;
		}
	}

	/**
	 * 释放ogg连接
	 * 
	 * @param con
	 */
	public void releaseConnection(Connection con) {
		String key = con.getManagerIP() + ":" + con.getManagerPort();
		logger.debug("release ogg connect " + con.getManagerIP() + ":" + con.getManagerPort());
		ResourcePool pool = (ResourcePool) pools.get(key);
		pool.releaseResource(con);
	}

	public static synchronized ConnectionManagerOgg getInstance() {
		if (instance == null) {
			instance = new ConnectionManagerOgg();
		}
		return instance;
	}
}
