package com.tomwo.app.payroll.model.transactions

import com.tomwo.app.payroll.model.PayrollDatabase

class DeleteEmployeeTransaction(private val empId : Int): Transaction
{
    override fun execute()
    {
        PayrollDatabase.deleteEmployee(empId)
    }
}