package com.gnarfodolfus.pixel.input;

import java.io.Serializable;
import java.util.*;

/**
 * Simple bitset implementation for internal use. The initial size is constant.
 * No range checks are performed for performance reasons.
 */
final class FixedBitSet implements Serializable {

    private static final long WORD_MASK = 0xffffffffffffffffL;

    private final long[] words;

    private static int wordIndex(int bitIndex) {
        return bitIndex >> 6; // divide by 64. doh.
    }

    public FixedBitSet(int bits) {
        if (bits < 0)
            throw new IllegalArgumentException("bits < 0");
        this.words = new long[wordIndex(bits - 1) + 1];
    }

    private FixedBitSet(long[] words) {
        this.words = words;
    }

    /**
     * Apply all values of the specified bitset to this bitset, overwriting
     * existing values. This method is ment to copy values of bitsets of the
     * exactly same size. If the specified bitset is smaller than this bitset
     * the remaining bits are not changed. If the specified bitset is larger
     * than this bitset an exception will be thrown and this bitset is garbage.
     *
     * @param bitset Bitset to apply
     */
    public void set(FixedBitSet bitset) {
        // Alternative:
        // System.arraycopy(bitset.words, 0, words, 0, bitset.words.length);
        // I have the suspicion that a primitive loop is actually faster
        // TODO: Verify!
        for (int i = 0; i < bitset.words.length; i++)
            words[i] = bitset.words[i];
    }

    public boolean isEmpty() {
        for (long word : words)
            if (word != 0)
                return false;
        return true;
    }

    public int cardinality() {
        int sum = 0;
        for (long word : words)
            sum += Long.bitCount(word);
        return sum;
    }
    
    public int nextSetBit(int fromIndex) {
        int wordIndex = wordIndex(fromIndex);
        if (wordIndex > words.length)
            return -1;
        long word = words[wordIndex] & (WORD_MASK << fromIndex);
        while (true) {
            if (word != 0)
                return (wordIndex * 64) + Long.numberOfTrailingZeros(word);
            if (++wordIndex >= words.length)
                return -1;
            word = words[wordIndex];
        }
    }

    /**
     * Write the indices of all bits that are currently set into the
     * desitination array. The array must be sufficiently large, otherwise
     * remaining bit indexes are omitted. Returns the number of set bits.
     *
     * @param dst Destination array
     * @return Number of bits written into the array
     */
    public int getSetBits(int[] dst) {
        int index = 0;
        for (int bit = nextSetBit(0); bit >= 0 && index < dst.length; bit = nextSetBit(bit + 1))
            dst[index++] = bit;
        return index;
    }

    /**
     * Returns the result of an XOR operatioin followed by an AND operation
     * against the other bitset. If the specified bitset is smaller than this
     * bitset an exception will be thrown.
     *
     * @param other Bitset
     */
    public FixedBitSet xorAnd(FixedBitSet other) {
        long[] result = new long[words.length];
        for (int i = 0; i < words.length; i++)
            result[i] = (other.words[i] ^ words[i]) & other.words[i];
        return new FixedBitSet(result);
    }

    public boolean get(int index) {
        int wordIndex = wordIndex(index);
        return (words[wordIndex] & (1L << index)) != 0;
    }

    public void set(int index, boolean value) {
        if (value)
            set(index);
        else
            clear(index);
    }

    public void set(int index) {
        int wordIndex = index >> 6;
        words[wordIndex] |= (1L << index);
    }

    public void clear(int index) {
        int wordIndex = index >> 6;
        words[wordIndex] &= ~(1L << index);
    }

    @Override
    public String toString() {
        int bufSize = Math.min(100, cardinality() + 10);
        int buf[] = new int[bufSize];
        int len = getSetBits(buf);
        buf = Arrays.copyOf(buf, len);
        return Arrays.toString(buf);
    }
}
