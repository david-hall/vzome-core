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

        FIELDS.add (new SqrtField(1));
        FIELDS.add (new SqrtField(2));
        FIELDS.add (new SqrtField(3));
        FIELDS.add (new SqrtField(4));
        FIELDS.add (new SqrtField(5));

        // order 2
        FIELDS.add (new PolygonField(4));
        FIELDS.add (new PolygonField(5));
        // order 3
        FIELDS.add (new PolygonField(6));
        FIELDS.add (new PolygonField(7));
        // order 4
        FIELDS.add (new PolygonField(8));
        FIELDS.add (new PolygonField(9));
        // order 5
        FIELDS.add (new PolygonField(10));
        FIELDS.add (new PolygonField(11));
        // higher orders ad nauseum...
        FIELDS.add (new PolygonField(12));
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
