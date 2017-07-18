
/**
 * This template file was generated by dynaTrace client.
 * The dynaTrace community portal can be found here: http://community.compuwareapm.com/
 * For information how to publish a plugin please visit http://community.compuwareapm.com/plugins/contribute/
 **/

package com.Dynatrace;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.JSONObject;

import com.dynatrace.diagnostics.pdk.ActionEnvironment;
import com.dynatrace.diagnostics.pdk.ActionV2;
import com.dynatrace.diagnostics.pdk.Incident;
import com.dynatrace.diagnostics.pdk.Status;
import com.dynatrace.diagnostics.pdk.Violation;

public class SlackChat implements ActionV2 {

	private static final Logger log = Logger.getLogger(SlackChat.class.getName());
	private Config confs = new Config();
	private Connection con = new Connection();
	private Map<String, Incident> dbMonitorIncident = new HashMap<String, Incident>();

	/**
	 * Initializes the Plugin. This method is called in the following cases:
	 * <ul>
	 * <li>before <tt>execute</tt> is called the first time for this scheduled
	 * Plugin</li>
	 * <li>before the next <tt>execute</tt> if <tt>teardown</tt> was called
	 * after the last execution</li>
	 * </ul>
	 * 
	 * <p>
	 * If the returned status is <tt>null</tt> or the status code is a
	 * non-success code then {@link #teardown(ActionEnvironment)} will be called
	 * next.
	 * 
	 * <p>
	 * Resources like sockets or files can be opened in this method. Resources
	 * like sockets or files can be opened in this method.
	 * 
	 * @param env
	 *            the configured <tt>ActionEnvironment</tt> for this Plugin
	 * @see #teardown(ActionEnvironment)
	 * @return a <tt>Status</tt> object that describes the result of the method
	 *         call
	 */
	@Override
	public Status setup(ActionEnvironment env) throws Exception {

		confs.setWebHookUrl(env.getConfigUrl("url"));
		confs.setChannel(env.getConfigString("channel"));
		confs.setNotifyAllChannel(env.getConfigBoolean("notifyAll"));
		confs.setLinkedDashboard(env.getConfigString("linkedDashboard"));

		// connection to SlackChat needs
		// to be started and finished at every message

		return new Status(Status.StatusCode.Success);
	}

	/**
	 * Executes the Action Plugin to process incidents.
	 * 
	 * <p>
	 * This method may be called at the scheduled intervals, but only if
	 * incidents occurred in the meantime. If the Plugin execution takes longer
	 * than the schedule interval, subsequent calls to
	 * {@link #execute(ActionEnvironment)} will be skipped until this method
	 * returns. After the execution duration exceeds the schedule timeout,
	 * {@link ActionEnvironment#isStopped()} will return <tt>true</tt>. In this
	 * case execution should be stopped as soon as possible. If the Plugin
	 * ignores {@link ActionEnvironment#isStopped()} or fails to stop execution
	 * in a reasonable timeframe, the execution thread will be stopped
	 * ungracefully which might lead to resource leaks!
	 * 
	 * @param env
	 *            a <tt>ActionEnvironment</tt> object that contains the Plugin
	 *            configuration and incidents
	 * @return a <tt>Status</tt> object that describes the result of the method
	 *         call
	 */
	@Override
	public Status execute(ActionEnvironment env) throws Exception {
		log.finer("Start Execute");

		// Map all incidents to a collection
		Collection<Incident> incidents = env.getIncidents();

		for (Incident incident : incidents) {
			log.fine(incident.getIncidentRule().getName().toString());
			log.fine(incident.getMessage());
			// the point here is to check for a DBMonitor plugin incident
			// when that's the case, the column value incident comes "repeated"
			// 3 times
			// the difference is very small btw the 3, and they are called by
			// the execute method
			// so the plugin does not know whether the next incident is related
			// to a DBMonitor or not
			// TODO: Get only the two last calls from DBMonitor incident
			// and put into one single message
			// The last two have the real column name and actual value. The 1st
			// one is generic
			if (isDbMonitorPlugin(incident.getMessage().toString())) {
				log.fine("DB Monitor");
				if (isDbMonitorIncidentRepeated(dbMonitorIncident, incident.getIncidentRule().getName().toString())) {
					log.finer("Do nothing");
					// do nothing
					// if it is a dbmonitor incident it should have been sent
					// before by the else below
				} else {
					log.fine("Store and Send DB Monitor incident");
					// store incident in case of the next incident
					// (being called by the execute() method again)
					// is the same DBMonitor incident name,
					// so we do not alert it again
					dbMonitorIncident.put(incident.getIncidentRule().getName().toString(), incident);
					prepareAndSendMessage(incident, true);
				}
			} else {
				log.fine("NOT DB Monitor");
				try {
					dbMonitorIncident.clear();
					prepareAndSendMessage(incident, false);
				} catch (Exception e) {
					log.severe("ERROR: " + e.toString());
					return new Status(Status.StatusCode.ErrorInternal);
				}
			}
		}
		return new Status(Status.StatusCode.Success);
	}

