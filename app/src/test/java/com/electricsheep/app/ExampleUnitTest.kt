package com.electricsheep.app

import org.junit.Test
import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun `should calculate addition correctly`() {
        // Arrange
        val operand1 = 2
        val operand2 = 2
        val expectedResult = 4
        
        // Act
        val result = operand1 + operand2
        
        // Assert
        assertEquals(expectedResult, result)
    }
}

