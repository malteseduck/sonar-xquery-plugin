package org.sonar.plugins.xquery.parser

import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.misc.Utils
import org.antlr.v4.runtime.tree.Tree
import org.antlr.v4.runtime.tree.Trees.getNodeText
import org.assertj.core.internal.Failures
import org.sonar.plugins.xquery.parser.XQueryParser.*
import kotlin.reflect.KClass

fun ParserRuleContext.getName(): String = XQueryParser.ruleNames[this.ruleIndex] ?: ""

inline fun <reified T : ParserRuleContext> ParserRuleContext.findText(): String =
    find<T>().text

inline fun <reified T : ParserRuleContext> ParserRuleContext.findTextIn(vararg ctx: KClass<*>): String =
    findIn<T>(ctx = *arrayOf(*ctx, T::class)).text

inline fun <reified T : ParserRuleContext> ParserRuleContext.exists(skip: Boolean = true): Boolean =
    find(root = this, skip = skip, ctx = *arrayOf(T::class)) as T? != null

inline fun <reified T : ParserRuleContext> ParserRuleContext.find(skip: Boolean = true): T =
    findOrNull(skip)
        ?: throw Failures.instance().failure("${T::class.simpleName} not found")

inline fun <reified T : ParserRuleContext> ParserRuleContext.findOrNull(skip: Boolean = true): T? =
    find(root = this, skip = skip, ctx = *arrayOf(T::class))

inline fun <reified T : ParserRuleContext> ParserRuleContext.findIn(vararg ctx: KClass<*>): T =
    findIn(skip = true, ctx = *ctx)

inline fun <reified T : ParserRuleContext> ParserRuleContext.findIn(skip: Boolean = true, vararg ctx: KClass<*>): T =
    findInOrNull(skip = skip, ctx = *ctx)
        ?: throw Failures.instance().failure("${ctx.joinToString { it.simpleName ?: it.toString() }} not found")

inline fun <reified T : ParserRuleContext> ParserRuleContext.findInOrNull(vararg ctx: KClass<*>): T? =
    findInOrNull(skip = true, ctx = *ctx)

inline fun <reified T : ParserRuleContext> ParserRuleContext.findInOrNull(skip: Boolean = true, vararg ctx: KClass<*>): T? =
    find(root = this, skip = skip, ctx = *arrayOf(*ctx, T::class))

fun <T : ParserRuleContext> ParserRuleContext.find(root: ParserRuleContext?, skip: Boolean = true, vararg ctx: KClass<*>): T? {
    if (root == null) return null

    return when {
        ctx.size == 1 && ctx[0] == root::class -> root as T
        ctx[0] == root::class -> findChildren(root, *ctx.drop(1).toTypedArray())
        skip -> findChildren(root, *ctx)
        else -> null
    }
}

fun <T : ParserRuleContext> ParserRuleContext.findChildren(node: ParserRuleContext, vararg ctx: KClass<*>): T? = node.children
    ?.asSequence()
    ?.filter { it is ParserRuleContext }
    ?.mapNotNull {
        find<T>(root = it as ParserRuleContext, ctx = *ctx)
    }?.firstOrNull()

fun ParserRuleContext.asStringTree(): String = this.toStringTree(XQueryParser.ruleNames.toList())

fun ParserRuleContext.asDebugTree(): String = asDebugTree(0, this, XQueryParser.ruleNames.toList())

fun asDebugTree(level: Int, t: Tree, ruleNames: List<String>): String {
    var s = Utils.escapeWhitespace(getNodeText(t, ruleNames), false)
    if (t.childCount == 0) return s
    val buf = StringBuilder()
    buf.append("\n")
    for (i in 0 until level) buf.append(' ')
    s = Utils.escapeWhitespace(getNodeText(t, ruleNames), false)
    buf.append(s)
    buf.append("<[$level]( ")
    for (i in 0 until t.childCount) {
        if (i > 0) buf.append(' ')
        buf.append(asDebugTree(level + 1, t.getChild(i), ruleNames))
    }
    buf.append(" )[$level]>")
    return buf.toString()
}

fun List<ParserRuleContext>.childText() =
    this
        .joinToString(separator = "") { it.text }

fun ForVarContext.typeText() =
    type?.sequenceType()?.itemType()?.text

fun CaseClauseContext.typeText() =
    type?.itemType()?.text

fun LetVarContext.typeText() =
    type?.sequenceType()?.itemType()?.text

fun VarDeclContext.typeText() =
    type?.sequenceType()?.itemType()?.text

fun ParamContext.typeText() =
    type?.sequenceType()?.itemType()?.text

