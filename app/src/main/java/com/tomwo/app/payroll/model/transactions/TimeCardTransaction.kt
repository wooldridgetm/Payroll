package com.tomwo.app.payroll.model.transactions

class TimeCardTransaction(private val empId: Int, private val date : Long, private val hours: Double): Transaction
{
    override fun execute()
    {
        TODO("not implemented")
    }
}