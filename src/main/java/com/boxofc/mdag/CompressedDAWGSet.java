package com.boxofc.mdag;

import com.boxofc.mdag.util.SemiNavigableMap;
import com.boxofc.mdag.util.SimpleEntry;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

public class CompressedDAWGSet extends DAWGSet implements Serializable {
    private static final CompressedDAWGNode EMPTY_NODE = new CompressedDAWGNode(null, DAWGNode.EMPTY);
    
    CompressedDAWGNode endNode;
    
    //Array that will contain a space-saving version of the ModifiableDAWGSet after a call to compress().
    int data[];
    
    /**
     * An array of all letters used in this dictionary (an alphabet of the language defined by this DAWG).
     */
    char letters[];
    
    boolean withIncomingTransitions;
    
    /**
     * A mapping from characters of {@link #letters} array to their positions in that array.
     */
    private transient Map<Character, Integer> lettersIndex;
    
    /**
     * Quantity of words in this DAWG.
     */
    transient Integer size;
    
    /**
     * Maximal length of words contained in this DAWG.
     */
    transient Integer maxLength;
    
    private transient int transitionSizeInInts;
    
    /**
     * SimpleMDAGNode from which all others in the structure are reachable (will be defined if this ModifiableDAWGSet is simplified)
     */
    private transient CompressedDAWGNode sourceNode;
    
    /**
     * Package-private constructor.
     * Use {@link ModifiableDAWGSet#compress} to create instances of this class.
     */
    CompressedDAWGSet() {
    }
    
