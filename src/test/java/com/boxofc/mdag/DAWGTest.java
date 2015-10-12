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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Kevin
 */
public class DAWGTest 
{
    static ArrayList<String> initialWordArrayList;
    ArrayList<String> wordArrayList;
    MDAG dawg1;
    MDAG dawg2;
    
    
    @BeforeClass
    public static void init() throws IOException
    {
        initialWordArrayList = new ArrayList<String>(100000);
        BufferedReader breader = new BufferedReader(new FileReader(new File("words.txt")));
        String currentWord;
        
        while((currentWord = breader.readLine()) != null)
            initialWordArrayList.add(currentWord);
    }
    
    
    @Before
    public void initializer()
    {
        wordArrayList = new ArrayList<String>(initialWordArrayList);
        dawg1 = new MDAG(wordArrayList);
        
        Collections.shuffle(wordArrayList);
        dawg2 = new MDAG(wordArrayList);

        dawg2.simplify();
    }
    
    
    @Test
    public void dawgBGetMinimizationIndexTest()
    {
       assert dawg1.calculateMinimizationProcessingStartIndex("", "") == -1;
       assert dawg1.calculateMinimizationProcessingStartIndex("abcd", "efg") == 0;
       assert dawg1.calculateMinimizationProcessingStartIndex("abcd", "ab") == 2;
       assert dawg1.calculateMinimizationProcessingStartIndex("abcd", "abcd") == -1;
       assert dawg1.calculateMinimizationProcessingStartIndex("abcd", "abd") == 2;
       assert dawg1.calculateMinimizationProcessingStartIndex("abcd", "abcr") == 3;
       assert dawg1.calculateMinimizationProcessingStartIndex("abcd", "") == 0;
       assert dawg1.calculateMinimizationProcessingStartIndex("", "abcd") == -1;
    }
    
    @Test
    public void dawgBGetLongestStoredSubsequenceTest()
    {  
        assert(dawg1.determineLongestPrefixInMDAG("do").equals("do"));
        assert(dawg1.determineLongestPrefixInMDAG("doggy").equals("doggy"));
        assert(dawg1.determineLongestPrefixInMDAG("c").equals("c"));
        assert(dawg1.determineLongestPrefixInMDAG("catsing").equals("cats"));
        assert(dawg1.determineLongestPrefixInMDAG("brolic").equals("bro"));
        assert(dawg1.determineLongestPrefixInMDAG("1234").equals(""));
        assert(dawg1.determineLongestPrefixInMDAG("Czechoslovakians").equals("Czechoslovakians"));
    }
    
    @Test
    public void dawgBGetTransitionPathFirstConfluenceNodeDataTest()
    {
        assert(dawg1.getTransitionPathFirstConfluenceNodeData((MDAGNode)dawg1.getSourceNode(), "caution").get("confluenceNode") != null);
        assert(dawg1.getTransitionPathFirstConfluenceNodeData((MDAGNode)dawg1.getSourceNode(), "abated").get("confluenceNode") != null);
        assert(dawg1.getTransitionPathFirstConfluenceNodeData((MDAGNode)dawg1.getSourceNode(), "watching").get("confluenceNode") != null);
    }
    
    
    @Test
    public void dawgBBuildTest()
    {
        for(String currentWord : wordArrayList)
        {
            assert dawg1.contains(currentWord) : "dawg1 does not contain " + currentWord;
            assert dawg2.contains(currentWord) : "dawg2 does not contain " + currentWord;
        }
            
    }
    
    @Test
    public void removeWordTest() throws IOException
    {
        int numberOfRuns = 20;
        int wordArrayListSize = wordArrayList.size();
        
        for (int i = 0; i < numberOfRuns; i++) {
            initializer();
            int wordIndex = (int)(Math.random() * wordArrayListSize);
            String toBeRemovedWord = wordArrayList.get(wordIndex);

            MDAG testDAWG = new MDAG(wordArrayList);
            testDAWG.removeString(wordArrayList.get(wordIndex));

            wordArrayList.remove((int)wordIndex);
            MDAG controlTestDAWG = new MDAG(wordArrayList);

            assert testDAWG.getNodeCount() == controlTestDAWG.getNodeCount() : "Removed word: " + toBeRemovedWord;
            assert testDAWG.getEquivalenceClassCount() == controlTestDAWG.getEquivalenceClassCount() : "Removed word: " + toBeRemovedWord;
            assert testDAWG.getTransitionCount() == controlTestDAWG.getTransitionCount() : "Removed word: " + toBeRemovedWord;
        }
    }
    
