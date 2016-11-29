package org.geogebra.web.html5.awt;

import org.geogebra.common.awt.GColorN;

public class GColorW extends GColorN {

	private int r;
	private int g;
	private int b;
	private int alpha;

	private static final double FACTOR = 0.7;

	public GColorW(GColorW col) {
		setRed(col.getRed());
		setGreen(col.getGreen());
		setBlue(col.getBlue());
		setAlpha(col.getAlpha());
	}

	public GColorW() {
		setRed(0);
		setGreen(0);
		setBlue(0);
		setAlpha(255);
	}

	public GColorW(int r, int g, int b) {
		setRed(r);
		setGreen(g);
		setBlue(b);
		setAlpha(255);
	}

	public GColorW(int rgb) {
		int b = rgb % 256;
		int g = (rgb / 256) % 256;
		int r = ((rgb / 256) / 256) % 256;
		setRed(r);
		setGreen(g);
		setBlue(b);
		setAlpha(255);
	}

	public GColorW(int r, int g, int b, int alpha) {
		setRed(r);
		setGreen(g);
		setBlue(b);
		setAlpha(alpha);
	}

	public GColorW(float r, float g, float b, float alpha) {
		if (r > 1) {
			r = 1;
		} else if (r < 0) {
			r = 0;
		}

		if (g > 1) {
			g = 1;
		} else if (g < 0) {
			g = 0;
		}

		if (b > 1) {
			b = 1;
		} else if (b < 0) {
			b = 0;
		}

		if (alpha < 0 || alpha > 1) {
			alpha = 1;
		}

		setRed((int) (r * 255));
		setGreen((int) (g * 255));
		setBlue((int) (b * 255));
		setAlpha((int) (alpha * 255));

	}

	public GColorW(float red, float green, float blue) {
		this(red, green, blue, 1);
	}

	public void setRed(int r) {
		this.r = r;
	}

	@Override
	public int getRed() {
		return r;
	}

	public void setGreen(int g) {
		this.g = g;
	}

	@Override
	public int getGreen() {
		return g;
	}

	public void setBlue(int b) {
		this.b = b;
	}

	@Override
	public int getBlue() {
		return b;
	}

	public void setAlpha(int alpha2) {
		this.alpha = alpha2;
	}

	@Override
	public int getAlpha() {
		return alpha;
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof GColorW)) {
			return false;
		}
		GColorW other = (GColorW) object;
		return other.r == this.r && other.g == this.g && other.b == this.b
		        && other.alpha == this.alpha;
	}

	@Override
	public int hashCode() {
		return ((((getRed() * 256) + getGreen()) * 256) + getBlue()) * 256
				+ getAlpha();
	}

	// public int getRGBOnly() {
	// return (((getRed() * 256) + getGreen()) * 256) + getBlue();
	// }

	// @Override
	// public GColorW darker() {
	// return new GColorW(Math.max((int) (getRed() * FACTOR), 0), Math.max(
	// (int) (getGreen() * FACTOR), 0), Math.max(
	// (int) (getBlue() * FACTOR), 0));
	// }
	//
	// @Override
	// public GColor brighter() {
	// return new GColorW(Math.min((int) (getRed() / FACTOR), 255), Math.min(
	// (int) (getGreen() / FACTOR), 255), Math.min(
	// (int) (getBlue() / FACTOR), 255));
	// }

	// this could have been in GColor,
	// but is its default toString needed somewhere?
	// @Override
//	public String toString() {
//		return getColorString(this);
//	}
}
