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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * A minimalistic directed acyclical graph suitable for storing a set of Strings.
 
 * @author Kevin
 */
public class ModifiableDAWGSet extends DAWGSet {
    //Increment for node identifiers.
    private int id;
    
    //MDAGNode from which all others in the structure are reachable (all manipulation and non-simplified ModifiableDAWGSet search operations begin from this).
    private final ModifiableDAWGNode sourceNode = new ModifiableDAWGNode(this, false, id++);
    
    private final ModifiableDAWGNode endNode = new ModifiableDAWGNode(this, false, id++);

    //HashMap which contains the MDAGNodes collectively representing the all unique equivalence classes in the ModifiableDAWGSet.
    //Uniqueness is defined by the types of transitions allowed from, and number and type of nodes reachable
    //from the node of interest. Since there are no duplicate nodes in an ModifiableDAWGSet, # of equivalence classes == # of nodes.
    private final HashMap<ModifiableDAWGNode, ModifiableDAWGNode> equivalenceClassMDAGNodeHashMap = new HashMap<>();
    
    //NavigableSet which will contain the set of unique characters used as transition labels in the ModifiableDAWGSet
    private final TreeSet<Character> charTreeSet = new TreeSet<>();
    
    //An int denoting the total number of transitions between the nodes of the ModifiableDAWGSet
    private int transitionCount;
    
    //Total number of words contained in this ModifiableDAWGSet.
    private int size;
    
    //Maximal length of all words added to this DAWG. Does not decrease on removing.
    private int maxLength;
    
    private boolean withIncomingTransitions = true;
    
    /**
     * Creates an MDAG from a collection of Strings.
     
     * @param strCollection     a {@link java.util.Iterable} containing Strings that the MDAG will contain
     */
    public ModifiableDAWGSet(Iterable<? extends String> strCollection) {
        addAll(strCollection);
    }
    
    /**
     * Creates empty MDAG. Use {@link #addString} to fill it.
     */
    public ModifiableDAWGSet() {
    }
    
    /**
     * Creates a ModifiableDAWGSet from a newline delimited file containing the data of interest.
     
     * @param dataFile          a {@link java.io.File} representation of a file
                          containing the Strings that the ModifiableDAWGSet will contain
     * @return true if and only if this ModifiableDAWGSet was changed as a result of this call
     * @throws IOException      if {@code datafile} cannot be opened, or a read operation on it cannot be carried out
     */
    public boolean addAll(File dataFile) throws IOException {
        try (FileInputStream fis = new FileInputStream(dataFile)) {
            return addAll(fis);
        }
    }
    
    /**
     * Creates a ModifiableDAWGSet from a newline delimited file containing the data of interest.
     
     * @param dataFile          a {@link java.io.InputStream} representation of a file
                          containing the Strings that the ModifiableDAWGSet will contain
     * @return true if and only if this ModifiableDAWGSet was changed as a result of this call
     * @throws IOException      if {@code datafile} cannot be opened, or a read operation on it cannot be carried out
     */
    public boolean addAll(InputStream dataFile) throws IOException {
        final IOException exceptionToThrow[] = new IOException[1];
        try (InputStreamReader isr = new InputStreamReader(dataFile);
             final BufferedReader br = new BufferedReader(isr)) {
            return addAll(new Iterable<String>() {
                @Override
                public Iterator<String> iterator() {
                    return new Iterator<String>() {
                        String nextLine;

                        @Override
                        public boolean hasNext() {
                            if (nextLine == null) {
                                try {
                                    nextLine = br.readLine();
                                    return nextLine != null;
                                } catch (IOException e) {
                                    exceptionToThrow[0] = e;
                                    throw new RuntimeException(e);
                                }
                            } else
                                return true;
                        }

                        @Override
                        public String next() {
                            if (nextLine != null || hasNext()) {
                                String line = nextLine;
                                nextLine = null;
                                return line;
                            } else
                                throw new NoSuchElementException();
                        }
                    };
                }
            });
        } catch (RuntimeException e) {
            if (e.getCause() == exceptionToThrow[0] && exceptionToThrow[0] != null)
                throw exceptionToThrow[0];
            throw e;
        }
    }
    
    /**
     * Adds a Collection of Strings to the ModifiableDAWGSet.
     
     * @param strCollection     a {@link java.util.Collection} containing Strings to be added to the ModifiableDAWGSet
     * @return true if and only if this ModifiableDAWGSet was changed as a result of this call
     */
    public final boolean addAll(String... strCollection) {
        return addAll(Arrays.asList(strCollection));
    }
    
