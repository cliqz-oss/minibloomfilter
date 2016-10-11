package com.cliqz.minibloomfilter;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Simple, short and {@link Serializable} bit array implementation. It uses a
 * integer array to store the bits.
 *
 * @author Stefano Pacifici
 */
public class BitArray implements Serializable{

    private static final int[] BITS = {
            1,       1 << 1,  1 << 2,  1 << 3,  1 << 4,  1 << 5,  1 << 6,  1 << 7,
            1 << 8,  1 << 9,  1 << 10, 1 << 11, 1 << 12, 1 << 13, 1 << 14, 1 << 15,
            1 << 16, 1 << 17, 1 << 18, 1 << 19, 1 << 20, 1 << 21, 1 << 22, 1 << 23,
            1 << 24, 1 << 25, 1 << 26, 1 << 27, 1 << 28, 1 << 29, 1 << 30, 1 << 31
    };

    private static final int[] MASKS = {
            0xfffffffe, 0xfffffffd, 0xfffffffb, 0xfffffff7,
            0xffffffef, 0xffffffdf, 0xffffffbf, 0xffffff7f,
            0xfffffeff, 0xfffffdff, 0xfffffbff, 0xfffff7ff,
            0xffffefff, 0xffffdfff, 0xffffbfff, 0xffff7fff,
            0xfffeffff, 0xfffdffff, 0xfffbffff, 0xfff7ffff,
            0xffefffff, 0xffdfffff, 0xffbfffff, 0xff7fffff,
            0xfeffffff, 0xfdffffff, 0xfbffffff, 0xf7ffffff,
            0xefffffff, 0xdfffffff, 0xbfffffff, 0x7fffffff,
    };

    private int size;
    private int[] words;

    // Only for serialization.
    private BitArray() {}

    /**
     * Constructs a BitArray of the given size, the size in bytes of the
     * BitArray will always be a multiple of 4.
     *
     * @param size The size (in bits) of the BitArray
     * @throws IllegalArgumentException if size &le; 0
     */
    public BitArray(int size) {
        if (size <=0) {
            throw new IllegalArgumentException("Size can't be <= 0");
        }
        this.size = size;
        final int words = (size + 31) / 32;
        this.words = new int[words];
    }

    /**
     * Return the value (true or false) of the bit at the given index
     *
     * @param index the bit index
     * @return true or false
     * @throws IllegalArgumentException if index &lt; 0 or index &ge; size
     */
    public boolean getBit(int index) {
        if (index >= size || index < 0) {
            throw new IndexOutOfBoundsException();
        }

        final int word = index / 32;
        final int bit = index % 32;
        return (words[word] & BITS[bit]) != 0;
    }

    /**
     * Set the bit at the given index to the given value
     *
     * @param index the bit index
     * @param value the new value for the bit
     * @throws  IllegalArgumentException if index &lt; 0 or index &ge; size
     */
    public void setBit(int index, boolean value) {
        if (index >= size || index < 0) {
            throw new IndexOutOfBoundsException();
        }

        final int word = index / 32;
        final int bit = index % 32;

        if (value) {
            words[word] |= BITS[bit];
        } else {
            words[word] &= MASKS[bit];
        }
    }

    /**
     * @return The size in bytes of the BitArray
     */
    public int byteSize() {
        return words.length * 4;
    }

    /**
     * @return the size in bits of the BitArray
     */
    public int size() {
        return size;
    }

    // Serialization: we write out the size in bits, the size in integers and
    // all the integers int the storage array
    private void writeObject(ObjectOutputStream out)
            throws IOException {
        out.writeInt(size);
        out.writeInt(words.length);
        for (int word: words) {
            out.writeInt(word);
        }
    }

    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        size = in.readInt();
        final int wordsSize = in.readInt();
        words = new int[wordsSize];
        for (int i = 0; i < wordsSize; i++) {
            words[i] = in.readInt();
        }
    }

}
