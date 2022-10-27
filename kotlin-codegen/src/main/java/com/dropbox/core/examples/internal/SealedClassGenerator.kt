package com.dropbox.core.examples.internal

import com.dropbox.core.examples.CodegenStateRepo
import com.dropbox.core.examples.DEBUGGING
import com.dropbox.core.examples.DataType
import com.dropbox.core.examples.JsonSerializer
import com.dropbox.core.examples.Namespace
import com.dropbox.core.examples.SealedClass
import com.dropbox.core.examples.packageNameFromNamespace
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import java.lang.RuntimeException
import kotlinx.serialization.Serializable

object SealedClassGenerator {
    fun generateSealedClass(
        fileSpecBuilder: FileSpec.Builder,
        namespace: String,
        sealedInterfaceDataType: DataType
    ) {
        // ADD TO OUR REPO
        CodegenStateRepo.addType(
            SealedClass(
                name = sealedInterfaceDataType.name,
                namespace = namespace,
                doc = sealedInterfaceDataType.doc,
                dataType = sealedInterfaceDataType
            )
        )

        val sealedInterface = TypeSpec
            .interfaceBuilder(sealedInterfaceDataType.name)
            .addModifiers(KModifier.SEALED)
            .addAnnotation(Serializable::class)
            .apply {
                sealedInterfaceDataType.doc?.let {
                    addKdoc(CodeBlock.of(it))
                }
            }

        if (DEBUGGING) {
            sealedInterface.addKdoc(
                CodeBlock.of(
                    "\n" + JsonSerializer.JSON.encodeToString(
                        DataType.serializer(),
                        sealedInterfaceDataType
                    )
                )
            )
        }

        sealedInterfaceDataType.fields.forEach { fieldDataType ->
            val typeInfo = fieldDataType.type_info!!
            if (typeInfo.is_void_type) {
                val objectBuilder = TypeSpec.objectBuilder(fieldDataType.name)
                    .apply {
                        fieldDataType.doc?.let {
                            addKdoc(CodeBlock.of(it))
                        }
                    }
                    .addSuperinterface(
                        ClassName(packageNameFromNamespace(namespace), sealedInterfaceDataType.name)
                    )
                sealedInterface
                    .addType(objectBuilder.build())
            } else {
                val classBuilder = TypeSpec.classBuilder(fieldDataType.name)
                    .apply {
                        fieldDataType.doc?.let {
                            addKdoc(CodeBlock.of(it))
                        }
                        if (DEBUGGING) {
                            addKdoc(
                                CodeBlock.of(
                                    "\n" + JsonSerializer.JSON.encodeToString(
                                        DataType.serializer(),
                                        fieldDataType
                                    )
                                )
                            )
                        }
                    }
                    .addSuperinterface(
                        ClassName(packageNameFromNamespace(namespace), sealedInterfaceDataType.name)
                    )

                val constructorBuilder = FunSpec.constructorBuilder()

                fieldDataType.also { field ->
                    constructorBuilder.apply {
                        addParameter(
                            name = field.name,
                            type = ClassGenerator.getKotlinPoetClassNameFromTypeInfo(
                                defaultNamespace = getNamespaceName(field.type_info!!, namespace),
                                defaultTypeInfo = field.type_info
                            )
                        )
                    }

                    classBuilder.addProperty(
                        PropertySpec
                            .builder(
                                name = field.name,
                                type = ClassGenerator.getKotlinPoetClassNameFromTypeInfo(
                                    defaultNamespace = getNamespaceName(field.type_info!!, namespace),
                                    defaultTypeInfo = field.type_info
                                )
                            )
                            .initializer(field.name)
                            .build()
                    )
                }
                classBuilder.primaryConstructor(constructorBuilder.build())
                sealedInterface.addType(classBuilder.build())

            }
        }
        fileSpecBuilder
            .addType(sealedInterface.build())
    }

}