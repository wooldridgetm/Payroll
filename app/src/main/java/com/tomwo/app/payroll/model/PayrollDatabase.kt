package com.tomwo.app.payroll.model

import com.tomwo.app.payroll.extensions.debug

object PayrollDatabase
{
    private val employees : MutableMap<Int, Employee> = mutableMapOf()

    fun getEmployee(empId : Int) : Employee? = employees[empId]

    fun addEmployee(employee: Employee)
    {
        employees[employee.empId] = employee
    }

    fun deleteEmployee(empId: Int)
    {
        employees.remove(empId)?.let {
            debug("employee $empId was removed from the database!")
        }
    }

    fun clear()
    {
        employees.clear()
    }
}