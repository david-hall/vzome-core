//(c) Copyright 2005, Scott Vorthmann.  All rights reserved.

package com.vzome.core.algebra;

import java.util.ArrayList;
import java.util.StringTokenizer;

public abstract class AlgebraicField
{
    public abstract AlgebraicNumber getDefaultStrutScaling();

    abstract BigRational[] multiply( BigRational[] v1, BigRational[] v2 );

    abstract double evaluateNumber( BigRational[] factors );

    abstract BigRational[] scaleBy( BigRational[] factors, int whichIrrational );

    void normalize( BigRational[] factors ) {}

    public abstract void defineMultiplier( StringBuffer instances, int w );

    public final int getOrder() { return _order; }

    public int getNumIrrationals()
    {
        return this .getOrder() - 1;
    }

    public abstract String getIrrational( int i, int format );

    public String getIrrational( int which )
    {
        return this .getIrrational( which, DEFAULT_FORMAT );
    }

    private final String name;

    private final int _order;

    private final AlgebraicNumber one;

    private final AlgebraicNumber zero;

    private final AlgebraicField subfield;

    /**
     * Positive powers of the first irrational.
     */
    private final ArrayList<AlgebraicNumber> positivePowers = new ArrayList<>( 8 );

    /**
     * Negative powers of the first irrational.
     */
    private final ArrayList<AlgebraicNumber> negativePowers = new ArrayList<>( 8 );

    public AlgebraicField( String name, int order )
    {
        this( name, order, null );
    }

    public AlgebraicField( String name, int order, AlgebraicField subfield )
    {
        this.name = name;
        this._order = order;
        this .subfield  = subfield;
        // Since derived class constructors are not fully initialized,
        // it's possible that they may not be able to perform some operations including multiply.
        // Specifically, reciprocal() depends on scaleBy()
        // which, in the case of ParameterizedField classes,
        // depends on the constructor being fully executed beforehand.
        // Also, normalize() may not work for some AlgebraicNumbers with non-zero irrational factors
        // although createRational() can safely be created at this point.
        zero = this .createRational( 0 );
        one = this .createRational( 1 );
    }

