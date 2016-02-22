package com.mchz.errorhandle.implement;


import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;


/**
 * @author liurh
 * @date 2016年2月18日
 * @intro 将获取的表名、表空间名等字符串做进一步的提取，获取可用的表名、空间名等
 */
public class TableNameExtractTool {

	private static final Logger	logger				= Logger.getLogger(ProcessInfoAll.class);
	private final static String	MATCH_TBS_NAME		= "(tablespace)[ ]+[^ ]+";
	private final static String	MATCH_COMPRESS_NAME	= "(table)[ ]+[^ ]+[.][^ ]+";
	private final static String	MATCH_UPDATE_NAME	= "(UPDATE)(.)+[^ ]+[.][^ ]+";
	private final static String	MATCH_QUOTATION		= "[\"|']";
	private final static String	MATCH_PERIOD		= "[.]\\B";

	// 获取表空间名
	public String getTbsName(String errorLine) {
		Pattern pattern = Pattern.compile(MATCH_TBS_NAME);
		Matcher matcher = pattern.matcher(errorLine);
		if (matcher.find()) {
			return getTableOrSpaceName(matcher.group(0));
		}
		return null;
	}

	// 获取表名方法
	public String getTableName(String errorLine) {
		Pattern pattern = Pattern.compile(MATCH_UPDATE_NAME);
		Matcher matcher = pattern.matcher(errorLine);
		if (matcher.find()) {
			return getTableOrSpaceName(matcher.group());
		}
		return null;
	}

	// 获取压缩表表名
	public String getCompressTableName(String errorLine) {
		Pattern pattern = Pattern.compile(MATCH_COMPRESS_NAME);
		Matcher matcher = pattern.matcher(errorLine);
		if (matcher.find()) {
			return getTableOrSpaceName(matcher.group(0));
		}
		return null;
	}

	/**
	 * 根据字符串提取出表名或空间名等
	 * 
	 * @param 字符串格式
	 *            (xxx AAAA),AAAA为需要提取内容
	 * @return 提取出字段
	 */
	private String getTableOrSpaceName(String tempName) {
		String extractName = null;
		// 去除字符串两边空格
		tempName = tempName.trim();
		// 取得字符串最后一段
		String[] mStringArr = tempName.split(" ");
		extractName = mStringArr[mStringArr.length - 1];
		// 去除取得字段中的引号（单引号&双引号）
		Pattern pattern = Pattern.compile(MATCH_QUOTATION);
		Matcher matcher = pattern.matcher(extractName);
		if (matcher.find()) {
			extractName = matcher.replaceAll("");
		}
		// 去除最后的句号
		pattern = Pattern.compile(MATCH_PERIOD);
		matcher = pattern.matcher(extractName);
		if (matcher.find()) {
			extractName = matcher.replaceAll("");
		}
		return extractName;
	}
}
