/*******************************************************************************
 * Copyright (c) 2012, 2020 Original authors and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package dev.equo.nattable;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Object representation of a row in the table
 */
public class SimplePerson {
    private int id;
    private String name;
    private Date birthDate;

    public SimplePerson(int id, String name, Date birthDate) {
        this.id = id;
        this.name = name;
        this.birthDate = birthDate;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public Date getBirthDate() {
        return this.birthDate;
    }

    public static List<SimplePerson> getList() {
        return Arrays.asList(
                new SimplePerson(100, "Mickey Mouse", new Date(1000000)),
                new SimplePerson(110, "Batman", new Date(2000000)),
                new SimplePerson(120, "Bender", new Date(3000000)),
                new SimplePerson(130, "Cartman", new Date(4000000)),
                new SimplePerson(140, "Dogbert", new Date(5000000)));
    }
}
