package com.tomwo.app.payroll.model

import com.tomwo.app.payroll.extensions.debug
import java.lang.IllegalArgumentException
import java.text.SimpleDateFormat
import java.util.*

data class Employee(val empId: Int, val address: String, val name: String, val classification: PaymentClassification, val schedule: PaymentSchedule, val method: PaymentMethod)
{
    var affiliation: Affiliation = NoAffiliation()

    fun isPayDate(d: Date): Boolean
    {
        return schedule.isPayDay(d)
    }

    fun payDay(pc: Paycheck)
    {
        val grossPay = classification.calculatePay(pc)
        val deductions = affiliation.calculateDeductions(pc)
        val netPay = grossPay - deductions
        pc.grossPay = grossPay
        pc.deductions = deductions
        pc.netPay = netPay
    }

    fun getPayPeriodStartDate(payPeriodEndDate: Date): Date
    {
        return schedule.getPayPeriodStartDate(payPeriodEndDate)
    }
}

data class Paycheck(val payPeriodStartDate: Date, val payDate: Date, val empId: Int)
{
    val payPeriodEndDate: Date
        get() = payDate

    var grossPay: Float = 0f
    var netPay: Float = 0f
    var deductions: Float = 0f

    fun getField(type: String): String
    {
        return when (type)
        {
            "Disposition" -> PayrollDatabase.getEmployee(empId)?.let { e ->
                when (e.method)
                {
                    is HoldMethod -> "Hold"
                    is DirectMethod -> "Direct"
                    is MailMethod -> "Mail"
                    else -> throw IllegalArgumentException("${e.method} isn't supported!")
                }
            } ?: throw IllegalStateException("Employee not found")
            else -> throw IllegalArgumentException("Paycheck getField(type=$type) isn't supported!")
        }
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
    abstract fun isPayDay(payDate: Date): Boolean

    abstract fun getPayPeriodStartDate(d: Date): Date
}

class MonthlySchedule : PaymentSchedule()
{
    override fun isPayDay(payDate: Date): Boolean = isLastDayOfMonth(payDate)

    private fun isLastDayOfMonth(d: Date): Boolean
    {
        val cal = Calendar.getInstance().apply {
            time = d
        }
        val m1 = cal.get(Calendar.MONTH)
        cal.add(Calendar.DAY_OF_MONTH, 1)
        val m2 = cal.get(Calendar.MONTH)
        return (m1 != m2)
    }

    override fun getPayPeriodStartDate(d: Date): Date
    {
        val cal = Calendar.getInstance().apply {
            time = d
        }
        cal.set(Calendar.DATE, 1)
        return cal.time
    }
}
class WeeklySchedule : PaymentSchedule()
{
    override fun isPayDay(payDate: Date): Boolean
    {
        // Locale.getDefault(Locale.Category.FORMAT)
        val dotw = SimpleDateFormat("EE", Locale.US).format(payDate)
        debug("dotw: $dotw")
        return dotw == "Fri"
    }

    override fun getPayPeriodStartDate(d: Date): Date
    {
        val cal = Calendar.getInstance().apply {
            time = d
        }
        cal.add(Calendar.DATE, -5)
        return cal.time
    }
}
class BiweeklySchedule : PaymentSchedule()
{
    companion object
    {
        private val dateFormat = SimpleDateFormat("EE, MM/dd/yyyy", Locale.US)
    }

    override fun isPayDay(payDate: Date): Boolean
    {
        val fridays = get2ndAnd4thFri(payDate)

        // test if date is equal to the 2nd or 4th Friday of the month
        return (payDate == fridays.first || payDate == fridays.second)
    }

    override fun getPayPeriodStartDate(d: Date): Date
    {
        val cal = Calendar.getInstance().apply {
            time = d
        }
        val fridays = get2ndAnd4thFri(d)
        if (d == fridays.first)
        {
            cal.set(Calendar.DATE, 1)
            return cal.time
        }

        cal.time = fridays.first
        cal.add(Calendar.DATE, 3);
        return cal.time
    }

    private fun get2ndAnd4thFri(d: Date): Pair<Date, Date>
    {
        val cal = Calendar.getInstance().apply {
            time = d
        }

        cal.set(Calendar.DATE, 1)
        debug("1st day of the month is ${dateFormat.format(cal.time)}")
        val num2Add = when(val dotw = cal.get(Calendar.DAY_OF_WEEK)) {
            in Calendar.SUNDAY..Calendar.THURSDAY -> (6 - dotw)
            Calendar.FRIDAY -> 0
            else -> 6
        }

        cal.add(Calendar.DAY_OF_MONTH, num2Add+7)
        val secondFri = cal.time

        cal.add(Calendar.DAY_OF_MONTH, 14)
        val fourthFri = cal.time

        return Pair(secondFri, fourthFri)
    }
}


/**
 * 2. PaymentClassification
 */
abstract class PaymentClassification
{
    abstract fun calculatePay(pc: Paycheck): Float

