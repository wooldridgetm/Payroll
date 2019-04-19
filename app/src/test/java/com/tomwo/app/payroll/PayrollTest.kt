package com.tomwo.app.payroll

import android.util.Log
import com.google.common.truth.Truth.assertThat
import com.tomwo.app.payroll.extensions.clazz
import com.tomwo.app.payroll.model.AddHourlyEmployee
import com.tomwo.app.payroll.model.AddSalariedEmployee
import com.tomwo.app.payroll.model.HoldMethod
import com.tomwo.app.payroll.model.HourlyClassification
import com.tomwo.app.payroll.model.MonthlySchedule
import com.tomwo.app.payroll.model.PaymentClassification
import com.tomwo.app.payroll.model.PayrollDatabase
import com.tomwo.app.payroll.model.SalariedClassification
import com.tomwo.app.payroll.model.WeeklySchedule
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
        Log.d(clazz(this), "android.util.Log is successfully mocked")
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
        assertThat(pc).isInstanceOf(HourlyClassification::class.java)
        val hc = pc as HourlyClassification
        assertThat(hc.hourlyRate).isWithin(.001).of(100.00)

        // schedule
        val schedule = e.schedule
        assertThat(schedule).isInstanceOf(WeeklySchedule::class.java)
        // schedule as? WeeklySchedule ?: throw AssertionError("schedule isn't of correct type 'Weekly Schedule'")

        assertThat(e.method).isInstanceOf(HoldMethod::class.java)
    }

    fun addCommissedEmployeeTest()
    {
        val empId = 3
        
    }

    companion object
    {
        private val TAG = PayrollTest::class.java.simpleName
    }

    /**
     * Sample Code - it's meant as a reference for future study
     */
    fun sampleUnitTest()
    {
        assertEquals(4, 2+2)
        val result = 2+2
        assertThat(result).isEqualTo(4)
    }
}