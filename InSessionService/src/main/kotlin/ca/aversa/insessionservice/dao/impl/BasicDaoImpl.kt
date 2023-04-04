package ca.aversa.insessionservice.dao.impl

import ca.aversa.insessionservice.dao.BasicDao
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression
import com.amazonaws.services.dynamodbv2.datamodeling.KeyPair
import com.amazonaws.services.dynamodbv2.model.AttributeValue

class BasicDaoImpl<T: Any>(

    private val mapper: DynamoDBMapper,
    private val classType: Class<T>,
    private val tableName: String

): BasicDao<T> {

    override fun update(row: T) {
        mapper.save(row)
    }

    override fun get(hashKey: String): T? {
        return mapper.load(classType, hashKey)
    }

    override fun get(hashKey: String, rangeKey: String): T? {
        return mapper.load(classType, hashKey, rangeKey)
    }

    @Suppress("UNCHECKED_CAST")
    override fun batchLoad(hashKeys: Collection<String>): List<T> {
        val results = mapper.batchLoad(
            mapOf(classType to createKeyPairs(hashKeys))
        )
        val rows = results[tableName] ?: emptyList()

        return rows as List<T>
    }

    @Suppress("UNCHECKED_CAST")
    override fun queryGsi(indexName: String,
                          keyConditionExpression: String, valuesMap: Map<String, AttributeValue>): List<T> {
        val queryExpression = DynamoDBQueryExpression<T>()
            .withIndexName(indexName)
            .withConsistentRead(false)
            .withKeyConditionExpression(keyConditionExpression)
            .withExpressionAttributeValues(valuesMap)

        val results = mapper.query(classType, queryExpression)

        return results.toArray().toList() as List<T>
    }

    @Suppress("UNCHECKED_CAST")
    override fun query(keyConditionExpression: String, valuesMap: Map<String, AttributeValue>,
                       filterExpression: String?): List<T> {
        val queryExpression = DynamoDBQueryExpression<T>()
            .withConsistentRead(false)
            .withKeyConditionExpression(keyConditionExpression)
            .withExpressionAttributeValues(valuesMap)
            .withFilterExpression(filterExpression)

        val results = mapper.query(classType, queryExpression)

        return results.toArray().toList() as List<T>
    }

    override fun delete(row: T) {
        return mapper.delete(row)
    }

    override fun batchSave(objects: List<T>) {
        mapper.batchSave(objects)
    }

    override fun batchDelete(objects: List<T>) {
        mapper.batchDelete(objects)
    }

    private fun createKeyPairs(hashKeys: Collection<String>): List<KeyPair> {
        val keyPairs = mutableListOf<KeyPair>()

        hashKeys.forEach { key ->
            keyPairs.add(
                KeyPair().withHashKey(key)
            )
        }

        return keyPairs
    }
}
