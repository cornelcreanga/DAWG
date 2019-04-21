package org.quinto.dawg.memory;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.ehcache.sizeof.SizeOf;
import org.junit.Test;
import org.quinto.dawg.CompressedDAWGSet;
import org.quinto.dawg.ModifiableDAWGSet;

public class MemoryTest {

    @Test
    public void testMemorySize() throws IOException {
        SizeOf sizeOf = SizeOf.newInstance();
        List<String> lines = java.nio.file.Files.readAllLines(Path.of("words.txt"), Charset.forName("utf-8"));

        ModifiableDAWGSet dawg1 = new ModifiableDAWGSet(lines);

        CompressedDAWGSet dawg2 = dawg1.compress();
        Set<String> javaSet = new TreeSet<>(lines);

        long setSize = sizeOf.deepSizeOf(javaSet);
        System.out.println(readableSize(setSize));
        System.out.println(readableSize(sizeOf.deepSizeOf(dawg1)));
        long compressedSize = sizeOf.deepSizeOf(dawg2);
        System.out.println(readableSize(compressedSize));

        assertTrue(setSize>compressedSize*4);

    }

    private static String readableSize(long size) {
        if (size <= 0) return "0";
        final String[] units = new String[]{"B", "kB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }


}
