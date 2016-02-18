/**
 * 版权所有：美创科技
 * 项目名称:OggErrorHandle
 * 创建者: sandy
 * 创建日期: 2016年2月17日
 * 文件说明:
 * 最近修改者：sandy
 * 最近修改日期：2016年2月17日
 */
package com.mchz.errorhandle.business;


import java.util.List;
import org.apache.log4j.Logger;
import com.mchz.errorhandle.implement.ExcludeCompressedTable;
import com.mchz.errorhandle.implement.ProcessInfoAll;
import com.mchz.errorhandle.implement.StartProcess;
import com.mchz.errorhandle.implement.ViewReport;


/**
 * 错误处理逻辑类
 * 
 * @author sandy
 */
public class CheckErrorProcess {

	private static final Logger		logger					= Logger.getLogger(CheckErrorProcess.class);
	private ProcessInfoAll			processInfoAll			= new ProcessInfoAll();
	private ViewReport				viewReport				= new ViewReport();
	private StartProcess			startProcess			= new StartProcess();
	private ExcludeCompressedTable	excludeCompressedTable	= new ExcludeCompressedTable();
	private static final String		EXTRACT_TYPE			= "EXTRACT";
	private static final String		REPLICAT_TYPE			= "REPLICAT";

	public void doCheckErrorProcess() {
		// 获取错误的进程，没有错误返回0
		String processTypeName = processInfoAll.doProcessInfoAll();
		if (!processTypeName.equals("0")) {
			boolean handleError = false;
			String[] process = processTypeName.split("@@");
			logger.error(process[1] + " is not running");
			// 根据进程名称，返回错误和告警列表
			List<String> errorList = viewReport.viewErrorReport(process[1]);
			if (EXTRACT_TYPE.equalsIgnoreCase(process[0])) {
				// 处理挖掘进程的错误
				// 找到错误内容
				for (String errorLine : errorList) {
					logger.info(errorLine);
					if (errorLine.indexOf("OGG-01433") > 0) {
						handleError = compressedTableError(errorLine);
						break;
					}
				}
				// 是否能够处理错误
				// 是否需要重启进程
				if (handleError)
					startProcess.doStartProcess(process[1]);
			} else if (REPLICAT_TYPE.equalsIgnoreCase(process[0])) {
				// 处理应用进程的错误
				// 找到错误内容
				for (String errorLine : errorList) {
					logger.info(errorLine);
				}
				// 是否能够处理错误
				// 是否需要重启进程
			}
		}
	}

	/**
	 * 处理表空间满错误
	 * 
	 * @return
	 */
	private boolean addTableSpaceError() {
		return false;
	}

	/**
	 * 处理表空间不存在错误
	 * 
	 * @return
	 */
	private boolean createTableSpaceError() {
		return false;
	}

	/**
	 * 分区表未打开行迁移错误
	 * 
	 * @return
	 */
	private boolean rowMovementError() {
		return false;
	}

	/**
	 * 排除压缩表错误
	 * 
	 * @return
	 */
	private boolean compressedTableError(String errorLine) {
		return false;
	}
}
