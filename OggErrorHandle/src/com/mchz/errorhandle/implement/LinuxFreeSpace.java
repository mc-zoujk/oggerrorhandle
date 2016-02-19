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


import java.io.BufferedReader;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import com.mchz.errorhandle.util.ConnectionManagerSsh;


/**
 * Linux剩余空间
 * 
 * @author sandy
 */
public class LinuxFreeSpace {

	private static final Logger	logger				= Logger.getLogger(LinuxFreeSpace.class);
	private static final String	COMMAND_LINE_FIRST	= "df ";									// 命令行

	/**
	 * 返回剩余空间大小
	 * 
	 * @param tbsFilePath
	 * @return
	 */
	public long isAvailable(String tbsFilePath) {
		long availableSize = 0;
		// 判断系统类型，win或其他
		String osName = System.getProperty("os.name").toLowerCase();
		int sysNo = osName.indexOf("win");
		if (sysNo >= 0) {// windows
			File file = null;
			try {
				file = new File(tbsFilePath);
				if (file.exists()) {
					availableSize = file.getFreeSpace();
				} else {
					logger.error("该文件不存在");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {// 其他
			availableSize = getDiskSize(tbsFilePath) * 1024;
		}
		return availableSize;
	}

	/**
	 * 获取磁盘剩余空间大小
	 * 
	 * @param tbsFilePath
	 * @return
	 */
	private long getDiskSize(String tbsFilePath) {
		String matchLine = null;
		String commandLine = COMMAND_LINE_FIRST + tbsFilePath;
		String readTemp = getCommandLineResult(commandLine);
		if (!StringUtils.isEmpty(readTemp)) {
			Pattern pattern = Pattern.compile("[ ][\\d]+[ ]");
			Matcher matcher = pattern.matcher(readTemp);
			while (matcher.find()) {
				matchLine = matcher.group().trim();
			}
			return Long.parseLong(matchLine);
		} else
			return 0;
	}

	/**
	 * 执行SSH命令
	 * 
	 * @param commandLine
	 * @return
	 */
	private String getCommandLineResult(String commandLine) {
		String readTemp = null;
		String readLine = null;
		try {
			BufferedReader bufferedReader = ConnectionManagerSsh.getInstance().execCommand(commandLine);
			while ((readLine = bufferedReader.readLine()) != null) {
				readTemp = readLine;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return readTemp;
	}
}
