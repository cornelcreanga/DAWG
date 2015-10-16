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
    public void addSimple() throws IOException {
        String words[] = {
            "a", "xes", "xe", "xs"
        };
        MDAG dawg = new MDAG();
        dawg.addAll(words);
        Arrays.sort(words);

        int i = 0;
        for (String word : dawg.getAllStrings())
            assertEquals(words[i++], word);
        assertEquals(words.length, i);

        String wordsXe[] = {"xe", "xes"};
        i = 0;
        for (String word : dawg.getStringsStartingWith("xe"))
            assertEquals(wordsXe[i++], word);

        String wordsS[] = {"xes", "xs"};
        Set<String> expected = new HashSet<>(Arrays.asList(wordsS));
        Set<String> actual = new HashSet<>();
        for (String word : dawg.getStringsEndingWith("s"))
            actual.add(word);
        assertEquals(expected, actual);
        
        assertEquals(4, dawg.size());
        assertEquals(4, dawg.getNodeCount());
        assertEquals(5, dawg.getTransitionCount());
    }
  
    @Test
    public void addCasual() throws IOException {
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
        for ( String w[] : Permutations.from(words)) {
            MDAG dawg = new MDAG();
            dawg.addAll(w);
            int i = 0;
            for (String s : dawg)
                assertEquals(words[i++], s);
            assertEquals(words.length, i);

            Set<String> actual = new HashSet<>();
            for (String word : dawg.getStringsEndingWith(""))
                actual.add(word);
            assertEquals(expected, actual);
        }
    }

    @Test
    public void add() throws IOException {
        MDAG dawg = new MDAG();
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

        Arrays.sort(words);
        int i = 0;
        for (String word : dawg)
            assertEquals(words[i++], word);
        assertEquals(words.length, i);

        String wordsAs[] = {"as", "asse", "assent", "asses", "assiez", "assions"};
        i = 0;
        for (String word : dawg.getStringsStartingWith("as"))
            assertEquals(wordsAs[i++], word);

        String wordsOns[] = {"assions", "erions", "erons", "ions", "ons"};
        Set<String> expected = new HashSet<>(Arrays.asList(wordsOns));
        Set<String> actual = new HashSet<>();
        for (String word : dawg.getStringsEndingWith("ons"))
            actual.add(word);
        assertEquals(expected, actual);

        String wordsXe[] = {"xe"};
        expected = new HashSet<>(Arrays.asList(wordsXe));
        actual = new HashSet<>();
        for (String word : dawg.getStringsEndingWith("xe"))
            actual.add(word);
        assertEquals(expected, actual);
        
        assertEquals(25, dawg.getNodeCount());
        assertEquals(39, dawg.size());
    }

    @Test(expected = NoSuchElementException.class)
    public void empty() {
        MDAG dawg = new MDAG();
        assertFalse(dawg.iterator().hasNext());
        dawg.iterator().next();
    }

    @Test(expected = NoSuchElementException.class)
    public void emptySuffix() {
        MDAG dawg = new MDAG();
        assertFalse(dawg.getStringsEndingWith("").iterator().hasNext());
        dawg.getStringsEndingWith("").iterator().next();
    }

    @Test(expected = NoSuchElementException.class)
    public void emptyCollection() {
        MDAG dawg = new MDAG();
        dawg.addAll();
        assertFalse(dawg.getStringsEndingWith("").iterator().hasNext());
        dawg.getStringsEndingWith("").iterator().next();
    }

    @Test
    public void file() throws IOException {
        MDAG dawg = new MDAG();
        // Source: http://www.mieliestronk.com/wordlist.html
        try (FileInputStream fis = new FileInputStream("corncob_lowercase.txt")) {
            dawg.addAll(fis);
        }
        int i = 0;
        for (String word : dawg)
            i++;
        assertEquals(58109, i);

        i = 0;
        for (String word : dawg.getStringsEndingWith(""))
            i++;
        assertEquals(58109, i);
    }

    @Test
    public void blank() {
        MDAG dawg = new MDAG();
        dawg.add("");

        Iterator<String> iterator = dawg.iterator();
        assertTrue(iterator.hasNext());
        assertEquals("", iterator.next());
        assertFalse(iterator.hasNext());

        iterator = dawg.getStringsEndingWith("").iterator();
        assertTrue(iterator.hasNext());
        assertEquals("", iterator.next());
        assertFalse(iterator.hasNext());
    }
