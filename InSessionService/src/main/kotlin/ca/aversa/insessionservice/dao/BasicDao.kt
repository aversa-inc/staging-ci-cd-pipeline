package ca.aversa.insessionservice.dao

import com.amazonaws.services.dynamodbv2.model.AttributeValue

interface BasicDao<T : Any> {

    fun update(row: T)
    fun get(hashKey: String): T?
    fun get(hashKey: String, rangeKey: String): T?
    fun batchLoad(hashKeys: Collection<String>): List<T>
    fun queryGsi(indexName: String, keyConditionExpression: String, valuesMap: Map<String, AttributeValue>): List<T>
    fun query(keyConditionExpression: String, valuesMap: Map<String, AttributeValue>, filterExpression: String? = null): List<T>
    fun delete(row: T)
    fun batchDelete(objects: List<T>)
    fun batchSave(objects: List<T>)
}
