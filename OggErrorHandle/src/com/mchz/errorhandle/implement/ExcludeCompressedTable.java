/**
 * 版权所有：美创科技
 * 项目名称:OggErrorHandle
 * 创建者: sandy
 * 创建日期: 2016年2月17日
 * 文件说明:
 * 最近修改者：sandy
 * 最近修改日期：2016年2月17日
 */
package com.mchz.errorhandle.implement;


import java.text.DateFormat;
import java.util.Date;
import org.apache.log4j.Logger;
import com.goldengate.gdsc.common.ex.GDSCException;
import com.goldengate.gdsc.common.ex.GDSCNetConnectException;
import com.goldengate.gdsc.common.ex.GDSCNetHostFileException;
import com.goldengate.gdsc.common.ex.GDSCNetHostFileExistsException;
import com.goldengate.gdsc.common.ex.GDSCNetHostFileNotFoundException;
import com.goldengate.gdsc.common.ex.GDSCNetHostInternalException;
import com.goldengate.gdsc.server.net.ggsci.ConnectException;
import com.goldengate.gdsc.server.net.ggsci.Connection;
import com.goldengate.gdsc.server.net.ggsci.Response;
import com.mchz.errorhandle.util.ConnectionManagerOgg;


/**
 * 排除压缩表错误
 * 
 * @author sandy
 */
public class ExcludeCompressedTable {

	private static final Logger	logger	= Logger.getLogger(ExcludeCompressedTable.class);
	private DateFormat			df		= DateFormat.getDateTimeInstance(3, 3);

	public String getFile(String filename){
		filename = "param " + filename;
		Connection con = null;
		Response reply = null;
		StringBuffer retval = new StringBuffer();
		try {
			con = ConnectionManagerOgg.getInstance().getConnection();
			// con =
			// ConnectionManager.getInstance().getConnection("172.16.8.14",
			// 7809);
			reply = con.ggsciGetFile(filename);
			if (reply.getStatus() == Response.CMD_STATUS_FILE_ERROR) {
				throw new GDSCNetHostFileException(reply.getError());
			}
			if (reply.getStatus() == Response.CMD_STATUS_FILE_NODIR) {
				throw new GDSCNetHostFileException(reply.getError());
			}
			if (reply.getStatus() == Response.CMD_STATUS_FILE_NOFILE) {
				throw new GDSCNetHostFileException(reply.getError());
			}
			if (reply.getStatus() == Response.CMD_STATUS_FILE_NOGROUP) {
				throw new GDSCNetHostFileException(reply.getError());
			}
			if (reply.getStatus() == Response.CMD_STATUS_FILE_NOPERM) {
				throw new GDSCNetHostFileException(reply.getError());
			}
			if (reply.getStatus() != Response.CMD_STATUS_SUCCESS) {
				throw new GDSCNetHostInternalException(reply.getError());
			}
			retval.append(reply.getReply());
			while (reply.hasMore()) {
				reply = con.fetchMoreData();
				if (reply.getStatus() != Response.CMD_STATUS_SUCCESS) {
					throw new GDSCNetHostInternalException(reply.getError());
				}
				if ((reply.hasMore()) && ("".equals(reply.getReply().trim()))) {
					throw new GDSCNetHostInternalException("Unknown internal error, failed to retreive entire response");
				}
				retval.append(reply.getReply());
			}
			if (logger.isDebugEnabled()) {
				logger.debug("GETFILE " + filename);
			}
		} catch (ConnectException e) {
			logger.error(e.getMessage());
		} catch (GDSCException h) {
			logger.error(h.getMessage());
		} finally {
			if (con != null) {
				ConnectionManagerOgg.getInstance().releaseConnection(con);
			}
		}
		return retval.toString();
	}

