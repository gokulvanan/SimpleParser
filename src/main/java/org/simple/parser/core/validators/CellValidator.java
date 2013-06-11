package org.simple.parser.core.validators;

import org.simple.parser.core.annotations.ColumnDef;

/**
 * Custom Validator classes should implement this interface, in order to be
 *  used in {@link ColumnDef} annotation to specify the type of formating for each column field.
 * @author gokulvanan
 *
 */
public interface CellValidator {
	
	/**
	 * If valid return null else return error message String
	 * @return
	 */
	public String valid(Object data);

}
