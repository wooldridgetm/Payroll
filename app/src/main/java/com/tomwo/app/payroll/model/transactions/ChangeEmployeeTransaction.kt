package com.tomwo.app.payroll.model.transactions

import android.util.Log
import com.tomwo.app.payroll.extensions.debug
import com.tomwo.app.payroll.model.Employee
import com.tomwo.app.payroll.model.PayrollDatabase

abstract class ChangeEmployeeTransaction(private val empId : Int) : Transaction
{
    override fun execute()
    {
        val e = PayrollDatabase.getEmployee(empId)
        e?.let {
            change(e)
        } ?: debug("employee with id $empId doesn't exist")
    }

    protected abstract fun change(e: Employee)
}

class ChangeNameTransaction(empId: Int, private val name : String) : ChangeEmployeeTransaction(empId)
{
    override fun change(e: Employee)
    {
        // create a copy of the object with the new value for the property in question
        val employee = e.copy(name = name)

        // replace existing one in the database
        PayrollDatabase.addEmployee(employee)
    }
}

class ChangeAddressTransaction(empId: Int, private val address: String) : ChangeEmployeeTransaction(empId)
{
    override fun change(e: Employee)
    {
        // create a copy of the object with the new value for the property in question
        val employee = e.copy(address = address)

        // replace existing one in the database
        PayrollDatabase.addEmployee(employee)
    }
}