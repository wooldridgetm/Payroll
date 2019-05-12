package com.tomwo.app.payroll

import android.util.Log
import com.google.common.truth.Truth.assertThat
import com.tomwo.app.payroll.extensions.clazz
import com.tomwo.app.payroll.extensions.clazzName
import com.tomwo.app.payroll.extensions.debug
import com.tomwo.app.payroll.model.*
import com.tomwo.app.payroll.model.transactions.*
import org.junit.Before
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.*

class PayrollTest
{
    @Before
    fun init()
    {
    }

    @Test
    fun `test android_util_Log is mocked`()
    {
        Log.d(clazzName(this), "android.util.Log is successfully mocked")
    }

    @Test
    fun addSalariedEmployeeTest()
    {
        val empId = 1
        val test = AddSalariedEmployee(empId, "Bob", "Home", 1000F)
        test.execute()

        val employee = PayrollDatabase.getEmployee(empId)
        assertThat(employee?.name ?: "").isEqualTo("Bob")

        // if we've gotten to this point, then the last test passed & we know it's not null
        val e = employee!!

        val pc : PaymentClassification = e.classification
        assertThat(pc).isInstanceOf(SalariedClassification::class.java)

        val sc = pc as SalariedClassification

        assertThat(sc.salary).isWithin(.001F).of(1000F)

        val schedule = e.schedule
        assertThat(schedule).isInstanceOf(MonthlySchedule::class.java)
        //val ps = schedule as? MonthlySchedule ?: throw AssertionError("schedule isn't off correct type 'MonthlySchedule'")
        //assertThat(ps).isNotNull()
        assertThat(e.method).isInstanceOf(HoldMethod::class.java)
    }

    @Test
    fun addHourlyEmployeeTest()
    {
        val empId = 2
        val test = AddHourlyEmployee(empId, "Thomas", "1600 Pennsylvania Ave", 100F)
        test.execute()

        val employee = PayrollDatabase.getEmployee(empId)
        assertThat(employee?.name ?: "").isEqualTo("Thomas")

        val e = employee!!

        // test the hourly rate
        val pc = e.classification
        //assertThat(pc).isInstanceOf(HourlyClassification::class.java)
        assertThat(pc).isInstanceOf(clazz<HourlyClassification>())
        val hc = pc as HourlyClassification
        assertThat(hc.hourlyRate).isWithin(.001F).of(100F)

        // schedule
        val schedule = e.schedule
        assertThat(schedule).isInstanceOf(WeeklySchedule::class.java)
        // schedule as? WeeklySchedule ?: throw AssertionError("schedule isn't of correct type 'Weekly Schedule'")

        assertThat(e.method).isInstanceOf(HoldMethod::class.java)
    }

    @Test
    fun addCommissionedEmployeeTest()
    {
        val empId = 3
        val test = AddCommissionedEmployee(empId, "Matthew", "152 Lakewood Drive", 1_000_000F, 0.03F)
        test.execute()

        val employee = PayrollDatabase.getEmployee(empId)

        // test 1 - Employee Name
        assertThat(employee?.name ?: "").isEqualTo("Matthew")
        val e = employee!!

        // test 2 - Payment Classification
        val pc = e.classification
        assertThat(pc).isInstanceOf(clazz<CommissionedClassification>())
        val sc = pc as CommissionedClassification
        assertThat(sc.salary).isWithin(.001F).of(1_000_000F)
        assertThat(sc.commissionRate).isEqualTo(0.03F)

        // test 3 - Payment Schedule
        val schedule = e.schedule
        assertThat(schedule).isInstanceOf(clazz<BiweeklySchedule>())

        // test 3 - Payment Method
        assertThat(e.method).isInstanceOf(clazz<HoldMethod>())
    }

    @Test
    fun testDeleteEmployee()
    {
        val empId = 5
        val t = AddCommissionedEmployee(empId, "Lance", "Home", 2500F, 3.2F)
        t.execute()

        val e1 = PayrollDatabase.getEmployee(empId)
        assertThat(e1).isNotNull()

        val testDelete = DeleteEmployeeTransaction(empId)
        testDelete.execute()
        val e2 = PayrollDatabase.getEmployee(empId)
        assertThat(e2).isNull()
    }

