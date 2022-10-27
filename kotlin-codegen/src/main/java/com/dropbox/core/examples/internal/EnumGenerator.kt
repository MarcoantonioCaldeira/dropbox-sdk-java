package com.dropbox.core.examples.internal

import com.dropbox.core.examples.DataType
import com.dropbox.core.examples.EnumClass
import com.dropbox.core.examples.EnumField
import com.dropbox.core.examples.Namespace
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.TypeSpec

object EnumGenerator {
    fun generateEnumType(namespace: Namespace, dataType: DataType): EnumClass {
        val enumClass = EnumClass(
            namespace = namespace.name,
            name = dataType.name,
            doc = dataType.doc,
            enumFields = dataType.fields.map {
                EnumField(
                    name = it.name,
                    doc = it.doc
                )
            })

        return enumClass
    }


    fun genTypeSpec(enumClass: EnumClass): TypeSpec {
        return TypeSpec
            .enumBuilder(enumClass.name)
            .apply {
                enumClass.doc?.let {
                    addKdoc(CodeBlock.of(it))
                }
            }
            .apply {
                enumClass.enumFields.forEach {
                    addEnumConstant(it.name, TypeSpec.enumBuilder(it.name).apply {
                        it.doc?.let {
                            addKdoc(CodeBlock.of(it))
                        }
                    }.build())
                }
            }
            .build()
    }
}