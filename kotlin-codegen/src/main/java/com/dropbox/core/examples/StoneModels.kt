package com.dropbox.core.examples

import kotlinx.serialization.Serializable

@Serializable
data class Namespace(
    val name: String,
    val doc: String?,
    val types: List<DataType>
)

@Serializable
data class DataType(
    val name: String,
    val doc: String?,
    val type_info: TypeInfo?,
    val fields: List<DataType> = listOf(),
)

fun DataType.isEnum(): Boolean {
    if (type_info?.is_union_type == true) {
        return fields.all { field ->
            field.type_info?.is_void_type == true
        }
    } else {
        return false
    }
}

fun DataType.isSealedClass(): Boolean {
    if (type_info?.is_union_type == true && fields.isNotEmpty()) {
        return fields.any { field ->
            field.type_info?.is_void_type != true
        }
    } else {
        return false
    }
}

@Serializable
data class TypeInfo(
    val name: String,
    val is_struct_type: Boolean,
    val is_primitive_type: Boolean,
    val is_boolean_type: Boolean,
    val is_numeric_type: Boolean,
    val is_list_type: Boolean,
    val is_union_type: Boolean,
    val is_bytes_type: Boolean,
    val is_map_type: Boolean,
    val is_composite_type: Boolean,
    val is_nullable_type: Boolean,
    val is_string_type: Boolean,
    val is_void_type: Boolean,
    val is_timestamp_type: Boolean,
    val nullable_type_data: TypeInfo?,
    val list_item_type_data: TypeInfo?,
    val parent_type_data: DataType?,
    val namespace: String?
)
