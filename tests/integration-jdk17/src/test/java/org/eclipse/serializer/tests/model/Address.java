package org.eclipse.serializer.tests.model;

/*-
 * #%L
 * integration
 * %%
 * Copyright (C) 2019 - 2023 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import java.util.StringJoiner;

public class Address
{

    private long id;
    private String street;
    private String city;
    private String postalCode;

    public Address()
    {
        // Used by the RestructuringTest, not needed for standard deserialization.
    }

    public Address(long id, String street, String city, String postalCode)
    {
        this.id = id;
        this.street = street;
        this.city = city;
        this.postalCode = postalCode;
    }

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public String getStreet()
    {
        return street;
    }

    public void setStreet(String street)
    {
        this.street = street;
    }

    public String getCity()
    {
        return city;
    }

    public void setCity(String city)
    {
        this.city = city;
    }

    public String getPostalCode()
    {
        return postalCode;
    }

    public void setPostalCode(String postalCode)
    {
        this.postalCode = postalCode;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof Address))
        {
            return false;
        }

        Address address = (Address) o;

        if (!street.equals(address.street))
        {
            return false;
        }
        if (!city.equals(address.city))
        {
            return false;
        }
        return postalCode.equals(address.postalCode);
    }

    @Override
    public int hashCode()
    {
        int result = street.hashCode();
        result = 31 * result + city.hashCode();
        result = 31 * result + postalCode.hashCode();
        return result;
    }

    @Override
    public String toString()
    {
        return new StringJoiner(", ", Address.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("street='" + street + "'")
                .add("city='" + city + "'")
                .add("postalCode='" + postalCode + "'")
                .toString();
    }
}
