package com.tomwo.app.payroll.model.transactions

import com.tomwo.app.payroll.model.Employee
import com.tomwo.app.payroll.model.HourlyClassification
import com.tomwo.app.payroll.model.MonthlySchedule
import com.tomwo.app.payroll.model.PaymentClassification
import com.tomwo.app.payroll.model.PaymentSchedule
import com.tomwo.app.payroll.model.PayrollDatabase
import com.tomwo.app.payroll.model.SalariedClassification
import com.tomwo.app.payroll.model.WeeklySchedule

abstract class ChangeClassificationTransaction(empId: Int) : ChangeEmployeeTransaction(empId)
{
    override fun change(e: Employee)
    {
        val employee = e.copy(classification = getClassification(), schedule = getSchedule())
        PayrollDatabase.addEmployee(employee)
    }

    abstract fun getClassification(): PaymentClassification
    abstract fun getSchedule(): PaymentSchedule
}

class ChangeHourlyTransaction(empId: Int, private val hourlyRate: Double) : ChangeClassificationTransaction(empId)
{
    override fun getClassification(): PaymentClassification
    {
        return HourlyClassification(hourlyRate)
    }

    override fun getSchedule(): PaymentSchedule
    {
        return WeeklySchedule()
    }
}

class ChangeSalariedTransaction(empId: Int, private val salary: Double) : ChangeClassificationTransaction(empId)
{
    override fun getClassification(): PaymentClassification
    {
        return SalariedClassification(salary)
    }

    override fun getSchedule(): PaymentSchedule
    {
        return MonthlySchedule()
    }
}
