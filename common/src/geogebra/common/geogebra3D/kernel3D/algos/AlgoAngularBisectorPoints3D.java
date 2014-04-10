/* 
GeoGebra - Dynamic Mathematics for Everyone
http://www.geogebra.org

This file is part of GeoGebra.

This program is free software; you can redistribute it and/or modify it 
under the terms of the GNU General Public License as published by 
the Free Software Foundation.

*/

/*
 * AlgoAngularBisector.java
 *
 * Created on 26. Oktober 2001
 */

package geogebra.common.geogebra3D.kernel3D.algos;

import geogebra.common.euclidian.EuclidianConstants;
import geogebra.common.geogebra3D.kernel3D.geos.GeoLine3D;
import geogebra.common.kernel.Construction;
import geogebra.common.kernel.StringTemplate;
import geogebra.common.kernel.Matrix.Coords;
import geogebra.common.kernel.commands.Commands;
import geogebra.common.kernel.geos.GeoElement;
import geogebra.common.kernel.kernelND.GeoDirectionND;
import geogebra.common.kernel.kernelND.GeoPointND;


/**
 *
 * @author  Markus
 * @version 
 */
public class AlgoAngularBisectorPoints3D extends AlgoElement3D {

    protected GeoPointND A, B, C; // input    
    protected GeoLine3D bisector; // output   


    /** Creates new AlgoLineBisector 
     * @param cons 
     * @param label 
     * @param A 
     * @param B 
     * @param C */
    public AlgoAngularBisectorPoints3D(
            Construction cons,
            String label,
            GeoPointND A,
            GeoPointND B,
            GeoPointND C) {

    	this(cons, label, A, B, C, null);
    }
    
    /**
     * @param cons
     * @param label
     * @param A
     * @param B
     * @param C
     * @param orientation
     */
    protected AlgoAngularBisectorPoints3D(
        Construction cons,
        String label,
        GeoPointND A,
        GeoPointND B,
        GeoPointND C, 
        GeoDirectionND orientation) {
        super(cons);
        this.A = A;
        this.B = B;
        this.C = C;
        bisector = new GeoLine3D(cons);
        bisector.setStartPoint(B);
        setInput(orientation); // for AlgoElement
        setOutput(); // for AlgoElement


        // compute bisector of angle(A, B, C)
        compute();
        bisector.setLabel(label);
    }
    

    @Override
	public Commands getClassName() {
        return Commands.AngularBisector;
    }

    @Override
	public int getRelatedModeID() {
    	return EuclidianConstants.MODE_ANGULAR_BISECTOR;
    }
    
    // for AlgoElement
    /**
     * set input
     * @param orientation orientation
     */
	protected void setInput(GeoDirectionND orientation) {
        input = new GeoElement[3];
        input[0] = (GeoElement) A;
        input[1] = (GeoElement) B;
        input[2] = (GeoElement) C;
	}
	
	/**
	 * set output
	 */
	private void setOutput(){

        setOutputLength(1);
        setOutput(0,bisector);
        setDependencies(); // done by AlgoElement
    }

    public GeoLine3D getLine() {
        return bisector;
    }
    // Made public for LocusEqu
    public GeoPointND getA() {
        return A;
    }
    // Made public for LocusEqu
    public GeoPointND getB() {
        return B;
    }
    // Made public for LocusEqu
    public GeoPointND getC() {
        return C;
    }

    @Override
	public final void compute() {
        boolean infiniteB = B.isInfinite();

 

        // set direction vector of bisector: (wx, wy)       
        double wx, wy;
        if (infiniteB) {
            // if B is at infinity then use it for direction
        	// and midpoint(A,B) for start point       	
        	Coords o = A.getInhomCoordsInD(3).add(C.getInhomCoordsInD(3)).mul(0.5);
            Coords d = B.getCoordsInD(3);
            bisector.setCoord(o, d);
        }
        // standard case: B is not at infinity            
        else {
        	Coords o = B.getInhomCoordsInD(3);
        	Coords v1 = A.getInhomCoordsInD(3).sub(o);
        	v1.normalize();
        	Coords v2 = C.getInhomCoordsInD(3).sub(o);
        	v2.normalize();
        	Coords d = v1.add(v2);
        	setCoordFromFiniteB(o, d, v1);
        } 
    }
    
    /**
     * set bisector coords when B is finite
     * @param o origin
     * @param d direction
     * @param v1 direction BA
     */
    protected void setCoordFromFiniteB(Coords o, Coords d, Coords v1){
    	bisector.setCoord(o, d);
    }

    @Override
	public String toString(StringTemplate tpl) {
        // Michael Borcherds 2008-03-30
        // simplified to allow better Chinese translation
        return loc.getPlain("AngleBisectorOfABC",A.getLabel(tpl),B.getLabel(tpl),C.getLabel(tpl));

    }

    /*
	@Override
	public boolean isLocusEquable() {
		return true;
	}
	
	public EquationElementInterface buildEquationElementForGeo(GeoElement geo, EquationScopeInterface scope) {
		return LocusEquation.eqnAngularBisectorPoints(geo, this, scope);
	}
	*/
}
