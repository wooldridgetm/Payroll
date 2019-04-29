package com.tomwo.app.payroll.model.transactions

import com.tomwo.app.payroll.extensions.debug
import com.tomwo.app.payroll.model.Affiliation
import com.tomwo.app.payroll.model.Employee
import com.tomwo.app.payroll.model.NoAffiliation
import com.tomwo.app.payroll.model.PayrollDatabase
import com.tomwo.app.payroll.model.UnionAffiliation

abstract class ChangeAffiliationTransaction(empId: Int): ChangeEmployeeTransaction(empId)
{
    protected abstract val affiliation: Affiliation

    override fun change(e: Employee)
    {
        recordMembership(e)
        e.affiliation = affiliation
    }
    protected abstract fun recordMembership(e: Employee)
}

class ChangeUnionMemberTransaction(empId: Int, private val memberId: Int, dues: Float): ChangeAffiliationTransaction(empId)
{
    override val affiliation: Affiliation = UnionAffiliation(memberId, dues)

    override fun recordMembership(e: Employee)
    {
        PayrollDatabase.addUnionMember(memberId, e)
    }
}

class ChangeUnaffiliatedTransaction(empId: Int) : ChangeAffiliationTransaction(empId)
{
    override val affiliation : Affiliation = NoAffiliation()

    /**
     * delete the [MemberId] in the [PayrollDatabase]
     */
    override fun recordMembership(e: Employee)
    {
        (e.affiliation as? UnionAffiliation)?.let {
            val memberId = it.memberId
            PayrollDatabase.removeUnionMember(memberId)
        }
    }
}

