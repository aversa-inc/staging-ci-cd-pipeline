package ca.aversa.insessionservice.dao.impl

import ca.aversa.insessionservice.dao.BasicDao
import ca.aversa.insessionservice.util.BasicDaoTableRow
import ca.aversa.insessionservice.util.BasicDaoTestUtils
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.KeyPair
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class BasicDaoImplTest {

    private val mapper: DynamoDBMapper = mockk()
    private val tableName: String = "tablename"
    private val basicDao: BasicDao<BasicDaoTableRow> = BasicDaoImpl(mapper, BasicDaoTableRow::class.java, tableName)
    private val gsiIndexName = "someindex"
    private val gsiKeyExpression = "hashkey = :hashkey"
    private val gsiValuesMap = mapOf<String, AttributeValue>(
        ":hashkey" to AttributeValue().withS("somevalue")
    )

    @Test
    fun testAdd_SuccessfulAdd_ReturnsOtpAddedOtp() {
        val expectedRow = BasicDaoTestUtils.createSampleBasicDaoTableRow()

        justRun {
            mapper.save(expectedRow)
        }

        basicDao.update(expectedRow)

        verify {
            mapper.save(expectedRow)
        }
    }

    @Test
    fun testAdd_DynamoDbThrowsException_ThrowsException() {
        val expectedRow = BasicDaoTestUtils.createSampleBasicDaoTableRow()

        every {
            mapper.save(expectedRow)
        }.throws(RuntimeException("it broke lol"))

        assertThrows<Exception> {
            basicDao.update(expectedRow)
        }
    }

    @Test
    fun testGetHashKeyRangeKey_RowExistsInTable_ReturnsRow() {
        val expectedRow = BasicDaoTestUtils.createSampleBasicDaoTableRow()

        every {
            mapper.load(BasicDaoTableRow::class.java, BasicDaoTestUtils.HASH_KEY, BasicDaoTestUtils.RANGE_KEY)
        }.returns(expectedRow)

        val actualRow = basicDao.get(BasicDaoTestUtils.HASH_KEY, BasicDaoTestUtils.RANGE_KEY)

        assertNotNull(actualRow)
        assertEquals(BasicDaoTestUtils.HASH_KEY, actualRow!!.hashkey)
        assertEquals(BasicDaoTestUtils.RANGE_KEY, actualRow.rangeKey)
    }

    @Test
    fun testGetHashKeyRangeKey_RowDoesntExistInTable_ReturnNull() {
        every {
            mapper.load(BasicDaoTableRow::class.java, BasicDaoTestUtils.HASH_KEY, BasicDaoTestUtils.RANGE_KEY)
        }.returns(null)

        val actualRow = basicDao.get(BasicDaoTestUtils.HASH_KEY, BasicDaoTestUtils.RANGE_KEY)

        assertNull(actualRow)
    }

    @Test
    fun testGetHashKeyRangeKey_DynamoDbThrowsException_ThrowException() {
        every {
            mapper.load(BasicDaoTableRow::class.java, BasicDaoTestUtils.HASH_KEY, BasicDaoTestUtils.RANGE_KEY)
        }.throws(RuntimeException("it broke lol"))

        assertThrows<Exception> {
            basicDao.get(BasicDaoTestUtils.HASH_KEY, BasicDaoTestUtils.RANGE_KEY)
        }
    }

    @Test
    fun testGetHashKey_RowExistsInTable_ReturnsRow() {
        val expectedRow = BasicDaoTestUtils.createSampleBasicDaoTableRow()

        every {
            mapper.load(BasicDaoTableRow::class.java, BasicDaoTestUtils.HASH_KEY)
        }.returns(expectedRow)

        val actualRow = basicDao.get(BasicDaoTestUtils.HASH_KEY)

        assertNotNull(actualRow)
        assertEquals(BasicDaoTestUtils.HASH_KEY, actualRow!!.hashkey)
    }

    @Test
    fun testGetHashKey_RowDoesntExistInTable_ReturnNull() {
        every {
            mapper.load(BasicDaoTableRow::class.java, BasicDaoTestUtils.HASH_KEY)
        }.returns(null)

        val actualRow = basicDao.get(BasicDaoTestUtils.HASH_KEY)

        assertNull(actualRow)
    }

    @Test
    fun testGetHashKey_DynamoDbThrowsException_ThrowException() {
        every {
            mapper.load(BasicDaoTableRow::class.java, BasicDaoTestUtils.HASH_KEY)
        }.throws(RuntimeException("it broke lol"))

        assertThrows<Exception> {
            basicDao.get(BasicDaoTestUtils.HASH_KEY)
        }
    }

    @Test
    fun testBatchLoad_RowsExists_ReturnCastedListOfRows() {
        val expectedRow = BasicDaoTestUtils.createSampleBasicDaoTableRow()
        val expectedRowList = listOf(expectedRow, expectedRow)
        val hashKeys = listOf(expectedRow.hashkey)

        every {
            mapper.batchLoad(any<MutableMap<Class<*>, MutableList<KeyPair>>>())
        }.returns(
            mapOf(
                tableName to expectedRowList
            )
        )

        val actualRows = basicDao.batchLoad(hashKeys)

        assertEquals(expectedRowList, actualRows)
    }

    @Test
    fun testBatchLoad_NoRowsFound_ReturnEmptyList() {
        val expectedRow = BasicDaoTestUtils.createSampleBasicDaoTableRow()
        val hashKeys = listOf(expectedRow.hashkey)

        every {
            mapper.batchLoad(any<MutableMap<Class<*>, MutableList<KeyPair>>>())
        }.returns(
            mapOf(
                tableName to null
            )
        )

        val actualRows = basicDao.batchLoad(hashKeys)

        assertTrue(actualRows.isEmpty())
    }

    @Test
    fun testQueryGsi_ExpressionReturnsResults_ReturnResultsInArray() {
        val mockPaginatedQueryList: PaginatedQueryList<BasicDaoTableRow> = mockk()
        val expectedRows = arrayOf(BasicDaoTestUtils.createSampleBasicDaoTableRow())

        every {
            mockPaginatedQueryList.toArray()
        }.returns(expectedRows)

        every {
            mapper.query(BasicDaoTableRow::class.java, any())
        }.returns(mockPaginatedQueryList)

        val actualResultsList = basicDao.queryGsi(gsiIndexName, gsiKeyExpression, gsiValuesMap)

        assertEquals(expectedRows.toList(), actualResultsList)
    }

    @Test
    fun testQueryGsi_DynamoDbThrowsException_ThrowException() {
        every {
            mapper.query(BasicDaoTableRow::class.java, any())
        }.throws(RuntimeException("it broke lol"))

        assertThrows<Exception> {
            basicDao.queryGsi(gsiIndexName, gsiKeyExpression, gsiValuesMap)
        }
    }

    @Test
    fun testQuery_ExpressionReturnsResults_ReturnResultsInArray() {
        val mockPaginatedQueryList: PaginatedQueryList<BasicDaoTableRow> = mockk()
        val expectedRows = arrayOf(BasicDaoTestUtils.createSampleBasicDaoTableRow())

        every {
            mockPaginatedQueryList.toArray()
        }.returns(expectedRows)

        every {
            mapper.query(BasicDaoTableRow::class.java, any())
        }.returns(mockPaginatedQueryList)

        val actualResultsList = basicDao.query(gsiKeyExpression, gsiValuesMap)

        assertEquals(expectedRows.toList(), actualResultsList)
    }

    @Test
    fun testQuery_DynamoDbThrowsException_ThrowException() {
        every {
            mapper.query(BasicDaoTableRow::class.java, any())
        }.throws(RuntimeException("it broke lol"))

        assertThrows<Exception> {
            basicDao.query(gsiKeyExpression, gsiValuesMap)
        }
    }

    @Test
    fun testDelete_SuccessfullyDeleteRow_ReturnVoid() {
        val expectedRow = BasicDaoTestUtils.createSampleBasicDaoTableRow()

        justRun {
            mapper.delete(expectedRow)
        }

        basicDao.delete(expectedRow)
    }

    @Test
    fun testDelete_DynamoDbThrowsException_ThrowException() {
        val expectedRow = BasicDaoTestUtils.createSampleBasicDaoTableRow()

        every {
            mapper.delete(expectedRow)
        }.throws(RuntimeException("it broke"))

        assertThrows<Exception> {
            basicDao.delete(expectedRow)
        }
    }

    @Test
    fun testBatchSave_SuccessfullyAddRows_ReturnVoid() {
        val expectedRows = listOf(
            BasicDaoTestUtils.createSampleBasicDaoTableRow("hashkey1", "sortkey1"),
            BasicDaoTestUtils.createSampleBasicDaoTableRow("hashkey1", "sortkey2"),
            BasicDaoTestUtils.createSampleBasicDaoTableRow("hashkey1", "sortkey3")
        )

        every {
            mapper.batchSave(expectedRows)
        }.returns(emptyList())

        basicDao.batchSave(expectedRows)
    }

    @Test
    fun testBatchSave_DynamoDbThrowsException_ThrowException() {
        val expectedRows = listOf(
            BasicDaoTestUtils.createSampleBasicDaoTableRow("hashkey1", "sortkey1"),
            BasicDaoTestUtils.createSampleBasicDaoTableRow("hashkey1", "sortkey2"),
            BasicDaoTestUtils.createSampleBasicDaoTableRow("hashkey1", "sortkey3")
        )

        every {
            mapper.batchSave(expectedRows)
        }.throws(RuntimeException("something broke"))

        assertThrows<Exception> {
            basicDao.batchSave(expectedRows)
        }
    }

    @Test
    fun testBatchDelete_SuccessfullyDeleteRows_ReturnVoid() {
        val expectedRows = listOf(
            BasicDaoTestUtils.createSampleBasicDaoTableRow("hashkey1", "sortkey1"),
            BasicDaoTestUtils.createSampleBasicDaoTableRow("hashkey1", "sortkey2"),
            BasicDaoTestUtils.createSampleBasicDaoTableRow("hashkey1", "sortkey3")
        )

        every {
            mapper.batchDelete(expectedRows)
        }.returns(emptyList())

        basicDao.batchDelete(expectedRows)
    }

    @Test
    fun testBatchDelete_DynamoDbThrowsException_ThrowException() {
        val expectedRows = listOf(
            BasicDaoTestUtils.createSampleBasicDaoTableRow("hashkey1", "sortkey1"),
            BasicDaoTestUtils.createSampleBasicDaoTableRow("hashkey1", "sortkey2"),
            BasicDaoTestUtils.createSampleBasicDaoTableRow("hashkey1", "sortkey3")
        )

        every {
            mapper.batchDelete(expectedRows)
        }.throws(RuntimeException("something broke"))

        assertThrows<Exception> {
            basicDao.batchDelete(expectedRows)
        }
    }
}
