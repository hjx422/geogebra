package geogebra.common.kernel.commands;

import geogebra.common.kernel.Kernel;
import geogebra.common.kernel.arithmetic.NumberValue;
import geogebra.common.kernel.geos.GeoElement;

public class CmdDivisorsOrDivisorsSum extends CmdOneNumber {

	private boolean sum;

	public CmdDivisorsOrDivisorsSum(Kernel kernel,boolean sum) {
		super(kernel);
		this.sum = sum;
	}

	@Override
	protected GeoElement getResult(NumberValue num,String label){
		return kernelA.DivisorsOrDivisorsSum(label, num,sum);
	}

}