    /**
     * This method is invoked when the object is read from input stream.
     * @see Serializable
     */
    private void readObject( ObjectInputStream ois ) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        transitionSizeInInts = calculateTransitionSizeInInts();
    }
    
    /**
     * Returns the ModifiableDAWGSet's source node.
    
     * @return      the ModifiableDAWGNode or CompressedDAWGNode functioning as the ModifiableDAWGSet's source node.
     */
    @Override
    CompressedDAWGNode getSourceNode() {
        if (sourceNode == null)
            sourceNode = new CompressedDAWGNode(this, DAWGNode.START);
        return sourceNode;
    }
    
    @Override
    DAWGNode getEndNode() {
        return endNode;
    }
    
    @Override
    DAWGNode getEmptyNode() {
        return EMPTY_NODE;
    }

    @Override
    public boolean isWithIncomingTransitions() {
        return withIncomingTransitions;
    }
    
    @Override
    Collection<? extends DAWGNode> getNodesBySuffix(String suffix) {
        return null;
    }
    
    Map<Character, Integer> getLettersIndex() {
        if (lettersIndex == null) {
            Map<Character, Integer> ret = new HashMap<>();
            for (int i = 0; i < letters.length; i++)
                ret.put(letters[i], i);
            lettersIndex = ret;
        }
        return lettersIndex;
    }
    
    int getTransitionSizeInInts() {
        return transitionSizeInInts;
    }
    
    int calculateTransitionSizeInInts() {
        // Int 0:
        // Current letter (char)
        // Accept node mark (boolean)
        // Int 1:
        // Outgoing nodes array begin index (int)
        // The rest:
        // Bit array for each char denoting if there exists a transition
        // from this node to the letter in a specified position
        return transitionSizeInInts = 2 + ((letters.length + 31) >>> 5);
    }

    @Override
    int getMaxLength() {
        if (maxLength == null)
            maxLength = getMaxLength(getSourceNode(), 0);
        return maxLength;
    }
    
    int getMaxLength(CompressedDAWGNode node, int length) {
        int ret = length;
        for (CompressedDAWGNode child : node.getOutgoingTransitionsNodes())
            ret = Math.max(ret, getMaxLength(child, length + 1));
        return ret;
    }

    @Override
    public int getTransitionCount() {
        return data.length / transitionSizeInInts - 1;
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
    
    private void countNodes(CompressedDAWGNode node, HashSet<Integer> nodeIDHashSet) {
        if (node.getOutgoingTransitionsSize() != 0)
            nodeIDHashSet.add(node.getTransitionSetBeginIndex());
        for (CompressedDAWGNode child : node.getOutgoingTransitionsNodes())
            countNodes(child, nodeIDHashSet);
    }
    
    @Override
    public int getNodeCount() {
        HashSet<Integer> ids = new HashSet<>();
        countNodes(getSourceNode(), ids);
        return ids.size() + 1;
    }

    @Override
    public int hashCode() {
        return ((Arrays.hashCode(letters) * 31 + Arrays.hashCode(data)) << 1) + (isWithIncomingTransitions() ? 1 : 0);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof CompressedDAWGSet))
            return false;
        CompressedDAWGSet other = (CompressedDAWGSet) obj;
        return isWithIncomingTransitions() == other.isWithIncomingTransitions() &&
               Arrays.equals(letters, other.letters) &&
               Arrays.equals(data, other.data);
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
        private final int from;
        private final int to;
        
        public OutgoingTransitionsMap(CompressedDAWGNode cparent, boolean desc) {
            this.cparent = cparent;
            this.desc = desc;
            from = cparent.getTransitionSetBeginIndex();
            to = from + (cparent.getOutgoingTransitionsSize() - 1) * transitionSizeInInts;
        }
        
        @Override
        public Iterator<SimpleEntry<Character, DAWGNode>> iterator() {
            return new Iterator<SimpleEntry<Character, DAWGNode>>() {
                private int current = desc ? to : from;

                @Override
                public boolean hasNext() {
                    return desc ? current >= from : current <= to;
                }

                @Override
                public SimpleEntry<Character, DAWGNode> next() {
                    CompressedDAWGNode node = new CompressedDAWGNode(CompressedDAWGSet.this, current);
                    if (desc)
                        current -= transitionSizeInInts;
                    else
                        current += transitionSizeInInts;
                    return new SimpleEntry<>(node.getLetter(), node);
                }
            };
        }

        @Override
        public boolean isEmpty() {
            return cparent.getOutgoingTransitionsSize() == 0;
        }

        @Override
        public SemiNavigableMap<Character, DAWGNode> descendingMap() {
            return new OutgoingTransitionsMap(cparent, !desc);
        }
    }
    
    private class IncomingTransitionsMap implements SemiNavigableMap<Character, Collection<? extends DAWGNode>> {
        private final CompressedDAWGNode cparent;
        private final boolean desc;
        private final int from;
        private final int to;
        
        public IncomingTransitionsMap(CompressedDAWGNode cparent, boolean desc) {
            this.cparent = cparent;
            this.desc = desc;
            from = cparent.getTransitionSetBeginIndex();
            to = from + cparent.getOutgoingTransitionsSize() - 1;
        }
        
        @Override
        public Iterator<SimpleEntry<Character, Collection<? extends DAWGNode>>> iterator() {
            return new Iterator<SimpleEntry<Character, Collection<? extends DAWGNode>>>() {
                private int current = desc ? to : from;

                @Override
                public boolean hasNext() {
                    return desc ? current >= from : current <= to;
                }

                @Override
                public SimpleEntry<Character, Collection<? extends DAWGNode>> next() {
                    CompressedDAWGNode node = new CompressedDAWGNode(CompressedDAWGSet.this, current);
                    if (desc)
                        current -= transitionSizeInInts;
                    else
                        current += transitionSizeInInts;
                    return new SimpleEntry<>(node.getLetter(), null);
                }
            };
        }

        @Override
        public boolean isEmpty() {
            return false;//cparent.getOutgoingTransitionsSize() == 0;
        }

        @Override
        public SemiNavigableMap<Character, Collection<? extends DAWGNode>> descendingMap() {
            return new IncomingTransitionsMap(cparent, !desc);
        }
    }
}