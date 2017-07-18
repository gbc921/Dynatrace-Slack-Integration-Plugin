package com.Dynatrace;

import java.net.MalformedURLException;
import java.net.URL;

public class Config {
	private URL webHookUrl;
	private boolean notifyAllChannel;
	private boolean checkForDBMonitorPlugin;
	private boolean printAlertDescription;
	private boolean printAlertMessage;
	private String linkedDashboard;
	private String channel;
	private int connectionTimeout = 5000;
	private int readTimeout = 20000;

	public URL getWebHookUrl() {
		return webHookUrl;
	}

	public void setWebHookUrl(URL webHookUrl) throws MalformedURLException {
		this.webHookUrl = webHookUrl;
	}

	public boolean isNotifyAllChannel() {
		return notifyAllChannel;
	}

	public boolean isCheckForDBMonitorPlugin() {
		return checkForDBMonitorPlugin;
	}

	public void setCheckForDBMonitorPlugin(boolean checkForDBMonitorPlugin) {
		this.checkForDBMonitorPlugin = checkForDBMonitorPlugin;
	}

	public boolean isPrintAlertDescription() {
		return printAlertDescription;
	}

	public boolean isPrintAlertMessage() {
		return printAlertMessage;
	}

	public void setPrintAlertMessage(boolean printAlertMessage) {
		this.printAlertMessage = printAlertMessage;
	}

	public void setPrintAlertDescription(boolean printAlertDescription) {
		this.printAlertDescription = printAlertDescription;
	}

	public void setNotifyAllChannel(boolean notifyAllChannel) {
		this.notifyAllChannel = notifyAllChannel;
	}

	public String getLinkedDashboard() {
		return linkedDashboard;
	}

	public void setLinkedDashboard(String linkedDashboard) {
		this.linkedDashboard = linkedDashboard;
	}

	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	public int getReadTimeout() {
		return readTimeout;
	}

	public void setReadTimeout(int readTimeout) {
		this.readTimeout = readTimeout;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

}
