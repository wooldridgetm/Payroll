package com.tomwo.app.payroll.model.transactions

import com.tomwo.app.payroll.model.HourlyClassification
import com.tomwo.app.payroll.model.PayrollDatabase
import com.tomwo.app.payroll.model.TimeCard

class TimeCardTransaction(private val empId: Int, private val date : Long, private val hours: Float): Transaction
{
    override fun execute()
    {
        val employee = PayrollDatabase.getEmployee(empId)
        employee?.let { e ->
            val pc = e.classification
            val hc = pc as? HourlyClassification ?: throw Exception("Tried to add timeCard to non-hourly employee")

            hc.addTimeCard(TimeCard(date, hours))

        } ?: throw Exception("No such employee")
    }
}