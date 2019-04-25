package com.tomwo.app.payroll.model

import com.tomwo.app.payroll.extensions.debug

object PayrollDatabase
{
    private val employees : MutableMap<Int, Employee>    = mutableMapOf()
    private val unionMembers : MutableMap<Int, Int> = mutableMapOf()

    fun getEmployee(empId : Int) : Employee? = employees[empId]

    fun getEmployeeByMemberId(memberId: Int) : Employee? = unionMembers[memberId]?.let { employees[it] }

    fun addEmployee(employee: Employee)
    {
        employees[employee.empId] = employee
    }

    fun addUnionMember(memberId: Int, e: Employee)
    {
        unionMembers[memberId] = e.empId
    }

    fun deleteEmployee(empId: Int)
    {
        employees.remove(empId)?.let {
            debug("employee $empId was removed from the database.")
        }
    }

    fun deleteUnionMember(memberId: Int)
    {
        unionMembers.remove(memberId)?.let {
            debug("union member id $memberId was removed form the database.")
        }
    }

    fun clear()
    {
        employees.clear()
        unionMembers.clear()
    }
}