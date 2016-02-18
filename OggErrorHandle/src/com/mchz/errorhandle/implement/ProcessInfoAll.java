/**
 * 版权所有：美创科技
 * 项目名称:OggErrorHandle
 * 创建者: sandy
 * 创建日期: 2016年2月15日
 * 文件说明:
 * 最近修改者：sandy
 * 最近修改日期：2016年2月15日
 */
package com.mchz.errorhandle.implement;


import org.apache.log4j.Logger;
import com.goldengate.gdsc.server.net.ggsci.Connection;
import com.goldengate.gdsc.server.net.ggsci.Response;
import com.mchz.errorhandle.util.ConnectionManagerOgg;


/**
 * @author sandy
 */
public class ProcessInfoAll {

	private static final Logger	logger			= Logger.getLogger(ProcessInfoAll.class);
	private static final String	INFO_ALL_CMD	= " INFO ALL ";
	private static final String	EXTRACT_TYPE	= "EXTRACT";
	private static final String	REPLICAT_TYPE	= "REPLICAT";

	public String doProcessInfoAll() {
		Connection con = null;
		try {
			con = ConnectionManagerOgg.getInstance().getConnection();
			String cmd = INFO_ALL_CMD;
			Response res = null;
			res = con.ggsciCmd(cmd);
			if (res.getStatus() == Response.CMD_STATUS_SUCCESS) {
				logger.debug(res.getReply());
				String replyLines[] = res.getReply().split("\n");
				for (String replyLine : replyLines) {
					String replyCols[] = replyLine.split(" +");
					// 找到挖掘进程或者应用进程
					if (EXTRACT_TYPE.equalsIgnoreCase(replyCols[0]) || REPLICAT_TYPE.equalsIgnoreCase(replyCols[0])) {
						// 返回非运行状态的进程类型和进程名称
						if (!"RUNNING".equals(replyCols[1]))
							return replyCols[0] + "@@" + replyCols[2];
					}
				}
			} else {
				logger.error("ProcessInfoAll:" + res);
			}
		} catch (Exception e) {
			logger.error("ProcessInfoAll:" + e.getMessage());
		} finally {
			if (con != null) {
				ConnectionManagerOgg.getInstance().releaseConnection(con);
			}
		}
		return "0";
	}
}