    /**
     * Adds a Collection of Strings to the ModifiableDAWGSet.
     
     * @param strCollection     a {@link java.util.Iterable} containing Strings to be added to the ModifiableDAWGSet
     * @return true if and only if this ModifiableDAWGSet was changed as a result of this call
     */
    public final boolean addAll(Iterable<? extends String> strCollection) {
        boolean result = false;
        boolean empty = true;
        String previousString = "";

        //Add all the Strings in strCollection to the ModifiableDAWGSet.
        for (String currentString : strCollection) {
            empty = false;
            int mpsIndex = calculateMinimizationProcessingStartIndex(previousString, currentString);

            //If the transition path of the previousString needs to be examined for minimization or
            //equivalence class representation after a certain point, call replaceOrRegister to do so.
            if (mpsIndex != -1) {
                String transitionSubstring = previousString.substring(0, mpsIndex);
                String minimizationProcessingSubString = previousString.substring(mpsIndex);
                replaceOrRegister(sourceNode.transition(transitionSubstring), minimizationProcessingSubString);
            }

            result |= addStringInternal(currentString);
            previousString = currentString;
        }

        if (!empty) {
            //Since we delay the minimization of the previously-added String
            //until after we read the next one, we need to have a seperate
            //statement to minimize the absolute last String.
            if (!previousString.isEmpty())
                replaceOrRegister(sourceNode, previousString);
        }
        return result;
    }
    
    /**
     * Adds a string to the ModifiableDAWGSet.
     
     * @param str       the String to be added to the ModifiableDAWGSet
     * @return true if ModifiableDAWGSet didn't contain this string yet
     */
    public boolean add(String str) {
        boolean result = addStringInternal(str);
        if (!str.isEmpty())
            replaceOrRegister(sourceNode, str);
        return result;
    }

    public boolean isWithIncomingTransitions() {
        return withIncomingTransitions;
    }

    public void setWithIncomingTransitions(boolean withIncomingTransitions) {
        this.withIncomingTransitions = withIncomingTransitions;
    }
    
    private void splitTransitionPath(ModifiableDAWGNode originNode, String storedStringSubstr) {
        HashMap<String, Object> firstConfluenceNodeDataHashMap = getTransitionPathFirstConfluenceNodeData(originNode, storedStringSubstr);
        Integer toFirstConfluenceNodeTransitionCharIndex = (Integer)firstConfluenceNodeDataHashMap.get("toConfluenceNodeTransitionCharIndex");
        ModifiableDAWGNode firstConfluenceNode = (ModifiableDAWGNode)firstConfluenceNodeDataHashMap.get("confluenceNode");
        
        if (firstConfluenceNode != null) {
            ModifiableDAWGNode firstConfluenceNodeParent = originNode.transition(storedStringSubstr.substring(0, toFirstConfluenceNodeTransitionCharIndex));
            char letter = storedStringSubstr.charAt(toFirstConfluenceNodeTransitionCharIndex);
            ModifiableDAWGNode firstConfluenceNodeClone = firstConfluenceNode.clone(firstConfluenceNodeParent, letter, id++);
            if (firstConfluenceNodeClone.isAcceptNode())
                endNode.addIncomingTransition(letter, firstConfluenceNodeClone);
            transitionCount += firstConfluenceNodeClone.getOutgoingTransitionCount();
            String unprocessedSubString = storedStringSubstr.substring(toFirstConfluenceNodeTransitionCharIndex + 1);
            splitTransitionPath(firstConfluenceNodeClone, unprocessedSubString);
        }
    }
    
    /**
     * Calculates the length of the the sub-path in a transition path, that is used only by a given string.
     
     * @param str       a String corresponding to a transition path from sourceNode
     * @return          an int denoting the size of the sub-path in the transition path
     *                  corresponding to {@code str} that is only used by {@code str}
     */
    private int calculateSoleTransitionPathLength(String str) {
        Stack<ModifiableDAWGNode> transitionPathNodeStack = sourceNode.getTransitionPathNodes(str);
        transitionPathNodeStack.pop();  //The ModifiableDAWGNode at the top of the stack is not needed
                                        //(we are processing the outgoing transitions of nodes inside str's transition path,
                                        //the outgoing transitions of the ModifiableDAWGNode at the top of the stack are outside this path)
        
        transitionPathNodeStack.trimToSize();

        //Process each node in transitionPathNodeStack, using each to determine whether the
        //transition path corresponding to str is only used by str.  This is true if and only if
        //each node in the transition path has a single outgoing transition and is not an accept state.
        while (!transitionPathNodeStack.isEmpty()) {
            ModifiableDAWGNode currentNode = transitionPathNodeStack.peek();
            if (currentNode.getOutgoingTransitionCount() <= 1 && !currentNode.isAcceptNode())
                transitionPathNodeStack.pop();
            else
                break;
        }
        
        return transitionPathNodeStack.capacity() - transitionPathNodeStack.size();
    }
    
