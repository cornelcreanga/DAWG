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

import java.util.TreeMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author Kevin
 */
public class DAWGNodeTest {
    @Test
    public void addOutgoingTransitionTest() {
        MDAGNode node1 = new MDAGNode(false, 0);
        node1.addOutgoingTransition('a', true);
        node1.addOutgoingTransition('b', false);
        node1.addOutgoingTransition('c', false);
        
        TreeMap<Character, MDAGNode> outgoingTransitionTreeMap = node1.getOutgoingTransitions();
        
        assertEquals(3, outgoingTransitionTreeMap.size());
        assertTrue(outgoingTransitionTreeMap.get('a').isAcceptNode());
        assertFalse(outgoingTransitionTreeMap.get('b').isAcceptNode());
        assertFalse(outgoingTransitionTreeMap.get('b').isAcceptNode());
    }
    
    @Test
    public void cloneTest() {
        MDAGNode node1 = new MDAGNode(false, 0);
        node1.addOutgoingTransition('a', false);
        node1.addOutgoingTransition('b', true);
        MDAGNode cloneNode1 = new MDAGNode(node1, 0);
        
        MDAGNode node2 = new MDAGNode(true, 0);
        node2.addOutgoingTransition('c', false);
        node2.addOutgoingTransition('d', true);
        MDAGNode cloneNode2 = new MDAGNode(node2, 0);
        
        assertTrue(node1 != cloneNode1);
        assertEquals(node1.getIncomingTransitionCount(), cloneNode1.getIncomingTransitionCount());
        assertEquals(node1.isAcceptNode(), cloneNode1.isAcceptNode());
        assertEquals(node1.getOutgoingTransitions(), cloneNode1.getOutgoingTransitions());
        
        assertTrue(node2 != cloneNode2);
        assertEquals(node2.getIncomingTransitionCount(), cloneNode2.getIncomingTransitionCount());
        assertEquals(node2.isAcceptNode(), cloneNode2.isAcceptNode());
        assertEquals(node2.getOutgoingTransitions(), cloneNode2.getOutgoingTransitions());
    }
    
    @Test
    public void transitionTest1() {
        MDAGNode node1 = new MDAGNode(false, 0);
        MDAGNode currentNode = node1;
        
        char[] alphabet = {'a', 'b', 'c','d', 'e', 'f', 'g', 'h', 'i', 'j', 'k'};
        
        for (int i = 0; i < alphabet.length; i++)
            currentNode = currentNode.addOutgoingTransition(alphabet[i], i % 2 == 0);
        
        String alphaStr = new String(alphabet);
        
        assertNotNull(node1.transition(alphaStr));
    }
    
    @Test
    public void reassignOutgoingTransitionTest() {
        MDAGNode node1 = new MDAGNode(false, 0);
        node1.addOutgoingTransition('a', true);
        node1.addOutgoingTransition('b', false);
        node1.addOutgoingTransition('c', true);
        node1.addOutgoingTransition('d', false);
        
        MDAGNode node2 = new MDAGNode(true, 0);
        node1.reassignOutgoingTransition('a', node1.transition('a'), node2);
        
        MDAGNode node3 = new MDAGNode(false, 0);
        node1.reassignOutgoingTransition('b', node1.transition('b'), node3);
        
        MDAGNode node4 = new MDAGNode(false, 0);
        node1.reassignOutgoingTransition('c', node1.transition('c'), node4);
        
        MDAGNode node5 = new MDAGNode(true, 0);
        node1.reassignOutgoingTransition('d', node1.transition('d'), node5);
        
        assertTrue(node1.transition('a') == node2);
        assertEquals(1, node2.getIncomingTransitionCount());
        
        assertTrue(node1.transition('b') == node3);
        assertEquals(1, node3.getIncomingTransitionCount());
        
        assertTrue(node1.transition('c') == node4);
        assertEquals(1, node4.getIncomingTransitionCount());
        
        assertTrue(node1.transition('d') == node5);
        assertEquals(1, node5.getIncomingTransitionCount());
    }
    
    @Test
    public void cloneTest2() {
        MDAGNode node1 = new MDAGNode(false, 0);
        
        MDAGNode node2 = node1.addOutgoingTransition('\0', false);
        node2.addOutgoingTransition('a', false);
        node2.addOutgoingTransition('b', false);
        node2.addOutgoingTransition('c', false);
        
        MDAGNode node3 = node2.clone(node1, '\0', 0);
        
        assertTrue(node2 != node3);
        assertTrue(node2.hasOutgoingTransition('a') && node3.hasOutgoingTransition('a'));
        assertTrue(node2.hasOutgoingTransition('b') && node3.hasOutgoingTransition('b'));
        assertTrue(node2.hasOutgoingTransition('c') && node3.hasOutgoingTransition('c'));
        
        assertEquals(1, node1.getOutgoingTransitions().size());
        assertEquals(1, node3.getIncomingTransitionCount());
        assertEquals(0, node2.getIncomingTransitionCount());
    }
    
    @Test
    public void equalsTest() {
        MDAGNode node1 = new MDAGNode(false, 0);
        MDAGNode node2 = new MDAGNode(false, 0);
        
        MDAGNode node3 = new MDAGNode(true, 0);
        MDAGNode node4 = new MDAGNode(true, 0);
        
        MDAGNode currentNode1 = node1;
        MDAGNode currentNode2 = node2;

        char[] alphabet = {'a', 'b', 'c','d', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
        
        for (int i = 0; i < alphabet.length; i++) {
           currentNode1 = currentNode1.addOutgoingTransition(alphabet[i], i % 2 == 0);
           currentNode2 = currentNode2.addOutgoingTransition(alphabet[i], i % 2 == 0);
        }
            
        assertEquals(node1, node2);
        assertEquals(node3, node4);
        
        assertFalse(node1.equals(node3));
        assertFalse(node2.equals(node4));
    }
    
    @Test
    public void hashTest() {
        MDAGNode node1 = new MDAGNode(false, 0);
        MDAGNode node2 = new MDAGNode(false, 0);
        
        MDAGNode node3 = new MDAGNode(true, 0);
        MDAGNode node4 = new MDAGNode(true, 0);
        
        MDAGNode currentNode1 = node1;
        MDAGNode currentNode2 = node2;

        char[] alphabet = {'a', 'b', 'c','d', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
        
        for (int i = 0; i < alphabet.length; i++) {
           currentNode1 = currentNode1.addOutgoingTransition(alphabet[i], i % 2 == 0);
           currentNode2 = currentNode2.addOutgoingTransition(alphabet[i], i % 2 == 0);
        }
        
        assertEquals(node1.hashCode(), node2.hashCode());
        assertEquals(node3.hashCode(), node4.hashCode());
        assertTrue(node1.hashCode() != node3.hashCode());
        assertTrue(node2.hashCode() != node4.hashCode());
    }
}