    @Test
    fun testTimeCardTransaction()
    {
        val empId = 2
        val t = AddHourlyEmployee(empId, "Bill", "Home", 15.25F)
        t.execute()

        val date = df.parse("10/31/2001")
        val test1 = TimeCardTransaction(empId, date, 8.0F)
        test1.execute()

        val employee = PayrollDatabase.getEmployee(empId)
        assertThat(employee).isNotNull()

        val e = employee!!
        val pc = e.classification
        assertThat(pc).isInstanceOf(clazz<HourlyClassification>())
        val hc = pc as HourlyClassification
        val tc = hc.getTimeCard(date)
        assertThat(tc).isNotNull()

        assertThat(tc!!.hours).isEqualTo(8.0F)
    }

    @Test
    fun testSalesReceiptTransaction()
    {
        val empId = 10
        val t = AddCommissionedEmployee(empId, "Sonya", "90210 Beverly Hills", 1_000F, 2.3F)
        t.execute()

        val date = df.parse("4/22/2019")

        val test1 = SalesReceiptTransaction(empId, date, 9.0F)
        test1.execute()

        val employee = PayrollDatabase.getEmployee(empId)
        assertThat(employee).isNotNull()

        val e = employee!!
        val pc = e.classification
        assertThat(pc).isInstanceOf(clazz<CommissionedClassification>())
        val cc = pc as CommissionedClassification
        val sr = cc.getSalesReceipt(date)
        assertThat(sr).isNotNull()
        assertThat(sr!!.amount).isEqualTo(9.0F)
    }

    @Test
    fun testAddServiceChargeTransaction()
    {
        val empId = 2
        val t = AddHourlyEmployee(empId, "Bill", "home", 15.25F)
        t.execute()
        val employee = PayrollDatabase.getEmployee(empId)
        assertThat(employee).isNotNull()
        val e = employee!!

        val memberId = 86 // Maxwell Smart
        val af = UnionAffiliation(86,12.5F)
        e.affiliation = af

        PayrollDatabase.addUnionMember(memberId, e)
        val sct = ServiceChargeTransaction(memberId, 20011101, 12.95F)
        sct.execute()

        val sc = af.getServiceCharge(20011101)
        assertThat(sc).isNotNull()

        assertThat(sc!!.amount).isEqualTo(12.95F)
    }

    @Test
    fun testChangeNameTransaction()
    {
        val empId = 2
        val t = AddHourlyEmployee(empId, "Bill", "Home", 15.25F)
        t.execute()

        val changeName = ChangeNameTransaction(empId, "Thomas")
        changeName.execute()

        val employee = PayrollDatabase.getEmployee(empId)
        assertThat(employee).isNotNull()
        assertThat(employee!!.name).isEqualTo("Thomas")
    }

    @Test
    fun testChangeAddressTransaction()
    {
        val empId = 2
        val t = AddHourlyEmployee(empId, "Bill", "Home", 15.25F)
        t.execute()
        val changeAddress = ChangeAddressTransaction(empId, "152 Lakewood Drive")
        changeAddress.execute()

        val employee = PayrollDatabase.getEmployee(empId)
        assertThat(employee).isNotNull()
        assertThat(employee!!.address).isEqualTo("152 Lakewood Drive")
    }

    @Test
    fun testChangeHourlyTransaction()
    {
        val empId = 3
        val t = AddCommissionedEmployee(empId, "Lance", "Home", 2500F, 3.2F)
        t.execute()

        val test = ChangeHourlyTransaction(empId, 27.52F)
        test.execute()

        val employee = PayrollDatabase.getEmployee(empId)
        assertThat(employee).isNotNull()
        val e = employee!!

        val pc = e.classification
        assertThat(pc).isNotNull()
        assertThat(pc).isInstanceOf(clazz<HourlyClassification>())
        val hc = pc as HourlyClassification
        assertThat(hc.hourlyRate).isWithin(.001F).of(27.52F)

        val ps = e.schedule
        assertThat(ps).isNotNull()
        assertThat(ps).isInstanceOf(clazz<WeeklySchedule>())
    }

    @Test
    fun testChangeSalaryTransaction()
    {
        val empId = 100
        val t = AddHourlyEmployee(empId, "Thomas", "Home", 100F)
        t.execute()

        // here's the test
        val test = ChangeSalariedTransaction(empId, 1_000_000F)
        test.execute()

        val employee = PayrollDatabase.getEmployee(empId)
        assertThat(employee).isNotNull()
        val e = employee!!

        val pc = e.classification
        assertThat(pc).isNotNull()
        assertThat(pc).isInstanceOf(clazz<SalariedClassification>())
        val sc = pc as SalariedClassification

        assertThat(sc.salary).isWithin(0.001F).of(1_000_000F)

        val ps = e.schedule
        assertThat(ps).isNotNull()
        assertThat(ps).isInstanceOf(clazz<MonthlySchedule>())
    }