    public String getName()
    {
        return this.name;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public int hashCode() {
        int prime = 43;
        int result = 7;
        result = prime * result + name.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        return getClass().equals(obj.getClass());
    }

    public AlgebraicField getSubfield()
    {
        return subfield;
    }

    public AlgebraicNumber createAlgebraicNumber( BigRational[] factors )
    {
        return new AlgebraicNumber( this, factors );
    }

    public final AlgebraicNumber createAlgebraicNumber( int... factors )
    {
        BigRational[] brs = new BigRational[ factors .length ];
        for ( int j = 0; j < factors.length; j++ ) {
            brs[ j ] = new BigRational( factors[ j ] );
        }
        return new AlgebraicNumber( this, brs );
    }

    public AlgebraicNumber createAlgebraicNumber( int ones, int irrat, int denominator, int scalePower )
    {
        BigRational[] factors = new BigRational[ this .getOrder() ];
        factors[ 0 ] = new BigRational( ones, denominator );
        factors[ 1 ] = new BigRational( irrat, denominator );
        for ( int i = 2; i < factors.length; i++ ) {
            factors[ i ] = new BigRational( 0 );
        }
        if ( scalePower != 0 ) {
            AlgebraicNumber multiplier = this .createPower( scalePower );
            return new AlgebraicNumber( this, factors ) .times( multiplier );
        }
        else
            return new AlgebraicNumber( this, factors );
    }

    public final AlgebraicNumber createPower( int power )
    {
        if ( power == 0 )
            return this .one;
        if ( power > 0 )
        {
            // fill in any missing powers at the end of the list
            if(power >= positivePowers .size()) {
                if (positivePowers.isEmpty()) {
                    positivePowers.add(one);
                    positivePowers.add(createAlgebraicNumber(0, 1));
                }
                int size = positivePowers .size();
                AlgebraicNumber irrat = this .positivePowers .get( 1 );
                AlgebraicNumber last = this .positivePowers .get( size - 1 );
                for (int i = size; i <= power; i++) {
                    AlgebraicNumber next = last .times( irrat );
                    this .positivePowers .add( next );
                    last = next;
                }
            }
            return positivePowers .get( power );
        }
        else
        {
            power = - power; // power is now a positive number and can be used as an offset into negativePowers
            // fill in any missing powers at the end of the list
            if(power >= negativePowers .size()) {
                if (negativePowers.isEmpty()) {
                    negativePowers.add(one);
                    negativePowers.add(createAlgebraicNumber(0, 1).reciprocal());
                }
                int size = negativePowers .size();
                AlgebraicNumber irrat = this .negativePowers .get( 1 );
                AlgebraicNumber last = this .negativePowers .get( size - 1 );
                for (int i = size; i <= power; i++) {
                    AlgebraicNumber next = last .times( irrat );
                    this .negativePowers .add( next );
                    last = next;
                }
            }
            return negativePowers .get( power );
        }
    }

    /**
     * @param wholeNumber becomes the numerator with 1 as the denominator
     * @return AlgebraicNumber
     */
    public final AlgebraicNumber createRational( int wholeNumber )
    {
        return createRational( wholeNumber, 1 );
    }

    /**
     * @param numerator
     * @param denominator
     * @return AlgebraicNumber
     */
    public final AlgebraicNumber createRational( int numerator, int denominator )
    {
        return createAlgebraicNumber( numerator, 0, denominator, 0 );
    }
    
    /**
     * @return The AlgebraicNumber to be use for the Chord Ratio construction in the given field.
     * This can be used to generalize an AffinePolygon tool and a PolygonalAntiprismSymmetry.
     * In the case of the PentagonField, it returns phi.
     * In the case of the HeptagonField, it will be overridden to return sigma.
     * In the case of the PolygonField, it will also need to be be overridden.
     * In other fields, it may need to be overridden, especially if the field has multiple subfields
     * or if the order of the terms is different.
     * This implementation should work for any sqrt field 
     * and will be reasonable for any field with phi as its first irrational.
     */
    public AlgebraicNumber getAffineScalar() {
        return createAlgebraicNumber( new int[]{0, 1} );
    }

    /**
     * @param n specifies the ordinal of the term in the AlgebraicNumber which will be set to one.
     * When {@code n == 0}, the result is the same as {@code createRational(1)}.
     * When {@code n == 1}, the result is the same as {@code createPower(1)}.
     * When {@code n < 0}, the result will be {@code zero()}.
     * When {@code n >- getOrder()}, an IndexOutOfBoundsException will be thrown.
     * @return an AlgebraicNumber with the factor specified by {@code n} set to one.
     */
    public AlgebraicNumber getUnitTerm(int n) {
        if(n < 0) {
            return zero();
        }
        // Be sure to use this.createRational(0) here
        // instead of using this.zero()
        // because we are going to tweak the underlying factors.
        // The underlying factors of zero() and one() must remain immutable although it's not (currently) enforced in code.
        BigRational[] factors = this.createRational(0).getFactors();
        factors[n] = new BigRational(1);
        return createAlgebraicNumber(factors);
    }

    public BigRational[] negate( BigRational[] array )
    {
        BigRational[] result = new BigRational[ array.length ];
        for (int i = 0; i < array.length; i++) {
            result[ i ] = array[ i ] .negate();
        }
        return result;
    }

    public boolean isZero( BigRational[] array )
    {
        for (BigRational element : array) {
            if (!element.isZero()) {
                return false;
            }
        }
        return true;
    }

    public BigRational[] add( BigRational[] v1, BigRational[] v2 )
    {
        if ( v1.length != v2.length )
            throw new IllegalArgumentException( "arguments don't match" );
        BigRational[] result = new BigRational[ v1.length ];
        for (int i = 0; i < result.length; i++) {
            result[ i ] = v1[ i ] .plus( v2[ i ] );
        }
        return result;
    }

    public BigRational[] subtract( BigRational[] v1, BigRational[] v2 )
    {
        if ( v1.length != v2.length )
            throw new IllegalArgumentException( "arguments don't match" );
        BigRational[] result = new BigRational[ v1.length ];
        for (int i = 0; i < result.length; i++) {
            result[ i ] = v1[ i ] .minus( v2[ i ] );
        }
        return result;
    }

    /**
     * Drop one coordinate from the 4D vector. If wFirst (the usual), then drop
     * the first coordinate, taking the "imaginary part" of the vector. If
     * !wFirst (for old VEF import, etc.), drop the last coordinate.
     *
     * @param source
     * @param wFirst
     * @return
     */
    public final AlgebraicVector projectTo3d( AlgebraicVector source, boolean wFirst )
    {
        if ( source .dimension() == 3 )
            return source;
        else {
            AlgebraicVector result = this .origin( 3 );
            for ( int i = 0; i < 3; i++ )
                result .setComponent( i, source .getComponent( wFirst? i+1 : i ) );
            return result;
        }
    }

    public final AlgebraicVector origin( int dims )
    {
        return new AlgebraicVector( this, dims );
    }

    public final AlgebraicVector basisVector( int dims, int axis )
    {
        AlgebraicVector result = origin( dims );
        return result .setComponent( axis, this .one() );
    }

    // ======================================================================================
    // number operations
    // ======================================================================================


    protected BigRational[] reciprocal( BigRational[] fieldElement )
    {
        int order = fieldElement .length;
        BigRational[][] representation = new BigRational[ order ][ order ];
        for ( int i = 0; i < order; i++ ) {
            representation[ 0 ][ i ] = fieldElement[ i ];
        }
        for ( int j = 1; j < order; j++ ) {
            BigRational[] column = this .scaleBy( fieldElement, j );
            for ( int i = 0; i < order; i++ ) {
                representation[ j ][ i ] = column[ i ];
            }
        }
        BigRational[][] reciprocal = new BigRational[ order ][ order ];
        // create an identity matrix
        for ( int j = 0; j < order; j++ ) {
            for ( int i = 0; i < order; i++ ) {
                if ( i == j )
                    reciprocal[ j ][ i ] = new BigRational( 1 );
                else
                    reciprocal[ j ][ i ] = new BigRational( 0 );
            }
        }
        Fields .gaussJordanReduction( representation, reciprocal );
        BigRational[] reciprocalFactors = new BigRational[ order ];
        for ( int i = 0; i < order; i++ ) {
            reciprocalFactors[ i ] = reciprocal[ 0 ][ i ];
        }
        return reciprocalFactors;
    }

    public final static int DEFAULT_FORMAT = 0; // 4 + 3 \u03C6

    public final static int EXPRESSION_FORMAT = 1; // 4 +3*phi

    public final static int ZOMIC_FORMAT = 2; // 4 3

    public final static int VEF_FORMAT = 3; // (3,4)

    public AlgebraicNumber zero()
    {
        return this .zero;
    }

    public AlgebraicNumber one()
    {
        return this .one;
    }

    /**
     *
     * @param nums is an array of integer arrays: One array of coordinate terms per dimension.
     * Initially, this is designed to simplify migration of order 2 golden directions
     * to new fields of higher order having golden subfields as their first two factors.
     {@code
        field.createVector( new int[]  {  0,1,2,3,   4,5,6,7,   8,9,0,1  } );   // older code like this...
        field.createVector( new int[][]{ {0,1,2,3}, {4,5,6,7}, {8,9,0,1} } );   // should be replaced by this...
        field.createVector( new int[][]{ {0,1,2,3}, {4,5,6,7}, {8,9    } } );   // ... or even this.
     }
     * The older code shown in the first example requires an order 2 field.
     * The second example will work with any field of order 2 or greater.
     * This new overload has the advantage that the internal arrays representing the individual dimensions are more clearly delineated and controlled.
     * As shown in the third example, the internal arrays need not be all the same length. Trailing zero terms can be omitted as shown.
     * Inner arrays require an even number of elements since they represent a sequence of numerator/denominator pairs.
     * @return an AlgebraicVector
     */
    public AlgebraicVector createVector( int[][] nums )
    {
        int dims = nums.length;
        AlgebraicNumber[] coords = new AlgebraicNumber[ dims ];
        for(int c = 0; c < coords.length; c++) {
            int coordLength = nums[c].length;
            if ( coordLength % 2 != 0 ) {
                throw new IllegalStateException( "Vector dimension " + c + " has " + coordLength + " components. An even number is required." );
            }
            int nFactors = coordLength / 2;
            int order = getOrder();
            if ( nFactors > order ) {
                throw new IllegalStateException( "Vector dimension " + c + " has " + (coordLength /2) + " terms." 
                        + " Each dimension of the " + this.getName() + " field is limited to " + order + " terms."
                        + " Each term consists of a numerator and a denominator." );
            }
            BigRational[] factors = new BigRational[nFactors];
            for (int f = 0; f < nFactors; f++) {
                int numerator   = nums[c][(f*2)  ];
                int denominator = nums[c][(f*2)+1];
                factors[f] = new BigRational(numerator, denominator);
            }
            coords[c] = new AlgebraicNumber(this, factors);
        }
        return new AlgebraicVector( coords );
    }

    /**
     * @deprecated As of 11/1/2017: Use {@link #createVector( int[][] nums )} instead.
     */
    @Deprecated
    public AlgebraicVector createVector( int[] is )
    {
        int order = this .getOrder();
        if ( is.length % order != 0 )
            throw new IllegalStateException( "Field order (" + order + ") does not divide length for " + is );

        int dims = is.length / ( 2 * order );
        AlgebraicNumber[] coords = new AlgebraicNumber[ dims ];
        for ( int i = 0; i < dims; i++ ) {
            BigRational[] factors = new BigRational[ order ];
            for ( int j = 0; j < order; j++ ) {
                int numeratorIndex = 2 * ( i * order + j );
                factors[ j ] = new BigRational( is[ numeratorIndex ], is[ numeratorIndex+1 ] );
            }
            coords[ i ] = new AlgebraicNumber( this, factors );
        }
        return new AlgebraicVector( coords );
    }

    void getNumberExpression( StringBuffer buf, BigRational[] factors, int format )
    {
        switch ( format )
        {
        case ZOMIC_FORMAT:
            for ( int i = 0; i < factors.length; i++ )
            {
                if ( i > 0 )
                    buf.append( " " );
                buf .append( factors[ i ] .toString() );
            }
            break;

        case VEF_FORMAT:
            buf.append( "(" );
            for ( int i = factors.length; i > 0; i-- ) { // note that we go backwards!
                buf .append( factors[ i - 1 ] .toString() );
                if ( i > 1 )
                    buf.append( "," );
            }
            buf.append( ")" );
            break;

        default:
            int first = 0;
            for ( int i = 0; i < factors.length; i++ )
            {
                BigRational factor = factors[ i ];
                if ( factor .isZero() ) {
                    ++ first;
                    continue;
                }
                if ( i > first )
                {
                    buf .append( " " );
                }
                if ( factor .isNegative() )
                {
                    factor = factor .negate();
                    buf .append( "-" );
                }
                else if ( i > first )
                {
                    buf .append( "+" );
                }
                if ( i == 0 )
                    buf .append( factor .toString() );
                else
                {
                    if ( ! factor .isOne() )
                    {
                        buf .append( factor .toString() );
                        if ( format == EXPRESSION_FORMAT )
                            buf .append( "*" );
                    }
                    String multiplier = this .getIrrational( i, format );
                    buf .append(  multiplier );
                }
            }
            if ( first == factors.length )
                // all factors were zero
                buf .append( "0" );
            break;
        }
    }

    public AlgebraicNumber parseLegacyNumber( String val )
    {
        throw new IllegalStateException( "This field does not support vZome 2.x files." );
    }

    public AlgebraicNumber parseNumber( String nums )
    {
        StringTokenizer tokens = new StringTokenizer( nums, " " );
        return this .parseNumber( tokens );
    }

    /**
     * Consumes this.getOrder() tokens from the tokenizer
     * @param tokens
     * @return
     */
    private AlgebraicNumber parseNumber( StringTokenizer tokens )
    {
        BigRational[] rats = new BigRational[ this .getOrder() ];
        for ( int i = 0; i < rats.length; i++ ) {
            rats[ i ] = new BigRational( tokens .nextToken() );
        }
        return new AlgebraicNumber( this, rats );
    }

    public AlgebraicVector parseVector( String nums )
    {
        StringTokenizer tokens = new StringTokenizer( nums, " " );
        int numToks = tokens .countTokens();
        int order = this .getOrder();
        if ( numToks % order != 0 )
            throw new IllegalStateException( "Field order (" + order + ") does not divide token count: " + numToks + ", for '" + nums + "'" );

        int dims = numToks / order;
        AlgebraicNumber[] coords = new AlgebraicNumber[ dims ];
        for ( int i = 0; i < dims; i++ ) {
            coords[ i ] = this .parseNumber( tokens );
        }
        return new AlgebraicVector( coords );
    }

    public AlgebraicMatrix identityMatrix( int dims )
    {
        AlgebraicVector[] columns = new AlgebraicVector[ dims ];
        for ( int i = 0; i < columns.length; i++ ) {
            columns[ i ] = this .basisVector( dims, i );
        }
        return new AlgebraicMatrix( columns );
    }
}
