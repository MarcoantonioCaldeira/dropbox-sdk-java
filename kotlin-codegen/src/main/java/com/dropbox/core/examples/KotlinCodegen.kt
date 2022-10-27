package com.dropbox.core.examples

import com.dropbox.core.examples.JsonSerializer.JSON
import com.dropbox.core.examples.internal.ClassGenerator
import com.dropbox.core.examples.internal.EnumGenerator
import com.dropbox.core.examples.internal.SealedClassGenerator
import com.squareup.kotlinpoet.FileSpec
import java.io.File

val DEBUGGING = false

class KotlinCodegen {

    fun generate(namespaceName: String) {

        val folder = File("dropbox-sdk-java/kotlin/build/").apply { mkdirs() }
        val jsonFile = File(folder, "$namespaceName.json")
        val fileContents = jsonFile.readText()
        val namespace = JSON.decodeFromString(Namespace.serializer(), fileContents)
        val jsonContents = JSON.encodeToString(Namespace.serializer(), namespace)
//        println(jsonContents)
        File("dropbox-sdk-java/kotlin/build/${namespaceName}1.json").writeText(jsonContents)

        // Parse & Process
        namespace.types.map {
            val generatedClass = generateTypeInNamespace(namespace, it)
            generatedClass?.let {
                CodegenStateRepo.addType(generatedClass)
            }
        }
    }

    private fun generateTypeInNamespace(namespace: Namespace, dataType: DataType): BaseGeneratedClass? {
        return if (dataType.isEnum()) {
            EnumGenerator.generateEnumType(namespace, dataType)
        } else if (dataType.isSealedClass()) {
            SealedClass(
                name = dataType.name,
                namespace = namespace.name,
                doc = dataType.doc,
                dataType = dataType
            )
        } else {
            ClassGenerator.generateClassType(namespace, dataType)
        }
    }
}

fun main() {
    KotlinCodegen().generate("account")
    KotlinCodegen().generate("check")
    KotlinCodegen().generate("common")
//    KotlinCodegen().generate("contacts")
//    KotlinCodegen().generate("file_properties")
//    KotlinCodegen().generate("file_requests")
//    KotlinCodegen().generate("files")
//    KotlinCodegen().generate("openid")
//    KotlinCodegen().generate("paper")
//    KotlinCodegen().generate("secondary_emails")
//    KotlinCodegen().generate("seen_state")
//    KotlinCodegen().generate("sharing")
//    KotlinCodegen().generate("team")
    KotlinCodegen().generate("team_common")
//    KotlinCodegen().generate("team_log")
//    KotlinCodegen().generate("team_policies")
//    KotlinCodegen().generate("users")
    KotlinCodegen().generate("users_common")


    val outputDir = File("/Users/samedwards/src/dropbox-sdk-java/kotlin-codegen/src/main/java")

    // Generate Code
    CodegenStateRepo.getAllTypes().forEach {

        val fileSpecBuilder = FileSpec
            .builder(it.getPackageName(), it.name)
        when (it) {
            is DataClass -> {
                fileSpecBuilder
                    .addType(ClassGenerator.generateKotlinPoetTypeSpec(it))
                    .build()
                    .writeTo(outputDir)
            }

            is EnumClass -> {
                fileSpecBuilder
                    .addType(EnumGenerator.genTypeSpec(it))
                    .build()
                    .writeTo(outputDir)
            }

            is SealedClass -> {
                SealedClassGenerator.generateSealedClass(fileSpecBuilder, it.namespace, it.dataType)
                fileSpecBuilder
                    .build()
                    .writeTo(outputDir)
            }
        }
    }

    println("Done")
}