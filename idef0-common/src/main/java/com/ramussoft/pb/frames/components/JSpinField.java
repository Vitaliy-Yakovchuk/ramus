/*
 *  JSpinField.java  - A spin field using a JSpinner (JDK 1.4)
 *  Copyright (C) 2004 Kai Toedter
 *  kai@toedter.com
 *  www.toedter.com
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package com.ramussoft.pb.frames.components;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public class JSpinField extends JSpinner {

    private final SpinnerNumberModel model;

    public JSpinField() {
        super();
        model = new SpinnerNumberModel(0, Integer.MIN_VALUE, Integer.MAX_VALUE,
                1);
        setModel(model);
    }

    public JSpinField(final int min, final int max) {
        super();
        model = new SpinnerNumberModel(min, min, max, 1);
        setModel(model);
    }

    public void setMinimum(final int minimum) {
        model.setMinimum(minimum);
    }

    public void setMaximum(final int maximum) {
        model.setMaximum(maximum);
    }
}