    /**
     * Removes a String from the ModifiableDAWGSet.
     
     * @param str       the String to be removed from the ModifiableDAWGSet
     * @return true if ModifiableDAWGSet already contained this string
     */
    public boolean remove(String str) {
        //Split the transition path corresponding to str to ensure that
        //any other transition paths sharing nodes with it are not affected
        splitTransitionPath(sourceNode, str);

        //Remove from equivalenceClassMDAGNodeHashMap, the entries of all the nodes in the transition path corresponding to str.
        removeTransitionPathRegisterEntries(str);

        //Get the last node in the transition path corresponding to str
        ModifiableDAWGNode strEndNode = sourceNode.transition(str);

        //Removing non-existent word.
        if (strEndNode == null)
            return false;

        if (str.isEmpty() || strEndNode.hasOutgoingTransitions()) {
            boolean result = strEndNode.setAcceptStateStatus(false);
            if (!str.isEmpty())
                replaceOrRegister(sourceNode, str);
            if (result) {
                size--;
                if (str.isEmpty()) {
                    for (char c : strEndNode.getIncomingTransitions().keySet())
                        endNode.removeIncomingTransition(c, strEndNode);
                } else
                    endNode.removeIncomingTransition(str.charAt(str.length() - 1), strEndNode);
            }
            return result;
        } else {
            int soleInternalTransitionPathLength = calculateSoleTransitionPathLength(str);
            int internalTransitionPathLength = str.length() - 1;

            if (soleInternalTransitionPathLength == internalTransitionPathLength) {
                sourceNode.removeOutgoingTransition(str.charAt(0));
                transitionCount -= str.length();
            } else {
                //Remove the sub-path in str's transition path that is only used by str
                int toBeRemovedTransitionLabelCharIndex = internalTransitionPathLength - soleInternalTransitionPathLength;
                String prefix = str.substring(0, toBeRemovedTransitionLabelCharIndex);
                ModifiableDAWGNode latestNonSoloTransitionPathNode = sourceNode.transition(prefix);
                latestNonSoloTransitionPathNode.removeOutgoingTransition(str.charAt(toBeRemovedTransitionLabelCharIndex));
                transitionCount -= str.length() - toBeRemovedTransitionLabelCharIndex;
                endNode.removeIncomingTransition(str.charAt(str.length() - 1), strEndNode);
                replaceOrRegister(sourceNode, prefix);
            }
            size--;
            return true;
        }
    }
    
    /**
     * Determines the start index of the substring in the String most recently added to the ModifiableDAWGSet
 that corresponds to the transition path that will be next up for minimization processing.
     *
     * The "minimization processing start index" is defined as the index in {@code prevStr} which starts the substring
     * corresponding to the transition path that doesn't have its right language extended by {@code currStr}. The transition path of
     * the substring before this point is not considered for minimization in order to limit the amount of times the
     * equivalence classes of its nodes will need to be reassigned during the processing of Strings which share prefixes.
     
     * @param prevStr       the String most recently added to the ModifiableDAWGSet
     * @param currStr       the String next to be added to the ModifiableDAWGSet
     * @return              an int of the index in {@code prevStr} that starts the substring corresponding
     *                      to the transition path next up for minimization processing
     */
    int calculateMinimizationProcessingStartIndex(String prevStr, String currStr) {
        int mpsIndex;
        
        if (!currStr.startsWith(prevStr)) {
            //Loop through the corresponding indices of both Strings in search of the first index containing differing characters.
            //The transition path of the substring of prevStr from this point will need to be submitted for minimization processing.
            //The substring before this point, however, does not, since currStr will simply be extending the right languages of the
            //nodes on its transition path.
            int shortestStringLength = Math.min(prevStr.length(), currStr.length());
            for (mpsIndex = 0; mpsIndex < shortestStringLength && prevStr.charAt(mpsIndex) == currStr.charAt(mpsIndex);)
                mpsIndex++;
        } else
            mpsIndex =  -1;    //If the prevStr is a prefix of currStr, then currStr simply extends the right language of the transition path of prevStr.
        
        return mpsIndex;
    }
    
    /**
     * Determines the longest prefix of a given String that is
 the prefix of another String previously added to the ModifiableDAWGSet.
     
     * @param str       the String to be processed
     * @return          a String of the longest prefix of {@code str}
                  that is also a prefix of a String contained in the ModifiableDAWGSet
     */
    public String determineLongestPrefixInMDAG(String str) {
        ModifiableDAWGNode currentNode = sourceNode;
        int numberOfChars = str.length();
        int onePastPrefixEndIndex = 0;
        
        //Loop through the characters in str, using them in sequence to transition
        //through the ModifiableDAWGSet until the currently processing node doesn't have a transition
        //labeled with the current processing char, or there are no more characters to process.
        for (int i = 0; i < numberOfChars; i++, onePastPrefixEndIndex++) {
            char currentChar = str.charAt(i);
            if (currentNode.hasOutgoingTransition(currentChar))
                currentNode = currentNode.transition(currentChar);
            else
                break;
        }
        
        return str.substring(0, onePastPrefixEndIndex);
    }
    
