package org.burningwave.core;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import org.burningwave.core.ManagedLogger.Repository;

public class SimpleManagedLoggerRepository extends Repository.Abst {
	private Map<Class<?>, Boolean> loggers;
	
	public SimpleManagedLoggerRepository() {
		super();
		loggers = new HashMap<>();
	}
	
	private Boolean getLoggerEnabledFlag(Class<?> client) {
		if (!isEnabled || namesOfClassesForWhichLoggingIsDisabled.contains(client.getName())) {
			return !isEnabled;
		}
		Boolean loggerEnabledFlag = loggers.get(client);
		if (loggerEnabledFlag == null) {
			synchronized (getId(loggers, client)) {
				loggerEnabledFlag = loggers.get(client);
				if (loggerEnabledFlag == null) {
					loggers.put(client, loggerEnabledFlag = Boolean.TRUE);
				}
			}
		}
		return loggerEnabledFlag;
	}
	
	private void setLoggerEnabledFlag(Class<?> client, Boolean flag) {
		loggers.put(client, flag);
	}

	private void log(Class<?> client, PrintStream printStream, String text, Throwable exception) {
		if (getLoggerEnabledFlag(client)) {
			if (exception == null) {
				printStream.println(client.getName() + " - " + text);
			} else {
				printStream.println(client.getName() + " - " + text);
				exception.printStackTrace(printStream);
			}
		}
	}
	
	public void disableLogging(Class<?> client) {
		setLoggerEnabledFlag(client, false);
	}
	
	public void enableLogging(Class<?> client) {
		super.enableLogging(client);
		setLoggerEnabledFlag(client, true);
	}
	
	public void logError(Class<?> client, String message, Throwable exc) {
		log(client, System.err, message, exc);
	}

	public void logError(Class<?> client, String message) {
		log(client, System.err, message, null);
	}
	
	public void logDebug(Class<?> client, String message) {
		log(client, System.out, message, null);
	}
	
	public void logDebug(Class<?> client, String message, Object... arguments) {
		message = replacePlaceHolder(message, arguments);
		log(client, System.out, message, null);
	}
	
	public void logInfo(Class<?> client, String message) {
		log(client, System.out, message, null);
	}
	
	public void logInfo(Class<?> client, String message, Object... arguments) {
		message = replacePlaceHolder(message, arguments);
		log(client, System.out, message, null);
	}
	
	public void logWarn(Class<?> client, String message) {
		log(client, System.out, message, null);
	}
	
	public void logWarn(Class<?> client, String message, Object... arguments) {
		message = replacePlaceHolder(message, arguments);
		log(client, System.out, message, null);
	}
	
	private String replacePlaceHolder(String message, Object... arguments) {
		for (Object obj : arguments) {
			message = message.replaceFirst("\\{\\}", clear(obj.toString()));
		}
		return message;
	}
	
	private static String clear(String text) {
		return text
		.replace("\\", "\\")
		.replace("{", "\\{")
		.replace("}", "\\}")
		.replace("(", "\\(")
		.replace(")", "\\)")
		.replace(".", "\\.")
		.replace("$", "\\$");
	}
	
}
