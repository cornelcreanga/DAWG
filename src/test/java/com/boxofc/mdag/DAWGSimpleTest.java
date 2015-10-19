/**
 * MDAG is a Java library capable of constructing character-sequence-storing,
 * directed acyclic graphs of minimal size.
 *
 *  Copyright (C) 2012 Kevin Lawson <Klawson88@gmail.com>
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.boxofc.mdag;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class DAWGSimpleTest {
    @Test
    public void addSimple() {
        String words[] = {
            "a", "xes", "xe", "xs"
        };
        ModifiableDAWGSet dawg = new ModifiableDAWGSet();
        dawg.addAll(words);
        Arrays.sort(words);
        CompressedDAWGSet cdawg = dawg.compress();
        assertEquals(cdawg, dawg.compress());

        int i = 0;
        for (String word : dawg.getAllStrings())
            assertEquals(words[i++], word);
        assertEquals(words.length, i);

        i = 0;
        for (String word : cdawg.getAllStrings())
            assertEquals(words[i++], word);
        assertEquals(words.length, i);

        String wordsXe[] = {"xe", "xes"};
        i = 0;
        for (String word : dawg.getStringsStartingWith("xe"))
            assertEquals(wordsXe[i++], word);
        
        i = 0;
        for (String word : cdawg.getStringsStartingWith("xe"))
            assertEquals(wordsXe[i++], word);

        String wordsS[] = {"xes", "xs"};
        Set<String> expected = new HashSet<>(Arrays.asList(wordsS));
        Set<String> actual = new HashSet<>();
        for (String word : dawg.getStringsEndingWith("s"))
            actual.add(word);
        assertEquals(expected, actual);
        
        actual = new HashSet<>();
        for (String word : cdawg.getStringsEndingWith("s"))
            actual.add(word);
        assertEquals(expected, actual);
        
        assertEquals(4, dawg.size());
        assertEquals(4, dawg.getNodeCount());
        assertEquals(5, dawg.getTransitionCount());
        
        assertEquals(4, cdawg.size());
        assertEquals(4, cdawg.getNodeCount());
        assertEquals(5, cdawg.getTransitionCount());
        
        // Non-existent.
        dawg.remove("b");
        assertEquals(cdawg, dawg.compress());
        cdawg = dawg.compress();

        i = 0;
        for (String word : dawg.getAllStrings())
            assertEquals(words[i++], word);
        assertEquals(words.length, i);

        i = 0;
        for (String word : cdawg.getAllStrings())
            assertEquals(words[i++], word);
        assertEquals(words.length, i);
        
        assertEquals(4, dawg.size());
        assertEquals(4, dawg.getNodeCount());
        assertEquals(5, dawg.getTransitionCount());
        
        assertEquals(4, cdawg.size());
        assertEquals(4, cdawg.getNodeCount());
        assertEquals(5, cdawg.getTransitionCount());
        
        dawg.remove("");
        cdawg = dawg.compress();

        i = 0;
        for (String word : dawg.getAllStrings())
            assertEquals(words[i++], word);
        assertEquals(words.length, i);

        i = 0;
        for (String word : cdawg.getAllStrings())
            assertEquals(words[i++], word);
        assertEquals(words.length, i);
        
        assertEquals(4, dawg.size());
        assertEquals(4, dawg.getNodeCount());
        assertEquals(5, dawg.getTransitionCount());
        
        assertEquals(4, cdawg.size());
        assertEquals(4, cdawg.getNodeCount());
        assertEquals(5, cdawg.getTransitionCount());
    }
  
    @Test
    public void addCasual() {
        String words[] = {
            "assiez",
            "assions",
            "eriez",
            "erions",
            "eront",
            "iez",
            "ions"
        };
        Set<String> expected = new HashSet<>(Arrays.asList(words));
        for (String w[] : Permutations.from(words)) {
            ModifiableDAWGSet dawg = new ModifiableDAWGSet();
            dawg.addAll(w);
            CompressedDAWGSet cdawg = dawg.compress();
            int i = 0;
            for (String s : dawg)
                assertEquals(words[i++], s);
            assertEquals(words.length, i);
            
            i = 0;
            for (String s : cdawg)
                assertEquals(words[i++], s);
            assertEquals(words.length, i);

            Set<String> actual = new HashSet<>();
            for (String word : dawg.getStringsEndingWith(""))
                actual.add(word);
            assertEquals(expected, actual);

            actual = new HashSet<>();
            for (String word : cdawg.getStringsEndingWith(""))
                actual.add(word);
            assertEquals(expected, actual);
        }
    }
  
    @Test
    public void addCasualWithBlank() {
        String words[] = {
            "",
            "assiez",
            "assions",
            "eriez",
            "erions",
            "eront",
            "iez",
            "ions"
        };
        Set<String> expected = new HashSet<>(Arrays.asList(words));
        String removingWord = words[3];
        Set<String> expectedRemoveOne = new HashSet<>();
        for (String word : words)
            if (!word.equals(removingWord))
                expectedRemoveOne.add(word);
        Set<String> expectedRemoveBlank = new HashSet<>();
        for (String word : words)
            if (!word.isEmpty())
                expectedRemoveBlank.add(word);
        for ( String w[] : Permutations.from(words)) {
            ModifiableDAWGSet dawg = new ModifiableDAWGSet();
            dawg.addAll(w);
            CompressedDAWGSet cdawg = dawg.compress();
            int i = 0;
            for (String s : dawg)
                assertEquals(words[i++], s);
            assertEquals(words.length, i);
            
            i = 0;
            for (String s : cdawg)
                assertEquals(words[i++], s);
            assertEquals(words.length, i);

            Set<String> actual = new HashSet<>();
            for (String word : dawg.getStringsEndingWith(""))
                actual.add(word);
            assertEquals(expected, actual);

            actual = new HashSet<>();
            for (String word : cdawg.getStringsEndingWith(""))
                actual.add(word);
            assertEquals(expected, actual);
            
            dawg.remove(removingWord);
            cdawg = dawg.compress();
            
            i = 0;
            for (String s : dawg) {
                if (words[i].equals(removingWord))
                    i++;
                assertEquals(words[i++], s);
            }
            assertEquals(words.length, i);
            
            i = 0;
            for (String s : cdawg) {
                if (words[i].equals(removingWord))
                    i++;
                assertEquals(words[i++], s);
            }
            assertEquals(words.length, i);

            actual = new HashSet<>();
            for (String word : dawg.getStringsEndingWith(""))
                actual.add(word);
            assertEquals(expectedRemoveOne, actual);

            actual = new HashSet<>();
            for (String word : cdawg.getStringsEndingWith(""))
                actual.add(word);
            assertEquals(expectedRemoveOne, actual);
            
            dawg = new ModifiableDAWGSet();
            dawg.addAll(w);
            dawg.remove("");
            cdawg = dawg.compress();
            
            i = 0;
            for (String s : dawg) {
                if (words[i].isEmpty())
                    i++;
                assertEquals(words[i++], s);
            }
            assertEquals(words.length, i);
            
            i = 0;
            for (String s : cdawg) {
                if (words[i].isEmpty())
                    i++;
                assertEquals(words[i++], s);
            }
            assertEquals(words.length, i);

            actual = new HashSet<>();
            for (String word : dawg.getStringsEndingWith(""))
                actual.add(word);
            assertEquals(expectedRemoveBlank, actual);

            actual = new HashSet<>();
            for (String word : cdawg.getStringsEndingWith(""))
                actual.add(word);
            assertEquals(expectedRemoveBlank, actual);
        }
    }

    @Test
    public void add() {
        ModifiableDAWGSet dawg = new ModifiableDAWGSet();
        String words[] = {
            "aient", "ais", "ait", "ai", "ant",
            "assent", "asses", "asse", "assiez", "assions", "as", "a",
            "ent", "eraient", "erais", "erait", "erai", "eras", "era",
            "erez", "eriez", "erions",
            "erons", "eront", "er", "es", "ez", "e",
            "iez", "ions", "ons", "qmes", "qtes", "qt",
            "wrent", "xes", "xe", "xs", "x"
        };
        dawg.addAll(words);
        CompressedDAWGSet cdawg = dawg.compress();

        Arrays.sort(words);
        int i = 0;
        for (String word : dawg)
            assertEquals(words[i++], word);
        assertEquals(words.length, i);
        
        i = 0;
        for (String word : cdawg)
            assertEquals(words[i++], word);
        assertEquals(words.length, i);

        String wordsAs[] = {"as", "asse", "assent", "asses", "assiez", "assions"};
        i = 0;
        for (String word : dawg.getStringsStartingWith("as"))
            assertEquals(wordsAs[i++], word);
        
        i = 0;
        for (String word : cdawg.getStringsStartingWith("as"))
            assertEquals(wordsAs[i++], word);

        String wordsOns[] = {"assions", "erions", "erons", "ions", "ons"};
        Set<String> expected = new HashSet<>(Arrays.asList(wordsOns));
        Set<String> actual = new HashSet<>();
        for (String word : dawg.getStringsEndingWith("ons"))
            actual.add(word);
        assertEquals(expected, actual);
        
        actual = new HashSet<>();
        for (String word : cdawg.getStringsEndingWith("ons"))
            actual.add(word);
        assertEquals(expected, actual);

        String wordsXe[] = {"xe"};
        expected = new HashSet<>(Arrays.asList(wordsXe));
        actual = new HashSet<>();
        for (String word : dawg.getStringsEndingWith("xe"))
            actual.add(word);
        assertEquals(expected, actual);
        
        actual = new HashSet<>();
        for (String word : cdawg.getStringsEndingWith("xe"))
            actual.add(word);
        assertEquals(expected, actual);
        
        assertEquals(25, dawg.getNodeCount());
        assertEquals(39, dawg.size());
        
        assertEquals(25, cdawg.getNodeCount());
        assertEquals(39, cdawg.size());
    }

    @Test(expected = NoSuchElementException.class)
    public void empty() {
        DAWGSet dawg = new ModifiableDAWGSet();
        assertFalse(dawg.iterator().hasNext());
        dawg.iterator().next();
    }

    @Test(expected = NoSuchElementException.class)
    public void emptyCompressed() {
        DAWGSet dawg = new ModifiableDAWGSet().compress();
        assertFalse(dawg.iterator().hasNext());
        dawg.iterator().next();
    }

    @Test(expected = NoSuchElementException.class)
    public void emptySuffix() {
        DAWGSet dawg = new ModifiableDAWGSet();
        assertFalse(dawg.getStringsEndingWith("").iterator().hasNext());
        dawg.getStringsEndingWith("").iterator().next();
    }

    @Test(expected = NoSuchElementException.class)
    public void emptySuffixCompressed() {
        DAWGSet dawg = new ModifiableDAWGSet().compress();
        assertFalse(dawg.getStringsEndingWith("").iterator().hasNext());
        dawg.getStringsEndingWith("").iterator().next();
    }

    @Test(expected = NoSuchElementException.class)
    public void emptyCollection() {
        ModifiableDAWGSet dawg = new ModifiableDAWGSet();
        dawg.addAll();
        assertFalse(dawg.getStringsEndingWith("").iterator().hasNext());
        dawg.getStringsEndingWith("").iterator().next();
    }

    @Test(expected = NoSuchElementException.class)
    public void emptyCollectionCompressed() {
        ModifiableDAWGSet dawg = new ModifiableDAWGSet();
        dawg.addAll();
        CompressedDAWGSet cdawg = dawg.compress();
        assertFalse(cdawg.getStringsEndingWith("").iterator().hasNext());
        cdawg.getStringsEndingWith("").iterator().next();
    }

    @Test
    public void file() throws IOException {
        ModifiableDAWGSet dawg = new ModifiableDAWGSet();
        // Source: http://www.mieliestronk.com/wordlist.html
        try (FileInputStream fis = new FileInputStream("corncob_lowercase.txt")) {
            dawg.addAll(fis);
        }
        CompressedDAWGSet cdawg = dawg.compress();
        
        int i = 0;
        for (String word : dawg)
            i++;
        assertEquals(58109, i);
        
        i = 0;
        for (String word : cdawg)
            i++;
        assertEquals(58109, i);

        i = 0;
        for (String word : dawg.getStringsEndingWith(""))
            i++;
        assertEquals(58109, i);

        i = 0;
        for (String word : cdawg.getStringsEndingWith(""))
            i++;
        assertEquals(58109, i);
    }

    @Test
    public void blankCollection() {
        ModifiableDAWGSet dawg = new ModifiableDAWGSet();
        dawg.addAll("");
        CompressedDAWGSet cdawg = dawg.compress();

        Iterator<String> iterator = dawg.iterator();
        assertTrue(iterator.hasNext());
        assertEquals("", iterator.next());
        assertFalse(iterator.hasNext());
        
        iterator = cdawg.iterator();
        assertTrue(iterator.hasNext());
        assertEquals("", iterator.next());
        assertFalse(iterator.hasNext());

        iterator = dawg.getStringsEndingWith("").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("", iterator.next());
        assertFalse(iterator.hasNext());

        iterator = cdawg.getStringsEndingWith("").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("", iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void blank() {
        ModifiableDAWGSet dawg = new ModifiableDAWGSet();
        dawg.add("");
        CompressedDAWGSet cdawg = dawg.compress();

        Iterator<String> iterator = dawg.iterator();
        assertTrue(iterator.hasNext());
        assertEquals("", iterator.next());
        assertFalse(iterator.hasNext());
        
        iterator = cdawg.iterator();
        assertTrue(iterator.hasNext());
        assertEquals("", iterator.next());
        assertFalse(iterator.hasNext());

        iterator = dawg.getStringsEndingWith("").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("", iterator.next());
        assertFalse(iterator.hasNext());

        iterator = cdawg.getStringsEndingWith("").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("", iterator.next());
        assertFalse(iterator.hasNext());
        
        dawg.remove("");
        cdawg = dawg.compress();

        assertFalse(dawg.iterator().hasNext());
        assertFalse(dawg.getStringsEndingWith("").iterator().hasNext());

        assertFalse(cdawg.iterator().hasNext());
        assertFalse(cdawg.getStringsEndingWith("").iterator().hasNext());
    }

    @Test
    public void shortWord() {
        ModifiableDAWGSet dawg = new ModifiableDAWGSet();
        dawg.add("a");
        CompressedDAWGSet cdawg = dawg.compress();

        Iterator<String> iterator = dawg.iterator();
        assertTrue(iterator.hasNext());
        assertEquals("a", iterator.next());
        assertFalse(iterator.hasNext());
        
        iterator = cdawg.iterator();
        assertTrue(iterator.hasNext());
        assertEquals("a", iterator.next());
        assertFalse(iterator.hasNext());

        iterator = dawg.getStringsEndingWith("").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("a", iterator.next());
        assertFalse(iterator.hasNext());

        iterator = cdawg.getStringsEndingWith("").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("a", iterator.next());
        assertFalse(iterator.hasNext());

        iterator = dawg.getStringsStartingWith("a").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("a", iterator.next());
        assertFalse(iterator.hasNext());

        iterator = cdawg.getStringsStartingWith("a").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("a", iterator.next());
        assertFalse(iterator.hasNext());

        iterator = dawg.getStringsEndingWith("a").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("a", iterator.next());
        assertFalse(iterator.hasNext());

        iterator = cdawg.getStringsEndingWith("a").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("a", iterator.next());
        assertFalse(iterator.hasNext());

        assertFalse(dawg.getStringsStartingWith("b").iterator().hasNext());
        assertFalse(dawg.getStringsEndingWith("b").iterator().hasNext());

        assertFalse(cdawg.getStringsStartingWith("b").iterator().hasNext());
        assertFalse(cdawg.getStringsEndingWith("b").iterator().hasNext());
        
        dawg.remove("a");
        cdawg = dawg.compress();

        assertFalse(dawg.iterator().hasNext());
        assertFalse(dawg.getStringsEndingWith("").iterator().hasNext());

        assertFalse(cdawg.iterator().hasNext());
        assertFalse(cdawg.getStringsEndingWith("").iterator().hasNext());
    }

    @Test
    public void zero() {
        ModifiableDAWGSet dawg = new ModifiableDAWGSet();
        dawg.add("\0");
        CompressedDAWGSet cdawg = dawg.compress();

        Iterator<String> iterator = dawg.iterator();
        assertTrue(iterator.hasNext());
        assertEquals("\0", iterator.next());
        assertFalse(iterator.hasNext());
        
        iterator = cdawg.iterator();
        assertTrue(iterator.hasNext());
        assertEquals("\0", iterator.next());
        assertFalse(iterator.hasNext());

        iterator = dawg.getStringsEndingWith("").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("\0", iterator.next());
        assertFalse(iterator.hasNext());

        iterator = cdawg.getStringsEndingWith("").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("\0", iterator.next());
        assertFalse(iterator.hasNext());

        iterator = dawg.getStringsStartingWith("\0").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("\0", iterator.next());
        assertFalse(iterator.hasNext());

        iterator = cdawg.getStringsStartingWith("\0").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("\0", iterator.next());
        assertFalse(iterator.hasNext());

        iterator = dawg.getStringsEndingWith("\0").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("\0", iterator.next());
        assertFalse(iterator.hasNext());

        iterator = cdawg.getStringsEndingWith("\0").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("\0", iterator.next());
        assertFalse(iterator.hasNext());

        assertFalse(dawg.getStringsStartingWith("b").iterator().hasNext());
        assertFalse(dawg.getStringsEndingWith("b").iterator().hasNext());

        assertFalse(cdawg.getStringsStartingWith("b").iterator().hasNext());
        assertFalse(cdawg.getStringsEndingWith("b").iterator().hasNext());
    }

    @Test
    public void wordWithBlank() {
        ModifiableDAWGSet dawg = new ModifiableDAWGSet();
        dawg.add("");
        dawg.add("add");
        CompressedDAWGSet cdawg = dawg.compress();

        Set<String> expected = new HashSet<>(Arrays.asList("", "add"));
        Set<String> actual = new HashSet<>();
        for (String word : dawg.getStringsEndingWith(""))
            actual.add(word);
        assertEquals(expected, actual);
        
        actual = new HashSet<>();
        for (String word : cdawg.getStringsEndingWith(""))
            actual.add(word);
        assertEquals(expected, actual);

        dawg.add("a");
        cdawg = dawg.compress();
        expected = new HashSet<>(Arrays.asList("", "a", "add"));
        actual = new HashSet<>();
        for (String word : dawg.getStringsEndingWith(""))
            actual.add(word);
        assertEquals(expected, actual);
        
        actual = new HashSet<>();
        for (String word : cdawg.getStringsEndingWith(""))
            actual.add(word);
        assertEquals(expected, actual);

        dawg.add("ad");
        cdawg = dawg.compress();
        expected = new HashSet<>(Arrays.asList("", "a", "ad", "add"));
        actual = new HashSet<>();
        for (String word : dawg.getStringsEndingWith(""))
            actual.add(word);
        assertEquals(expected, actual);
        
        actual = new HashSet<>();
        for (String word : cdawg.getStringsEndingWith(""))
            actual.add(word);
        assertEquals(expected, actual);
        
        dawg.remove("");
        cdawg = dawg.compress();
        expected = new HashSet<>(Arrays.asList("a", "ad", "add"));
        actual = new HashSet<>();
        for (String word : dawg.getStringsEndingWith(""))
            actual.add(word);
        assertEquals(expected, actual);
        
        actual = new HashSet<>();
        for (String word : cdawg.getStringsEndingWith(""))
            actual.add(word);
        assertEquals(expected, actual);
    }

    @Test
    public void shortWordWithBlank() {
        ModifiableDAWGSet dawg = new ModifiableDAWGSet();
        dawg.add("");
        dawg.add("a");
        CompressedDAWGSet cdawg = dawg.compress();

        Iterator<String> iterator = dawg.iterator();
        assertTrue(iterator.hasNext());
        assertEquals("", iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals("a", iterator.next());
        assertFalse(iterator.hasNext());
        
        iterator = cdawg.iterator();
        assertTrue(iterator.hasNext());
        assertEquals("", iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals("a", iterator.next());
        assertFalse(iterator.hasNext());

        Set<String> expected = new HashSet<>(Arrays.asList("", "a"));
        Set<String> actual = new HashSet<>();
        for (String word : dawg.getStringsEndingWith(""))
            actual.add(word);
        assertEquals(expected, actual);
        
        actual = new HashSet<>();
        for (String word : cdawg.getStringsEndingWith(""))
            actual.add(word);
        assertEquals(expected, actual);

        iterator = dawg.getStringsStartingWith("a").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("a", iterator.next());
        assertFalse(iterator.hasNext());

        iterator = cdawg.getStringsStartingWith("a").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("a", iterator.next());
        assertFalse(iterator.hasNext());

        iterator = dawg.getStringsEndingWith("a").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("a", iterator.next());
        assertFalse(iterator.hasNext());

        iterator = cdawg.getStringsEndingWith("a").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("a", iterator.next());
        assertFalse(iterator.hasNext());

        assertFalse(dawg.getStringsStartingWith("b").iterator().hasNext());
        assertFalse(dawg.getStringsEndingWith("b").iterator().hasNext());

        assertFalse(cdawg.getStringsStartingWith("b").iterator().hasNext());
        assertFalse(cdawg.getStringsEndingWith("b").iterator().hasNext());
    }

    @Test
    public void zeroWithBlank() {
        ModifiableDAWGSet dawg = new ModifiableDAWGSet();
        dawg.add("");
        dawg.add("\0");
        CompressedDAWGSet cdawg = dawg.compress();

        Iterator<String> iterator = dawg.iterator();
        assertTrue(iterator.hasNext());
        assertEquals("", iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals("\0", iterator.next());
        assertFalse(iterator.hasNext());
        
        iterator = cdawg.iterator();
        assertTrue(iterator.hasNext());
        assertEquals("", iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals("\0", iterator.next());
        assertFalse(iterator.hasNext());

        Set<String> expected = new HashSet<>(Arrays.asList("", "\0"));
        Set<String> actual = new HashSet<>();
        for (String word : dawg.getStringsEndingWith(""))
            actual.add(word);
        assertEquals(expected, actual);
        
        actual = new HashSet<>();
        for (String word : cdawg.getStringsEndingWith(""))
            actual.add(word);
        assertEquals(expected, actual);

        iterator = dawg.getStringsStartingWith("\0").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("\0", iterator.next());
        assertFalse(iterator.hasNext());

        iterator = cdawg.getStringsStartingWith("\0").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("\0", iterator.next());
        assertFalse(iterator.hasNext());

        iterator = dawg.getStringsEndingWith("\0").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("\0", iterator.next());
        assertFalse(iterator.hasNext());

        iterator = cdawg.getStringsEndingWith("\0").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("\0", iterator.next());
        assertFalse(iterator.hasNext());

        assertFalse(dawg.getStringsStartingWith("b").iterator().hasNext());
        assertFalse(dawg.getStringsEndingWith("b").iterator().hasNext());

        assertFalse(cdawg.getStringsStartingWith("b").iterator().hasNext());
        assertFalse(cdawg.getStringsEndingWith("b").iterator().hasNext());
    }

    @Test
    public void similarBeginningAndEnd() {
        ModifiableDAWGSet dawg = new ModifiableDAWGSet();
        dawg.add("tet");
        dawg.add("tetatet");
        CompressedDAWGSet cdawg = dawg.compress();

        Iterator<String> iterator = dawg.iterator();
        assertTrue(iterator.hasNext());
        assertEquals("tet", iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals("tetatet", iterator.next());
        assertFalse(iterator.hasNext());
        
        iterator = cdawg.iterator();
        assertTrue(iterator.hasNext());
        assertEquals("tet", iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals("tetatet", iterator.next());
        assertFalse(iterator.hasNext());

        Set<String> expected = new HashSet<>(Arrays.asList("tet", "tetatet"));
        Set<String> actual = new HashSet<>();
        for (String word : dawg.getStringsEndingWith(""))
            actual.add(word);
        assertEquals(expected, actual);
        
        actual = new HashSet<>();
        for (String word : cdawg.getStringsEndingWith(""))
            actual.add(word);
        assertEquals(expected, actual);

        iterator = dawg.getStringsStartingWith("tet").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("tet", iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals("tetatet", iterator.next());
        assertFalse(iterator.hasNext());

        iterator = cdawg.getStringsStartingWith("tet").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("tet", iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals("tetatet", iterator.next());
        assertFalse(iterator.hasNext());

        expected = new HashSet<>(Arrays.asList("tet", "tetatet"));
        actual = new HashSet<>();
        for (String word : dawg.getStringsEndingWith("tet"))
            actual.add(word);
        assertEquals(expected, actual);
        
        actual = new HashSet<>();
        for (String word : cdawg.getStringsEndingWith("tet"))
            actual.add(word);
        assertEquals(expected, actual);

        iterator = dawg.getStringsStartingWith("teta").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("tetatet", iterator.next());
        assertFalse(iterator.hasNext());

        iterator = cdawg.getStringsStartingWith("teta").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("tetatet", iterator.next());
        assertFalse(iterator.hasNext());

        iterator = dawg.getStringsEndingWith("atet").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("tetatet", iterator.next());
        assertFalse(iterator.hasNext());

        iterator = cdawg.getStringsEndingWith("atet").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("tetatet", iterator.next());
        assertFalse(iterator.hasNext());

        iterator = dawg.getStringsStartingWith("tetatet").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("tetatet", iterator.next());
        assertFalse(iterator.hasNext());

        iterator = cdawg.getStringsStartingWith("tetatet").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("tetatet", iterator.next());
        assertFalse(iterator.hasNext());

        iterator = dawg.getStringsEndingWith("tetatet").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("tetatet", iterator.next());
        assertFalse(iterator.hasNext());

        iterator = cdawg.getStringsEndingWith("tetatet").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("tetatet", iterator.next());
        assertFalse(iterator.hasNext());

        assertFalse(dawg.getStringsStartingWith("b").iterator().hasNext());
        assertFalse(dawg.getStringsEndingWith("b").iterator().hasNext());

        assertFalse(cdawg.getStringsStartingWith("b").iterator().hasNext());
        assertFalse(cdawg.getStringsEndingWith("b").iterator().hasNext());
    }

    @Test
    public void oneWordPartOfAnother() {
        ModifiableDAWGSet dawg = new ModifiableDAWGSet();
        dawg.add("tet");
        dawg.add("tetra");
        CompressedDAWGSet cdawg = dawg.compress();

        Iterator<String> iterator = dawg.iterator();
        assertTrue(iterator.hasNext());
        assertEquals("tet", iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals("tetra", iterator.next());
        assertFalse(iterator.hasNext());
        
        iterator = cdawg.iterator();
        assertTrue(iterator.hasNext());
        assertEquals("tet", iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals("tetra", iterator.next());
        assertFalse(iterator.hasNext());

        Set<String> expected = new HashSet<>(Arrays.asList("tet", "tetra"));
        Set<String> actual = new HashSet<>();
        for (String word : dawg.getStringsEndingWith(""))
            actual.add(word);
        assertEquals(expected, actual);
        
        actual = new HashSet<>();
        for (String word : cdawg.getStringsEndingWith(""))
            actual.add(word);
        assertEquals(expected, actual);

        iterator = dawg.getStringsStartingWith("tet").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("tet", iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals("tetra", iterator.next());
        assertFalse(iterator.hasNext());

        iterator = cdawg.getStringsStartingWith("tet").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("tet", iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals("tetra", iterator.next());
        assertFalse(iterator.hasNext());

        expected = new HashSet<>(Arrays.asList("tet"));
        actual = new HashSet<>();
        for (String word : dawg.getStringsEndingWith("tet"))
            actual.add(word);
        assertEquals(expected, actual);
        
        actual = new HashSet<>();
        for (String word : cdawg.getStringsEndingWith("tet"))
            actual.add(word);
        assertEquals(expected, actual);

        expected = new HashSet<>(Arrays.asList("tet"));
        actual = new HashSet<>();
        for (String word : dawg.getStringsEndingWith("t"))
            actual.add(word);
        assertEquals(expected, actual);
        
        actual = new HashSet<>();
        for (String word : cdawg.getStringsEndingWith("t"))
            actual.add(word);
        assertEquals(expected, actual);

        iterator = dawg.getStringsStartingWith("tetr").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("tetra", iterator.next());
        assertFalse(iterator.hasNext());

        iterator = cdawg.getStringsStartingWith("tetr").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("tetra", iterator.next());
        assertFalse(iterator.hasNext());

        iterator = dawg.getStringsEndingWith("etra").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("tetra", iterator.next());
        assertFalse(iterator.hasNext());

        iterator = cdawg.getStringsEndingWith("etra").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("tetra", iterator.next());
        assertFalse(iterator.hasNext());

        iterator = dawg.getStringsStartingWith("tetra").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("tetra", iterator.next());
        assertFalse(iterator.hasNext());

        iterator = cdawg.getStringsStartingWith("tetra").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("tetra", iterator.next());
        assertFalse(iterator.hasNext());

        iterator = dawg.getStringsEndingWith("tetra").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("tetra", iterator.next());
        assertFalse(iterator.hasNext());

        iterator = cdawg.getStringsEndingWith("tetra").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("tetra", iterator.next());
        assertFalse(iterator.hasNext());

        assertFalse(dawg.getStringsStartingWith("b").iterator().hasNext());
        assertFalse(dawg.getStringsEndingWith("b").iterator().hasNext());

        assertFalse(cdawg.getStringsStartingWith("b").iterator().hasNext());
        assertFalse(cdawg.getStringsEndingWith("b").iterator().hasNext());
    }
}