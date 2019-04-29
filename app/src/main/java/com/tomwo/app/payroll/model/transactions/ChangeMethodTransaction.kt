package com.tomwo.app.payroll.model.transactions

import com.tomwo.app.payroll.model.DirectMethod
import com.tomwo.app.payroll.model.Employee
import com.tomwo.app.payroll.model.HoldMethod
import com.tomwo.app.payroll.model.MailMethod
import com.tomwo.app.payroll.model.PaymentMethod
import com.tomwo.app.payroll.model.PayrollDatabase

abstract class ChangeMethodTransaction(empId: Int) : ChangeEmployeeTransaction(empId)
{
    protected abstract val method : PaymentMethod

    override fun change(e: Employee)
    {
        val employee = e.copy(method = method)
        PayrollDatabase.addEmployee(employee)
    }
}


class ChangeDirectTransaction(empId: Int, bank : String, account : Float) : ChangeMethodTransaction(empId)
{
    override val method = DirectMethod(bank, account)
}

class ChangeMailTransaction(empId: Int, address: String): ChangeMethodTransaction(empId)
{
    override val method = MailMethod(address)
}

class ChangeHoldTransaction(empId: Int): ChangeMethodTransaction(empId)
{
    override val method = HoldMethod()
}