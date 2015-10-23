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
    
    /**
     * Retrieves Strings corresponding to all valid transition paths from a given node that satisfy a given condition.
     
     * @param strNavigableSet                    a NavigableSet of Strings to contain all those in the ModifiableDAWGSet satisfying
                                      {@code searchCondition} with {@code conditionString}
     * @param searchCondition               the SearchCondition enum field describing the type of relationship that Strings contained in the ModifiableDAWGSet
                                      must have with {@code conditionString} in order to be included in the result set
     * @param searchConditionString         the String that all Strings in the ModifiableDAWGSet must be related with in the fashion denoted
                                      by {@code searchCondition} in order to be included in the result set
     * @param prefixString                  the String corresponding to the currently traversed transition path
     * @param transitionSetBegin            an int denoting the starting index of a CompressedDAWGNode's transition set in mdagDataArray
     * @param onePastTransitionSetEnd       an int denoting one past the last index of a simpleMDAGNode's transition set in mdagDataArray
     */
    private void getStrings(NavigableSet<String> strNavigableSet, SearchCondition searchCondition, String searchConditionString, String prefixString, CompressedDAWGNode node) {
        int transitionSetBegin = node.getTransitionSetBeginIndex();
        int onePastTransitionSetEnd = transitionSetBegin + node.getOutgoingTransitionSetSize();
        
        //Traverse all the valid transition paths beginning from each transition in transitionTreeMap, inserting the
        //corresponding Strings in to strNavigableSet that have the relationship with conditionString denoted by searchCondition
        for (int i = transitionSetBegin; i < onePastTransitionSetEnd; i++) {
            CompressedDAWGNode currentNode = mdagDataArray[i];
            String newPrefixString = prefixString + currentNode.getLetter();
            
            SearchCondition childrenSearchCondition = searchCondition;
            if (searchCondition.satisfiesCondition(newPrefixString, searchConditionString)) {
                if (currentNode.isAcceptNode())
                    strNavigableSet.add(newPrefixString);
                //If the parent node satisfies the search condition then all its child nodes also satisfy this condition.
                if (searchCondition == SearchCondition.SUBSTRING_SEARCH_CONDITION)
                    childrenSearchCondition = SearchCondition.NO_SEARCH_CONDITION;
            }
            
            //Recursively call this to traverse all the valid transition paths from currentNode
            getStrings(strNavigableSet, childrenSearchCondition, searchConditionString, newPrefixString, currentNode);
        }
    }

    @Override
    DAWGNode getNodeByPath(DAWGNode from, String path) {
        return CompressedDAWGNode.traverseMDAG(mdagDataArray, (CompressedDAWGNode)from, path);
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
        getStrings(strNavigableSet, SearchCondition.SUFFIX_SEARCH_CONDITION, suffixStr, "", sourceNode);
        return strNavigableSet;
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
        return new ModifiableDAWGSet(this);
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