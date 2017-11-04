package com.vzome.core.viewing;

import com.vzome.core.algebra.AlgebraicNumber;
import com.vzome.core.algebra.AlgebraicVector;
import com.vzome.core.algebra.PolygonField;
import com.vzome.core.math.Polyhedron;
import com.vzome.core.math.symmetry.AntiprismSymmetry;
import com.vzome.core.math.symmetry.Axis;
import com.vzome.core.math.symmetry.Direction;
import com.vzome.core.math.symmetry.Symmetry;

/**
 * @author David Hall
 */
public class AntiprismShapes extends AbstractShapes {

    public AntiprismShapes(String name, AntiprismSymmetry symm) {
        super(null, name, symm);
    }

    @Override
    public AntiprismSymmetry getSymmetry() {
         // This cast to AntiprismSymmetry is safe because we require an AntiprismSymmetry in our c'tor
        return (AntiprismSymmetry) super.getSymmetry();
    }

    @Override
    protected Polyhedron buildConnectorShape(String pkgName) {
        final AntiprismSymmetry symm = getSymmetry();
        final PolygonField field = symm.getField();
        final int sides = field.polygonSides();
        final AlgebraicNumber scaleR = field.one();
        final AlgebraicNumber scaleZ = field.createRational(2,9); // somewhat arbitrary
        final Polyhedron antiprism = new Polyhedron( field );
        final Direction blue = symm.getSpecialOrbit(Symmetry.SpecialOrbit.BLUE);
        final Direction red = symm.getSpecialOrbit(Symmetry.SpecialOrbit.RED);
        AlgebraicVector z = red.getPrototype().scale(scaleZ);
        
        // add vertices
        for(Axis b : blue) {
            if(b.getSense() == 0) {
                antiprism.addVertex( b.normal().scale(scaleR).plus(z) );
            }
        }
        z = z.negate();
        for(Axis b : blue) {
            if(b.getSense() == 0) {
                antiprism.addVertex( b.normal().scale(scaleR).plus(z) );
            }
        }

        // add top N-gon face
        Polyhedron.Face face = antiprism.newFace();
        for(int i=0; i < sides; i++) {
            face.add(i);
        }
        antiprism.addFace( face );

        // add bottom N-gon face
        face = antiprism.newFace();
        for(int i=sides; i < sides*2; i++) {
            face.add(i);
        }
        antiprism.addFace( face );

        // add all of the rectangular face around the perimiter
        for(int i=0; i < sides; i++) {
            // top
            int t0 = i;
            int t1 = (i+1) % sides;
            // bottom
            int b0 = t0 + sides;
            int b1 = t1 + sides;
            face = antiprism.newFace();
            face.add(t0);
            face.add(t1);
            face.add(b1);
            face.add(b0);
            antiprism.addFace( face );
        }

        return antiprism;
    }

}
