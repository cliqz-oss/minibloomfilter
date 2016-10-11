package com.cliqz.minibloomfilter;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Created by stefano on 10/10/16.
 */
public class BitArrayTest {

    @Test
    public void setSingleBit() throws Exception {
        final BitArray test = new BitArray(128);
        final int testBit = 47;
        assertFalse(test.getBit(testBit));
        test.setBit(testBit, true);
        assertTrue(test.getBit(testBit));
        test.setBit(testBit, false);
        assertFalse(test.getBit(testBit));
    }

    @Test
    public void lastBitInWord() {
        final BitArray test = new BitArray(32);
        assertFalse(test.getBit(31));
        test.setBit(31, true);
        assertTrue(test.getBit(31));
    }

    @Test
    public void setRandomBits() throws Exception {
        final int bitSize = 128;
        final int testSize = 53;
        final Set<Integer> testIndexes = new HashSet<Integer>(testSize);
        final BitArray test = new BitArray(bitSize);
        while (testIndexes.size() < testSize) {
            final int index = (int) Math.round(Math.random() * (bitSize - 1));
            testIndexes.add(index);
        }

        for (int index: testIndexes) {
            assertFalse(test.getBit(index));
        }
        for (int index: testIndexes) {
            test.setBit(index, true);
        }
        for (int index: testIndexes) {
            assertTrue(test.getBit(index));
        }
        for (int index: testIndexes) {
            test.setBit(index, false);
        }
        for (int index: testIndexes) {
            assertFalse(test.getBit(index));
        }
    }

    @Test
    public void bitSize1() throws Exception {
        final BitArray test = new BitArray(1);
        assertEquals(4, test.byteSize());
    }

    @Test
    public void bitSize39() throws Exception {
        final BitArray test = new BitArray(39);
        assertEquals(8, test.byteSize());
    }

    @Test
    public void bitSize128() throws Exception {
        final BitArray test = new BitArray(128);
        assertEquals(16, test.byteSize());
    }

    @Test(expected = IllegalArgumentException.class)
    public void zeroSizedBitArray() throws Exception {
        new BitArray(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void negativeSizedBitArray() throws Exception {
        new BitArray(-3);
    }

    @Test
    public void serialization() throws Exception {
        final int ti = 123;
        final BitArray outba = new BitArray(1024);
        outba.setBit(ti, true);

        final ByteArrayOutputStream baos =
                new ByteArrayOutputStream(outba.byteSize() + 16);
        final ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(outba);

        final ByteArrayInputStream bais =
                new ByteArrayInputStream(baos.toByteArray());
        final ObjectInputStream ois = new ObjectInputStream(bais);
        final BitArray inba = (BitArray) ois.readObject();

        assertTrue(inba.getBit(ti));
        assertEquals(outba.byteSize(), inba.byteSize());
        assertEquals(outba.size(), inba.size());
    }
}