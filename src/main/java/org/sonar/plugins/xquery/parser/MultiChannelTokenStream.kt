package org.sonar.plugins.xquery.parser

import java.util.Arrays.copyOf

import org.antlr.v4.runtime.BufferedTokenStream
import org.antlr.v4.runtime.Token
import org.antlr.v4.runtime.TokenSource

class MultiChannelTokenStream(tokenSource: TokenSource) : BufferedTokenStream(tokenSource) {

    private var channels = intArrayOf(Token.DEFAULT_CHANNEL)

    val numberOfOnChannelTokens: Int
        get() {
            var n = 0
            fill()
            for (i in tokens.indices) {
                val t = tokens[i]
                for (channel in channels) {
                    if (t.channel == channel) {
                        n++
                    }
                }
                if (t.type == Token.EOF) {
                    break
                }
            }
            return n
        }

    fun enable(channel: Int) {
        for (existingChannel in channels) {
            if (channel == existingChannel) {
                return
            }
        }
        val len = channels.size
        channels = copyOf(channels, len + 1)
        channels[len] = channel
        var i = p - 1
        while (i >= 0) {
            val token = tokens[i]
            if (token.channel == channel || !matches(token.channel, channels)) {
                i--
            } else {
                break
            }
        }
        p = i + 1
    }

    fun disable(channel: Int) {
        var remainder = 0
        for (i in channels.indices) {
            if (channels[i] == channel) {
                continue
            } else {
                channels[remainder] = channels[i]
                remainder++
            }
        }
        channels = copyOf(channels, remainder)
    }

    override fun adjustSeekIndex(i: Int): Int {
        return nextTokenOnChannel(i, channels)
    }

    override fun LB(k: Int): Token? {
        if (k == 0 || p - k < 0) {
            return null
        }
        var i = p
        for (n in 1..k) {
            i = previousTokenOnChannel(i - 1, channels)
        }
        return if (i < 0) {
            null
        } else {
            tokens[i]
        }
    }

    override fun LT(k: Int): Token? {
        lazyInit()
        if (k == 0) {
            return null
        }
        if (k < 0) {
            return LB(-k)
        }

        var i = p
        for (n in 1 until k) {
            if (sync(i + 1)) {
                i = nextTokenOnChannel(i + 1, channels)
            }
        }
        return tokens[i]
    }

    protected fun nextTokenOnChannel(i: Int, channels: IntArray): Int {
        var i = i
        sync(i)
        if (i >= size()) {
            return size() - 1
        }

        var token = tokens[i]
        while (!matches(token.channel, channels)) {
            if (token.type == Token.EOF) {
                return i
            }
            i++
            sync(i)
            token = tokens[i]
        }
        return i
    }

    protected fun previousTokenOnChannel(i: Int, channels: IntArray): Int {
        var i = i
        sync(i)
        if (i >= size()) {
            return size() - 1
        }

        while (i >= 0) {
            val token = tokens[i]
            if (token.type == Token.EOF || matches(token.channel, channels)) {
                return i
            }
            i--
        }
        return i
    }

    private fun matches(channel: Int, channels: IntArray): Boolean {
        for (matchChannel in channels) {
            if (matchChannel == channel) {
                return true
            }
        }
        return false
    }
}