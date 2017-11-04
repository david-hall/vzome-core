package com.vzome.core.viewing;

import com.vzome.core.algebra.AlgebraicField;
import com.vzome.core.algebra.AlgebraicNumber;
import com.vzome.core.algebra.AlgebraicVector;
import com.vzome.core.algebra.HeptagonField;
import com.vzome.core.algebra.PolygonField;
import com.vzome.core.math.Polyhedron;
import com.vzome.core.math.Polyhedron.Face;
import com.vzome.core.math.symmetry.AntiprismSymmetry;
import com.vzome.fields.heptagon.HeptagonalAntiprismSymmetry;
import java.util.List;
import static junit.framework.TestCase.assertEquals;
import org.junit.Test;

/**
 *
 * @author David Hall
 */
public class AntiprismShapesTest {

    private void printPolyhedron(Polyhedron poly) {
        List<AlgebraicVector> vertexList = poly.getVertexList();
        for (Face face : poly.getFaceSet()) {
            System.out.println(face.size() + ": " + face.getNormal());
            for (int i = 0; i < face.size(); i++) {
                int vert = face.getVertex(i);
                AlgebraicVector vector = vertexList.get(vert);
                String msg = String.format("%1$8d %2$8d\t%3$-32s %4$-32s", i, vert,
                        vector.getVectorExpression(AlgebraicField.DEFAULT_FORMAT),
                        vector.getVectorExpression(AlgebraicField.VEF_FORMAT));
                System.out.println(msg);
            }
        }
    }

    @Test
    public void testBuildConnectorShape() {
        System.out.println("buildConnectorShape");
        String name = "polygon antiprism";
        for(int i = 4; i <= 24; i++) { // TODO: If i == 18, then BigRational fails because multOverflow isn't implemented.
        PolygonField field = new PolygonField(i);
        AntiprismSymmetry symmetry = new AntiprismSymmetry(field, name).createStandardOrbits( "blue" );
        AntiprismShapes antiprismShapes = new AntiprismShapes(name, symmetry);
        String pkgName = antiprismShapes.getPackage();
        Polyhedron connector = antiprismShapes.buildConnectorShape(pkgName);
        final int sides = field.polygonSides();
        assertEquals(sides * 2, connector.getVertexList().size());
        assertEquals(sides + 2, connector.getFaceSet().size());
        // TODO: should be assertEquals((sides + 1) * 2, connector.getFaceSet().size());

        //printPolyhedron(connector);
        }
    }

    @Test
    public void testHeptagonBuildConnectorShape() {
        System.out.println("buildHeptagonConnectorShape");
        String name = "heptagonal antiprism";
        HeptagonField field = new HeptagonField();
        HeptagonalAntiprismSymmetry symmetry = new HeptagonalAntiprismSymmetry(field, "blue", name).createStandardOrbits( "blue" );
        final AbstractShapes octahedralShapes = new OctahedralShapes( "octahedral", "triangular antiprism", symmetry );
    	final AbstractShapes antiprismShapes = new ExportedVEFShapes( null, "heptagon/antiprism", "heptagonal antiprism", symmetry, octahedralShapes );
        String pkgName = antiprismShapes.getPackage();
        Polyhedron connector = antiprismShapes.buildConnectorShape(pkgName);
        final int sides = 7;
        assertEquals(sides * 2, connector.getVertexList().size());
        assertEquals((sides + 1) * 2, connector.getFaceSet().size());

        printPolyhedron(connector);
    }

    @Test
    public void testReciprocals() {
        PolygonField field = new PolygonField(7);
//        AlgebraicNumber one = field.createAlgebraicNumber(1,0,0);
        AlgebraicNumber rho = field.createAlgebraicNumber(0,1,0);
        AlgebraicNumber sig = field.createAlgebraicNumber(0,0,1);
        System.out.println(rho.evaluate());
        System.out.println(sig.evaluate());
        System.out.println(rho.reciprocal().evaluate());
        System.out.println(sig.reciprocal().evaluate());
//        System.out.println(rho.times(sig.reciprocal()).toString(AlgebraicField.DEFAULT_FORMAT));
//        System.out.println(sig.times(rho.reciprocal()).toString(AlgebraicField.DEFAULT_FORMAT));
        System.out.println(rho.dividedBy(sig).evaluate());
        System.out.println(sig.dividedBy(rho).evaluate());
    }

}
