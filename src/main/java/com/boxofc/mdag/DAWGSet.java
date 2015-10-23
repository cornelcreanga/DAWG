package com.boxofc.mdag;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.BitSet;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NoSuchElementException;

public abstract class DAWGSet implements Iterable<String> {
    /**
     * Folder where to save images when {@link #saveAsImage} is called. Default is the relative directory named "temp".
     */
    private static String imagesPath = "temp";
    
    /**
     * Path to GraphViz dot executable. Default is "dot" (works if added to environment variables).
     */
    private static String dotExecutablePath = "dot";

    public static String getImagesPath() {
        return imagesPath;
    }

    public static void setImagesPath(String imagesPath) {
        DAWGSet.imagesPath = imagesPath;
    }

    public static String getDotExecutablePath() {
        return dotExecutablePath;
    }

    public static void setDotExecutablePath(String dotExecutablePath) {
        DAWGSet.dotExecutablePath = dotExecutablePath;
    }
    
    abstract SemiNavigableMap<Character, DAWGNode> getOutgoingTransitions(DAWGNode parent);
    
    abstract SemiNavigableMap<Character, Collection<? extends DAWGNode>> getIncomingTransitions(DAWGNode parent);
  
    public String toGraphViz(boolean withNodeIds, boolean withIncomingTransitions) {
        StringBuilder dot = new StringBuilder("digraph dawg {\n");
        dot.append("graph [rankdir=LR, ratio=fill];\n");
        dot.append("node [fontsize=14, shape=circle];\n");
        dot.append("edge [fontsize=12];\n");
        Deque<DAWGNode> stack = new LinkedList<>();
        BitSet visited = new BitSet();
        stack.add(getSourceNode());
        visited.set(getSourceNode().getId());
        while (true) {
            DAWGNode node = stack.pollLast();
            if (node == null)
                break;
            dot.append('n').append(node.getId()).append(" [label=\"").append(node.isAcceptNode() ? 'O' : ' ').append('\"');
            if (withNodeIds) {
                dot.append(", xlabel=\"");
                if (node.getId() == 0)
                    dot.append("START");
                else
                    dot.append(node.getId());
                dot.append('\"');
            }
            dot.append("];\n");
            for (Map.Entry<Character, DAWGNode> e : getOutgoingTransitions(node)) {
                DAWGNode nextNode = e.getValue();
                dot.append('n').append(node.getId()).append(" -> n").append(nextNode.getId()).append(" [label=\"").append(e.getKey()).append("\"];\n");
                if (!visited.get(nextNode.getId())) {
                    stack.addLast(nextNode);
                    visited.set(nextNode.getId());
                }
            }
            if (withIncomingTransitions) {
                for (Map.Entry<Character, Collection<? extends DAWGNode>> e : getIncomingTransitions(node)) {
                    for (DAWGNode prevNode : e.getValue()) {
                        dot.append('n').append(node.getId()).append(" -> n").append(prevNode.getId()).append(" [label=\"").append(e.getKey()).append("\", style=dashed];\n");
                        if (!visited.get(prevNode.getId())) {
                            stack.addLast(prevNode);
                            visited.set(prevNode.getId());
                        }
                    }
                }
            }
        }
        dot.append('}');
        return dot.toString();
    }

