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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class Employee
{

    private long id;
    private String name;
    private final List<Employee> employees = new ArrayList<>();
    private Employee manager;

    public Employee(long id, String name)
    {
        this.id = id;
        this.name = name;

    }

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public List<Employee> getEmployees()
    {
        return employees;
    }

    public Employee getManager()
    {
        return manager;
    }

    public void setManager(Employee manager)
    {
        this.manager = manager;
        manager.getEmployees()
                .add(this);
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

        Employee employee = (Employee) o;

        if (!name.equals(employee.name))
        {
            return false;
        }
        String thisEmployeeNames = employees.stream()
                .map(Employee::getName)
                .collect(Collectors.joining(", "));
        String thatEmployeeNames = employee.employees.stream()
                .map(Employee::getName)
                .collect(Collectors.joining(", "));
        if (!thisEmployeeNames.equals(thatEmployeeNames))
        {
            return false;
        }
        return Objects.equals(manager, employee.manager);
    }

    @Override
    public int hashCode()
    {
        int result = name.hashCode();
        result = 31 * result + employees.hashCode();
        result = 31 * result + (manager != null ? manager.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return new StringJoiner(", ", Employee.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("name='" + name + "'")
                .add("manager = '" + (manager == null ? " - " : manager.getName()) + "'")
                .add("directReports = '" + employees.stream()
                        .map(Employee::getName)
                        .collect(Collectors.joining(" - ")) + "'")
                .toString();
    }
}