    /**
     * Determines and retrieves data related to the first confluence node
     * (defined as a node with two or more incoming transitions) of a
     * transition path corresponding to a given String from a given node.
     
     * @param originNode        the ModifiableDAWGNode from which the transition path corresponding to str starts from
     * @param str               a String corresponding to a transition path in the ModifiableDAWGSet
     * @return                  a HashMap of Strings to Objects containing:
                              - an int denoting the length of the path to the first confluence node in the transition path of interest
                              - the ModifiableDAWGNode which is the first confluence node in the transition path of interest (or null if one does not exist)
     */
    HashMap<String, Object> getTransitionPathFirstConfluenceNodeData(ModifiableDAWGNode originNode, String str) {
        int currentIndex = 0;
        int charCount = str.length();
        ModifiableDAWGNode currentNode = originNode;
        
        //Loop thorugh the characters in str, sequentially using them to transition through the ModifiableDAWGSet in search of
        //(and breaking upon reaching) the first node that is the target of two or more transitions. The loop is
        //also broken from if the currently processing node doesn't have a transition labeled with the currently processing char.
        for (; currentIndex < charCount; currentIndex++) {
            char currentChar = str.charAt(currentIndex);
            currentNode = currentNode.hasOutgoingTransition(currentChar) ? currentNode.transition(currentChar) : null;
            
            if (currentNode == null || currentNode.isConfluenceNode())
                break;
        }
        
        boolean noConfluenceNode = currentNode == originNode || currentIndex == charCount;
        
        //Create a HashMap containing the index of the last char in the substring corresponding
        //to the transitoin path to the confluence node, as well as the actual confluence node
        HashMap<String, Object> confluenceNodeDataHashMap = new HashMap<>(2);
        confluenceNodeDataHashMap.put("toConfluenceNodeTransitionCharIndex", noConfluenceNode ? null : currentIndex);
        confluenceNodeDataHashMap.put("confluenceNode", noConfluenceNode ? null : currentNode);

        return confluenceNodeDataHashMap;
    }

    /**
     * Performs minimization processing on a transition path starting from a given node.
     *
     * This entails either replacing a node in the path with one that has an equivalent right language/equivalence class
     * (defined as set of transition paths that can be traversed and nodes able to be reached from it), or making it
     * a representative of a right language/equivalence class if a such a node does not already exist.
     
     * @param originNode        the ModifiableDAWGNode that the transition path corresponding to str starts from
     * @param str              a String related to a transition path
     */
    private void replaceOrRegister(ModifiableDAWGNode originNode, String str) {
        char transitionLabelChar = str.charAt(0);
        ModifiableDAWGNode relevantTargetNode = originNode.transition(transitionLabelChar);

        //If relevantTargetNode has transitions and there is at least one char left to process, recursively call
        //this on the next char in order to further processing down the transition path corresponding to str
        if (relevantTargetNode.hasOutgoingTransitions() && str.length() > 1)
            replaceOrRegister(relevantTargetNode, str.substring(1));

        //Get the node representing the equivalence class that relevantTargetNode belongs to. MDAGNodes hash on the
        //transitions paths that can be traversed from them and nodes able to be reached from them;
        //nodes with the same equivalence classes will hash to the same bucket.
        ModifiableDAWGNode equivalentNode = equivalenceClassMDAGNodeHashMap.get(relevantTargetNode);
        
        //if there is no node with the same right language as relevantTargetNode
        if (equivalentNode == null)
            equivalenceClassMDAGNodeHashMap.put(relevantTargetNode, relevantTargetNode);
        //if there is another node with the same right language as relevantTargetNode, reassign the
        //transition between originNode and relevantTargetNode, to originNode and the node representing the equivalence class of interest
        else if (equivalentNode != relevantTargetNode) {
            relevantTargetNode.decrementTargetIncomingTransitionCounts();
            transitionCount -= relevantTargetNode.getOutgoingTransitionCount(); //Since this method is recursive, the outgoing transitions of all of relevantTargetNode's child nodes have already been reassigned,
                                                                                //so we only need to decrement the transition count by the relevantTargetNode's outgoing transition count
            originNode.reassignOutgoingTransition(transitionLabelChar, relevantTargetNode, equivalentNode);
        }
    }
    
    /**
     * Adds a transition path starting from a specific node in the ModifiableDAWGSet.
     
     * @param originNode    the ModifiableDAWGNode which will serve as the start point of the to-be-created transition path
     * @param str           the String to be used to create a new transition path from {@code originNode}
     * @return true if and only if ModifiableDAWGSet has changed as a result of this call
     */
    private boolean addTransitionPath(ModifiableDAWGNode originNode, String str) {
        if (!str.isEmpty()) {
            ModifiableDAWGNode currentNode = originNode;
            int charCount = str.length();

            //Loop through the characters in str, iteratevely adding
            // a transition path corresponding to it from originNode
            for (int i = 0; i < charCount; i++, transitionCount++) {
                char currentChar = str.charAt(i);
                boolean isLastChar = i == charCount - 1;
                currentNode = currentNode.addOutgoingTransition(this, currentChar, isLastChar, id++);
                if (isLastChar)
                    endNode.addIncomingTransition(currentChar, currentNode);
                charTreeSet.add(currentChar);
            }
            size++;
            return true;
        } else if (originNode.setAcceptStateStatus(true)) {
            for (char c : originNode.getIncomingTransitions().keySet())
                endNode.addIncomingTransition(c, originNode);
            size++;
            return true;
        } else
            return false;
    }
    
