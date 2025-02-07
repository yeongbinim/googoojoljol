package googoo.joljol.common;

import static org.hibernate.type.StandardBasicTypes.*;

import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.boot.model.FunctionContributor;


public class CustomFunctionContributor implements FunctionContributor {
	private static final String FUNCTION_NAME = "match_against";
	private static final String FUNCTION_PATTERN = "MATCH (?1) AGAINST (?2 in boolean mode)";

	@Override
	public void contributeFunctions(FunctionContributions functionContributions) {
		functionContributions.getFunctionRegistry()
			.registerPattern(FUNCTION_NAME, FUNCTION_PATTERN,
				functionContributions.getTypeConfiguration().getBasicTypeRegistry()
					.resolve(DOUBLE)
			);
	}
}
