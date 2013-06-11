package org.simple.parser.core.annotations;

import org.simple.parser.core.formatters.CellFormatter;
import org.simple.parser.core.formatters.NoFormat;
import org.simple.parser.core.validators.CellValidator;
import org.simple.parser.core.validators.NoValidaion;


/**
 * Annotation used to map attributes of a java objects to columns read from a file 
 * index is a mandatory attribute while using this annoation.
 * By default validators and formatters are None
 * <br>Attributes:
 * index - location value from 1 to N -- location of colum on file
 * validators - array of classes that implement {@link CellValidator} used to validate cell contents
 * formatters - array of classes that implement {@ link CellFormatters} used to format cell contents to different datattypes
 * or in lable -value mapping
 * @author gokulvanan
 *
 */
@java.lang.annotation.Target(value={java.lang.annotation.ElementType.FIELD})
@java.lang.annotation.Retention(value=java.lang.annotation.RetentionPolicy.RUNTIME)
public @interface ColumnDef {

	public int  index();
	public Class<? extends CellValidator>[] validators() 	default {NoValidaion.class};
	public Class<? extends CellFormatter> formatter() 		default NoFormat.class;
	public Class<? extends CellFormatter> writeFormatter() 	default NoFormat.class;
	public boolean unique() default false;
}