    /**
     * Removes from equivalenceClassMDAGNodeHashmap the entries of all the nodes in a transition path.
     
     * @param str       a String corresponding to a transition path from sourceNode
     */
    private void removeTransitionPathRegisterEntries(String str) {
        ModifiableDAWGNode currentNode = sourceNode;

        int charCount = str.length();
        
        for (int i = 0; i < charCount; i++) {
            currentNode = currentNode.transition(str.charAt(i));
            
            //Removing non-existent word.
            if (currentNode == null)
                break;
            
            if (equivalenceClassMDAGNodeHashMap.get(currentNode) == currentNode)
                equivalenceClassMDAGNodeHashMap.remove(currentNode);
            
            //The hashCode of an ModifiableDAWGNode is cached the first time a hash is performed without a cache value present.
            //Since we just hashed currentNode, we must clear this regardless of its presence in equivalenceClassMDAGNodeHashMap
            //since we're not actually declaring equivalence class representatives here.
            currentNode.clearStoredHashCode();
        }
    }
    
    /**
     * Clones a transition path from a given node.
     
     * @param pivotConfluenceNode               the ModifiableDAWGNode that the cloning operation is to be based from
     * @param transitionStringToPivotNode       a String which corresponds with a transition path from souceNode to {@code pivotConfluenceNode}
     * @param str                               a String which corresponds to the transition path from {@code pivotConfluenceNode} that is to be cloned
     */
    private void cloneTransitionPath(ModifiableDAWGNode pivotConfluenceNode, String transitionStringToPivotNode, String str) {
        ModifiableDAWGNode lastTargetNode = pivotConfluenceNode.transition(str);      //Will store the last node which was used as the base of a cloning operation
        ModifiableDAWGNode lastClonedNode = null;                                     //Will store the last cloned node
        char lastTransitionLabelChar = '\0';                                //Will store the char which labels the transition to lastTargetNode from its parent node in the prefixString's transition path

        //Loop backwards through the indices of str, using each as a boundary to create substrings of str of decreasing length
        //which will be used to transition to, and duplicate the nodes in the transition path of str from pivotConfluenceNode.
        for (int i = str.length(); i >= 0; i--) {
            String currentTransitionString = i > 0 ? str.substring(0, i) : null;
            ModifiableDAWGNode currentTargetNode = i > 0 ? pivotConfluenceNode.transition(currentTransitionString) : pivotConfluenceNode;
            ModifiableDAWGNode clonedNode;

            //if we have reached pivotConfluenceNode
            if (i == 0) {
                //Clone pivotConfluenceNode in a way that reassigns the transition of its parent node (in transitionStringToConfluenceNode's path) to the clone.
                String transitionStringToPivotNodeParent = transitionStringToPivotNode.substring(0, transitionStringToPivotNode.length() - 1);
                char parentTransitionLabelChar = transitionStringToPivotNode.charAt(transitionStringToPivotNode.length() - 1);
                clonedNode = pivotConfluenceNode.clone(sourceNode.transition(transitionStringToPivotNodeParent), parentTransitionLabelChar, id++);
                if (clonedNode.isAcceptNode())
                    endNode.addIncomingTransition(parentTransitionLabelChar, clonedNode);
            } else {
                clonedNode = new ModifiableDAWGNode(currentTargetNode, id++);     //simply clone currentTargetNode
                if (clonedNode.isAcceptNode())
                    endNode.addIncomingTransition(lastTransitionLabelChar, clonedNode);
            }

            transitionCount += clonedNode.getOutgoingTransitionCount();

            //If this isn't the first node we've cloned, reassign clonedNode's transition labeled
            //with the lastTransitionChar (which points to the last targetNode) to the last clone.
            if (lastClonedNode != null) {
                clonedNode.reassignOutgoingTransition(lastTransitionLabelChar, lastTargetNode, lastClonedNode);
                lastTargetNode = currentTargetNode;
            }

            //Store clonedNode and the char which labels the transition between the node it was cloned from (currentTargetNode) and THAT node's parent.
            //These will be used to establish an equivalent transition to clonedNode from the next clone to be created (it's clone parent).
            lastClonedNode = clonedNode;
            lastTransitionLabelChar = i > 0 ? str.charAt(i - 1) : '\0';
        }
    }
    