    fun isInPayPeriod(d: Date, pc: Paycheck): Boolean
    {
        val payPeriodEndDate = pc.payPeriodEndDate
        val payPeriodStartDate = pc.payPeriodStartDate

        return !(d.before(payPeriodStartDate) || d.after(payPeriodEndDate))
    }
}
data class SalariedClassification(val salary: Float) : PaymentClassification()
{
    override fun calculatePay(pc: Paycheck): Float
    {
        return salary
    }
}

data class TimeCard(val date: Date, val hours: Float)
data class HourlyClassification(val hourlyRate: Float) : PaymentClassification()
{
    //val getTimeCard: TimeCard = object : TimeCard{}
    private val timeCards: MutableMap<Date, TimeCard> = mutableMapOf()

    fun addTimeCard(timeCard: TimeCard)
    {
        timeCards[timeCard.date] = timeCard
    }

    fun getTimeCard(date: Date): TimeCard?
    {
        return timeCards[date]
    }

    override fun calculatePay(pc: Paycheck): Float
    {
        var totalPay = 0f

        timeCards.forEach {
            val tc = it.value
            if (isInPayPeriod(tc.date, pc))
            {
                totalPay += calculatePayForTimeCard(tc)
            }
        }
        return totalPay
    }

    private fun calculatePayForTimeCard(tc: TimeCard): Float
    {
        val hours = tc.hours
        val overtime = maxOf(0.0f, hours - 8.0f)
        val straightTime = hours - overtime
        return straightTime * hourlyRate + overtime * hourlyRate * 1.5f
    }
}

data class SalesReceipt(val date: Date, val amount: Float)
data class CommissionedClassification(val commissionRate: Float, val salary: Float) : PaymentClassification()
{
    private val salesReceipts: MutableMap<Date, SalesReceipt> = mutableMapOf()

    fun addSalesReceipt(salesReceipt: SalesReceipt)
    {
        salesReceipts[salesReceipt.date] = salesReceipt
    }

    fun getSalesReceipt(date: Date): SalesReceipt?
    {
        return salesReceipts[date]
    }

    override fun calculatePay(pc: Paycheck): Float
    {
        var totalPay = salary

        salesReceipts.forEach {
            val sr = it.value
            if (isInPayPeriod(sr.date, pc))
            {
                totalPay += calculatePayForSalesReceipts(sr)
            }
        }
        return totalPay
    }

    private fun calculatePayForSalesReceipts(sr: SalesReceipt): Float
    {
        return sr.amount * commissionRate
    }
}

/**
 * Affiliation
 */
abstract class Affiliation
{
    abstract fun calculateDeductions(pc: Paycheck): Float
}
class NoAffiliation : Affiliation()
{
    override fun calculateDeductions(pc: Paycheck) = 0F
}

data class ServiceCharge(val date: Long, val amount: Float)
data class UnionAffiliation(val memberId: Int, val dues: Float) : Affiliation()
{
    private val serviceCharges: MutableMap<Long, ServiceCharge> = mutableMapOf()

    fun addServiceCharge(charge: ServiceCharge)
    {
        serviceCharges[charge.date] = charge
    }

    fun getServiceCharge(date: Long): ServiceCharge?
    {
        return serviceCharges[date]
    }

    override fun calculateDeductions(pc: Paycheck): Float
    {
        val fridays = numberOfFridaysInPayPeriod(pc.payPeriodStartDate, pc.payPeriodEndDate)
        val total = dues*fridays
        debug("total deductions: $total")
        return total
    }

    private fun numberOfFridaysInPayPeriod(payPeriodStart :Date, payPeriodEnd: Date): Int
    {
        val cal = Calendar.getInstance().apply {
            time = payPeriodStart
        }
        val start = cal.get(Calendar.DATE)
        cal.time = payPeriodEnd
        val end = cal.get(Calendar.DATE)

        var fridays = 0
        for(i in start..end)
        {
            cal.set(Calendar.DATE, i)
            val dotw = cal.get(Calendar.DAY_OF_WEEK)

            if (dotw == Calendar.FRIDAY) {
                fridays++
            }
        }

        return fridays
    }
}





