/**
 * 版权所有：美创科技
 * 项目名称:OggErrorHandle
 * 创建者: sandy
 * 创建日期: 2016年2月18日
 * 文件说明:
 * 最近修改者：sandy
 * 最近修改日期：2016年2月18日
 */
package com.mchz.errorhandle.implement;


import org.apache.log4j.Logger;
import com.goldengate.gdsc.server.net.ggsci.Connection;
import com.goldengate.gdsc.server.net.ggsci.Response;
import com.mchz.errorhandle.util.ConnectionManagerOgg;


/**
 * 启动进程
 * @author sandy
 */
public class StartProcess {

	private static final Logger	logger		= Logger.getLogger(StartProcess.class);
	private static final String	START_CMD	= "START ";

	public void doStartProcess(String processName) {
		Connection con = null;
		try {
			con = ConnectionManagerOgg.getInstance().getConnection();
			String cmd = START_CMD + processName;
			Response res = null;
			res = con.ggsciCmd(cmd);
			if (res.getStatus() == Response.CMD_STATUS_SUCCESS) {
			} else {
				logger.error("StartProcess:" + res);
			}
		} catch (Exception e) {
			logger.error("StartProcess:" + e.getMessage());
		} finally {
			if (con != null) {
				ConnectionManagerOgg.getInstance().releaseConnection(con);
			}
		}
	}
}