    public int[][] removeWord2DataProvider()
    {
        int numberOfRuns = 20;
        int intervalSize = 20;
        
        int[][] parameterObjectDoubleArray = new int[numberOfRuns][];

        int wordArrayListSize = wordArrayList.size();
        for(int i = 0; i < numberOfRuns; i++, wordArrayListSize -= intervalSize)
        {
            int intervalBoundaryIndex1 = (int)(Math.random() * wordArrayListSize);
            int intervalBoundaryIndex2;
            
            if(intervalBoundaryIndex1 + intervalSize >= wordArrayListSize)
                intervalBoundaryIndex2 = intervalBoundaryIndex1 - intervalSize;
            else
                intervalBoundaryIndex2 = intervalBoundaryIndex1 + intervalSize;
            
            int[] currentParameterArray = new int[2];
            currentParameterArray[0] = Math.min(intervalBoundaryIndex1, intervalBoundaryIndex2);
            currentParameterArray[1] = Math.max(intervalBoundaryIndex1, intervalBoundaryIndex2);
            parameterObjectDoubleArray[i] = currentParameterArray;
        }
        
        return parameterObjectDoubleArray;
    }
    
    
    @Test
    public void removeWord2() throws IOException
    {
        for (int interval[] : removeWord2DataProvider()) {
            initializer();
            int intervalBegin = interval[0];
            int onePastIntervalEnd = interval[1];
            MDAG testDAWG = new MDAG(wordArrayList);

            int intervalSize = onePastIntervalEnd - intervalBegin;
            for(int i = 0; i < intervalSize; i++)
                testDAWG.removeString(wordArrayList.get(intervalBegin + i));

            for(int i = 0; i < intervalSize; i++)
                wordArrayList.remove(intervalBegin);

            MDAG controlTestDAWG = new MDAG(wordArrayList);

            assert testDAWG.getNodeCount() == controlTestDAWG.getNodeCount();
            assert testDAWG.getEquivalenceClassCount() == controlTestDAWG.getEquivalenceClassCount();
            assert testDAWG.getTransitionCount() == controlTestDAWG.getTransitionCount();
        }
    }
    
    

    
    
    @Test
    public void getAllWordsTest()
    {
        HashSet<String> wordHashSet1 = dawg1.getAllStrings();
        HashSet<String> wordHashSet2 = dawg2.getAllStrings();
        assert wordHashSet1.containsAll(wordArrayList);
        assert wordHashSet2.containsAll(wordArrayList);
        
    }
    
    
    @Test
    public void containsTest()
    {
        for(int i = 0; i < 100; i++)
        {
            assert dawg1.contains(wordArrayList.get(i));
            assert dawg2.contains(wordArrayList.get(i));
        }
    }
    
    @Test
    public void getAllWordsWithPrefixTest() throws IOException
    {
        for (String prefixStr : new String[]{"ang", "iter", "con", "pro", "nan", "ing", "inter", "ton", "tion" }) {
            initializer();
            HashSet<String> controlSet = new HashSet<String>();

            for(String str : wordArrayList)
            {
                if(str.startsWith(prefixStr))
                    controlSet.add(str);
            }

            assert dawg1.getStringsStartingWith(prefixStr).equals(controlSet);
            assert dawg2.getStringsStartingWith(prefixStr).equals(controlSet);
        }
    }
    
    
    @Test
    public void getStringsWithSubstringTest() throws IOException
    {
        for (String substringStr : new String[]{"ang", "iter", "con", "pro", "nan", "ing", "inter", "ton", "tion" }) {
            initializer();
            HashSet<String> controlSet = new HashSet<String>();

            for(String str : wordArrayList)
            {
                if(str.contains(substringStr))
                    controlSet.add(str);
            }

            assert dawg1.getStringsWithSubstring(substringStr).equals(controlSet);
            assert dawg2.getStringsWithSubstring(substringStr).equals(controlSet);
        }
    }
    
    
    @Test
    public void getStringsEndingWithTest() throws IOException
    {
        for (String suffixStr : new String[]{"ang", "iter", "con", "pro", "nan", "ing", "inter", "ton", "tion" }) {
            initializer();
            HashSet<String> controlSet = new HashSet<String>();

            for(String str : wordArrayList)
            {
                if(str.endsWith(suffixStr))
                    controlSet.add(str);
            }

            assert dawg1.getStringsEndingWith(suffixStr).equals(controlSet);
            assert dawg2.getStringsEndingWith(suffixStr).equals(controlSet);
        }
    }
}
