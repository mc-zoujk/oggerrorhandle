/**
 * 版权所有：美创科技
 * 项目名称:OggErrorHandle
 * 创建者: sandy
 * 创建日期: 2016年2月15日
 * 文件说明:
 * 最近修改者：sandy
 * 最近修改日期：2016年2月15日
 */
package com.mchz.errorhandle.main;


import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;
import com.mchz.errorhandle.business.CheckErrorProcess;
import com.mchz.errorhandle.util.PropertiesConfig;


/**
 * @author sandy
 */
public class HandleMain {

	private static final Logger	logger			= Logger.getLogger(HandleMain.class);
	public static int			mailHashCode	= 0;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final CheckErrorProcess check = new CheckErrorProcess();
		PropertiesConfig config = new PropertiesConfig();
		config.getParameter();
		Runnable runnable = new Runnable(){

			public void run() {
				check.doCheckErrorProcess();
			}
		};
		ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);
		// 第二个参数为首次执行的延时时间(s)，第三个参数为定时执行的间隔时间(s)
		executorService.scheduleAtFixedRate(runnable, 1, PropertiesConfig.checkTimer, TimeUnit.SECONDS);
	}
}
