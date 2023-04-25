package ascob.impl.tools.rundeck;

import java.util.Map;

public class SubmitJobRequest {

	
	String argString;
	LogLevel logLevel = LogLevel.INFO;
	String filter;	
	public String getArgString() {
		return argString;
	}
	public void setArgString(String argString) {
		this.argString = argString;
	}
	public LogLevel getLogLevel() {
		return logLevel;
	}
	public void setLogLevel(LogLevel logLevel) {
		this.logLevel = logLevel;
	}
	public String getFilter() {
		return filter;
	}
	public void setFilter(String filter) {
		this.filter = filter;
	}
	public Map<String, String> getOptions() {
		return options;
	}
	public void setOptions(Map<String, String> options) {
		this.options = options;
	}
	Map<String,String> options;
}
