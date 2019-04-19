package com.tomwo.app.payroll

import android.util.Log
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test


class ExampleUnitTest
{
    @Before
    fun setUp()
    {
        Log.d("ExampleUnitTest", "Fx setUp()")
    }

    @Test
    fun addition_isCorrect()
    {
        val result = 2+2
        assertEquals(4, 2 + 2)
        assertThat(result).isEqualTo(4)

    }

    @After
    fun dispose()
    {
        Log.d("ExampleUnitTest", "Fx dispose()")
    }
}