    /**
     * Adds a String to the ModifiableDAWGSet (called by addString to do actual ModifiableDAWGSet manipulation).
     
     * @param str       the String to be added to the ModifiableDAWGSet
     * @return true if and only if ModifiableDAWGSet has changed as a result of this call
     */
    private boolean addStringInternal(String str) {
        if (maxLength < str.length())
            maxLength = str.length();
        String prefixString = determineLongestPrefixInMDAG(str);
        String suffixString = str.substring(prefixString.length());

        //Retrive the data related to the first confluence node (a node with two or more incoming transitions)
        //in the transition path from sourceNode corresponding to prefixString.
        HashMap<String, Object> firstConfluenceNodeDataHashMap = getTransitionPathFirstConfluenceNodeData(sourceNode, prefixString);
        ModifiableDAWGNode firstConfluenceNodeInPrefix = (ModifiableDAWGNode)firstConfluenceNodeDataHashMap.get("confluenceNode");
        Integer toFirstConfluenceNodeTransitionCharIndex = (Integer) firstConfluenceNodeDataHashMap.get("toConfluenceNodeTransitionCharIndex");
        
        //Remove the register entries of all the nodes in the prefixString transition path up to the first confluence node
        //(those past the confluence node will not need to be removed since they will be cloned and unaffected by the
        //addition of suffixString). If there is no confluence node in prefixString, then remove the register entries in prefixString's entire transition path
        removeTransitionPathRegisterEntries(toFirstConfluenceNodeTransitionCharIndex == null ? prefixString : prefixString.substring(0, toFirstConfluenceNodeTransitionCharIndex));
                
        //If there is a confluence node in the prefix, we must duplicate the transition path
        //of the prefix starting from that node, before we add suffixString (to the duplicate path).
        //This ensures that we do not disturb the other transition paths containing this node.
        if (firstConfluenceNodeInPrefix != null) {
            String transitionStringOfPathToFirstConfluenceNode = prefixString.substring(0, toFirstConfluenceNodeTransitionCharIndex + 1);
            String transitionStringOfToBeDuplicatedPath = prefixString.substring(toFirstConfluenceNodeTransitionCharIndex + 1);
            cloneTransitionPath(firstConfluenceNodeInPrefix, transitionStringOfPathToFirstConfluenceNode, transitionStringOfToBeDuplicatedPath);
        }
        
        //Add the transition based on suffixString to the end of the (possibly duplicated) transition path corresponding to prefixString
        return addTransitionPath(sourceNode.transition(prefixString), suffixString);
    }

    /**
     * Creates a CompressedDAWGNode version of an ModifiableDAWGNode's outgoing transition set in mdagDataArray.
     
     * @param node                                      the ModifiableDAWGNode containing the transition set to be inserted in to {@code mdagDataArray}
     * @param mdagDataArray                             an array of SimpleMDAGNodes containing a subset of the data of the ModifiableDAWGSet
     * @param onePastLastCreatedConnectionSetIndex      an int of the index in {@code mdagDataArray} that the outgoing transition set of {@code node} is to start from
     * @return                                          an int of one past the end of the transition set located farthest in {@code mdagDataArray}
     */
    private int createSimpleMDAGTransitionSet(ModifiableDAWGNode node, CompressedDAWGNode[] mdagDataArray, int onePastLastCreatedTransitionSetIndex) {
        int pivotIndex = onePastLastCreatedTransitionSetIndex;
        node.setTransitionSetBeginIndex(pivotIndex);
        
        onePastLastCreatedTransitionSetIndex += node.getOutgoingTransitionCount();

        //Create a CompressedDAWGNode representing each transition label/target combo in transitionTreeMap, recursively calling this method (if necessary)
        //to set indices in these SimpleMDAGNodes that the set of transitions emitting from their respective transition targets starts from.
        TreeMap<Character, ModifiableDAWGNode> transitionTreeMap = node.getOutgoingTransitions();
        for (Entry<Character, ModifiableDAWGNode> transitionKeyValuePair : transitionTreeMap.entrySet()) {
            //Use the current transition's label and target node to create a CompressedDAWGNode
            //(which is a space-saving representation of the transition), and insert it in to mdagDataArray
            char transitionLabelChar = transitionKeyValuePair.getKey();
            ModifiableDAWGNode transitionTargetNode = transitionKeyValuePair.getValue();
            mdagDataArray[pivotIndex] = new CompressedDAWGNode(transitionLabelChar, transitionTargetNode.isAcceptNode(), transitionTargetNode.getOutgoingTransitionCount());
            
            //If targetTransitionNode's outgoing transition set hasn't been inserted in to mdagDataArray yet, call this method on it to do so.
            //After this call returns, transitionTargetNode will contain the index in mdagDataArray that its transition set starts from
            if (transitionTargetNode.getTransitionSetBeginIndex() == -1)
                onePastLastCreatedTransitionSetIndex = createSimpleMDAGTransitionSet(transitionTargetNode, mdagDataArray, onePastLastCreatedTransitionSetIndex);
            
            mdagDataArray[pivotIndex++].setTransitionSetBeginIndex(transitionTargetNode.getTransitionSetBeginIndex());
        }
        
        return onePastLastCreatedTransitionSetIndex;
    }
    
    /**
     * Creates a space-saving version of the ModifiableDAWGSet in the form of an array.
     * Once the ModifiableDAWGSet is simplified, Strings can no longer be added to or removed from it.
     * @return an instance of {@link CompressedDAWGSet} containing all the words added to this DAWG
     */
    public CompressedDAWGSet compress() {
        CompressedDAWGSet compressed = new CompressedDAWGSet();
        compressed.size = size;
        compressed.maxLength = maxLength;
        compressed.mdagDataArray = new CompressedDAWGNode[transitionCount];
        createSimpleMDAGTransitionSet(sourceNode, compressed.mdagDataArray, 0);
        compressed.sourceNode = new CompressedDAWGNode('\0', sourceNode.isAcceptNode(), sourceNode.getOutgoingTransitionCount());
        //Clear all transition begin indexes.
        Deque<ModifiableDAWGNode> queue = new LinkedList<>();
        queue.add(sourceNode);
        while (true) {
            ModifiableDAWGNode node = queue.pollLast();
            if (node == null)
                break;
            node.setTransitionSetBeginIndex(-1);
            queue.addAll(node.getOutgoingTransitions().values());
        }
        return compressed;
    }
    
