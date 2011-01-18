package org.scale7.utility;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionsHelper {
	/**
	 * Format an exception stack trace so that it may be entered into a log file and
	 * parsed out later.
	 * @param ex The exception
	 * @return A string representing the exception stack trace with start and stop markers
	 */
	public static String stackTraceToString(Exception ex) {
		StringWriter sw = new StringWriter();
	    PrintWriter pw = new PrintWriter(sw);
	    ex.printStackTrace(pw);
	    return sw.toString();
	}

	public static String stackTraceForLog(Exception ex) {
		StringWriter sw = new StringWriter();
	    PrintWriter pw = new PrintWriter(sw);
	    pw.print("\n__exception_stack_trace_start\n");
	    ex.printStackTrace(pw);
	    pw.print("__exception_stack_trace_stop");
	    return sw.toString();
	}
}
