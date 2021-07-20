package org.geogebra.common.gui.view.algebra;

import org.geogebra.common.kernel.StringTemplate;
import org.geogebra.common.kernel.geos.BaseSymbolicTest;
import org.geogebra.common.kernel.geos.GeoElement;
import org.junit.Test;

public class SuggestionSolveForSymbolicTest extends BaseSymbolicTest {

	@Test
	public void testMultiVariableSolveOnFirstEquation() {
		GeoElement first = add("x + y = 0");
		add("y + 1 = 0");
		SuggestionSolveForSymbolic.get(first).execute(first);
		String solveOutput = first.getKernel().getConstruction().getLastGeoElement()
				.toOutputValueString(StringTemplate.algebraTemplate);
		assertThatEquals(solveOutput, "{{y = -1, x = 1}}", "{{x = 1, y = -1}}");
	}
}