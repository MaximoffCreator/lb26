package dev.kestrel.lb26.value

abstract class Value<T>(val name: String, var value: T) {
    open fun set(v: T) { value = v }
}

class BoolValue(name: String, v: Boolean) : Value<Boolean>(name, v)

class IntValue(name: String, v: Int, val min: Int, val max: Int) : Value<Int>(name, v) {
    override fun set(v: Int) { value = v.coerceIn(min, max) }
}

class FloatValue(name: String, v: Float, val min: Float, val max: Float) : Value<Float>(name, v) {
    override fun set(v: Float) { value = v.coerceIn(min, max) }
}