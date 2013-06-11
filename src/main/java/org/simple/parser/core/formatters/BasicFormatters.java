package org.simple.parser.core.formatters;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public interface BasicFormatters {

	public static class ToUpperCase implements CellFormatter{

		public Object format(Object data) throws Exception{
			String input = (data == null) ? null : data.toString();
			if(input != null && input.trim().length() > 0)
				return input.toUpperCase();
			return input;
		}
	}

	/**
	 * Sample Date formatter converts DD-MM-YYYY to YYYY-MM-DD.
	 * User has to timplement his own custom formatter for other format chagnes
	 * @author gokulvanan
	 *
	 */
	public static class FormatDate implements CellFormatter{
		public static String clientFormat = "DD-MM-YYYY";
		public static String serverFormat = "YYYY-MM-DD";
		public Object format(Object data) throws Exception {
			String input = (data == null) ? null : data.toString();
			Date d = new SimpleDateFormat(clientFormat).parse(input);
			return new SimpleDateFormat(serverFormat).format(d);
		}
	}

}
