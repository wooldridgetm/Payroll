package com.tomwo.app.payroll.model

import com.tomwo.app.payroll.extensions.debug
import java.text.SimpleDateFormat
import java.util.*

data class Employee(val empId: Int, val address: String, val name: String,
                    val classification: PaymentClassification,
                    val schedule: PaymentSchedule,
                    val method : PaymentMethod)
{
    var affiliation: Affiliation = NoAffiliation()

    fun isPayDate(d : Date) : Boolean
    {
        return schedule.isPayDate(d)
    }

    fun payDay(pc : Paycheck)
    {
        val grossPay = classification.calculatePay(pc)
        val deductions = affiliation.calculateDeductions(pc)
        val netPay = grossPay - deductions
        pc.grossPay = grossPay
        pc.deductions = deductions
        pc.netPay = netPay

    }
}

data class Paycheck(val payDate: Date, val empId: Int)
{
    var grossPay: Float = 0f
    var netPay: Float = 0f
    var deductions: Float = 0f

    fun getField(type: String): String
    {
        TODO("not implemented")
    }
}


/**
 * Payment Method
 */
abstract class PaymentMethod
{
    abstract fun pay(pc: Paycheck)
}

class HoldMethod : PaymentMethod()
{
    override fun pay(pc: Paycheck)
    {
        TODO("not implemented")
    }
}

data class DirectMethod(val bank: String, val account: Float) : PaymentMethod()
{
    override fun pay(pc: Paycheck)
    {
        TODO("not implemented")
    }
}

data class MailMethod(val address: String) : PaymentMethod()
{
    override fun pay(pc: Paycheck)
    {
        TODO("not implemented")
    }
}

/**
 * 1. Payment Schedule
 *
 * NOTE: it is within [Transaction] that we associate this ([PaymentSchedule]) with [PaymentClassification]
 */
abstract class PaymentSchedule
{
    abstract fun isPayDate(payDate : Date): Boolean
}
class WeeklySchedule : PaymentSchedule()
{
    override fun isPayDate(payDate: Date): Boolean
    {
        // Locale.getDefault(Locale.Category.FORMAT)
        val dotw = SimpleDateFormat("EE", Locale.US).format(payDate)
        debug("dotw: $dotw")
        return dotw == "Fri"
    }
}
class BiweeklySchedule : PaymentSchedule()
{
    override fun isPayDate(payDate: Date): Boolean
    {
        TODO("not implemented")
    }
}
class MonthlySchedule : PaymentSchedule()
{
    override fun isPayDate(payDate: Date) : Boolean = isLastDayOfMonth(payDate)

    private fun isLastDayOfMonth(d: Date) : Boolean
    {
        val cal = Calendar.getInstance()
        val m1 = cal.get(Calendar.MONTH)
        cal.add(Calendar.DAY_OF_MONTH, 1)
        val m2 = cal.get(Calendar.MONTH)
        return (m1 != m2)
    }
}


/**
 * 2. Payment Type
 */
abstract class PaymentClassification
{
    abstract fun calculatePay(pc: Paycheck) : Float
}

/**
 * 2a. Salary
 */
data class SalariedClassification(val salary : Float) : PaymentClassification()
{
    override fun calculatePay(pc: Paycheck): Float
    {
        TODO("not implemented")
    }
}

/**
 * 2b. Hourly - [HourlyClassification]
 */
data class TimeCard(val date: Date, val hours: Float)
data class HourlyClassification(val hourlyRate: Float) : PaymentClassification()
{
    //val getTimeCard: TimeCard = object : TimeCard{}
    private val timeCards : MutableMap<Date, TimeCard> = mutableMapOf()
    fun addTimeCard(timeCard: TimeCard)
    {
        timeCards[timeCard.date] = timeCard
    }
    fun getTimeCard(date : Date) : TimeCard?
    {
        return timeCards[date]
    }
    override fun calculatePay(pc: Paycheck): Float
    {
        TODO("not implemented")
    }
}

/**
 * 2c. Commissioned - [CommissionedClassification]
 */
data class SalesReceipt(val date : Long, val amount : Float)
data class CommissionedClassification(val commissionRate : Float, val salary: Float) : PaymentClassification()
{
    private val salesReceipts : MutableMap<Long, SalesReceipt> = mutableMapOf()
    fun addSalesReceipt(salesReceipt: SalesReceipt)
    {
        salesReceipts[salesReceipt.date] = salesReceipt
    }
    fun getSalesReceipt(date: Long): SalesReceipt?
    {
        return salesReceipts[date]
    }
    override fun calculatePay(pc: Paycheck): Float
    {
        TODO("not implemented")
    }
}


abstract class Affiliation
{
    abstract fun calculateDeductions(pc: Paycheck) : Float
}
class NoAffiliation : Affiliation()
{
    override fun calculateDeductions(pc: Paycheck) = 0F
}
data class UnionAffiliation(val memberId: Int, val dues: Float) : Affiliation()
{
    private val serviceCharges : MutableMap<Long, ServiceCharge> = mutableMapOf()

    fun addServiceCharge(charge: ServiceCharge)
    {
        serviceCharges[charge.date] = charge
    }
    fun getServiceCharge(date : Long) : ServiceCharge?
    {
        return serviceCharges[date]
    }
    override fun calculateDeductions(pc: Paycheck): Float
    {
        TODO("not implemented")
    }
}


data class ServiceCharge(val date: Long, val amount: Float)



