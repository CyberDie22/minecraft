package com.benbuzard.minecraft.io.nbt

sealed class NBTTag(val id: kotlin.Int) {
    class End : NBTTag(0)
    data class Byte(val value: kotlin.Byte) : NBTTag(1)
    data class Short(val value: kotlin.Short) : NBTTag(2)
    data class Int(val value: kotlin.Int) : NBTTag(3)
    data class Long(val value: kotlin.Long) : NBTTag(4)
    data class Float(val value: kotlin.Float) : NBTTag(5)
    data class Double(val value: kotlin.Double) : NBTTag(6)
    data class ByteArray(val value: kotlin.ByteArray) : NBTTag(7) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ByteArray

            return value.contentEquals(other.value)
        }

        override fun hashCode(): kotlin.Int {
            return value.contentHashCode()
        }
    }
    data class String(val value: kotlin.String) : NBTTag(8)
    data class List(val tagType: kotlin.Int, val values: kotlin.collections.List<NBTTag>) : NBTTag(9)
    data class Compound(val values: kotlin.collections.Map<kotlin.String, NBTTag>) : NBTTag(10)
}