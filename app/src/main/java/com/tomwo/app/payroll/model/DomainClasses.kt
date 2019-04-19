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
interface TimeCard
interface SalesReceipt
class NoClassification : PaymentClassification()
data class SalariedClassification(val salary : Double) : PaymentClassification()
data class HourlyClassification(val hourlyRate: Double, val timeCard: TimeCard = object : TimeCard{}) : PaymentClassification()
data class CommissionedClassification(val commissionRate : Double, val salary: Float, val salesReceipt: SalesReceipt = object : SalesReceipt{}) : PaymentClassification()

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


