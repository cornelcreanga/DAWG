package com.boxofc.mdag;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.TreeSet;

public class CompressedDAWGSet extends DAWGSet {
    //SimpleMDAGNode from which all others in the structure are reachable (will be defined if this ModifiableDAWGSet is simplified)
    CompressedDAWGNode sourceNode;
    
    CompressedDAWGNode endNode;
    
    //Array that will contain a space-saving version of the ModifiableDAWGSet after a call to simplify().
    CompressedDAWGNode[] mdagDataArray;
    
    /**
     * Quantity of words in this DAWG.
     */
    transient Integer size;
    
    /**
     * Maximal length of words contained in this DAWG.
     */
    transient Integer maxLength;
    
    boolean withIncomingTransitions;
    
    /**
     * Determines whether a String is present in the ModifiableDAWGSet.
     
     * @param str       the String to be searched for
     * @return          true if {@code str} is present in the ModifiableDAWGSet, and false otherwise
     */
    public boolean contains(String str) {
        CompressedDAWGNode targetNode = CompressedDAWGNode.traverseMDAG(mdagDataArray, sourceNode, str);
        return targetNode != null && targetNode.isAcceptNode();
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

    @Override
    public boolean isWithIncomingTransitions() {
        return withIncomingTransitions;
    }

    @Override
    DAWGNode getNodeByPrefix(DAWGNode from, String path) {
        return CompressedDAWGNode.traverseMDAG(mdagDataArray, (CompressedDAWGNode)from, path);
    }
    
    @Override
    Collection<? extends DAWGNode> getNodesBySuffix(String suffix) {
        return null;
    }

    @Override
    int getMaxLength() {
        if (maxLength == null)
            maxLength = getMaxLength(sourceNode, 0);
        return maxLength;
    }
    
    int getMaxLength(CompressedDAWGNode node, int length) {
        int transitionSetBegin = node.getTransitionSetBeginIndex();
        int onePastTransitionSetEnd = transitionSetBegin + node.getOutgoingTransitionSetSize();
        int ret = length;
        for (int i = transitionSetBegin; i < onePastTransitionSetEnd; i++) {
            CompressedDAWGNode currentNode = mdagDataArray[i];
            int len = getMaxLength(currentNode, length + 1);
            if (len > ret)
                ret = len;
        }
        return ret;
    }

    @Override
    public int getTransitionCount() {
        return mdagDataArray.length;
    }
    
    public int size() {
        if (size == null) {
            int s = 0;
            for (String word : this)
                s++;
            size = s;
        }
        return size;
    }
    
    private void countNodes(CompressedDAWGNode originNode, HashSet<Integer> nodeIDHashSet) {
        nodeIDHashSet.add(originNode.getId());
        int transitionSetBegin = originNode.getTransitionSetBeginIndex();
        int onePastTransitionSetEnd = transitionSetBegin + originNode.getOutgoingTransitionSetSize();
        
        //Traverse all the valid transition paths beginning from each transition in transitionTreeMap, inserting the
        //corresponding Strings in to strNavigableSet that have the relationship with conditionString denoted by searchCondition
        for (int i = transitionSetBegin; i < onePastTransitionSetEnd; i++) {
            CompressedDAWGNode currentNode = mdagDataArray[i];
            countNodes(currentNode, nodeIDHashSet);
        }
    }
    
    @Override
    public int getNodeCount() {
        HashSet<Integer> ids = new HashSet<>();
        countNodes(sourceNode, ids);
        return ids.size();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + Objects.hashCode(sourceNode);
        hash = 83 * hash + Arrays.deepHashCode(mdagDataArray);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof CompressedDAWGSet))
            return false;
        CompressedDAWGSet other = (CompressedDAWGSet) obj;
        return Objects.equals(sourceNode, other.sourceNode) &&
               Arrays.deepEquals(mdagDataArray, other.mdagDataArray);
    }
    
    public ModifiableDAWGSet uncompress() {
        ModifiableDAWGSet ret = new ModifiableDAWGSet();
        ret.setWithIncomingTransitions(isWithIncomingTransitions());
        ret.addAll(this);
        return ret;
    }

    @Override
    SemiNavigableMap<Character, DAWGNode> getOutgoingTransitions(DAWGNode parent) {
        return new OutgoingTransitionsMap((CompressedDAWGNode)parent, false);
    }

    @Override
    SemiNavigableMap<Character, Collection<? extends DAWGNode>> getIncomingTransitions(DAWGNode parent) {
        return new IncomingTransitionsMap((CompressedDAWGNode)parent, false);
    }
    
    private class OutgoingTransitionsMap implements SemiNavigableMap<Character, DAWGNode> {
        private final CompressedDAWGNode cparent;
        private final boolean desc;
        
        public OutgoingTransitionsMap(CompressedDAWGNode cparent, boolean desc) {
            this.cparent = cparent;
            this.desc = desc;
        }
        
        @Override
        public Iterator<Entry<Character, DAWGNode>> iterator() {
            return new Iterator<Entry<Character, DAWGNode>>() {
                private final int from = cparent.getTransitionSetBeginIndex();
                private final int to = from + cparent.getOutgoingTransitionSetSize() - 1;
                private int current = desc ? to : from;

                @Override
                public boolean hasNext() {
                    return desc ? current >= from : current <= to;
                }

                @Override
                public Entry<Character, DAWGNode> next() {
                    final int nodePos = current;
                    CompressedDAWGNode node = mdagDataArray[current];
                    if (desc)
                        current--;
                    else
                        current++;
                    return new Entry<Character, DAWGNode>() {
                        @Override
                        public Character getKey() {
                            return node.getLetter();
                        }

                        @Override
                        public DAWGNode getValue() {
                            return node;
                        }

                        @Override
                        public DAWGNode setValue(DAWGNode value) {
                            return mdagDataArray[nodePos] = (CompressedDAWGNode)value;
                        }
                    };
                }
            };
        }

        @Override
        public boolean isEmpty() {
            return cparent.getOutgoingTransitionSetSize() == 0;
        }

        @Override
        public SemiNavigableMap<Character, DAWGNode> descendingMap() {
            return new OutgoingTransitionsMap(cparent, !desc);
        }
    }
    
    private class IncomingTransitionsMap implements SemiNavigableMap<Character, Collection<? extends DAWGNode>> {
        private final CompressedDAWGNode cparent;
        private final boolean desc;
        
        public IncomingTransitionsMap(CompressedDAWGNode cparent, boolean desc) {
            this.cparent = cparent;
            this.desc = desc;
        }
        
        @Override
        public Iterator<Entry<Character, Collection<? extends DAWGNode>>> iterator() {
            return new Iterator<Entry<Character, Collection<? extends DAWGNode>>>() {
                private final int from = cparent.getTransitionSetBeginIndex();
                private final int to = from + cparent.getOutgoingTransitionSetSize() - 1;
                private int current = desc ? to : from;

                @Override
                public boolean hasNext() {
                    return desc ? current >= from : current <= to;
                }

                @Override
                public Entry<Character, Collection<? extends DAWGNode>> next() {
                    final int nodePos = current;
                    CompressedDAWGNode node = mdagDataArray[current];
                    if (desc)
                        current--;
                    else
                        current++;
                    return new Entry<Character, Collection<? extends DAWGNode>>() {
                        @Override
                        public Character getKey() {
                            return node.getLetter();
                        }

                        @Override
                        public Collection<? extends DAWGNode> getValue() {
                            return null;//node;
                        }

                        @Override
                        public Collection<? extends DAWGNode> setValue(Collection<? extends DAWGNode> value) {
                            return null;//mdagDataArray[nodePos] = (CompressedDAWGNode)value;
                        }
                    };
                }
            };
        }

        @Override
        public boolean isEmpty() {
            return false;//cparent.getOutgoingTransitionSetSize() == 0;
        }

        @Override
        public SemiNavigableMap<Character, Collection<? extends DAWGNode>> descendingMap() {
            return new IncomingTransitionsMap(cparent, !desc);
        }
    }
}