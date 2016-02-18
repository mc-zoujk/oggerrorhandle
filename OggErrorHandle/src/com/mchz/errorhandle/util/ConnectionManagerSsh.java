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


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import org.apache.log4j.Logger;
import ch.ethz.ssh2.ChannelCondition;
import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;


/**
 * ssh连接
 * 
 * @author sandy
 */
public class ConnectionManagerSsh {

	private static final Logger			logger	= Logger.getLogger(ConnectionManagerSsh.class);
	private static ConnectionManagerSsh	instance;

	private ConnectionManagerSsh() {
	}

	public static synchronized ConnectionManagerSsh getInstance() {
		if (instance == null) {
			instance = new ConnectionManagerSsh();
		}
		return instance;
	}

	/**
	 * 执行SSH命令
	 * 
	 * @param commandLine
	 * @return
	 * @throws Exception
	 */
	public BufferedReader execCommand(String commandLine) throws Exception {
		String IpAddress = PropertiesConfig.hostIp;
		int port = PropertiesConfig.sshPort;
		String user = PropertiesConfig.osUser;
		// String privatekey = null;
		String password = PropertiesConfig.osPassword;
		BufferedReader br;
		BufferedReader brerror;
		Connection conn = null;
		Session session = null;
		try {
			conn = new Connection(IpAddress, port);
			conn.connect();
			// boolean isAuthenticated;
			// isAuthenticated =
			// 有秘钥文件的使用秘钥文件
			// if (privatekey != null && !privatekey.isEmpty()) {
			// File fkey = new File(privatekey);
			// conn.authenticateWithPublicKey(user, fkey, password);
			// } else {
			conn.authenticateWithPassword(user, password);
			// }
			session = conn.openSession();
			session.execCommand(commandLine);
			InputStream stdout = new StreamGobbler(session.getStdout());
			InputStream stderr = new StreamGobbler(session.getStderr());
			br = new BufferedReader(new InputStreamReader(stdout));
			brerror = new BufferedReader(new InputStreamReader(stderr));
			session.waitForCondition(ChannelCondition.CLOSED | ChannelCondition.EOF, 1000 * 20);
			StringBuffer err = new StringBuffer();
			String line;
			while ((line = brerror.readLine()) != null) {
				err.append(line);
			}
			brerror.close();
			if (err.length() > 0) {
				throw new Exception(err.toString());
			}
			// session.waitForCondition(ChannelCondition.CLOSED
			// | ChannelCondition.EOF | ChannelCondition.EXIT_STATUS,
			// 1000 * 20);
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw e;
		} finally {
			if (session != null) {
				session.close();
			}
			if (conn != null) {
				conn.close();
			}
		}
		return br;
	}

	public BufferedReader execShellCommands(String commandLine) throws Exception {
		String IpAddress = PropertiesConfig.hostIp;
		int port = PropertiesConfig.sshPort;
		String user = PropertiesConfig.osUser;
		// String privatekey = null;
		String password = PropertiesConfig.osPassword;
		BufferedReader br;
		Connection conn = null;
		Session session = null;
		try {
			conn = new Connection(IpAddress, port);
			conn.connect();
			session = null;
			// boolean isAuthenticated;
			// isAuthenticated =
			// 有秘钥文件的使用秘钥文件
			// if (privatekey != null && !privatekey.isEmpty()) {
			// File fkey = new File(privatekey);
			// conn.authenticateWithPublicKey(user, fkey, password);
			// } else {
			conn.authenticateWithPassword(user, password);
			// }
			session = conn.openSession();
			session.requestPTY("vt100", 80, 24, 640, 480, null);
			session.startShell();
			OutputStream stdin = session.getStdin();
			PrintStream pstdin = new PrintStream(stdin);
			InputStream stdout = new StreamGobbler(session.getStdout());
			br = new BufferedReader(new InputStreamReader(stdout));
			pstdin.print(commandLine);
			pstdin.flush();
			pstdin.close();
			session.waitForCondition(ChannelCondition.CLOSED | ChannelCondition.EOF, 1000 * 20);
			// session.waitForCondition(ChannelCondition.CLOSED
			// | ChannelCondition.EOF | ChannelCondition.EXIT_STATUS,
			// 1000 * 20);
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw e;
		} finally {
			if (session != null) {
				session.close();
			}
			if (conn != null) {
				conn.close();
			}
		}
		return br;
	}
}