    /**
     * Determines whether a String is present in the ModifiableDAWGSet.
     
     * @param str       the String to be searched for
     * @return          true if {@code str} is present in the ModifiableDAWGSet, and false otherwise
     */
    public boolean contains(String str) {
        ModifiableDAWGNode targetNode = sourceNode.transition(str);
        return targetNode != null && targetNode.isAcceptNode();
    }
    
    /**
     * Retrieves Strings corresponding to all valid transition paths from a given node that satisfy a given condition.
     
     * @param strNavigableSet                a NavigableSet of Strings to contain all those in the ModifiableDAWGSet satisfying
                                  {@code searchCondition} with {@code conditionString}
     * @param searchCondition           the SearchCondition enum field describing the type of relationship that Strings contained in the ModifiableDAWGSet
                                  must have with {@code conditionString} in order to be included in the result set
     * @param searchConditionString     the String that all Strings in the ModifiableDAWGSet must be related with in the fashion denoted
                                  by {@code searchCondition} in order to be included in the result set
     * @param prefixString              the String corresponding to the currently traversed transition path
     * @param transitionTreeMap         a TreeMap of Characters to MDAGNodes collectively representing an ModifiableDAWGNode's transition set
     * @param descending                traverse a tree from the end
     */
    private void getStrings(NavigableSet<String> strNavigableSet, SearchCondition searchCondition, String searchConditionString, String prefixString, TreeMap<Character, ModifiableDAWGNode> transitionTreeMap, boolean descending) {
        //Traverse all the valid transition paths beginning from each transition in transitionTreeMap, inserting the
        //corresponding Strings in to strNavigableSet that have the relationship with conditionString denoted by searchCondition
        for (Entry<Character, ModifiableDAWGNode> transitionKeyValuePair : transitionTreeMap.entrySet()) {
            String newPrefixString = prefixString + transitionKeyValuePair.getKey();
            ModifiableDAWGNode currentNode = transitionKeyValuePair.getValue();

            SearchCondition childrenSearchCondition = searchCondition;
            boolean addCurrent = false;
            if (searchCondition.satisfiesCondition(newPrefixString, searchConditionString)) {
                if (currentNode.isAcceptNode())
                    addCurrent = true;
                //If the parent node satisfies the search condition then all its child nodes also satisfy this condition.
                if (searchCondition == SearchCondition.SUBSTRING_SEARCH_CONDITION)
                    childrenSearchCondition = SearchCondition.NO_SEARCH_CONDITION;
            }
            
            if (addCurrent && !descending)
                strNavigableSet.add(newPrefixString);
            //Recursively call this to traverse all the valid transition paths from currentNode
            getStrings(strNavigableSet, childrenSearchCondition, searchConditionString, newPrefixString, currentNode.getOutgoingTransitions(), descending);
            if (addCurrent && descending)
                strNavigableSet.add(newPrefixString);
        }
    }

    @Override
    DAWGNode getNodeByPath(DAWGNode from, String path) {
        return ((ModifiableDAWGNode)from).transition(path);
    }

    @Override
    int getMaxLength() {
        return maxLength;
    }
    
    /**
     * Retrieves all the Strings in the ModifiableDAWGSet that begin with a given String.
     
     * @param suffixStr         a String that is the suffix for all the desired Strings
     * @return                  a NavigableSet containing all the Strings present in the ModifiableDAWGSet that end with {@code suffixStr}
     */
    @Override
    public Iterable<String> getStringsEndingWith(String suffixStr) {
        NavigableSet<String> strNavigableSet = new TreeSet<>();
        if (suffixStr.isEmpty() && sourceNode.isAcceptNode())
            strNavigableSet.add(suffixStr);
        getStrings(strNavigableSet, SearchCondition.SUFFIX_SEARCH_CONDITION, suffixStr, "", sourceNode.getOutgoingTransitions(), false);
        return strNavigableSet;
    }
    
    /**
     * Returns the ModifiableDAWGSet's source node.
    
     * @return      the ModifiableDAWGNode or CompressedDAWGNode functioning as the ModifiableDAWGSet's source node.
     */
    @Override
    DAWGNode getSourceNode() {
        return sourceNode;
    }
    
    @Override
    DAWGNode getEndNode() {
        return endNode;
    }
    
    /**
     * Procures the set of characters which collectively label the ModifiableDAWGSet's transitions.
     
     * @return      a TreeSet of chars which collectively label all the transitions in the ModifiableDAWGSet
     */
    public TreeSet<Character> getTransitionLabelSet() {
        return charTreeSet;
    }
    
