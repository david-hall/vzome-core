package com.vzome.core.algebra;

import java.util.HashSet;
import java.util.Set;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.*;
import static junit.framework.TestCase.assertTrue;
import org.junit.Test;

/**
 * @author David Hall
 */
public class AlgebraicFieldTest {
    private static final Set<AlgebraicField> FIELDS = new HashSet<>();
    
    static {
        AlgebraicField pentagonField = new PentagonField();
        FIELDS.add (pentagonField);
        FIELDS.add (new RootTwoField());
        FIELDS.add (new RootThreeField());
        FIELDS.add (new HeptagonField());
        FIELDS.add (new SnubDodecField(pentagonField));
    }

    @Test
    public void testNotEqual() {
        int pass = 0;
        AlgebraicField last = null;
        for(AlgebraicField field : FIELDS) {
            String msg = "field = " + field + ". last = " + (last == null ? "null" : last.toString());
            assertFalse(msg, field.equals(last));
            pass++;
            last = field;
        }
        assertTrue(pass > 1);
        assertEquals(FIELDS.size(), pass);
	}
        
    @Test
	public void testOrder() {
        int pass = 0;
        for(AlgebraicField field : FIELDS) {
            assertTrue(field.toString(), field.getOrder() >= 2);
            pass++;
        }
        assertEquals(FIELDS.size(), pass);
	}

}
