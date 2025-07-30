/*******************************************************************************
 * Copyright (c) 2000, 2021 IBM Corporation and others. All rights reserved.
 * The contents of this file are made available under the terms
 * of the GNU Lesser General Public License (LGPL) Version 2.1 that
 * accompanies this distribution (lgpl-v21.txt).  The LGPL is also
 * available at http://www.gnu.org/licenses/lgpl.html.  If the version
 * of the LGPL at http://www.gnu.org is different to the version of
 * the LGPL accompanying this distribution and there is any conflict
 * between the two license versions, the terms of the LGPL accompanying
 * this distribution shall govern.
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.swt.internal.gtk3;

import org.eclipse.swt.internal.gtk.*;
public class GdkGeometry implements GeometryInterface{
	public int min_width;
	public int min_height;
	public int max_width;
	public int max_height;
	public int base_width;
	public int base_height;
	public int width_inc;
	public int height_inc;
	public double min_aspect;
	public double max_aspect;
	public int win_gravity;
	public static final int sizeof = GTK3.GdkGeometry_sizeof();

	@Override
	public int getMinWidth() {
		return min_width;
	}
	@Override
	public int getMinHeight() {
		return min_height;
	}
	@Override
	public int getMaxWidth() {
		return max_width;
	}
	@Override
	public int getMaxHeight() {
		return max_height;
	}
	@Override
	public boolean getResize() {
		return false;
	}
	@Override
	public int getRequestedWidth() {
		return 0;
	}
	@Override
	public int getRequestedHeight() {
		return 0;
	}
	@Override
	public void setMinWidth(int value) {
		min_width = value;
	}
	@Override
	public void setMinHeight(int value) {
		min_height = value;
	}
	@Override
	public void setMaxWidth(int value) {
		max_width = value;
	}
	@Override
	public void setMaxHeight(int value) {
		max_height = value;
	}
	@Override
	public void setResize(boolean value) {
	}
	@Override
	public void setRequestedWidth(int value) {
	}
	@Override
	public void setRequestedHeight(int value) {
	}
	@Override
	public boolean getMinSizeRequested() {
		return false;
	}
	@Override
	public void setMinSizeRequested(boolean value) {
	}
}
