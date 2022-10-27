package com.dropbox.core.examples

import java.lang.RuntimeException

object CodegenStateRepo {
    private val types = mutableMapOf<String, BaseGeneratedClass>()

    fun addType(type: BaseGeneratedClass) {
        val namespacePlusName = namespacePlusName(type.namespace, type.name)
        if(namespacePlusName.contains("sharing.GroupSummary")){
            throw RuntimeException(namespacePlusName)
        }
        types[namespacePlusName] = type
    }

    fun getType(namespace: String, name: String): BaseGeneratedClass? {

//        println("Looking for ${namespacePlusName(namespace, name)}")
        return types[namespacePlusName(namespace, name)] ?: return types[namespacePlusName(
            packageNameFromNamespace(namespace),
            name
        )]
    }

    fun getType(base: BaseGeneratedClass): BaseGeneratedClass? {
        return getType(base.namespace, base.name)
    }

    private fun namespacePlusName(namespace: String, name: String): String {
        return "${namespace}.${name}"
    }

    fun getAllTypes(): List<BaseGeneratedClass> {
        return types.values.toList()
    }

    fun isFieldDefinedInSuperType(superType: SuperType?, name: String): Boolean {
        if (superType == null) {
            return false
        }

        val type = getType(superType.namespace, superType.name)
        if (type is DataClass) {
            return type.fields.any { it.name == name }
        }
        return false
    }
}