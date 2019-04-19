package com.tomwo.app.payroll.model

object PayrollDatabase
{
    private val employees : MutableMap<Int, Employee> = mutableMapOf()

    fun getEmployee(empId : Int) : Employee? = employees[empId]

    fun addEmployee(employee: Employee)
    {
        employees[employee.empId] = employee
    }

    fun clear()
    {
        employees.clear()
    }
}