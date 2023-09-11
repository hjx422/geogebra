package org.geogebra.common.properties.impl.graphics;

import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.main.Localization;
import org.geogebra.common.main.settings.EuclidianSettings;
import org.geogebra.common.properties.IconsEnumeratedProperty;
import org.geogebra.common.properties.PropertyResource;
import org.geogebra.common.properties.impl.AbstractNamedEnumeratedProperty;

/**
 * This property controls the style of the grid.
 */
public class GridStyleProperty extends AbstractNamedEnumeratedProperty<Integer>
		implements IconsEnumeratedProperty<Integer> {

	private EuclidianSettings euclidianSettings;

	private PropertyResource[] icons = new PropertyResource[]{
			PropertyResource.ICON_CARTESIAN,
			PropertyResource.ICON_CARTESIAN_MINOR, PropertyResource.ICON_POLAR,
			PropertyResource.ICON_ISOMETRIC};

	/**
	 * Controls a grid style property.
	 * @param localization localization for the title
	 * @param euclidianSettings euclidian settings.
	 */
	public GridStyleProperty(Localization localization, EuclidianSettings euclidianSettings) {
		super(localization, "GridType");
		this.euclidianSettings = euclidianSettings;
		setValues(EuclidianView.GRID_CARTESIAN, EuclidianView.GRID_CARTESIAN_WITH_SUBGRID,
				EuclidianView.GRID_POLAR, EuclidianView.GRID_ISOMETRIC);
		setValueNames("Grid.Major", "Grid.MajorAndMinor", "Polar", "Isometric");
	}

	@Override
	public Integer getValue() {
		return euclidianSettings.getGridType();
	}

	@Override
	protected void doSetValue(Integer value) {
		euclidianSettings.setGridType(value);
	}

	@Override
	public boolean isEnabled() {
		return euclidianSettings.getShowGrid();
	}

	@Override
	public PropertyResource[] getValueIcons() {
		return icons;
	}
}
