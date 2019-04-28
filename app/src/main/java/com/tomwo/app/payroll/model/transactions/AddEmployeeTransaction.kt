package com.tomwo.app.payroll.model.transactions

import com.tomwo.app.payroll.model.*

abstract class AddEmployeeTransaction(private val empId: Int, private val name: String, private val address: String) : Transaction
{
    override fun execute()
    {
        val pc : PaymentClassification = getClassification()
        val ps : PaymentSchedule = getSchedule()
        val pm : PaymentMethod = HoldMethod()

        val e = Employee(empId, address, name, classification = pc, schedule = ps, method = pm)

        PayrollDatabase.addEmployee(e)
    }

    abstract fun getClassification(): PaymentClassification
    abstract fun getSchedule() : PaymentSchedule
}

class AddSalariedEmployee(empId: Int, name: String, address: String, private val salary : Float) : AddEmployeeTransaction(empId, name, address)
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

class AddHourlyEmployee(empId: Int, name: String, address: String, private val hourlyRate: Float) : AddEmployeeTransaction(empId, name, address)
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

class AddCommissionedEmployee(empId: Int, name: String, address: String, private val salary: Float, private val commissionRate: Float) : AddEmployeeTransaction(empId, name, address)
{
    override fun getClassification(): PaymentClassification
    {
        return CommissionedClassification(this.commissionRate, this.salary)
    }

    override fun getSchedule(): PaymentSchedule
    {
        return BiweeklySchedule()
    }

}