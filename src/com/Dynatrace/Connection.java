package com.Dynatrace;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Connection {
	private static final Logger log = Logger.getLogger(SlackChat.class.getName());

	private HttpURLConnection con;

	public HttpURLConnection setConnection(String channel, URL url) throws IOException {
		// open url connection and set timeouts - uses connection method 'post'
		con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("POST");
		con.setConnectTimeout(5000);
		con.setReadTimeout(20000);

		return con;

	}

	public void disconnect() {
		con.disconnect();
	}

	@SuppressWarnings("unchecked") // jsonObject
	public void sendMessage(Connection con, Config config, String title, String state, String msg,
			String severity) throws UnsupportedEncodingException, IOException {

		JSONObject attachment = new JSONObject();
		attachment.put("title", title);
		attachment.put("color", getSeverityColor(severity));
		attachment.put("text", msg);

		// if (!(serverName == null || serverName.equals("") ||
		// serverName.isEmpty())) {
		// attachment.put("title_link", "http://" + serverName +
		// "/rest/management/reports/create/"
		// + URLEncoder.encode(dashboardName, "UTF-8").replaceAll("\\+",
		// "%20"));
		// }

		JSONArray attachArray = new JSONArray();
		attachArray.add(attachment);

		JSONObject jsonObj = new JSONObject();
		jsonObj.put("username", "dynatrace");
		jsonObj.put("icon_url", "https://media.glassdoor.com/sqll/309684/dynatrace-squarelogo-1458744847928.png");
		jsonObj.put("channel", config.getChannel());
		jsonObj.put("text", state);
		jsonObj.put("attachments", attachArray);

		sendMessage(jsonObj);

	}

	private void sendMessage(JSONObject jsonObj) throws IOException {
		OutputStream out = null;
		InputStream in = null;
		int responseCode = 0;
		String responseBody = "";

		// json to string
		String jsonString = jsonObj.toJSONString();
		log.finer("JSONString: " + jsonString);
		// json string to bytes
		byte[] payload = jsonString.getBytes();

		con.setFixedLengthStreamingMode(payload.length);
		con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
		con.setDoOutput(true);

//		// TRY TO GET OUTPUT STREAM
//		try {
			out = con.getOutputStream();
//		}
//
//		// CATCH EXCEPTION, LOG IT THEN SEND RESPONSE ERROR CODE
//		catch (IOException e) {
//			log.severe("Exception thrown whilst getting output stream...");
//			log.severe(e.toString());
//			con.disconnect();
//		}
//
//		// TRY TO SEND PAYLOAD
//		try {
			out.write(payload);
			out.close();
//		}
//
//		// CATCH EXCEPTION, LOG IT THEN SEND RESPONSE ERROR CODE
//		catch (IOException e) {
//			log.severe("Exception thrown whilst writing to output stream...");
//			log.severe(e.toString());
//			con.disconnect();
//		}
//
//		// LOG PROGRESS
//		log.fine("Trying to connect...");
//
//		// TRY TO GET RESPONSE CODE
//		try {
			responseCode = con.getResponseCode();
//			log.fine("Response Code : " + responseCode);
//		}
//
//		// CATCH EXCEPTION, LOG IT THEN SEND RESPONSE ERROR CODE
//		catch (IOException e) {
//			log.severe("Exception thrown whilst writing to output stream...");
//			log.severe(e.toString());
//			con.disconnect();
//		} finally {
//			log.info("Finally Disconnecting");
//			con.disconnect();
//		}
			con.disconnect();
	}

	private String getSeverityColor(String severity) {
		String color = "good";
		switch (severity) {
		case "Error":
			color = "danger";
			break;
		case "Warning":
			color = "warning";
			break;
		default:
			color = "good";
		}
		return color;
	}

}
