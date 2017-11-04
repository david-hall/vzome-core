package com.vzome.core.math.symmetry;

import com.vzome.core.algebra.AlgebraicMatrix;
import com.vzome.core.algebra.AlgebraicNumber;
import com.vzome.core.algebra.AlgebraicVector;
import com.vzome.core.algebra.PolygonField;
import com.vzome.core.math.RealVector;

/**
 * @author David Hall
 */
public class AntiprismSymmetry extends AbstractSymmetry {

    private final String name;
    private final double sigmaX2;
    private final double skewFactor;
    private Axis preferredAxis;

    public AntiprismSymmetry(PolygonField field, String defaultStyle) {
        super( field.polygonSides()*2, field, "blue", defaultStyle );
        name = "polygon" + field.polygonSides().toString()+ " antiprism";
        // TODO: Verify that this math is correct for any N-gon,
        // not just the heptagon for which it was originally derived.
        // TODO: Generalize these two numbers and rename them appropriately for any N-gon.
        // I think that eventually, sigmaX2 and skewFactor should be derived
        // from the inverse of one of the rotation matrices generated in createFrameOrbit()
        sigmaX2 = 2.0d * field.getAffineScalar().evaluate();
        skewFactor = Math .sin( (3.0d * Math.PI)/field.polygonSides() );
        System.out.println("sigmaX2        = " + sigmaX2);
        System.out.println("skewFactor     = " + skewFactor);
    }

    @Override
    public PolygonField getField() {
        // This cast to PolygonField is safe because we require a PolygonField in our c'tor
        return (PolygonField) super.getField();
    }

	/**
	 * Called by the super constructor.
	 */
	@Override
	protected void createInitialPermutations()
	{
        final int sides = getField().polygonSides();
        mOrientations[0] = new Permutation( this, null );

        // first, define the N-fold rotation
        // for example, when sides == 7, then map looks like this...
        //{ 1, 2,  3,  4,  5,  6, 0,
        //  8, 9, 10, 11, 12, 13, 7 };
        int[] map1 = new int[sides*2];
        for(int i = 0; i < sides; i++) {
            map1[i] = (i+1) % sides;
            map1[i+sides] = map1[i] + sides;
        }
        mOrientations[1] = new Permutation( this, map1 );

        // then, then 2-fold rotation
        // be sure to use a new array, don't just reorder the one used for mOrientations[1] above
        // for example, when sides == 7, then map2 looks like this...
        // { 7, 13, 12, 11, 10, 9, 8,
        //   0,  6,  5,  4,  3, 2, 1 };
        // map2 is just map1 in reverse order
        int[] map2 = new int[map1.length];
        int n = sides*2;
        for(int i = 0; i < map2.length; i++) {
            n--;
            map2[i] = map1[n];
        }
        mOrientations[sides] = new Permutation( this, map2 );
    }

