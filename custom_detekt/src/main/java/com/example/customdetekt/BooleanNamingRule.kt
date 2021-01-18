package com.example.customdetekt

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.rules.identifierName
import org.jetbrains.kotlin.js.descriptorUtils.getJetTypeFqName
import org.jetbrains.kotlin.psi.KtCallableDeclaration
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.typeBinding.createTypeBindingForReturnType

class BooleanNamingRule(config: Config = Config.empty) : Rule(config) {

    companion object {
        const val BOOLEAN_TYPE_NAME = "Boolean"
    }

    override val issue = Issue(javaClass.simpleName, Severity.CodeSmell,
        "Boolean property name should starts with 'is/has/should/need/was' prefix.",
        Debt.FIVE_MINS)

    override fun visitParameter(parameter: KtParameter) {
        super.visitParameter(parameter)
        validateDeclaration(parameter)
    }

    override fun visitProperty(property: KtProperty) {
        super.visitProperty(property)
        validateDeclaration(property)
    }

    private fun validateDeclaration(declaration: KtCallableDeclaration) {
        val typeName = getTypeName(declaration)
        val name = declaration.identifierName()

        if (bindingContext == BindingContext.EMPTY || typeName == null) {
            return
        }

        if (!name.startsWithAllowedWords() && typeName.contains(BOOLEAN_TYPE_NAME)) {
            report(reportCodeSmell(declaration, name, typeName.toString()))
        }
    }

    private fun String.startsWithAllowedWords(): Boolean {
        return startsWith("is")
                || startsWith("has")
                || startsWith("should")
                || startsWith("need")
                || startsWith("was")
    }

    private fun reportCodeSmell(
        declaration: KtCallableDeclaration,
        name: String,
        typeName: String
    ): CodeSmell {
        return CodeSmell(
            issue,
            Entity.from(declaration),
            message = "Boolean properties should start with 'is/has/should/need/was' prefix. " +
                    "Actual type of $name: $typeName"
        )
    }

    private fun getTypeName(parameter: KtCallableDeclaration): String? {
        return parameter.createTypeBindingForReturnType(bindingContext)
            ?.type
            ?.getJetTypeFqName(false)
    }
}