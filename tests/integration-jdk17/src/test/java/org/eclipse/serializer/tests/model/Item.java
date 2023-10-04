package org.eclipse.serializer.tests.model;

/*-
 * #%L
 * Eclipse Serializer Test on JDK 17
 * %%
 * Copyright (C) 2023 MicroStream Software
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */

import java.util.Objects;

public class Item
{

    private String name;
    private Season season;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Season getSeason()
    {
        return season;
    }

    public void setSeason(Season season)
    {
        this.season = season;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        Item item = (Item) o;

        if (!Objects.equals(name, item.name))
        {
            return false;
        }
        return season == item.season;
    }

    @Override
    public int hashCode()
    {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (season != null ? season.hashCode() : 0);
        return result;
    }
}
