package org.simple.parser.core.formatters;

import org.simple.parser.core.annotations.ColumnDef;

/**
 * Default formatter in {@link ColumnDef} .. Does no formatting to output
 * @author gokulvanan
 *  
 */
public class NoFormat implements CellFormatter{

	public Object format(Object input) {
		return input;
	}

}
