package org.sonar.plugins.xquery.parser

import org.antlr.v4.runtime.Parser
import org.antlr.v4.runtime.TokenStream

abstract class MultiChannelBaseParser(input: TokenStream) : Parser(input) {

    companion object {
        const val WHITESPACE = XQueryLexer.WHITESPACE
    }

    fun enable(channel: Int) {
        if (_input is MultiChannelTokenStream) {
            (_input as MultiChannelTokenStream).enable(channel)
        }
    }

    fun disable(channel: Int) {
        if (_input is MultiChannelTokenStream) {
            (_input as MultiChannelTokenStream).disable(channel)
        }
    }

}