    public void saveAsImage(boolean withNodeIds, boolean withIncomingTransitions) throws IOException {
        String graphViz = toGraphViz(withNodeIds, withIncomingTransitions);
        Path dotFile = Files.createTempFile("dawg", ".dot");
        Files.write(dotFile, graphViz.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        Path dir = Paths.get(imagesPath);
        if (!Files.exists(dir))
            dir = Files.createDirectory(dir);
        Path imageFile = Files.createTempFile(dir, "dawg" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssn")), ".png");
        ProcessBuilder pb = new ProcessBuilder(dotExecutablePath, "-Tpng", dotFile.toFile().getAbsolutePath(), "-o", imageFile.toFile().getAbsolutePath());
        try {
            pb.start().waitFor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            Files.deleteIfExists(dotFile);
        }
    }
    
    /**
     * Returns the DAWGSet's source node.
    
     * @return      the ModifiableDAWGNode or CompressedDAWGNode functioning as the DAWGSet's source node.
     */
    abstract DAWGNode getSourceNode();

    @Override
    public Iterator<String> iterator() {
        return getAllStrings().iterator();
    }
    
    /**
     * Retrieves all the valid Strings that have been inserted in to the DAWGSet.
     
     * @return      a NavigableSet containing all the Strings that have been inserted into the DAWGSet
     */
    public Iterable<String> getAllStrings() {
        return getStrings("", null, false, null, false, null, false);
    }
    
    /**
     * Retrieves all the Strings in the DAWGSet that begin with a given String.
     
     * @param prefixStr     a String that is the prefix for all the desired Strings
     * @return              a NavigableSet containing all the Strings present in the DAWGSet that begin with {@code prefixString}
     */
    public Iterable<String> getStringsStartingWith(String prefixStr) {
        return getStrings(prefixStr, null, false, null, false, null, false);
    }
    
    /**
     * Retrieves all the Strings in the DAWGSet that contain a given String.
     
     * @param str       a String that is contained in all the desired Strings
     * @return          a NavigableSet containing all the Strings present in the DAWGSet that begin with {@code prefixString}
     */
    public Iterable<String> getStringsWithSubstring(String str) {
        return getStrings("", str, false, null, false, null, false);
    }
    
    /**
     * Retrieves all the Strings in the DAWGSet that begin with a given String.
     
     * @param suffixStr         a String that is the suffix for all the desired Strings
     * @return                  a NavigableSet containing all the Strings present in the DAWGSet that end with {@code suffixStr}
     */
    public abstract Iterable<String> getStringsEndingWith(String suffixStr);
    
    /**
     * Returns the quantity of transitions in this DAWG: number of edges in graph.
     * @return quantity of transitions
     */
    public abstract int getTransitionCount();
    
    public abstract int getNodeCount();
    
    abstract DAWGNode getNodeByPath(DAWGNode from, String path);
    
    abstract int getMaxLength();
    
    Iterable<String> getStrings(String prefixString, String subString, boolean descending, String fromString, boolean inclFrom, String toString, boolean inclTo) {
        return new Iterable<String>() {
            @Override
            public Iterator<String> iterator() {
                return new LookaheadIterator<String>() {
                    private char buffer[];
                    private Deque<Character> charsStack;
                    private Deque<Integer> levelsStack;
                    private Deque<Integer> flagsStack;
                    private final Deque<DAWGNode> stack = new LinkedList<>();
                    private char from[];
                    private char to[];
                    private char sub[];
                    private final String prefixStr = prefixString == null ? "" : prefixString;
                    
                    {
                        String fromStr = fromString;
                        String toStr = toString;
                        String subStr = subString;
                        //attempt to transition down the path denoted by prefixStr
                        DAWGNode originNode = getNodeByPath(getSourceNode(), prefixStr);
                        if (originNode != null && fromStr != null) {
                            // If fromStr > toStr then return an empty set.
                            if (toStr != null) {
                                int cmp = fromStr.compareTo(toStr);
                                if (cmp > 0 || cmp == 0 && (!inclFrom || !inclTo))
                                    // Here and further in this method it means to return an empty set.
                                    originNode = null;
                            }
                            if (originNode != null) {
                                int cmp = fromStr.compareTo(prefixStr);
                                // No need to limit the range if our prefix definitely lies in this range.
                                if (cmp < 0 || cmp == 0 && inclFrom)
                                    fromStr = null;
                                // Our prefix is out of range.
                                else if (cmp > 0 && !fromStr.startsWith(prefixStr))
                                    originNode = null;
                            }
                        }
                        if (originNode != null && toStr != null) {
                            int cmp = toStr.compareTo(prefixStr);
                            // Our prefix is out of range.
                            if (cmp < 0 || cmp == 0 && !inclTo)
                                originNode = null;
                            // No need to limit the range if our prefix definitely lies in this range.
                            else if (cmp > 0 && !toStr.startsWith(prefixStr))
                                toStr = null;
                        }
                        if (originNode != null && subStr != null) {
                            if (subStr.isEmpty() || prefixStr.contains(subStr))
                                subStr = null;
                        }
                        //if there a transition path corresponding to prefixString (one or more stored Strings begin with prefixString)
                        if (originNode != null) {
                            buffer = new char[getMaxLength()];
                            System.arraycopy(prefixStr.toCharArray(), 0, buffer, 0, prefixStr.length());
                            stack.add(originNode);
                            levelsStack = new LinkedList<>();
                            levelsStack.add(prefixStr.length() - 1);
                            charsStack = new LinkedList<>();
                            flagsStack = new LinkedList<>();
                            flagsStack.add(encodeFlags(true, true, true));
                            if (fromStr != null && (!inclFrom || !fromStr.isEmpty()))
                                from = fromStr.toCharArray();
                            if (toStr != null)
                                to = toStr.toCharArray();
                            if (subStr != null)
                                sub = subStr.toCharArray();
                        }
                    }
                    
                    private int encodeFlags(boolean checkFrom, boolean checkTo, boolean checkSubstring) {
                        return (checkFrom ? 1 : 0) |
                               (checkTo ? 2 : 0) |
                               (checkSubstring ? 4 : 0);
                    }
                    
                    private void clearCaches() {
                        stack.clear();
                        charsStack.clear();
                        levelsStack.clear();
                        flagsStack.clear();
                    }
                    
                    @Override
                    public String nextElement() throws NoSuchElementException {
                        while (true) {
                            DAWGNode node = stack.pollLast();
                            if (node == null)
                                throw new NoSuchElementException();
                            int level = levelsStack.pollLast();
                            if (level >= prefixStr.length()) {
                                char c = charsStack.pollLast();
                                buffer[level] = c;
                            }
                            int flags = flagsStack.pollLast();
                            boolean checkFrom = (flags & 1) != 0;
                            boolean checkTo = (flags & 2) != 0;
                            boolean checkSubstring = (flags & 4) != 0;
                            boolean skipCurrentString = false;
                            boolean skipChildren = false;
                            if (from != null && checkFrom) {
                                // Here are two variants possible:
                                // 1. from = prefix, inclFrom = false.
                                // 2. from starts with prefix.
                                // Other variants were checked in the constructor.
                                if (level >= prefixStr.length()) {
                                    // Current string starts with prefix.
                                    // The first variant is impossible here
                                    // because it will be rejected while checking current string = prefix.
                                    int cmp;
                                    boolean fromEqualsToCurrent = false;
                                    if (from.length > level) {
                                        cmp = from[level] - buffer[level];
                                        // If we have reached the last letter then all the previous letters match.
                                        // If the last letter of current string equals to the last letter of the lower bound
                                        // then the behavior depends on inclFrom.
                                        // Inclusive behavior is the same as if from < current string.
                                        // Exclusive means that from > current string.
                                        if (cmp == 0 && level + 1 == from.length) {
                                            cmp = -1;
                                            fromEqualsToCurrent = true;
                                            if (!inclFrom)
                                                skipCurrentString = true;
                                        }
                                    } else
                                        cmp = -1;
                                    if (cmp < 0) {
                                        if (descending) {
                                            if (!fromEqualsToCurrent)
                                                checkFrom = false;
                                        } else {
                                            // All further strings match.
                                            from = null;
                                        }
                                    } else if (cmp > 0) {
                                        // All previously added strings are less than current one,
                                        // so they don't suit filter condition.
                                        if (descending)
                                            clearCaches();
                                        // Current string and all its children don't match.
                                        continue;
                                    } else
                                        // Lower bound starts with current string,
                                        // so current string < lower bound => doesn't match.
                                        // But its children may match.
                                        skipCurrentString = true;
                                } else {
                                    // Current string equals to prefix.
                                    // Both variants make us skip current string.
                                    skipCurrentString = true;
                                    // from = prefix, inclFrom = false.
                                    // All the rest strings should be accepted.
                                    // No need to check further.
                                    if (from.length == prefixStr.length())
                                        from = null;
                                }
                            }
                            SemiNavigableMap<Character, DAWGNode> childrenMap = getOutgoingTransitions(node);
                            if (to != null && checkTo) {
                                // Here are two variants possible:
                                // 1. to = prefix, inclTo = true.
                                // 2. to starts with prefix.
                                // Other variants were checked in the constructor.
                                if (level >= prefixStr.length()) {
                                    // Current string starts with prefix.
                                    // Also, to starts with prefix.
                                    int cmp;
                                    boolean toEqualsToCurrent = false;
                                    if (to.length > level) {
                                        cmp = to[level] - buffer[level];
                                        if (cmp == 0 && level + 1 == to.length) {
                                            cmp = inclTo ? 1 : -1;
                                            toEqualsToCurrent = true;
                                        }
                                    } else
                                        cmp = -1;
                                    if (cmp > 0) {
                                        if (descending) {
                                            if (!toEqualsToCurrent || childrenMap.isEmpty())
                                                // All further strings match.
                                                to = null;
                                        } else {
                                            if (!toEqualsToCurrent)
                                                checkTo = false;
                                        }
                                    } else if (cmp < 0) {
                                        // All previously added strings are less than current one,
                                        // so they don't suit filter condition.
                                        if (!descending)
                                            clearCaches();
                                        // Current string and all its children don't match.
                                        continue;
                                    }
                                } else {
                                    // Current string equals to prefix.
                                    if (to.length == prefixStr.length())
                                        // Return only current string. No other strings match.
                                        skipChildren = true;
                                }
                            }
                            if (sub != null && checkSubstring) {
                                boolean endsWithSub = level >= sub.length - 1;
                                if (endsWithSub)
                                    for (int i = 0; i < sub.length; i++)
                                        if (sub[i] != buffer[level - sub.length + 1 + i])
                                            endsWithSub = false;
                                if (endsWithSub)
                                    checkSubstring = false;
                                else
                                    skipCurrentString = true;
                            }
                            boolean retCurrentString = false;
                            if (node.isAcceptNode() && !skipCurrentString) {
                                // Natural ordering: return short string immediately then process all strings starting with it.
                                // Descending ordering: add an artificial node to stack (without children) to process current (short)
                                // string after all strings starting with it.
                                if (!descending || childrenMap.isEmpty())
                                    retCurrentString = true;
                                else {
                                    char letter = level >= prefixStr.length() ? buffer[level] : '\0';
                                    stack.add(node instanceof ModifiableDAWGNode ? new ModifiableDAWGNode(true, node.getId()) : new CompressedDAWGNode(letter, true, 0));
                                    levelsStack.add(level);
                                    charsStack.add(letter);
                                    flagsStack.add(encodeFlags(checkFrom, checkTo, checkSubstring));
                                }
                            }
                            level++;
                            if (!skipChildren) {
                                // This is not a typo. When we need natural ordering, we have to add nodes to stack in reverse order.
                                // Then the first letter in alphabetic order would be the last in the stack and would be processed first.
                                if (!descending)
                                    childrenMap = childrenMap.descendingMap();
                                for (Map.Entry<Character, DAWGNode> e : childrenMap) {
                                    stack.add(e.getValue());
                                    levelsStack.add(level);
                                    charsStack.add(e.getKey());
                                    flagsStack.add(encodeFlags(checkFrom, checkTo, checkSubstring));
                                }
                            }
                            if (retCurrentString)
                                return String.valueOf(buffer, 0, level);
                        }
                    }
                };
            }
        };
    }
    
    //Enum containing fields collectively denoting the set of all conditions that can be applied to a search on the ModifiableDAWGSet
    static enum SearchCondition {
        NO_SEARCH_CONDITION, SUBSTRING_SEARCH_CONDITION, SUFFIX_SEARCH_CONDITION;
        
        /**
        * Determines whether two Strings have a given type of relationship.

        * @param processingString      a String
        * @param conditionString       a String
        * @param searchCondition       an int denoting the type of condition to be satisfied
        * @return                      true if {@code processingString} has a relationship with
        *                              {@code conditionString} described by the condition
        *                              represented by {@code searchCondition}
        */
        public boolean satisfiesCondition(String str1, String str2) {
            boolean satisfiesSearchCondition;
         
            switch (this) {
                case SUBSTRING_SEARCH_CONDITION:
                    // When we find a string ending with a given pattern,
                    // we accept all strings starting with the found one.
                    // So, all these strings would contain a pattern as substring.
                case SUFFIX_SEARCH_CONDITION:
                    satisfiesSearchCondition = str1.endsWith(str2);
                    break;
                default:
                    satisfiesSearchCondition = true;
                    break;
            }

            return satisfiesSearchCondition;
        }
    };
}