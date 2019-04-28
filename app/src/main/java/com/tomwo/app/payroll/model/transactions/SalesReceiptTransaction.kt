package com.tomwo.app.payroll.model.transactions

import com.tomwo.app.payroll.model.CommissionedClassification
import com.tomwo.app.payroll.model.PayrollDatabase
import com.tomwo.app.payroll.model.SalesReceipt

class SalesReceiptTransaction(private val empId: Int, private val date: Long, private val amount: Float) : Transaction
{
    override fun execute()
    {
        val employee = PayrollDatabase.getEmployee(empId)

        employee?.let { e ->
            val pc = e.classification
            val cc = e.classification as? CommissionedClassification ?: throw Exception("tried to add SalesReceipt to non-commissioned employee")

            cc.addSalesReceipt(SalesReceipt(date, amount))
        } ?: throw Exception("No such employee.")
    }

}