	/**
	 * Shuts the Plugin down and frees resources. This method is called in the
	 * following cases:
	 * <ul>
	 * <li>the <tt>setup</tt> method failed</li>
	 * <li>the Plugin configuration has changed</li>
	 * <li>the execution duration of the Plugin exceeded the schedule
	 * timeout</li>
	 * <li>the schedule associated with this Plugin was removed</li>
	 * </ul>
	 * <p>
	 * The Plugin methods <tt>setup</tt>, <tt>execute</tt> and <tt>teardown</tt>
	 * are called on different threads, but they are called sequentially. This
	 * means that the execution of these methods does not overlap, they are
	 * executed one after the other.
	 * 
	 * <p>
	 * Examples:
	 * <ul>
	 * <li><tt>setup</tt> (failed) -&gt; <tt>teardown</tt></li>
	 * <li><tt>execute</tt> starts, configuration changes, <tt>execute</tt> ends
	 * -&gt; <tt>teardown</tt><br>
	 * on next schedule interval: <tt>setup</tt> -&gt; <tt>execute</tt> ...</li>
	 * <li><tt>execute</tt> starts, execution duration timeout, <tt>execute</tt>
	 * stops -&gt; <tt>teardown</tt></li>
	 * <li><tt>execute</tt> starts, <tt>execute</tt> ends, schedule is removed
	 * -&gt; <tt>teardown</tt></li>
	 * </ul>
	 * Failed means that either an unhandled exception is thrown or the status
	 * returned by the method contains a non-success code.
	 * 
	 * <p>
	 * All by the Plugin allocated resources should be freed in this method.
	 * Examples are opened sockets or files.
	 * 
	 * @see #setup(ActionEnvironment)
	 */
	@Override
	public void teardown(ActionEnvironment env) throws Exception {
		log.finer("Teardown: Disconnecting");
		con.disconnect();
	}

	private ArrayList<String> patternFinder(String regex, String textToMatch) {
		Pattern p = null;
		Matcher m = null;
		ArrayList<String> results = new ArrayList<String>();

		log.fine("PATTERN: " + regex + "=" + textToMatch);
		p = Pattern.compile(regex);
		m = p.matcher(textToMatch);

		if (m.find()) {
			for (int i = 1; i <= m.groupCount(); i++) {
				log.fine(m.group(i).toString());
				results.add(m.group(i));
			}
			return results;
		}
		log.fine("null");
		return null;
	}

	private boolean isDbMonitorPlugin(String message) {
		if (message.contains("Query Monitor")) {
			return true;
		}
		// look for something like "...[queryName->columName]..."
		else if (patternFinder("\\[\\w+->\\w+\\]", message) != null) {
			return true;
		} else {
			return false;
		}
	}

	private boolean isDbMonitorIncidentRepeated(Map<String, Incident> dbMonitorIncident, String alertName) {
		if (dbMonitorIncident.containsKey(alertName)) {
			log.fine("Has the key: " + alertName);

			Incident incident = dbMonitorIncident.get(alertName);
			long timestampDiff = System.currentTimeMillis() - incident.getStartTime().getTimestampInMs();

			log.finer("timestamp incident: " + String.valueOf(incident.getStartTime().getTimestampInMs()));
			log.finer("timestamp Now: " + String.valueOf(System.currentTimeMillis()));
			log.finer("timestamp diff: " + String.valueOf(timestampDiff));
			// it may be the case that the same incident happens again without
			// being the repeated scenario
			if (timestampDiff <= 60000) {
				log.fine("Is less then 60s from this last alert");
				return true;
			}
		}
		return false;
	}

	public void prepareAndSendMessage(Incident incident, boolean isDbMonitor) throws Exception {
		// LOG INCIDENT MESSAGE
		String message = incident.getMessage();
		log.finer("INCIDENT: " + message);

		if (isDbMonitor) {
			ArrayList<String> dbMonitorMessage = new ArrayList<String>();
			// the regex replaces the whole message by
			// dbMonitorName@Host + Test (upper|lower) bound exceed
			// when a column value incident is triggered
			dbMonitorMessage = patternFinder("\\((.*)\\).\\w*(..*)", message);
			if (dbMonitorMessage != null) {
				log.fine("1st Pattern: " + dbMonitorMessage.toString());
				// we only need regex group 1 and 2
				message = dbMonitorMessage.get(0) + dbMonitorMessage.get(1);
			} else {
				dbMonitorMessage = patternFinder("\\(.*\\)", message);
				if (dbMonitorMessage != null) {
					log.fine("2nd Pattern: " + dbMonitorMessage.toString());
					message = dbMonitorMessage.get(0);
				}
			}
		}

		// SET INPUT FIELDS
		URL url = confs.getWebHookUrl();
		String dashboard = confs.getLinkedDashboard();

		// JSON CREATION
		JSONObject jsonObj = new JSONObject();
		String state = "";
		// Compose string chat_message => This message will be sent to the
		// SlackChat channel
		if (confs.isNotifyAllChannel()) {
			state = "<!channel> ";
		}
		if (incident.isOpen()) {
			state = state + "dynatrace incident triggered:";
		} else if (incident.isClosed()) {
			state = state + "dynatrace incident ended:";
		}
		String title = incident.getIncidentRule().getName();
		String chat_message = "";
		// chat_message = chat_message + " <ul>";
		// chat_message = chat_message + "Incident UUID: " +
		// incident.getKey().getUUID() + "\n";

		chat_message = chat_message + "Incident start: " + incident.getStartTime() + "\n";
		chat_message = chat_message + "Incident end: " + incident.getEndTime() + "\n";

		// chat_message = chat_message + "<li><strong>Status state
		// code:</strong> " + incident.getState() + "</li>";

		if (isDbMonitor) {
			chat_message += "DB Monitor@Host: " + message + "\n";
		} else {
			chat_message += "Message: " + message + "\n";
		}

		for (Violation violation : incident.getViolations()) {
			// TODO: Get the actual incident triggered value and compare with the threshold
			chat_message = chat_message + "Violated Measure: " + violation.getViolatedMeasure().getName()
					+ " - Threshold: " + violation.getViolatedThreshold().getValue() + "\n";
		}

		con.setConnection(confs.getChannel(), confs.getWebHookUrl());
		con.sendMessage(con, confs, title, state, chat_message, incident.getSeverity().toString());
	}
}