	public void putFile(String filename, String filetext, boolean overwrite){
		filename = "param " + filename;
		Connection con = null;
		Response reply = null;
		try {
			if ((filename.toLowerCase().endsWith(".prm")) || (filename.toLowerCase().startsWith("params"))) {
				createBackup(filename);
			}
			con = ConnectionManagerOgg.getInstance().getConnection();
			reply = con.ggsciPutFile(filename, filetext, overwrite);
			if (reply.getStatus() == Response.CMD_STATUS_FILE_EXISTS) {
				throw new GDSCNetHostFileExistsException(reply.getError());
			}
			if (reply.getStatus() == Response.CMD_STATUS_FILE_ERROR) {
				throw new GDSCNetHostFileException(reply.getError());
			}
			if (reply.getStatus() == Response.CMD_STATUS_FILE_NODIR) {
				throw new GDSCNetHostFileNotFoundException(reply.getError());
			}
			if (reply.getStatus() == Response.CMD_STATUS_FILE_NOFILE) {
				throw new GDSCNetHostFileNotFoundException(reply.getError());
			}
			if (reply.getStatus() == Response.CMD_STATUS_FILE_NOGROUP) {
				throw new GDSCNetHostFileException(reply.getError());
			}
			if (reply.getStatus() == Response.CMD_STATUS_FILE_NOPERM) {
				throw new GDSCNetHostFileException(reply.getError());
			}
			if (reply.getStatus() != Response.CMD_STATUS_SUCCESS) {
				throw new GDSCNetHostInternalException(reply.getError());
			}
			if (logger.isDebugEnabled()) {
				logger.debug("PUTFILE " + filename);
			}
		} catch (ConnectException e) {
			logger.error(e.getMessage());
		} catch (GDSCException h) {
			logger.error(h.getMessage());
		} finally {
			if (con != null) {
				ConnectionManagerOgg.getInstance().releaseConnection(con);
			}
		}
	}

	private void createBackup(String fileName){
		Connection con = null;
		Response reply = null;
		StringBuffer text = new StringBuffer();
		text.append("-- Backup Auto-created: ");
		text.append(this.df.format(new Date()));
		text.append("\n\n");
		try {
			con = ConnectionManagerOgg.getInstance().getConnection();
			if (logger.isDebugEnabled()) {
				logger.debug("createBackup() calling: GETFILE" + fileName);
			}
			reply = con.ggsciGetFile(fileName);
			if (reply.getStatus() == Response.CMD_STATUS_SUCCESS) {
				text.append(reply.getReply());
				String backupName = null;
				int fileSuffix = 0;
				int status = Response.CMD_STATUS_FILE_EXISTS;
				while (status == Response.CMD_STATUS_FILE_EXISTS) {
					backupName = fileName + "." + fileSuffix;
					reply = con.ggsciPutFile(backupName, text.toString(), false);
					status = reply.getStatus();
					fileSuffix++;
				}
				if (reply.getStatus() != Response.CMD_STATUS_SUCCESS) {
					throw new GDSCNetHostFileException(reply.getError());
				}
				if (logger.isDebugEnabled()) {
					logger.debug("PUTFILE " + backupName);
				}
			} else if (reply.getStatus() == Response.CMD_STATUS_FILE_NOFILE) {
				if (logger.isDebugEnabled()) {
					logger.debug("createBackup() requested for new file, no backup required.");
				}
			} else {
				throw new GDSCNetHostFileException(reply.getError());
			}
		} catch (ConnectException e) {
			logger.error(e.getMessage());
		} catch (GDSCException ge) {
			logger.error(ge.getMessage());
		} finally {
			if (con != null) {
				ConnectionManagerOgg.getInstance().releaseConnection(con);
			}
		}
	}

//	public static void main(String[] args) {
//		ExcludeCompressedTable e = new ExcludeCompressedTable();
//		String a = "";
//		try {
//			a = e.getFile("param TJ_DMP");
//		} catch (GDSCException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		a = a + "\r\n" + "Tableexclude test.test1;";
//		try {
//			e.putFile("param TJ_DMP", a, true);
//		} catch (GDSCException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//	}
}
