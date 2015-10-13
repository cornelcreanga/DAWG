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

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class DAWGSimpleTest {
    @Test
    public void addSimple() throws IOException {
        String words[] = {
            "a", "xes", "xe", "xs"
        };
        MDAG dawg = new MDAG(words);
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
        assertEquals(3, dawg.getNodeCount());
        assertEquals(5, dawg.getTransitionCount());
    }
}