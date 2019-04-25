package com.tomwo.app.payroll

import android.util.Log
import com.google.common.truth.Truth.assertThat
import com.tomwo.app.payroll.extensions.clazz
import com.tomwo.app.payroll.extensions.clazzName
import com.tomwo.app.payroll.model.*
import com.tomwo.app.payroll.model.transactions.*
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

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
        val test = AddSalariedEmployee(empId, "Bob", "Home", 1000.00)
        test.execute()

        val employee = PayrollDatabase.getEmployee(empId)
        assertThat(employee?.name ?: "").isEqualTo("Bob")

        // if we've gotten to this point, then the last test passed & we know it's not null
        val e = employee!!

        val pc : PaymentClassification = e.classification
        assertThat(pc).isInstanceOf(SalariedClassification::class.java)

        val sc = pc as SalariedClassification

        assertThat(sc.salary).isWithin(.001).of(1000.00)

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
        val test = AddHourlyEmployee(empId, "Thomas", "1600 Pennsylvania Ave", 100.00)
        test.execute()

        val employee = PayrollDatabase.getEmployee(empId)
        assertThat(employee?.name ?: "").isEqualTo("Thomas")

        val e = employee!!

        // test the hourly rate
        val pc = e.classification
        //assertThat(pc).isInstanceOf(HourlyClassification::class.java)
        assertThat(pc).isInstanceOf(clazz<HourlyClassification>())
        val hc = pc as HourlyClassification
        assertThat(hc.hourlyRate).isWithin(.001).of(100.00)

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
        val test = AddCommissionedEmployee(empId, "Matthew", "152 Lakewood Drive", 1_000_000.00, 0.03)
        test.execute()

        val employee = PayrollDatabase.getEmployee(empId)

        // test 1 - Employee Name
        assertThat(employee?.name ?: "").isEqualTo("Matthew")
        val e = employee!!

        // test 2 - Payment Classification
        val pc = e.classification
        assertThat(pc).isInstanceOf(clazz<CommissionedClassification>())
        val sc = pc as CommissionedClassification
        assertThat(sc.salary).isWithin(.001).of(1_000_000.00)
        assertThat(sc.commissionRate).isEqualTo(0.03)

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
        val t = AddCommissionedEmployee(empId, "Lance", "Home", 2500.toDouble(), 3.2)
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
        val t = AddHourlyEmployee(empId, "Bill", "Home", 15.25)
        t.execute()

        val test1 = TimeCardTransaction(empId, 20011031, 8.0)
        test1.execute()

        val employee = PayrollDatabase.getEmployee(empId)
        assertThat(employee).isNotNull()

        val e = employee!!
        val pc = e.classification
        assertThat(pc).isInstanceOf(clazz<HourlyClassification>())
        val hc = pc as HourlyClassification
        val tc = hc.getTimeCard(20011031)
        assertThat(tc).isNotNull()

        assertThat(tc!!.hours).isEqualTo(8.0)
    }

    @Test
    fun testSalesReceiptTransaction()
    {
        val empId = 10
        val t = AddCommissionedEmployee(empId, "Sonya", "90210 Beverly Hills", 1_000.00, 2.3)
        t.execute()

        val test1 = SalesReceiptTransaction(empId, 20190422, 9.0)
        test1.execute()

        val employee = PayrollDatabase.getEmployee(empId)
        assertThat(employee).isNotNull()

        val e = employee!!
        val pc = e.classification
        assertThat(pc).isInstanceOf(clazz<CommissionedClassification>())
        val cc = pc as CommissionedClassification
        val sr = cc.getSalesReceipt(20190422)
        assertThat(sr).isNotNull()
        assertThat(sr!!.amount).isEqualTo(9.0)
    }

    @Test
    fun testAddServiceChargeTransaction()
    {
        val empId = 2
        val t = AddHourlyEmployee(empId, "Bill", "home", 15.25)
        t.execute()
        val employee = PayrollDatabase.getEmployee(empId)
        assertThat(employee).isNotNull()
        val e = employee!!

        val af = UnionAffiliation(12.5)
        e.affiliation = af

        val memberId = 86 // Maxwell Smart
        PayrollDatabase.addUnionMember(memberId, e)
        val sct = ServiceChargeTransaction(memberId, 20011101, 12.95)
        sct.execute()

        val sc = af.getServiceCharge(20011101)
        assertThat(sc).isNotNull()

        assertThat(sc!!.amount).isEqualTo(12.95)
    }

    @Test
    fun testChangeNameTransaction()
    {
        val empId = 2
        val t = AddHourlyEmployee(empId, "Bill", "Home", 15.25)
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
        val t = AddHourlyEmployee(empId, "Bill", "Home", 15.25)
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
        val t = AddCommissionedEmployee(empId, "Lance", "Home", 2500.0, 3.2)
        t.execute()

        val test = ChangeHourlyTransaction(empId, 27.52)
        test.execute()

        val employee = PayrollDatabase.getEmployee(empId)
        assertThat(employee).isNotNull()
        val e = employee!!

        val pc = e.classification
        assertThat(pc).isNotNull()
        assertThat(pc).isInstanceOf(clazz<HourlyClassification>())
        val hc = pc as HourlyClassification
        assertThat(hc.hourlyRate).isWithin(.001).of(27.52)

        val ps = e.schedule
        assertThat(ps).isNotNull()
        assertThat(ps).isInstanceOf(clazz<WeeklySchedule>())
    }

    @Test
    fun testChangeSalaryTransaction()
    {
        val empId = 100;
        val t = AddHourlyEmployee(empId, "Thomas", "Home", 100.0)
        t.execute()

        // here's the test
        val test = ChangeSalariedTransaction(empId, 1_000_000.0)
        test.execute()

        val employee = PayrollDatabase.getEmployee(empId)
        assertThat(employee).isNotNull()
        val e = employee!!

        val pc = e.classification
        assertThat(pc).isNotNull()
        assertThat(pc).isInstanceOf(clazz<SalariedClassification>())
        val sc = pc as SalariedClassification

        assertThat(sc.salary).isWithin(0.001).of(1_000_000.0)

        val ps = e.schedule
        assertThat(ps).isNotNull()
        assertThat(ps).isInstanceOf(clazz<MonthlySchedule>())
    }

    @Test
    fun testChangeCommissionedTransaction()
    {
        val empId = 967
        val t = AddSalariedEmployee(empId, "Thomas", "home", 567_920.0)
        t.execute()

        val test = ChangeCommissionedTransaction(empId, 3.2, 123_000.0)
        test.execute()

        val employee = PayrollDatabase.getEmployee(empId)
        assertThat(employee).isNotNull()
        val e = employee!!

        val pc = e.classification
        assertThat(pc).isNotNull()
        assertThat(pc).isInstanceOf(clazz<CommissionedClassification>())
        val cc = pc as CommissionedClassification

        assertThat(cc.commissionRate).isEqualTo(3.2)
        assertThat(cc.salary).isEqualTo(123_000.0)

        val ps = e.schedule
        assertThat(ps).isNotNull()
        assertThat(ps).isInstanceOf(clazz<BiweeklySchedule>())
    }
}