    @Test
    fun testChangeCommissionedTransaction()
    {
        val empId = 967
        val t = AddSalariedEmployee(empId, "Thomas", "home", 567_920F)
        t.execute()

        val test = ChangeCommissionedTransaction(empId, 3.2F, 123_000F)
        test.execute()

        val employee = PayrollDatabase.getEmployee(empId)
        assertThat(employee).isNotNull()
        val e = employee!!

        val pc = e.classification
        assertThat(pc).isNotNull()
        assertThat(pc).isInstanceOf(clazz<CommissionedClassification>())
        val cc = pc as CommissionedClassification

        assertThat(cc.commissionRate).isEqualTo(3.2F)
        assertThat(cc.salary).isEqualTo(123_000.0F)

        val ps = e.schedule
        assertThat(ps).isNotNull()
        assertThat(ps).isInstanceOf(clazz<BiweeklySchedule>())
    }

    @Test
    fun testChangeDirectTransaction()
    {
        val empId = 999
        val t = AddSalariedEmployee(empId, "Thomas", "", 567_920F)
        t.execute()

        val test = ChangeDirectTransaction(empId, "SunTrust", 90210F)
        test.execute()

        val employee = PayrollDatabase.getEmployee(empId)
        assertThat(employee).isNotNull()
        val e = employee!!

        val pm = e.method
        assertThat(pm).isInstanceOf(clazz<DirectMethod>())
        val dm = pm as DirectMethod

        assertThat(dm.bank).isEqualTo("SunTrust")
        assertThat(dm.account).isEqualTo(90210F)
    }

    @Test
    fun testChangeMailTransaction()
    {
        val empId = 1
        val t = AddSalariedEmployee(empId, "Thomas", "", 567_920F)
        t.execute()

        val test = ChangeMailTransaction(empId, "1640 Claymore St")
        test.execute()

        val employee = PayrollDatabase.getEmployee(empId)
        assertThat(employee).isNotNull()
        val e = employee!!

        val pm = e.method
        assertThat(pm).isInstanceOf(clazz<MailMethod>())
        val mm = pm as MailMethod

        assertThat(mm.address).isEqualTo("1640 Claymore St")
    }

    @Test
    fun testChangeHoldTransaction()
    {
        val empId = 1
        val t = AddSalariedEmployee(empId, "Thomas", "", 567_920F)
        t.execute()

        // b/c all employees added to the database use (by default) the HoldMethod, let's change to the Mail Method
        val test1 = ChangeMailTransaction(empId, "1640 Claymore St")
        test1.execute()

        val test2 = ChangeHoldTransaction(empId)
        test2.execute()

        val employee = PayrollDatabase.getEmployee(empId)
        assertThat(employee).isNotNull()
        val e = employee!!

        val pm = e.method
        assertThat(pm).isNotNull()
        assertThat(pm).isInstanceOf(clazz<HoldMethod>())
    }

    @Test
    fun testChangeUnionMemberTransaction()
    {
        val empId = 2
        val memberId = 7734
        val t = AddHourlyEmployee(empId, "Bill", "Home", 15.25F)
        t.execute()

        val cmt = ChangeUnionMemberTransaction(empId, memberId, 99.42F)
        cmt.execute()

        val employee = PayrollDatabase.getEmployee(empId)
        assertThat(employee).isNotNull()
        val e = employee!!

        val af = e.affiliation
        assertThat(af).isNotNull()
        assertThat(af).isInstanceOf(clazz<UnionAffiliation>())

        val uf = af as UnionAffiliation

        assertThat(uf.dues).isWithin(.001F).of(99.42F)
        val member = PayrollDatabase.getUnionMember(memberId)
        assertThat(member).isNotNull()
        assertThat(e == member)
    }

    @Test
    fun testChangeUnaffiliatedTransaction()
    {
        val empId = 2
        val memberId = 7734
        val t = AddHourlyEmployee(empId, "Bill", "Home", 15.25F)
        t.execute()

        // change to UnionMember
        val cmt = ChangeUnionMemberTransaction(empId, memberId, 99.42F)
        cmt.execute()

        // change to NoAffiliation
        val test = ChangeUnaffiliatedTransaction(empId)
        test.execute()

        // assert that employee.affiliation = NoAffiliation
        val employee = PayrollDatabase.getEmployee(empId)
        assertThat(employee).isNotNull()
        val e = employee!!

        val noAf = e.affiliation
        assertThat(noAf).isNotNull()
        assertThat(noAf).isInstanceOf(clazz<NoAffiliation>())
    }

