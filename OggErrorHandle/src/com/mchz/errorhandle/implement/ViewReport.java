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


import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import com.goldengate.gdsc.server.net.ggsci.Connection;
import com.goldengate.gdsc.server.net.ggsci.Response;
import com.mchz.errorhandle.util.ConnectionManagerOgg;


/**
 * 查看当前进程的报告
 * 
 * @author sandy
 */
public class ViewReport {

	private static final Logger	logger			= Logger.getLogger(ViewReport.class);
	private static final String	VIEW_REPORT_CMD	= "VIEW REPORT ";

	public List<String> viewErrorReport(String processName) {
		Connection con = null;
		List<String> errorList = new ArrayList<String>();
		try {
			con = ConnectionManagerOgg.getInstance().getConnection();
			String cmd = VIEW_REPORT_CMD + processName;
			Response res = null;
			res = con.ggsciCmd(cmd);
			// 查看错误报告
			if (res.getStatus() == Response.CMD_STATUS_SUCCESS) {
				String replyLines[] = res.getReply().split("\n");
				for (String replyLine : replyLines) {
					if (replyLine.indexOf("ERROR") > 0 || replyLine.indexOf("WARNING") > 0)
						errorList.add(replyLine);
				}
			} else {
				logger.error("ErrorReport:" + res);
			}
		} catch (Exception e) {
			logger.error("ErrorReport:" + e.getMessage());
		} finally {
			if (con != null) {
				ConnectionManagerOgg.getInstance().releaseConnection(con);
			}
		}
		return errorList;
	}
}
