package com.newbieeming.hookhyper.ksp

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.WildcardTypeName
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.ksp.writeTo

private const val ANNOTATION_NAME = "com.newbieeming.hookhyper.core.hook.HookModule"
private const val AGGREGATE_FILE = "hookhyper-registrars.txt"
private const val HOOK_PACKAGE = "com.newbieeming.hookhyper.core.hook"
private const val HOOKER_CLASS = "FeatureHooker"
private const val REGISTRAR_CLASS = "FeatureRegistrar"

/*
 * Generated registries depend on every annotated class in their module.
 *
 * Marking them as aggregating is essential: an incremental KSP round may otherwise expose
 * only the changed symbol, which would replace a registry with that single HookModule.
 */
private val AGGREGATING_DEPENDENCIES = Dependencies(aggregating = true)

class HookModuleProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor = HookModuleProcessor(
        environment.codeGenerator,
        environment.logger,
        environment.options["hookhyper.aggregate"] == "true",
    )
}

private class HookModuleProcessor(
    private val codeGenerator: com.google.devtools.ksp.processing.CodeGenerator,
    private val logger: com.google.devtools.ksp.processing.KSPLogger,
    private val aggregate: Boolean,
) : SymbolProcessor {

    private var processed = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (processed) return emptyList()

        val symbols = resolver.getSymbolsWithAnnotation(ANNOTATION_NAME)
        val deferred = mutableListOf<KSAnnotated>()
        val entries = mutableListOf<HookEntry>()

        for (symbol in symbols) {
            if (!symbol.validate()) {
                deferred += symbol
            } else {
                parseEntry(symbol)?.let(entries::add)
            }
        }

        if (deferred.isNotEmpty()) return deferred

        if (entries.isNotEmpty()) {
            // Feature 模块：生成 HookRegistry + FeatureHooker + FeatureRegistrar
            processed = true
            generateFeatureModule(entries)
        } else if (aggregate) {
            // App 模块：读取聚合文件，生成 GeneratedHookEntry
            processed = true
            generateAppEntry()
        }

        return deferred
    }

    private fun parseEntry(symbol: KSAnnotated): HookEntry? {
        if (symbol !is KSClassDeclaration) {
            logger.error("@HookModule must annotate a class", symbol)
            return null
        }
        val annotation = symbol.annotations.first {
            it.annotationType.resolve().declaration.qualifiedName?.asString() == ANNOTATION_NAME
        }
        val targetPkg = annotation.arg("packageName") as? String
        if (targetPkg.isNullOrBlank()) {
            logger.error("@HookModule requires packageName", symbol)
            return null
        }
        val fqName = symbol.qualifiedName?.asString()
        if (fqName == null) {
            logger.error("Cannot resolve qualified name", symbol)
            return null
        }
        return HookEntry(targetPkg, symbol.packageName.asString(), ClassName.bestGuess(fqName))
    }

    private fun com.google.devtools.ksp.symbol.KSAnnotation.arg(name: String): Any? = arguments.firstOrNull { it.name?.asString() == name }?.value

    // ── Feature 模块 ──────────────────────────────────────────────

    private fun generateFeatureModule(entries: List<HookEntry>) {
        val genPkg = entries.first().classPkg
        val targetPkg = entries.first().targetPkg

        generateHookRegistry(genPkg, entries)
        generateHooker(genPkg, targetPkg)
        generateRegistrar(genPkg)
    }

    private fun generateHookRegistry(pkg: String, entries: List<HookEntry>) {
        val subHooker = ClassName(HOOK_PACKAGE, "SubHooker")
        val listType = List::class.asTypeName().parameterizedBy(subHooker)

        val code = CodeBlock.builder().add("listOf(\n").indent()
        for (entry in entries) {
            code.add("%T(),\n", entry.className)
        }
        code.unindent().add(")\n")

        val prop = PropertySpec.builder("modules", listType).initializer(code.build()).build()
        val obj = TypeSpec.objectBuilder("HookRegistry")
            .addKdoc("Auto-generated by @HookModule KSP processor. Do not edit manually.")
            .addProperty(prop)
            .build()

        FileSpec.builder(pkg, "HookRegistry").addType(obj).build()
            .writeTo(codeGenerator, AGGREGATING_DEPENDENCIES)
    }

    private fun generateHooker(genPkg: String, targetPkg: String) {
        val modularHooker = ClassName(HOOK_PACKAGE, "ModularHooker")
        val obj = TypeSpec.objectBuilder(HOOKER_CLASS)
            .superclass(modularHooker)
            .addSuperclassConstructorParameter("%S", "HookHyper")
            .addSuperclassConstructorParameter("%S", targetPkg)
            .addKdoc("Auto-generated. Main hooker for [%L].", targetPkg)
            .build()

        FileSpec.builder(genPkg, HOOKER_CLASS).addType(obj).build()
            .writeTo(codeGenerator, AGGREGATING_DEPENDENCIES)
    }

    private fun generateRegistrar(genPkg: String) {
        val registrarIface = ClassName(HOOK_PACKAGE, "Registrar")
        val modularHooker = ClassName(HOOK_PACKAGE, "ModularHooker")
        val hookerClass = ClassName(genPkg, HOOKER_CLASS)

        val obj = TypeSpec.objectBuilder(REGISTRAR_CLASS)
            .addSuperinterface(registrarIface)
            .addFunction(
                FunSpec.builder("hooker")
                    .addModifiers(com.squareup.kotlinpoet.KModifier.OVERRIDE)
                    .returns(modularHooker)
                    .addStatement("return %T", hookerClass)
                    .build(),
            )
            .addKdoc("Auto-generated. Discovered by ServiceLoader at runtime.")
            .build()

        FileSpec.builder(genPkg, REGISTRAR_CLASS).addType(obj).build()
            .writeTo(codeGenerator, AGGREGATING_DEPENDENCIES)

        // 写入聚合文件，供 app 模块读取
        codeGenerator.createNewFile(AGGREGATING_DEPENDENCIES, "", AGGREGATE_FILE, "")
            .bufferedWriter()
            .use { it.write("$genPkg.FeatureRegistrar\n") }
    }

    // ── App 模块 ──────────────────────────────────────────────────

    private fun generateAppEntry() {
        val registrars = readAggregationFile()
        if (registrars.isEmpty()) {
            logger.warn("No @HookModule registrars found. Ensure feature modules apply hook.module plugin.")
            return
        }

        val genPkg = "com.newbieeming.hookhyper.hook.gen"
        val registrarIface = ClassName(HOOK_PACKAGE, "Registrar")

        val listCode = CodeBlock.builder().add("listOf(\n").indent()
        for (fqn in registrars) {
            listCode.add("%T::class.java,\n", ClassName.bestGuess(fqn))
        }
        listCode.unindent().add(")\n")

        val listType = List::class.asTypeName().parameterizedBy(
            Class::class.asTypeName().parameterizedBy(WildcardTypeName.producerOf(registrarIface)),
        )

        val registerFun = FunSpec.builder("register")
            .addParameter("context", ClassName(HOOK_PACKAGE, "HookContext"))
            .addStatement(
                "registrars.forEach { it.getDeclaredConstructor().newInstance().hooker().onHook(context) }",
            )
            .build()

        val obj = TypeSpec.objectBuilder("GeneratedHookEntry")
            .addKdoc("Auto-generated. Aggregates all feature module hookers.")
            .addProperty(
                PropertySpec.builder("registrars", listType)
                    .initializer(listCode.build())
                    .build(),
            )
            .addFunction(registerFun)
            .build()

        FileSpec.builder(genPkg, "GeneratedHookEntry").addType(obj).build()
            .writeTo(codeGenerator, AGGREGATING_DEPENDENCIES)
    }

    private fun readAggregationFile(): List<String> {
        // 从 feature 模块所有已生成的变体输出读取。不能把路径固定为 debug，
        // 否则 release 构建无法生成 GeneratedHookEntry。
        val rootDir = System.getProperty("user.dir")
        return listOf("feature/systemui", "feature/settings").flatMap { path ->
            val kspOutputDir = java.io.File(rootDir, "$path/build/generated/ksp")
            kspOutputDir.listFiles()
                .orEmpty()
                .map { variantDir -> java.io.File(variantDir, "resources/$AGGREGATE_FILE") }
                .filter(java.io.File::isFile)
                .flatMap { file -> file.readLines().map(String::trim).filter(String::isNotBlank) }
        }.distinct()
    }
}

private data class HookEntry(
    val targetPkg: String,
    val classPkg: String,
    val className: ClassName,
)