    @Test
    fun testDates()
    {
        val format = SimpleDateFormat("MM/dd/yyyy")
        val payDate = format.parse("11/30/2001")
        val d1 = format.parse("11/30/2001")
        val d2 = Date()

        assertThat(d1).isEqualTo(payDate)
        assertThat(d2).isNotEqualTo(payDate)
    }

    /**
     * Paying Employees
     */
    @Test
    fun testPaySingleSalariedEmployee()
    {
        val empId = 1
        val t = AddSalariedEmployee(empId, "Bob", "Home", 1000F)
        t.execute()

        val payDate = df.parse("11/30/2001")
        val pt = PaydayTransaction(payDate)
        pt.execute()
        val paycheck = pt.getPaycheck(empId)
        assertThat(paycheck).isNotNull()

        paycheck?.let {
            assertThat(it.payDate).isEqualTo(payDate)
            assertThat(it.grossPay).isWithin(0.001F).of(1000F)
            assertThat(it.getField("Disposition")).isEqualTo("Hold")
            assertThat(it.deductions).isWithin(0.001F).of(0.0F)
            assertThat(it.netPay).isWithin(0.001F).of(1000F)
        }
    }

    @Test
    fun testPaySingleSalariedEmployeeOnWrongDate()
    {
        val empId = 1
        val test = AddSalariedEmployee(empId, "Bob", "home", 1000F)
        test.execute()

        val pt = PaydayTransaction(df.parse("11/9/2001"))
        pt.execute()

        val pc = pt.getPaycheck(empId)
        assertThat(pc).isNull()
    }

    /**
     * Hourly Employees
     */
    @Test
    fun testPaySingleHourlyEmployeeNoTimeCards()
    {
        val empId = 2
        val test = AddHourlyEmployee(empId, "Bill", "Home", 15.25F)
        test.execute()

        val pt = PaydayTransaction(payDate)
        pt.execute()

        validateHourlyPaycheck(pt, empId, payDate,0.0f)
    }

    @Test
    fun testPaySingleHourlyEmployeeOneTimeCard()
    {
        val empId = 2
        val t = AddHourlyEmployee(empId, "Bill", "home", 15.25f)
        t.execute()

        val tc = TimeCardTransaction(empId=empId, payDate=payDate, hours=2f)
        tc.execute()


        val pt = PaydayTransaction(df.parse("11/9/2001"))
        pt.execute()
        validateHourlyPaycheck(pt, empId, payDate, 30.5f)
    }

    @Test
    fun testPaySingleHourlyEmployeeOvertimeOneTimeCard()
    {
        val empId = 2
        val t = AddHourlyEmployee(empId, "Bill", "Home", 15.25f)
        t.execute()

        val tc = TimeCardTransaction(empId=empId, payDate=payDate, hours=9.0f)
        tc.execute()

        val pt = PaydayTransaction(payDate)
        pt.execute()
        validateHourlyPaycheck(pt, empId, payDate, (8 + 1.5f) * 15.25f)
    }

    @Test
    fun testPaySingleHourlyEmployeeOnWrongDate()
    {
        val empId = 2
        val t = AddHourlyEmployee(empId, "bill", "Home", 15.25f)
        t.execute()

        val payDate = df.parse("11/8/2001")
        val tc = TimeCardTransaction(empId, payDate, 9.0f)
        tc.execute()

        val pt = PaydayTransaction(payDate)
        pt.execute()

        val pc = pt.getPaycheck(empId)
        assertThat(pc).isNull()
    }

    @Test
    fun testPaySingleHourlyEmployeeTwoTimeCards()
    {
        val empId = 2
        val t = AddHourlyEmployee(empId, "Bill", "Home", 15.25f)
        t.execute()

        val tc1 = TimeCardTransaction(empId, payDate, 2.0f)
        val tc2 = TimeCardTransaction(empId, df.parse("11/8/2001"), 5.0f)
        tc1.execute()
        tc2.execute()

        val pt = PaydayTransaction(payDate)
        pt.execute()

        validateHourlyPaycheck(pt, empId, payDate, 7*15.25f)
    }

