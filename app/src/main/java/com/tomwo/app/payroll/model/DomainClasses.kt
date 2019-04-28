package com.tomwo.app.payroll.model

data class Employee(val empId: Int, val address: String, val name: String,
                    val classification: PaymentClassification,
                    val schedule: PaymentSchedule,
                    val method : PaymentMethod)
{
    var affiliation: Affiliation = NoAffiliation()
}


/**
 * Payment Method
 */
abstract class PaymentMethod
class HoldMethod : PaymentMethod()
data class DirectMethod(val bank: String, val account: Float) : PaymentMethod()
data class MailMethod(val address: String) : PaymentMethod()

/**
 * 1. Payment Schedule
 *
 * NOTE: it is within [Transactions] that we associate this ([PaymentSchedule]) with [PaymentClassification]
 */
abstract class PaymentSchedule
class WeeklySchedule : PaymentSchedule()
class MonthlySchedule : PaymentSchedule()
class BiweeklySchedule : PaymentSchedule()

/**
 * 2. Payment Type
 */
abstract class PaymentClassification

/**
 * 2a. Salary
 */
data class SalariedClassification(val salary : Float) : PaymentClassification()

/**
 * 2b. Hourly - [HourlyClassification]
 */
data class TimeCard(val date: Long, val hours: Float)
data class HourlyClassification(val hourlyRate: Float) : PaymentClassification()
{
    //val getTimeCard: TimeCard = object : TimeCard{}
    private val timeCards : MutableMap<Long, TimeCard> = mutableMapOf()
    fun addTimeCard(timeCard: TimeCard)
    {
        timeCards[timeCard.date] = timeCard
    }
    fun getTimeCard(date : Long) : TimeCard?
    {
        return timeCards[date]
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
}


abstract class Affiliation
class NoAffiliation : Affiliation()
data class UnionAffiliation(val dues: Float) : Affiliation()
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
}


data class ServiceCharge(val date: Long, val amount: Float)



