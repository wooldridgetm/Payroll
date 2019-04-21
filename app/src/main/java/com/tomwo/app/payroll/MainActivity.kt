package com.tomwo.app.payroll

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.tomwo.app.payroll.extensions.clazzName
import com.tomwo.app.payroll.extensions.debug

class MainActivity : AppCompatActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //debug<MainActivity>("Fx onCreate()")


        debug(clazzName(this))
    }
}
