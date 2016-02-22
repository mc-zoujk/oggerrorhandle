package com.mchz.errorhandle.implement;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @author liurh
 * @date 2016年2月4日
 * @intro 对行进行错误及类型判断
 *
 */
public class LogAnalyzeDispose {
	// 正则匹配
	private Pattern pattern;
	private Matcher matcher;
	// 匹配字符串(表空间满）01654 改成 01653
	private final static String MATCH_ORA_01653 = "(ORA-01653)";
	// 匹配字符串(表空间不存在）
	private final static String MATCH_ORA_00959 = "(ORA-00959)";
	// 匹配字符串(分区表未打开行迁移）
	private final static String MATCH_ORA_14402 = "(ORA-14402)";

	/**
	 * 分析错误类型
	 * @param readLineTemp
	 * @return
	 */
	public int getErrorType(String readLineTemp) {
		// 表空间满匹配
		pattern = Pattern.compile(MATCH_ORA_01653);
		matcher = pattern.matcher(readLineTemp);
		if (matcher.find())
			return 1;

		// 表空间不存在匹配
		pattern = Pattern.compile(MATCH_ORA_00959);
		matcher = pattern.matcher(readLineTemp);
		if (matcher.find())
			return 2;

		// 分区表未打开行迁移
		pattern = Pattern.compile(MATCH_ORA_14402);
		matcher = pattern.matcher(readLineTemp);
		if (matcher.find())
			return 3;

		return 0;
	}

	/**
	 * 判断是否为待处理错误行
	 * @param readLineTemp
	 * @return
	 */
	public boolean isErrorLine(String readLineTemp) {
		// 表空间满匹配
		pattern = Pattern.compile(MATCH_ORA_01653);
		matcher = pattern.matcher(readLineTemp);
		if (matcher.find())
			return true;

		// 表空间不存在匹配
		pattern = Pattern.compile(MATCH_ORA_00959);
		matcher = pattern.matcher(readLineTemp);
		if (matcher.find())
			return true;

		// 分区表未打开行迁移
		pattern = Pattern.compile(MATCH_ORA_14402);
		matcher = pattern.matcher(readLineTemp);
		if (matcher.find())
			return true;

		return false;
	}

}
