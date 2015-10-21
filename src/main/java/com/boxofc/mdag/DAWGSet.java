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
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

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
    
    abstract Iterable<Map.Entry<Character, DAWGNode>> getOutgoingTransitions(DAWGNode parent);
  
    public String toGraphViz(boolean withNodeIds) {
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
        }
        dot.append('}');
        return dot.toString();
    }

    public void saveAsImage(boolean withNodeIds) throws IOException {
        String graphViz = toGraphViz(withNodeIds);
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
     * Returns the ModifiableDAWGSet's source node.
    
     * @return      the ModifiableDAWGNode or CompressedDAWGNode functioning as the ModifiableDAWGSet's source node.
     */
    abstract DAWGNode getSourceNode();

    @Override
    public Iterator<String> iterator() {
        return getAllStrings().iterator();
    }
    
    /**
     * Retrieves all the valid Strings that have been inserted in to the ModifiableDAWGSet.
     
     * @return      a NavigableSet containing all the Strings that have been inserted into the ModifiableDAWGSet
     */
    public Iterable<String> getAllStrings() {
        return getStringsStartingWith("");
    }
    
    /**
     * Retrieves all the Strings in the ModifiableDAWGSet that begin with a given String.
     
     * @param prefixStr     a String that is the prefix for all the desired Strings
     * @return              a NavigableSet containing all the Strings present in the ModifiableDAWGSet that begin with {@code prefixString}
     */
    public abstract Iterable<String> getStringsStartingWith(String prefixStr);
    
    /**
     * Retrieves all the Strings in the ModifiableDAWGSet that contain a given String.
     
     * @param str       a String that is contained in all the desired Strings
     * @return          a NavigableSet containing all the Strings present in the ModifiableDAWGSet that begin with {@code prefixString}
     */
    public abstract Iterable<String> getStringsWithSubstring(String str);
    
    /**
     * Retrieves all the Strings in the ModifiableDAWGSet that begin with a given String.
     
     * @param suffixStr         a String that is the suffix for all the desired Strings
     * @return                  a NavigableSet containing all the Strings present in the ModifiableDAWGSet that end with {@code suffixStr}
     */
    public abstract Iterable<String> getStringsEndingWith(String suffixStr);
    
    /**
     * Returns the quantity of transitions in this DAWG: number of edges in graph.
     * @return quantity of transitions
     */
    public abstract int getTransitionCount();
    
    public abstract int getNodeCount();
    
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