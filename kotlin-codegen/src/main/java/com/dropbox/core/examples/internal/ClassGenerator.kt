package com.dropbox.core.examples.internal

import com.dropbox.core.examples.DEBUGGING
import com.dropbox.core.examples.DataClass
import com.dropbox.core.examples.DataClassField
import com.dropbox.core.examples.DataType
import com.dropbox.core.examples.Namespace
import com.dropbox.core.examples.SuperType
import com.dropbox.core.examples.TypeInfo
import com.dropbox.core.examples.packageNameFromNamespace
import com.dropbox.core.examples.toIndentString
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import kotlinx.serialization.Serializable


fun getNamespaceName(typeInfo: TypeInfo, fallbackNamespaceName: String): String {
    return typeInfo.namespace
        ?: typeInfo.nullable_type_data?.namespace
        ?: typeInfo.list_item_type_data?.namespace
        ?: fallbackNamespaceName
}

object ClassGenerator {
    fun generateClassType(namespace: Namespace, dataType: DataType): DataClass {
        return DataClass(
            superType = dataType.getSupertype(namespace.name),
            doc = dataType.doc,
            name = dataType.name,
            namespace = namespace.name,
            fields = dataType.fields.map { field ->
                val fieldNamespaceName = getNamespaceName(field.type_info!!, namespace.name)
                DataClassField(
                    name = field.name,
                    type = getKotlinPoetClassNameFromTypeInfo(
                        defaultNamespace = fieldNamespaceName,
                        defaultTypeInfo = field.type_info
                    ),
                    doc = field.doc,
                    type_info = field.type_info
                )
            }
        )
    }

    fun generateKotlinPoetTypeSpec(generatedClass: DataClass): TypeSpec {
        val classBuilder = TypeSpec
            .classBuilder(generatedClass.name)
            .addModifiers(KModifier.DATA)
            .addAnnotation(Serializable::class)

        classBuilder
            .apply {
                if (DEBUGGING) {
                    addKdoc(CodeBlock.of("\n" + generatedClass.toString().toIndentString()))
                }
                generatedClass.doc?.let {
                    addKdoc(CodeBlock.of(it))
                }
                val constructorBuilder = FunSpec.constructorBuilder()
                generatedClass.fields.forEach { field: DataClassField ->
                    val namespaceName = getNamespaceName(field.type_info, generatedClass.namespace)


                    val parameter = ParameterSpec.builder(
                        name = field.name,
                        type = getKotlinPoetClassNameFromTypeInfo(
                            defaultNamespace = namespaceName,
                            defaultTypeInfo = field.type_info
                        )
                    ).apply {
                        if(field.type_info.is_nullable_type){
                            this.defaultValue("null")
                        }
                    }.build()
                    constructorBuilder.addParameter(parameter)
                    addProperty(
                        PropertySpec
                            .builder(
                                name = field.name,
                                type = getKotlinPoetClassNameFromTypeInfo(
                                    defaultNamespace = getNamespaceName(field.type_info, generatedClass.namespace),
                                    defaultTypeInfo = field.type_info
                                )
                            )
                            .apply {
                                field.doc?.let {
                                    addKdoc(CodeBlock.of(it))
                                }

//                                if (CodegenStateRepo.isFieldDefinedInSuperType(generatedClass.superType, field.name)) {
//                                    addModifiers(KModifier.OVERRIDE)
////                                    classBuilder.addSuperclassConstructorParameter(field.name)
//                                } else {
////                                    addModifiers(KModifier.OPEN)
//                                }


//                                if (!generatedClass.isInterface()) {
                                initializer(field.name)
//                                }
                            }
                            .build()
                    )
                }

//                if (!generatedClass.isInterface()) {
                primaryConstructor(constructorBuilder.build())
//                }
            }

        return classBuilder.build()
    }

    fun getKotlinPoetClassNameFromTypeInfo(
        defaultNamespace: String,
        defaultTypeInfo: TypeInfo
    ): TypeName {
        val typeName: String = defaultTypeInfo.nullable_type_data?.name ?: defaultTypeInfo.name
        val result: TypeName = when (typeName) {
            "List" -> {
                val typeInfo: TypeInfo? = defaultTypeInfo.nullable_type_data ?: defaultTypeInfo.list_item_type_data
                if (typeInfo != null) {
                    val namespaceName = getNamespaceName(typeInfo, defaultNamespace)
                    val listItemType: TypeName = getKotlinPoetClassNameFromTypeInfo(namespaceName, typeInfo)
                    List::class.asClassName().parameterizedBy(listItemType)
                } else {
                    List::class.asClassName().parameterizedBy(String::class.asClassName())
                }
            }

            "UInt32",
            "UInt64",
            "Int64",
            "Timestamp" -> Long::class.asClassName()

            "Int32" -> Int::class.asClassName()

            "Float64" -> Double::class.asClassName()

            "String" -> String::class.asClassName()
            else -> {
                val namespaceName = getNamespaceName(defaultTypeInfo, defaultNamespace)
                ClassName(packageNameFromNamespace(namespaceName), typeName)
            }
        }
        return result.copy(nullable = defaultTypeInfo.is_nullable_type)
    }

}

private fun DataType.getSupertype(namespace: String): SuperType? {
    val dataType = this
    return if (dataType.type_info?.parent_type_data != null) {
        SuperType(namespace, dataType.type_info.parent_type_data.name)
    } else {
        null
    }
}