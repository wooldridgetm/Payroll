package com.tomwo.app.payroll.model.transactions

import com.tomwo.app.payroll.extensions.debug
import com.tomwo.app.payroll.model.Paycheck
import com.tomwo.app.payroll.model.PayrollDatabase
import java.util.Date

class PaydayTransaction(private val payDate: Date) : Transaction
{
    private val payChecks = mutableMapOf<Int, Paycheck>()

    override fun execute()
    {
        val empIds = PayrollDatabase.getAllEmployeeIds()

        for (empId in empIds)
        {
            val e = PayrollDatabase.getEmployee(empId)
            e?.let {
                if (it.isPayDate(payDate))
                {
                    val pc = Paycheck(payDate, empId)
                    payChecks[empId] = pc
                    it.payDay(pc)
                }
            } ?: debug("employee $empId is not in the PayrollDatabase")
        } // loop
    }

    fun getPaycheck(empId: Int) : Paycheck?
    {
        if (!payChecks.contains(empId))
        {
            debug("employee $empId doesn't have a paycheck!")
        }
        return payChecks[empId]
    }
}