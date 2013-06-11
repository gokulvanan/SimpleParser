package org.simple.parser.core.formatters;

import org.simple.parser.core.annotations.ColumnDef;

/**
 * Custom Formatter classes should implement this interface, in order to be
 *  used in {@link ColumnDef} annotation to specify the type of formating for each column field.
 * @author gokulvanan
 *
 */
public interface CellFormatter	{
	
	public Object format(Object input) throws Exception;

}
