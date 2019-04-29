package com.tomwo.app.payroll.model.transactions

import com.tomwo.app.payroll.model.Paycheck
import com.tomwo.app.payroll.model.PayrollDatabase
import java.util.Date

class PaydayTransaction(private val payDate: Date) : Transaction
{
    override fun execute()
    {
        val empIds = PayrollDatabase.getAllEmployeeIds()// mutableListOf<Int>()
    }

    fun getPaycheck(empId: Int) : Paycheck?
    {
        TODO("not implemented")
    }
}