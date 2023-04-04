package ca.aversa.insessionservice.util

object BasicDaoTestUtils {

    const val HASH_KEY = "hashkey"
    const val RANGE_KEY = "rangekey"

    fun createSampleBasicDaoTableRow(hashKey: String = HASH_KEY, rangeKey: String = RANGE_KEY): BasicDaoTableRow {
        return BasicDaoTableRow(hashKey, rangeKey)
    }
}