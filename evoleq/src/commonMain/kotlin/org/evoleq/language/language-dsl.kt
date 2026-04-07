package org.evoleq.language

import org.evoleq.configuration.Configuration
/*
class LanguageConfiguration : Configuration<Lang> {

    lateinit var lang: Lang

    override fun configure(): Lang = lang

    // fun block(block: BlockConfiguration)
}
*/
class BlockConfiguration : Configuration<Block> {

    private val value: MutableList<Lang> = mutableListOf()
    lateinit var key: String

    override fun configure(): Block = Block(key, value)

    fun variable(configuration: VariableConfiguration.()->Unit) = with(VariableConfiguration()){
        this.configuration()
        val item = configure()
        this@BlockConfiguration.value+=item
    }

    infix fun String.colon(value: String) = with(VariableConfiguration()) {
        this.key = this@colon
        this.value = value
        val item = configure()
        this@BlockConfiguration.value+=item
    }

    fun block(configuration: BlockConfiguration.()->Unit) = with(BlockConfiguration()){
        this.configuration()
        val item = configure()
        this@BlockConfiguration.value += item
    }

    infix fun String.block(configuration: BlockConfiguration.()->Unit) = with(BlockConfiguration()) {
        this.configuration()
        this.key = this@block
        val item = configure()
        this@BlockConfiguration.value += item
    }

    fun add(vararg values: Lang) = value.addAll(values)
}

class VariableConfiguration : Configuration<Lang.Variable> {

    lateinit var key: String
    lateinit var value: String

    override fun configure(): Lang.Variable = Lang.Variable(key, value)

}

fun Any?.language(configuration: BlockConfiguration.()->Unit): Lang = with(BlockConfiguration()) {
    configuration()
    configure() as Lang
}

fun texts(configuration: BlockConfiguration.()->Unit): Block = with(BlockConfiguration()) {
    configuration()
    return configure()
}

fun Lang.Block.extend(configuration: BlockConfiguration.()->Unit): Block = texts{
    key = this@extend.key
    add(*this@extend.value.toTypedArray())
    configuration()
}