/*
    @Test
    public void shortWord() {
      DAWG dawg = new DAWG();
      dawg.add( "a" );

      Iterator< String > iterator = dawg.iterator();
      assertTrue( iterator.hasNext() );
      assertEquals( "a", iterator.next() );
      assertFalse( iterator.hasNext() );

      iterator = dawg.getWordsEndingWith( "" ).iterator();
      assertTrue( iterator.hasNext() );
      assertEquals( "a", iterator.next() );
      assertFalse( iterator.hasNext() );

      iterator = dawg.getWordsStartingWith( "a" ).iterator();
      assertTrue( iterator.hasNext() );
      assertEquals( "a", iterator.next() );
      assertFalse( iterator.hasNext() );

      iterator = dawg.getWordsEndingWith( "a" ).iterator();
      assertTrue( iterator.hasNext() );
      assertEquals( "a", iterator.next() );
      assertFalse( iterator.hasNext() );

      assertFalse( dawg.getWordsStartingWith( "b" ).iterator().hasNext() );
      assertFalse( dawg.getWordsEndingWith( "b" ).iterator().hasNext() );
    }

    @Test
    public void zero() {
      DAWG dawg = new DAWG();
      dawg.add( "\0" );

      Iterator< String > iterator = dawg.iterator();
      assertTrue( iterator.hasNext() );
      assertEquals( "\0", iterator.next() );
      assertFalse( iterator.hasNext() );

      iterator = dawg.getWordsEndingWith( "" ).iterator();
      assertTrue( iterator.hasNext() );
      assertEquals( "\0", iterator.next() );
      assertFalse( iterator.hasNext() );

      iterator = dawg.getWordsStartingWith( "\0" ).iterator();
      assertTrue( iterator.hasNext() );
      assertEquals( "\0", iterator.next() );
      assertFalse( iterator.hasNext() );

      iterator = dawg.getWordsEndingWith( "\0" ).iterator();
      assertTrue( iterator.hasNext() );
      assertEquals( "\0", iterator.next() );
      assertFalse( iterator.hasNext() );

      assertFalse( dawg.getWordsStartingWith( "b" ).iterator().hasNext() );
      assertFalse( dawg.getWordsEndingWith( "b" ).iterator().hasNext() );
    }

    @Test
    public void wordWithBlank() {
      DAWG dawg = new DAWG();
      dawg.add( "" );
      dawg.add( "add" );

      Set< String > expected = new HashSet<>( Arrays.asList( "", "add" ) );
      Set< String > actual = new HashSet<>();
      for ( String word : dawg.getWordsEndingWith( "" ) )
        actual.add( word );
      assertEquals( expected, actual );

      dawg.add( "a" );
      expected = new HashSet<>( Arrays.asList( "", "a", "add" ) );
      actual = new HashSet<>();
      for ( String word : dawg.getWordsEndingWith( "" ) )
        actual.add( word );
      assertEquals( expected, actual );

      dawg.add( "ad" );
      expected = new HashSet<>( Arrays.asList( "", "a", "ad", "add" ) );
      actual = new HashSet<>();
      for ( String word : dawg.getWordsEndingWith( "" ) )
        actual.add( word );
      assertEquals( expected, actual );
    }

    @Test
    public void shortWordWithBlank() {
      DAWG dawg = new DAWG();
      dawg.add( "" );
      dawg.add( "a" );

      Iterator< String > iterator = dawg.iterator();
      assertTrue( iterator.hasNext() );
      assertEquals( "", iterator.next() );
      assertTrue( iterator.hasNext() );
      assertEquals( "a", iterator.next() );
      assertFalse( iterator.hasNext() );

      Set< String > expected = new HashSet<>( Arrays.asList( "", "a" ) );
      Set< String > actual = new HashSet<>();
      for ( String word : dawg.getWordsEndingWith( "" ) )
        actual.add( word );
      assertEquals( expected, actual );

      iterator = dawg.getWordsStartingWith( "a" ).iterator();
      assertTrue( iterator.hasNext() );
      assertEquals( "a", iterator.next() );
      assertFalse( iterator.hasNext() );

      iterator = dawg.getWordsEndingWith( "a" ).iterator();
      assertTrue( iterator.hasNext() );
      assertEquals( "a", iterator.next() );
      assertFalse( iterator.hasNext() );

      assertFalse( dawg.getWordsStartingWith( "b" ).iterator().hasNext() );
      assertFalse( dawg.getWordsEndingWith( "b" ).iterator().hasNext() );
    }

    @Test
    public void zeroWithBlank() {
      DAWG dawg = new DAWG();
      dawg.add( "" );
      dawg.add( "\0" );

      Iterator< String > iterator = dawg.iterator();
      assertTrue( iterator.hasNext() );
      assertEquals( "", iterator.next() );
      assertTrue( iterator.hasNext() );
      assertEquals( "\0", iterator.next() );
      assertFalse( iterator.hasNext() );

      Set< String > expected = new HashSet<>( Arrays.asList( "", "\0" ) );
      Set< String > actual = new HashSet<>();
      for ( String word : dawg.getWordsEndingWith( "" ) )
        actual.add( word );
      assertEquals( expected, actual );

      iterator = dawg.getWordsStartingWith( "\0" ).iterator();
      assertTrue( iterator.hasNext() );
      assertEquals( "\0", iterator.next() );
      assertFalse( iterator.hasNext() );

      iterator = dawg.getWordsEndingWith( "\0" ).iterator();
      assertTrue( iterator.hasNext() );
      assertEquals( "\0", iterator.next() );
      assertFalse( iterator.hasNext() );

      assertFalse( dawg.getWordsStartingWith( "b" ).iterator().hasNext() );
      assertFalse( dawg.getWordsEndingWith( "b" ).iterator().hasNext() );
    }

    @Test
    public void similarBeginningAndEnd() {
      DAWG dawg = new DAWG();
      dawg.add( "tet" );
      dawg.add( "tetatet" );

      Iterator< String > iterator = dawg.iterator();
      assertTrue( iterator.hasNext() );
      assertEquals( "tet", iterator.next() );
      assertTrue( iterator.hasNext() );
      assertEquals( "tetatet", iterator.next() );
      assertFalse( iterator.hasNext() );

      Set< String > expected = new HashSet<>( Arrays.asList( "tet", "tetatet" ) );
      Set< String > actual = new HashSet<>();
      for ( String word : dawg.getWordsEndingWith( "" ) )
        actual.add( word );
      assertEquals( expected, actual );

      iterator = dawg.getWordsStartingWith( "tet" ).iterator();
      assertTrue( iterator.hasNext() );
      assertEquals( "tet", iterator.next() );
      assertTrue( iterator.hasNext() );
      assertEquals( "tetatet", iterator.next() );
      assertFalse( iterator.hasNext() );

      expected = new HashSet<>( Arrays.asList( "tet", "tetatet" ) );
      actual = new HashSet<>();
      for ( String word : dawg.getWordsEndingWith( "tet" ) )
        actual.add( word );
      assertEquals( expected, actual );

      iterator = dawg.getWordsStartingWith( "teta" ).iterator();
      assertTrue( iterator.hasNext() );
      assertEquals( "tetatet", iterator.next() );
      assertFalse( iterator.hasNext() );

      iterator = dawg.getWordsEndingWith( "atet" ).iterator();
      assertTrue( iterator.hasNext() );
      assertEquals( "tetatet", iterator.next() );
      assertFalse( iterator.hasNext() );

      iterator = dawg.getWordsStartingWith( "tetatet" ).iterator();
      assertTrue( iterator.hasNext() );
      assertEquals( "tetatet", iterator.next() );
      assertFalse( iterator.hasNext() );

      iterator = dawg.getWordsEndingWith( "tetatet" ).iterator();
      assertTrue( iterator.hasNext() );
      assertEquals( "tetatet", iterator.next() );
      assertFalse( iterator.hasNext() );

      assertFalse( dawg.getWordsStartingWith( "b" ).iterator().hasNext() );
      assertFalse( dawg.getWordsEndingWith( "b" ).iterator().hasNext() );
    }

    @Test
    public void oneWordPartOfAnother() {
      DAWG dawg = new DAWG();
      dawg.add( "tet" );
      dawg.add( "tetra" );

      Iterator< String > iterator = dawg.iterator();
      assertTrue( iterator.hasNext() );
      assertEquals( "tet", iterator.next() );
      assertTrue( iterator.hasNext() );
      assertEquals( "tetra", iterator.next() );
      assertFalse( iterator.hasNext() );

      Set< String > expected = new HashSet<>( Arrays.asList( "tet", "tetra" ) );
      Set< String > actual = new HashSet<>();
      for ( String word : dawg.getWordsEndingWith( "" ) )
        actual.add( word );
      assertEquals( expected, actual );

      iterator = dawg.getWordsStartingWith( "tet" ).iterator();
      assertTrue( iterator.hasNext() );
      assertEquals( "tet", iterator.next() );
      assertTrue( iterator.hasNext() );
      assertEquals( "tetra", iterator.next() );
      assertFalse( iterator.hasNext() );

      expected = new HashSet<>( Arrays.asList( "tet" ) );
      actual = new HashSet<>();
      for ( String word : dawg.getWordsEndingWith( "tet" ) )
        actual.add( word );
      assertEquals( expected, actual );

      expected = new HashSet<>( Arrays.asList( "tet" ) );
      actual = new HashSet<>();
      for ( String word : dawg.getWordsEndingWith( "t" ) )
        actual.add( word );
      assertEquals( expected, actual );

      iterator = dawg.getWordsStartingWith( "tetr" ).iterator();
      assertTrue( iterator.hasNext() );
      assertEquals( "tetra", iterator.next() );
      assertFalse( iterator.hasNext() );

      iterator = dawg.getWordsEndingWith( "etra" ).iterator();
      assertTrue( iterator.hasNext() );
      assertEquals( "tetra", iterator.next() );
      assertFalse( iterator.hasNext() );

      iterator = dawg.getWordsStartingWith( "tetra" ).iterator();
      assertTrue( iterator.hasNext() );
      assertEquals( "tetra", iterator.next() );
      assertFalse( iterator.hasNext() );

      iterator = dawg.getWordsEndingWith( "tetra" ).iterator();
      assertTrue( iterator.hasNext() );
      assertEquals( "tetra", iterator.next() );
      assertFalse( iterator.hasNext() );

      assertFalse( dawg.getWordsStartingWith( "b" ).iterator().hasNext() );
      assertFalse( dawg.getWordsEndingWith( "b" ).iterator().hasNext() );
    }*/
}