    private void countNodes(ModifiableDAWGNode originNode, HashSet<Integer> nodeIDHashSet) {
        nodeIDHashSet.add(originNode.getId());
        
        TreeMap<Character, ModifiableDAWGNode> transitionTreeMap = originNode.getOutgoingTransitions();
        
        for (ModifiableDAWGNode transition : transitionTreeMap.values())
            countNodes(transition, nodeIDHashSet);
    }
    
    @Override
    public int getNodeCount() {
        HashSet<Integer> ids = new HashSet<>();
        countNodes(sourceNode, ids);
        return ids.size();
    }
    
    public int getEquivalenceClassCount() {
        return equivalenceClassMDAGNodeHashMap.size();
    }
    
    @Override
    public int getTransitionCount() {
        return transitionCount;
    }
    
    public int size() {
        return size;
    }

    @Override
    SemiNavigableMap<Character, DAWGNode> getOutgoingTransitions(DAWGNode parent) {
        return new OutgoingTransitionsMap((ModifiableDAWGNode)parent, false);
    }

    @Override
    SemiNavigableMap<Character, Collection<? extends DAWGNode>> getIncomingTransitions(DAWGNode parent) {
        return new IncomingTransitionsMap((ModifiableDAWGNode)parent, false);
    }
    
    private static class OutgoingTransitionsMap implements SemiNavigableMap<Character, DAWGNode> {
        private final ModifiableDAWGNode parent;
        private final NavigableMap<Character, ModifiableDAWGNode> outgoingTransitions;
        private final boolean desc;
        
        public OutgoingTransitionsMap(ModifiableDAWGNode parent, boolean desc) {
            this.parent = parent;
            outgoingTransitions = parent.getOutgoingTransitions();
            this.desc = desc;
        }

        @Override
        public Iterator<Entry<Character, DAWGNode>> iterator() {
            return new Iterator<Entry<Character, DAWGNode>>() {
                private final Iterator<Entry<Character, ModifiableDAWGNode>> it = (desc ? outgoingTransitions.descendingMap() : outgoingTransitions).entrySet().iterator();

                @Override
                public boolean hasNext() {
                    return it.hasNext();
                }

                @Override
                public Entry<Character, DAWGNode> next() {
                    Entry<Character, ModifiableDAWGNode> next = it.next();
                    return new Entry<Character, DAWGNode>() {
                        @Override
                        public Character getKey() {
                            return next.getKey();
                        }

                        @Override
                        public DAWGNode getValue() {
                            return next.getValue();
                        }

                        @Override
                        public DAWGNode setValue(DAWGNode value) {
                            return next.setValue((ModifiableDAWGNode)value);
                        }
                    };
                }
            };
        }

        @Override
        public boolean isEmpty() {
            return outgoingTransitions.isEmpty();
        }

        @Override
        public SemiNavigableMap<Character, DAWGNode> descendingMap() {
            return new OutgoingTransitionsMap(parent, !desc);
        }
    }
    
    private static class IncomingTransitionsMap implements SemiNavigableMap<Character, Collection<? extends DAWGNode>> {
        private final ModifiableDAWGNode parent;
        private final NavigableMap<Character, Map<Integer, ModifiableDAWGNode>> incomingTransitions;
        private final boolean desc;
        
        public IncomingTransitionsMap(ModifiableDAWGNode parent, boolean desc) {
            this.parent = parent;
            incomingTransitions = parent.getIncomingTransitions();
            this.desc = desc;
        }

        @Override
        public Iterator<Entry<Character, Collection<? extends DAWGNode>>> iterator() {
            return new Iterator<Entry<Character, Collection<? extends DAWGNode>>>() {
                private final Iterator<Entry<Character, Map<Integer, ModifiableDAWGNode>>> it = (desc ? incomingTransitions.descendingMap() : incomingTransitions).entrySet().iterator();

                @Override
                public boolean hasNext() {
                    return it.hasNext();
                }

                @Override
                public Entry<Character, Collection<? extends DAWGNode>> next() {
                    Entry<Character, Map<Integer, ModifiableDAWGNode>> next = it.next();
                    return new Entry<Character, Collection<? extends DAWGNode>>() {
                        @Override
                        public Character getKey() {
                            return next.getKey();
                        }

                        @Override
                        public Collection<? extends DAWGNode> getValue() {
                            return next.getValue().values();
                        }

                        @Override
                        public Collection<? extends DAWGNode> setValue(Collection<? extends DAWGNode> value) {
                            Map<Integer, ModifiableDAWGNode> current = next.getValue();
                            Collection<? extends DAWGNode> prev;
                            if (current == null) {
                                next.setValue(current = new HashMap<>());
                                prev = null;
                            } else {
                                prev = current.values();
                                current.clear();
                            }
                            for (DAWGNode node : value)
                                current.put(node.getId(), (ModifiableDAWGNode)node);
                            return prev;
                        }
                    };
                }
            };
        }

        @Override
        public boolean isEmpty() {
            return incomingTransitions.isEmpty();
        }

        @Override
        public SemiNavigableMap<Character, Collection<? extends DAWGNode>> descendingMap() {
            return new IncomingTransitionsMap(parent, !desc);
        }
    }
}