package com.cliqz.minibloomfilter;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.junit.Assert.*;

/**
 * Created by stefano on 11/10/16.
 */
public class BloomFilterTest {
    static final String TEST_STRING = "test123321tset";

    static final String[] TEST_TEXT = new String[] {
            "accumsan", "accusam", "aliquyam", "amet", "at", "augue", "autem",
            "blandit", "clita", "consequat", "consetetur", "delenit", "diam",
            "dignissim", "dolor", "dolore", "dolores", "duis", "duo", "ea",
            "eirmod", "elitr", "eos", "erat", "eros", "est", "eu", "eum",
            "facilisi", "facilisis", "feugait", "feugiat", "gubergren",
            "hendrerit", "illum", "in", "invidunt", "ipsum", "iriure", "iusto",
            "justo", "kasd", "labore", "lorem", "luptatum", "magna", "molestie",
            "no", "nonumy", "nulla", "odio", "praesent", "qui", "rebum",
            "sadipscing", "sanctus", "sea", "sed", "sit", "stet", "takimata",
            "te", "tempor", "ut", "vel", "velit", "vero", "voluptua",
            "vulputate", "zzril",
    };

    @Test
    public void putOneKey() throws Exception {
        final BloomFilter filter = BloomFilter.create(10, 0.1);

        filter.put(TEST_STRING);
        assertTrue(filter.maybe(TEST_STRING));
    }

    @Test
    public void noKey() throws Exception {
        final BloomFilter filter = BloomFilter.create(10, 0.1);

        assertFalse(filter.maybe(TEST_STRING));
    }

    @Test(expected = IllegalArgumentException.class)
    public void notValidElementsNumber() {
        BloomFilter.create(-3, 0.1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void notValidBitsNumber() {
        BloomFilter.create(20, 0.6);
    }

    @Test
    public void loremIpsum() {
        final String[] inverseText = new String[TEST_TEXT.length];
        final BloomFilter filter = loremIpsumBloomFilter();

        for (int i = 0; i < TEST_TEXT.length; i++) {
            inverseText[i] = invert(TEST_TEXT[i]);
        }

        for (String key: TEST_TEXT) {
            assertTrue(filter.maybe(key));
        }

        for (String key: inverseText) {
            assertFalse(filter.maybe(key));
        }
    }

    @Test
    public void serialization() throws Exception {
        final BloomFilter ofilter = loremIpsumBloomFilter();

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(ofilter);

        final ByteArrayInputStream bais =
                new ByteArrayInputStream(baos.toByteArray());
        final ObjectInputStream ois = new ObjectInputStream(bais);
        final BloomFilter ifilter = (BloomFilter) ois.readObject();

        assertNotEquals(ofilter, ifilter);
        assertEquals(ofilter.getHashes(), ifilter.getHashes());
        assertEquals(ofilter.size(), ifilter.size());
        assertTrue(ofilter.maybe(TEST_TEXT[0]));
        assertTrue(ifilter.maybe(TEST_TEXT[0]));
        assertFalse(ofilter.maybe("claxxx"));
        assertFalse(ifilter.maybe("claxxx"));
    }

    private BloomFilter loremIpsumBloomFilter() {
        final BloomFilter filter = BloomFilter.create(TEST_TEXT.length, 0.0001);
        for (String key: TEST_TEXT) {
            filter.put(key);
        }
        return filter;
    }

    private String invert(String s) {
        if (s == null) {
            return null;
        }
        final int size = s.length();
        switch (size) {
            case 0:
            case 1:
                return s;
            case 2:
                return new String(new char[] {s.charAt(1), s.charAt(0)});
            default:
                final char[] chars = new char[size];
                if (size % 2 == 1) {
                    chars[size/2 + 1] = s.charAt(size/2 + 1);
                }
                for (int i = 0; i < size / 2; i++) {
                    chars[i] = s.charAt(size - 1 - i);
                    chars[size - 1 - i] = s.charAt(i);
                }
                return new String(chars);
        }
    }

}
