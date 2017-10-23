package com.vzome.core.algebra;

/**
 * @author David Hall
 *
 * When the radicand is 3, this field is the basis for the vertices
 * of the 4D Triangular Hebesphenorotundaeic Rhombochoron
 * as described at http://eusebeia.dyndns.org/4d/J92_rhombochoron
 * See also http://eusebeia.dyndns.org/4d/bipara2gyrC1010
 * and http://eusebeia.dyndns.org/4d/smbr which both use a root2 field
 * and http://eusebeia.dyndns.org/4d/cubicpyramid which uses only integers.
 * The vertices of numerous other regular polychora are described at http://eusebeia.dyndns.org/4d/uniform
 */
public class PhiPlusSqrtField extends ParameterizedField<Integer> {

    private final SqrtField sqrtSubField;

    public PhiPlusSqrtField(int radicand) {
        super("phiPlusSqrt" + radicand, 4, radicand, new PentagonField()); // HACK: PentagonField as subfield allows golden VEF to work
        sqrtSubField = new SqrtField(radicand);
        initialize();
    }

    @Override
    protected void initialize() {
        if(sqrtSubField != null) {  // sqrtSubField will be null during c'tor call to super()
            super.initialize();
        }
    }

    @Override
    protected void validate() {
        sqrtSubField.validate();
    }

    @Override
    protected void initializeCoefficients() {
        // Note that these coefficients are not necessarily in increasing order, depending on the operand
        // but rather, the first two are in the same position as the golden subfield
        // then the remaining coefficients are in increasing order.
        // This is not strictly necessary , but it means that the base class implementation
        // of getAffineScalar() will work. 
        // Manually converting between two fields in VEF format is also simplified.
        // In addition, since createPower works on the first irrational, we want that to be phi whenever applicable
        final double PHI_VALUE = PentagonField.PHI_VALUE; // ( 1.0 + Math.sqrt( 5.0 ) ) / 2.0;
        final Double squareRoot = sqrtSubField.sqrt();
        coefficients[0] = 1.0d;
        coefficients[1] = PHI_VALUE;
        coefficients[2] =             squareRoot;
        coefficients[3] = PHI_VALUE * squareRoot; // Never used for a normalized perfect square
    }

    /*

    Multiplication table:
    N = operand

    p = phi
    s =     sqrt(N)
    t = phi*sqrt(N) = s*p

    When N is a perfect square, s will be an integer.
      *  |  1  |  p    |  s   |  t
    -----+-----+-------+------+------
      1  |  1  |  p    |  s   |  t
      p  |  p  |  1+p  |  t   |  s+t
      s  |  s  |  t    |  N   |  Np
      t  |  t  |  s+t  |  Np  | 1N+Np

    *********************************************************************
    Perfect square example:
    N = 9
    s = 3
    t = 3p
      *  |  1  |  p    |  3   |  3p
    -----+-----+-------+------+------
      1  |  1  |  p    |  3   |  3p
      p  |  p  |  1+p  |  3p  |  3+3p
      3  |  3  |  3p   |  9   |  9p
      3p |  3p |  3+3p |  9p  |  9+9p

    *********************************************************************
    Non-perfect square example:
    N = 7
    s = sqrt(N)
    t = sp
      *  |  1  |  p    |  s   |  t
    -----+-----+-------+------+------
      1  |  1  |  p    |  s   |  t
      p  |  p  |  1+p  |  t   |  s+t
      s  |  s  |  t    |  7   |  7p
      t  |  t  |  s+t  |  7p  |  7+7p

    *********************************************************************
     */
    @Override
    protected void initializeMultiplierMatrix() {
        final int r = sqrtSubField.radicand();
        if(sqrtSubField.isPerfectSquare) {
            int[][][] mm = {
                { // 1
                  { 1, 0, 0, 0, },
                  { 0, 1, 0, 0, },
                  { 0, 0, r, 0, },
                  { 0, 0, 0, r, },
                },
                { // p = phi
                  { 0, 1, 0, 0, },
                  { 1, 1, 0, 0, },
                  { 0, 0, 0, r, },
                  { 0, 0, r, r, },
                },
                { // s = sqrt(N)
                  { 0, 0, 1, 0, },
                  { 0, 0, 0, 1, },
                  { 1, 0, 0, 0, },
                  { 0, 1, 0, 0, },
                },
                { // t = phi*sqrt(N)
                  { 0, 0, 0, 1, },
                  { 0, 0, 1, 1, },
                  { 0, 1, 0, 0, },
                  { 1, 1, 0, 0, },
                },
            };
            multiplierMatrix = mm;
        } else {
            // NOT a Perfect Square
            int[][][] mm = {
                { // 1
                  { 1, 0, 0, 0, },
                  { 0, 1, 0, 0, },
                  { 0, 0, r, 0, },
                  { 0, 0, 0, r, },
                },
                { // p = phi
                  { 0, 1, 0, 0, },
                  { 1, 1, 0, 0, },
                  { 0, 0, 0, r, },
                  { 0, 0, r, r, },
                },
                { // s = sqrt(N)
                  { 0, 0, 1, 0, },
                  { 0, 0, 0, 1, },
                  { 1, 0, 0, 0, },
                  { 0, 1, 0, 0, },
                },
                { // t = phi*sqrt(N)
                  { 0, 0, 0, 1, },
                  { 0, 0, 1, 1, },
                  { 0, 1, 0, 0, },
                  { 1, 1, 0, 0, },
                },
            };
            multiplierMatrix = mm;
        }
    }

//    @Override
//    protected BigRational[] multiply(BigRational[] v1, BigRational[] v2) {
//        BigRational[] result = super.multiply(v1, v2);
//        // The base class doesn't expect to need normalization for multiplication, but we need it here
//        // until I can get multiplierMatrix initialized correctly for the perfect square case
//        normalize(result);
//        return result;
//    }

    @Override
    void normalize(BigRational[] factors) {
        if(sqrtSubField == null)
            return;  // sqrtSubField will be null during c'tor call to super()
        if(sqrtSubField.isPerfectSquare) {
            final BigRational R = new BigRational(sqrtSubField.sqrt().longValue());
            if( !factors[2].isZero()) {
                // move sqrt(N) factor to unit factor
                factors[0] = factors[0].plus(factors[2].times(R));
                factors[2] = BigRational.ZERO;
            }
            if (!factors[3].isZero()) {
                // move phi*sqrt(N) factor to phi factor
                factors[1] = factors[1].plus(factors[3].times(R));
                factors[3] = BigRational.ZERO;
            }
        }
    }

    @Override
    protected void initializeLabels() {
        final AlgebraicField phiSubfield = getSubfield();

        irrationalLabels[1] = new String[]{ phiSubfield.getIrrational(1,0),  phiSubfield.getIrrational(1,1)};
        irrationalLabels[2] = new String[]{sqrtSubField.getIrrational(1,0), sqrtSubField.getIrrational(1,1)};
        irrationalLabels[3] = new String[]{ irrationalLabels[1][0] +       irrationalLabels[2][0],
                                            irrationalLabels[1][1] + "*" + irrationalLabels[2][1] }; //"phi*sqrt(#)"
    }

    public SqrtField getSqrtSubField() {
        return sqrtSubField;
    }

}