    @Test
    fun testPaySingleHourlyEmployeeWithTimeCardsSpanningTwoPayPeriods()
    {
        val empId = 2
        val t = AddHourlyEmployee(empId, "Bill", "Home", 15.25f)
        t.execute()

        val dateInPreviousPayPeriod = df.parse("11/2/2001")

        val tc = TimeCardTransaction(empId, payDate, 2.0f)
        tc.execute()

        val tc2 = TimeCardTransaction(empId, dateInPreviousPayPeriod, 5.0f)
        tc2.execute()

        val pt = PaydayTransaction(payDate)
        pt.execute()

        validateHourlyPaycheck(pt, empId, payDate, 2*15.25f)
    }


    // utility function for testing hourly employees!
    private fun validateHourlyPaycheck(pt: PaydayTransaction, empId: Int, payDate: Date, pay: Float)
    {
        val pc = pt.getPaycheck(empId)
        assertThat(pc).isNotNull()

        pc?.let {
            assertThat(it.payDate).isEqualTo(payDate)
            assertThat(it.grossPay).isWithin(slop).of(pay)
            assertThat(it.getField("Disposition")).isEqualTo("Hold")
            assertThat(it.deductions).isWithin(slop).of(0.0f)
            assertThat(it.netPay).isWithin(slop).of(pay)
        }
    }

    private fun validateCommissionPaycheck(pt: PaydayTransaction, empId: Int, payDate: Date, pay: Float) = validateHourlyPaycheck(pt, empId, payDate, pay)

    /**
     * Commissioned Employees
     */
    @Test
    fun biweeklyTest()
    {
        val bs = BiweeklySchedule()
        val isPayDay = bs.isPayDay(df.parse("2/25/2005"))
        debug("is 2/25/2005 payDay for Commissioned employees? $isPayDay")
    }

    @Test
    fun testPayNoSalesReceiptCommissionedEmployees()
    {
        val empId = 2
        val t = AddCommissionedEmployee(empId, "Thomas", "Home", 567_922f, 2.5f)
        t.execute()

        val payDate = df.parse("2/25/2005")
        val pt = PaydayTransaction(payDate)
        pt.execute()

        validateCommissionPaycheck(pt, empId, payDate, 567_922f)
    }

    @Test
    fun `testPay NoSalesReceipt FirstDayOfMonthOnFriday CommissionedEmployee`()
    {
        val empId = 2
        val t = AddCommissionedEmployee(empId, "Thomas", "Home", 567_922f, 2.5f)
        t.execute()

        val payDate = df.parse("11/8/2019")
        val pt = PaydayTransaction(payDate)
        pt.execute()

        validateCommissionPaycheck(pt, empId, payDate, 567_922f)
    }

    @Test
    fun `testPay SingleSalesReceipt FirstDayOfMonthOnSat CommissionedEmployee`()
    {
        val empId = 2
        val t = AddCommissionedEmployee(empId, "Thomas", "Home", 4_000f, 3.4f)
        t.execute()

        val payDate = df.parse("6/14/2019")  // sat
        val sr = SalesReceiptTransaction(empId, df.parse("6/12/2019"), 1_000f)
        sr.execute()

        val pt = PaydayTransaction(payDate)
        pt.execute()

        validateCommissionPaycheck(pt, empId, payDate, 4_000f + (1_000f * 3.4f))
    }

    @Test
    fun `testPay SingleSalesReceipt FirstDayOfMonthOnSun CommissionedEmployee`()
    {
        val empId = 2
        val t = AddCommissionedEmployee(empId, "Thomas", "Home", 4_000f, 3.4f)
        t.execute()

        // 9/01/2019 is Sunday
        val payDate = df.parse("9/13/2019")
        val sr = SalesReceiptTransaction(empId, df.parse("9/12/2019"), 1_000f)
        sr.execute()

        val pt = PaydayTransaction(payDate)
        pt.execute()

        validateCommissionPaycheck(pt, empId, payDate, 4_000f + (1_000f * 3.4f))
    }

    /**
     * Salaried Union Member Dues
     */
    @Test
    fun `test Salaried UnionMemberDues`()
    {
        val empId = 1
    }


    /**
     * companion object - static fields
     */
    companion object
    {

        private const val slop: Float = 0.001f

        // Testing Pay
        private val df = SimpleDateFormat("MM/dd/yyyy")
        private val payDate = df.parse("11/9/2001")
    }
}