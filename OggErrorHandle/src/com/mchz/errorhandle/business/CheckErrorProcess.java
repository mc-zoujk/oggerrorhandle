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


import java.io.File;
import java.util.List;
import org.apache.log4j.Logger;
import com.mchz.errorhandle.dao.ExecuteSQL;
import com.mchz.errorhandle.dao.QueryTableSpace;
import com.mchz.errorhandle.implement.ExcludeCompressedTable;
import com.mchz.errorhandle.implement.LinuxFreeSpace;
import com.mchz.errorhandle.implement.LogAnalyzeDispose;
import com.mchz.errorhandle.implement.ProcessInfoAll;
import com.mchz.errorhandle.implement.SimpleMailSender;
import com.mchz.errorhandle.implement.StartProcess;
import com.mchz.errorhandle.implement.TableNameExtractTool;
import com.mchz.errorhandle.implement.ViewReport;
import com.mchz.errorhandle.util.PropertiesConfig;


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
	private TableNameExtractTool	tableNameExtractTool	= new TableNameExtractTool();
	private LogAnalyzeDispose		logAnalyzeDispose		= new LogAnalyzeDispose();
	private ExecuteSQL				executeSQL				= new ExecuteSQL();
	private QueryTableSpace			queryTableSpace			= new QueryTableSpace();
	private LinuxFreeSpace			linuxFreeSpace			= new LinuxFreeSpace();
	private static final String		EXTRACT_TYPE			= "EXTRACT";
	private static final String		REPLICAT_TYPE			= "REPLICAT";
	private static final int		TBS_OVERFLOW			= 1;
	private static final int		TBS_NOEXIST				= 2;
	private static final int		ROW_MOVEMENT_UNABLE		= 3;

	public void doCheckErrorProcess() {
		// 获取错误的进程，没有错误返回0
		String processTypeName = processInfoAll.doProcessInfoAll();
		if (!processTypeName.equals("0")) {
			boolean handleError = false;
			String[] process = processTypeName.split("@@");
			logger.info(process[1] + " is not running");
			// 根据进程名称，返回错误和告警列表
			List<String> errorList = viewReport.viewErrorReport(process[1]);
			if (EXTRACT_TYPE.equalsIgnoreCase(process[0])) {
				// 处理挖掘进程的错误
				// 找到错误内容
				for (String errorLine : errorList) {
					if (errorLine.indexOf("OGG-01433") > 0) {
						logger.info(errorLine);
						handleError = compressedTableError(errorLine, process[1]);
						break;
					} else if (errorLine.indexOf("OGG-00455") > 0) {
						logger.info(errorLine);
						handleError = compressedTableError(errorLine, process[1]);
						break;
					}
				}
			} else if (REPLICAT_TYPE.equalsIgnoreCase(process[0])) {
				// 处理应用进程的错误
				// 找到错误内容
				for (String errorLine : errorList) {
					logger.info(errorLine);
					// 是否是已知错误
					// 根据错误类型分类，及处理错误
					if (logAnalyzeDispose.isErrorLine(errorLine)) {
						switch (logAnalyzeDispose.getErrorType(errorLine)) {
							case TBS_OVERFLOW:// 表空间满
								handleError = addTableSpaceError(errorLine);
								break;
							case TBS_NOEXIST:// 表空间不存在
								handleError = createTableSpaceError(errorLine);
								break;
							case ROW_MOVEMENT_UNABLE:// 分区表未打开行迁移
								handleError = rowMovementError(errorLine);
								break;
							default:
								break;
						}
					}
				}
			}
			// 是否需要重启进程
			if (handleError)
				startProcess.doStartProcess(process[1]);
			SimpleMailSender mail = new SimpleMailSender();
			StringBuffer sb = new StringBuffer();
			for (String s : errorList) {
				sb.append(s).append("\r\n");
			}
			if (handleError)
				mail.sendMail(PropertiesConfig.hostIp + ":" + process[1] + "进程已重启", sb.toString());
			else
				mail.sendMail(PropertiesConfig.hostIp + ":" + process[1] + "未运行状态", sb.toString());
		}
	}

	/**
	 * 处理表空间满错误
	 * 
	 * @return
	 */
	private boolean addTableSpaceError(String errorLine) {
		// 获取表空间名称
		String tableSpaceName = tableNameExtractTool.getTbsName(errorLine);
		// 获取表空间路径
		List<String> list = queryTableSpace.getTableSpaceByName(tableSpaceName);
		String spacePath = list.get(0);
		File file = new File(spacePath);
		String parentPath = file.getParent();
		String maxNum = getMaxNum(list);
		// 获取剩余空间大小
		Long freeSpace = linuxFreeSpace.isAvailable(parentPath);
		if (freeSpace > 1024 * 1024 * 1024 * 4) {
			String newPath = parentPath + File.separator + tableSpaceName.toLowerCase() + maxNum + ".dbf";
			String sql = "Alter tablespace " + tableSpaceName.toLowerCase() + " add datafile '" + newPath
					+ "' size 500m maxsize 4g autoextend on";
			boolean result = executeSQL.executeSql(sql);
			return result;
		} else {
			logger.error("剩余磁盘空间不足");
			return false;
		}
	}

	/**
	 * 获取表空间最大序号+1
	 * 
	 * @param list
	 * @return
	 */
	private String getMaxNum(List<String> list) {
		String maxNum = "01";
		for (String str : list) {
			String num = str.substring(str.length() - 6, str.length() - 4);
			boolean mat = num.matches("\\d+");
			if (mat) {
				if (Integer.parseInt(num) > Integer.parseInt(maxNum))
					maxNum = num;
			}
		}
		int maxInt = Integer.parseInt(maxNum) + 1;
		String maxStr = String.valueOf(maxInt);
		if (maxStr.length() == 1)
			maxStr = "0" + maxStr;
		return maxStr;
	}

	/**
	 * 处理表空间不存在错误
	 * 
	 * @return
	 */
	private boolean createTableSpaceError(String errorLine) {
		// 获取表空间名称
		String tableSpaceName = tableNameExtractTool.getTbsName(errorLine);
		// 获取表空间路径
		List<String> list = queryTableSpace.getTableSpace();
		String spacePath = list.get(0);
		File file = new File(spacePath);
		String parentPath = file.getParent();
		// 获取剩余空间大小
		Long freeSpace = linuxFreeSpace.isAvailable(parentPath);
		if (freeSpace > 1024 * 1024 * 1024 * 4) {
			String newPath = parentPath + File.separator + tableSpaceName.toLowerCase() + "01.dbf";
			String sql = "create tablespace " + tableSpaceName.toLowerCase() + " datafile '" + newPath
					+ "' size 500M maxsize 4g autoextend on";
			boolean result = executeSQL.executeSql(sql);
			return result;
		} else {
			logger.error("剩余磁盘空间不足");
			return false;
		}
	}

	/**
	 * 分区表未打开行迁移错误
	 * 
	 * @return
	 */
	private boolean rowMovementError(String errorLine) {
		// 获取行迁移的表格
		String tableName = tableNameExtractTool.getTableName(errorLine);
		// 打开行迁移
		String sql = "alter table " + tableName + " enable row movement";
		boolean result = executeSQL.executeSql(sql);
		return result;
	}

	/**
	 * 排除压缩表错误
	 * 
	 * @return
	 */
	private boolean compressedTableError(String errorLine, String processName) {
		// 查找压缩表表名
		String tableName = tableNameExtractTool.getCompressTableName(errorLine).toLowerCase();
		logger.info("需要排除的压缩表名:" + tableName);
		if (tableName != null) {
			String[] shcemaTablename = tableName.split("\\.");
			String allTable = "table " + shcemaTablename[0] + ".*;";
			String prmText = excludeCompressedTable.getFile(processName);
			String prmTestLowerCase = prmText.toLowerCase();
			if (prmTestLowerCase.indexOf(allTable) > 0) {
				String excludeTable = "tableexclude " + tableName + ";";
				if (prmTestLowerCase.indexOf(excludeTable) < 0)
					prmText = prmText + "\r\n" + excludeTable;
			} else {
				prmText = prmText.replaceAll("[T|t][A|a][B|b][L|l][E|e][ ]+(" + tableName.toUpperCase() + ";)", "")
						.replaceAll("[T|t][A|a][B|b][L|l][E|e][ ]+(" + tableName.toLowerCase() + ";)", "");
			}
			if (prmTestLowerCase.length() != prmText.length()) {
				excludeCompressedTable.putFile(processName, prmText, true);
				return true;
			}
		}
		return false;
	}
}