	@Override
	protected void createFrameOrbit( String frameColor )
	{
        //  The following drawing uses the 7-gon as an example of the general case for an odd-gon.
        //
        //                                                   Y
        //                                                (0,1)
        //                        +---+------ [2] ------- [f] ---------+--------+---+
        //                       /   /        /           /           /        /   /
        //                      +- [g] ------+-----------+--------- [1] ------+---+
        //                     /   /        /           /           /        /   /
        //                    /   /        /           /           /        /   /
        //                   /   /        /           /           /        /   /
        //          (-1,s) [3] -+--------+-----------+-----------+------ [e] -+
        //                 /   /        /           /           /        /   /
        //                /   /        /           /           /        /   /
        //               /   /        /           /           /        /   /
        //              /   /        /                       /        /   /
        //            [a] -+--------+---------  0  ---------+--------+- [0] (1,0)  X
        //            /   /        /                       /        /   /
        //           /   /        /           /<--- s --->/        /   /
        //          /   /        /           /<---- 1 --------------->/
        //         /   /        /           /           /        /   /
        //        +- [4| ------+-----------+-----------+--------+- [d]
        //       /   /        /           /           /        /   /
        //      /   /        /           /           /        /   /
        //     /   /        /           /           /        /   /
        //    +---+------ [b] ---------+-----------+------ [6] -+
        //   /   /        /           /           /        /   /
        //  +---+--------+--------- [5] ------- [c] ------+---+
        //                        (0,-1)
        //
        //
        //  Rotation maps v[0] to v[1]. v[0].x = 1 and v[0].y = 0, so this is the X unit vector.
        //  Rotation maps v[f] to v[g]. v[f].x = 0 and v[f].y = 1, so this is the Y unit vector.
        //  Components of v[1] and v[g] can therefore be used directly to generate the rotation matrix.
        //
        //  TODO: Odd-gons may behave slightly different from even-gons, so I may need to use two variants
        //      of the algorithm but ideally, I want a single algorithm that works for all PolygonFields.

        final PolygonField field = getField();
        final int sides = field.polygonSides();
        final AlgebraicVector zAxis = field.basisVector(3, AlgebraicVector.Z);
        final AlgebraicMatrix rotationMatrix = getRotationMatrix();
//        System.out.println("rotationMatrix           = " + rotationMatrix);
//        System.out.println("rotationMatrix.inverse() = " + rotationMatrix.inverse());
        final AlgebraicVector[] rotationVectors = new AlgebraicVector[sides];
        AlgebraicVector v = field .basisVector( 3, AlgebraicVector.X );
        for(int i = 0; i < sides; i++) {
            rotationVectors[i] = v;
            v = rotationMatrix.timesColumn(v);
        }

        // All mMatrices are mappings of the standard basis unit vectors.
        // e.g. [X,Y,Z] = field .identityMatrix( 3 );
        // Generate the first half using the rotationVectors we just calculated
        mMatrices[0] = field .identityMatrix( 3 );
        for(int i = 1; i < sides; i++) {
            mMatrices[i] = new AlgebraicMatrix( rotationVectors[i], rotationVectors[(i + sides - 2) % sides] .negate(), zAxis );
        }

        // Generate the second half using m2 and the matrices derived in the previous loop.
        final AlgebraicMatrix m2 = new AlgebraicMatrix( rotationVectors[ 0 ], rotationVectors[ 2 ].negate(), zAxis.negate() );
        mMatrices[sides] = m2;
        for(int i = sides; i < mMatrices.length; i++) {
            mMatrices[i] = mMatrices[ i-sides ] .times( m2 );
        }
//        for(int i = 0; i < mMatrices.length; i++) {
//            System.out.println("    mMatrices[" + i + "] = " + mMatrices[i]);
//        }
	}

    public AlgebraicMatrix getRotationMatrix() {
        final PolygonField field = getField();
        final int order = field.getOrder();
        final AlgebraicNumber p_x = field.getUnitTerm(order - 3);
        final AlgebraicNumber q_y = field.getUnitTerm(order - 2);
        final AlgebraicNumber den = field.getUnitTerm(order - 1);
        final AlgebraicNumber num = field.getUnitTerm(1);

        final AlgebraicVector p = field.origin(3)
                .setComponent(AlgebraicVector.X, p_x.dividedBy(den))
                .setComponent(AlgebraicVector.Y, num.dividedBy(den));
        final AlgebraicVector q = field.origin(3)
                .setComponent(AlgebraicVector.X, num.dividedBy(den).negate())
                .setComponent(AlgebraicVector.Y, q_y.dividedBy(den));
        final AlgebraicVector zAxis = field.basisVector(3, AlgebraicVector.Z);

        return new AlgebraicMatrix(p, q, zAxis);
    }

	@Override
	protected void createOtherOrbits()
	{
		// Breaking the bad pattern of orbit initialization in the AbstractSymmetry constructor
	}

	public AntiprismSymmetry createStandardOrbits( String frameColor )
	{
        Direction redOrbit = createZoneOrbit( "red", 0, 1, this .mField .basisVector( 3, AlgebraicVector.Z ), true );
        redOrbit .setDotLocation( 1d, 0d );
        this .preferredAxis = redOrbit .getAxis( Symmetry.PLUS, 0 );

        Direction blueOrbit = createZoneOrbit( frameColor, 0, getField().polygonSides(), this .mField .basisVector( 3, AlgebraicVector.X ), true );
        blueOrbit .setDotLocation( 0d, 1d );

        return this;
    }

    @Override
	public Axis getPreferredAxis()
	{
		return this .preferredAxis;
	}

	@Override
	public RealVector embedInR3( AlgebraicVector v )
	{
		RealVector rv = super.embedInR3( v );
        // TODO: Generalize this for any N-gon
        Double x = rv.x + ( rv.y / sigmaX2 );
        Double y = rv.y * skewFactor;
		return new RealVector( x, y, rv.z );
	}

    @Override
    public boolean isTrivial()
    {
    	return false; // signals the POV-Ray exporter to generate the tranform
    }

    @Override
    public String getName()
    {
        return name;
    }

	@Override
	public int[] subgroup( String name )
	{
		return null; // TODO
	}

	@Override
	public Direction getSpecialOrbit( Symmetry.SpecialOrbit which )
	{
        switch ( which ) {

        case BLUE:
            return this .getDirection( "blue" );

        case RED:
            return this .getDirection( "red" );

        case YELLOW:
            return this .getDirection( "blue" );

        default:
            return null;
        }
    }
}
