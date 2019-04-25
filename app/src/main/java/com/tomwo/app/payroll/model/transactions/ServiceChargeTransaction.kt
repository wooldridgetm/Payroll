package com.tomwo.app.payroll.model.transactions

import com.tomwo.app.payroll.model.PayrollDatabase
import com.tomwo.app.payroll.model.ServiceCharge
import com.tomwo.app.payroll.model.UnionAffiliation

class ServiceChargeTransaction(private val memberId: Int, private val date: Long, private val amount: Double) : Transaction
{
    override fun execute()
    {
        val employee = PayrollDatabase.getEmployeeByMemberId(memberId)

        employee?.let {e ->
            val af = e.affiliation

            if (af is UnionAffiliation)
            {
                af.addServiceCharge(ServiceCharge(date, amount))
            }
        }
    }
}