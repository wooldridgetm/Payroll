package com.tomwo.app.payroll.model


abstract class PaymentMethod
class HoldMethod : PaymentMethod()
data class DirectMethod(val bank: String, val account: Double) : PaymentMethod()
data class MailMethod(val address: String) : PaymentMethod()

/**
 * NOTE: it is within [Transactions] that we associate this ([PaymentSchedule]) with [PaymentClassification]
 */
abstract class PaymentSchedule
class WeeklySchedule : PaymentSchedule()
class MonthlySchedule : PaymentSchedule()
class BiweeklySchedule : PaymentSchedule()


abstract class PaymentClassification
class NoClassification : PaymentClassification()
data class SalariedClassification(val salary : Double) : PaymentClassification()

class TimeCard(val date: Long, val hours: Double)
data class HourlyClassification(val hourlyRate: Double) : PaymentClassification()
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

interface SalesReceipt
data class CommissionedClassification(val commissionRate : Double, val salary: Double, val salesReceipt: SalesReceipt = object : SalesReceipt{}) : PaymentClassification()

abstract class Affiliation
interface ServiceCharge
class NoAffiliation : Affiliation()
data class UnionAffiliation(val dues: Double, val serviceCharge : ServiceCharge) : Affiliation()

data class Employee(val empId: Int, val address: String, val name: String,
                    val classification: PaymentClassification,
                    val schedule: PaymentSchedule,
                    val method : PaymentMethod,
                    val affiliation: Affiliation = NoAffiliation())
{
    companion object
    {
//        const val NO_EMPLOYEE = Employee(-1, "", "", )
    }
}


