package com.dropbox.core.examples

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName

interface BaseGeneratedClass {
    val namespace: String
    val name: String
    val doc: String?
}

fun BaseGeneratedClass.toClassName(): ClassName {
    return ClassName(packageNameFromNamespace(namespace), name)
}

fun packageNameFromNamespace(namespaceName: String): String {
    return "com.dropbox.api.v2.kotlin.${namespaceName}"
}

fun Namespace.toPackageName(): String {
    return packageNameFromNamespace(this.name)
}


fun BaseGeneratedClass.getPackageName(): String {
    return packageNameFromNamespace(namespace)
}


data class EnumClass(
    override val namespace: String,
    override val name: String,
    override val doc: String?,
    val enumFields: List<EnumField>,
) : BaseGeneratedClass

data class EnumField(
    val name: String,
    val doc: String?,
)

data class SuperType(
    override val namespace: String,
    override val name: String,
    override val doc: String? = null,
) : BaseGeneratedClass {
    fun toClassName(): ClassName {
        return ClassName(packageNameFromNamespace(namespace), name)
    }
}

data class DataClass(
    override val namespace: String,
    override val name: String,
    override val doc: String?,
    val superType: SuperType?,
    val fields: List<DataClassField>
) : BaseGeneratedClass {
    fun isInterface(): Boolean {
        return superType == null
    }
}

data class DataClassField(
    val name: String,
    val type: TypeName,
    val doc: String?,
    val type_info: TypeInfo
)


data class SealedClass(
    override val namespace: String,
    override val name: String,
    override val doc: String?,
    val dataType: DataType
) : BaseGeneratedClass
