package org.simple.parser.core.validators;

import org.simple.parser.core.annotations.ColumnDef;


/**
 * Default validator of {@link ColumnDef} annotation .. 
 * validation is absent by default
 * @author gokulvanan
 *
 */
public class NoValidaion implements CellValidator {

	public String valid(Object data) {
		return null;
	}

}
