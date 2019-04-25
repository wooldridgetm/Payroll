package com.tomwo.app.payroll.model.transactions

import com.tomwo.app.payroll.model.*

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

class ChangeCommissionedTransaction(empId: Int, private val rate: Double, private val salary: Double) : ChangeClassificationTransaction(empId)
{
    override fun getClassification(): PaymentClassification
    {
        return CommissionedClassification(rate, salary)
    }

    override fun getSchedule(): PaymentSchedule
    {
        return BiweeklySchedule()
    }
}
