package com.boxofc.mdag;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class DAWGMapTest {
    @Test
    public void put() throws IOException, ClassNotFoundException {
        ModifiableDAWGMap dawg = new ModifiableDAWGMap();
        dawg.put("a", "b");
        assertEquals("b", dawg.get("a"));
        dawg.put("d", "ed");
        assertEquals("ed", dawg.get("d"));
        dawg.put("a", "c");
        assertEquals("c", dawg.get("a"));
        
        dawg.optimizeLetters();
        CompressedDAWGMap cdawg = dawg.compress();
        assertEquals("ed", cdawg.get("d"));
        assertEquals("c", cdawg.get("a"));
        assertEquals(dawg, cdawg.uncompress());
        assertEquals(cdawg, cdawg.uncompress().compress());
        assertEquals(cdawg, serializeAndRead(cdawg));
    }
    
    private static CompressedDAWGMap serializeAndRead(CompressedDAWGMap dawg) throws IOException, ClassNotFoundException {
        byte data[];
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(dawg);
            oos.flush();
            data = baos.toByteArray();
        }
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            return (CompressedDAWGMap)ois.readObject();
        }
    }
}