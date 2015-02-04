package com.axen.launcher.system;
/**
 * 信息类
 * @author lanhuanze
 *
 */
public interface MessageListener {
	/**
	 * 短信、彩信通知
	 * @param smsNumber 一共有的短信条数。
	 * @param newSmsNumber 一共有的新短信条数
	 */
	public void smsMessageChanged(int smsNumber, int newSmsNumber);
	
	/**
	 * 彩信cdtd
	 * @param mmsNumber 一共有的彩信条数
	 * @param newMmsNumber 新采集条数
	 */
	public void mmsMessageChanged(int mmsNumber, int newMmsNumber);
}
