package com.cliqz.minibloomfilter;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Simple bloom filter implementation using SipHash as hash function. It uses
 * only strings as keys and calculate automatically the optimal number of hash
 * functions given the maximum number of the elements and the bloom filter size
 * in bits.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Bloom_filter">BloomFilter</a>
 * @see <a href="https://131002.net/siphash/">SipHash</a>
 * @see SipHash_2_4
 * @author Stefano Pacifici
 */
public class BloomFilter implements Serializable {

    private final static double ln2 = (float) Math.log(2.0);
    private final static double squareLn2 = ln2 * ln2;

    private final static byte[] KEY = new byte[] {
            0x63, 0x6c, 0x69, 0x71, 0x7a, 0x32, 0x30, 0x31,
            0x36, 0x73, 0x74, 0x65, 0x66, 0x61, 0x6e, 0x6f
    };

    private int hashes;
    private BitArray bits;

    private BloomFilter() {}

    private BloomFilter(int bits, int hashes) {
        this.hashes = hashes;
        this.bits = new BitArray(bits);
    }

    /**
     * Create a BloomFilter instance for the given number of elements and the
     * given false positive probability
     *
     * @param n the maximum elements in the filter
     * @param p the false positive probability (or rate)
     *
     * @return a new BloomFilter instance
     */
    public static BloomFilter create (int n, double p) {
        if (n <= 0) {
            throw new IllegalArgumentException("Wrong elements number");
        }
        if (p < 0.0 || p > 0.5) {
            throw new IllegalArgumentException("Probability should be " +
                    "greater than 0 and less than 0.5");
        }
        final double m = -1.0 * n * Math.log(p) / squareLn2;
        final double k = m / n * ln2;
        return new BloomFilter((int) Math.ceil(m), (int) Math.ceil(k));
    }

    /**
     * If true the given key may belong to the filter, if false the element
     * does not definitely belong to the filter
     *
     * @param key the key the user want to check the filter against
     * @return true if the key may belong to the filter, false if it does not
     */
    public boolean maybe(String key) {
        final int[] hashes = calculateHashes(key);
        for (int index: hashes) {
            if (!bits.getBit(index)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Add a key to the filter. Please notice there is no limitation on the
     * number of keys the user can add to the filter.
     *
     * @param key the key to add
     */
    public void put(String key) {
        final int[] hashes = calculateHashes(key);
        for (int index: hashes) {
            bits.setBit(index, true);
        }
    }

    /**
     * @return the number of hash functions used by the filter
     */
    public int getHashes() {
        return hashes;
    }

    /**
     * @return  the number of bits in the filter
     */
    public int size() {
        return bits.size();
    }

    // Calculate the hashes for the given key using SipHash
    private int[] calculateHashes(String key) {
        final byte[] data = key.getBytes();
        final int[] result = new int[hashes];
        final int bitsSize = bits.size();
        final SipHash_2_4 sipHash24 = new SipHash_2_4();
        for (int i = 0; i < hashes; i++) {
            sipHash24.initialize(KEY);
            for (byte b: data) {
                sipHash24.updateHash(b);
            }
            for (byte b: intToBytes(i*3)) {
                sipHash24.updateHash(b);
            }
            final long hash = sipHash24.finish();
            // Take only first 31 bits (this remove the sign)
            final int hash32 = (int) (hash & 0x7fffffff);
            result[i] = hash32 % bitsSize;
        }
        return result;
    }

    private byte[] intToBytes(int i) {
        byte[] result = new byte[4];
        result[0] = (byte) (i & 0x000000ff);
        result[1] = (byte) ((i >> 8) & 0x000000ff);
        result[2] = (byte) ((i >> 16) & 0x000000ff);
        result[3] = (byte) ((i >> 24) & 0x000000ff);
        return result;
    }

    // Serialization
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(hashes);
        out.writeObject(bits);
    }

    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        hashes = in.readInt();
        bits = (BitArray) in.readObject();
    }

}
