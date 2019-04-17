package com.tomwo.app.payroll.data

import com.tomwo.app.payroll.data.domain.Employee
import com.tomwo.app.payroll.data.domain.HoldMethod
import com.tomwo.app.payroll.data.domain.PaymentClassification
import com.tomwo.app.payroll.data.domain.PaymentMethod
import com.tomwo.app.payroll.data.domain.PaymentSchedule

interface Transaction
{
    fun execute() : Unit
}

abstract class AddEmployeeTransaction(private val empId: Int, private val address: String, private val name: String) : Transaction
{
    override fun execute()
    {
        val pc : PaymentClassification = getClassification()
        val ps : PaymentSchedule = getSchedule()
        val pm : PaymentMethod = HoldMethod()

        val e = Employee(empId, address, name, classification = pc, schedule = ps, method = pm)

        // TODO: add implementation for PayrollDatabase
    }

    abstract fun getClassification(): PaymentClassification
    abstract fun getSchedule() : PaymentSchedule
}