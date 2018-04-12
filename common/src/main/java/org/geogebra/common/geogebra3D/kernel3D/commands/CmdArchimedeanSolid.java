package org.geogebra.common.geogebra3D.kernel3D.commands;

import org.geogebra.common.kernel.Kernel;
import org.geogebra.common.kernel.arithmetic.Command;
import org.geogebra.common.kernel.commands.CommandProcessor;
import org.geogebra.common.kernel.commands.Commands;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.kernelND.GeoDirectionND;
import org.geogebra.common.kernel.kernelND.GeoPointND;
import org.geogebra.common.main.MyError;

/**
 * Cube[ &lt;GeoPoint3D>, &lt;GeoPoint3D>, &lt;GeoDirectionND> ]
 * 
 * or Icosahedron etc.
 */
public class CmdArchimedeanSolid extends CommandProcessor {

	private Commands name;

	/**
	 * @param kernel
	 *            Kernel
	 * @param name
	 *            command name
	 */
	public CmdArchimedeanSolid(Kernel kernel, Commands name) {
		super(kernel);
		this.name = name;

	}

	@Override
	public GeoElement[] process(Command c) throws MyError {

		int n = c.getArgumentNumber();
		boolean[] ok = new boolean[n];
		GeoElement[] arg;

		switch (n) {
		case 2:
			arg = resArgs(c);
			if ((ok[0] = arg[0].isGeoPoint())
					&& (ok[1] = arg[1].isGeoPoint())) {

				// GeoElement[] ret =
				// kernelA.getManager3D().ArchimedeanSolid(c.getLabels(),
				// (GeoPointND) arg[0], (GeoPointND) arg[1],
				// kernelA.getXOYPlane(),
				// name) ;

				GeoElement[] ret = kernel.getManager3D().ArchimedeanSolid(
						c.getLabels(), (GeoPointND) arg[0], (GeoPointND) arg[1],
						name);
				return ret;

			}
			for (int i = 0; i < 2; i++) {
				if (!ok[i]) {
					throw argErr(app, c, arg[i]);
				}
			}
			break;
		case 3:
			arg = resArgs(c);
			if ((ok[0] = arg[0].isGeoPoint())
					&& (ok[1] = arg[1].isGeoPoint())) {

				if (arg[2] instanceof GeoDirectionND) {

					GeoElement[] ret = kernel.getManager3D().ArchimedeanSolid(
							c.getLabels(), (GeoPointND) arg[0],
							(GeoPointND) arg[1], (GeoDirectionND) arg[2], name);
					return ret;

				}

				if (arg[2] instanceof GeoPointND) {

					GeoElement[] ret = kernel.getManager3D().ArchimedeanSolid(
							c.getLabels(), (GeoPointND) arg[0],
							(GeoPointND) arg[1], (GeoPointND) arg[2], name);
					return ret;

				}

				ok[2] = false;
			}

			for (int i = 0; i < 3; i++) {
				if (!ok[i]) {
					throw argErr(app, c, arg[i]);
				}
			}
			break;
		}

		throw argNumErr(c);

	}

}
