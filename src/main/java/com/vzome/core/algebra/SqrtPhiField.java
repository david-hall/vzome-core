package com.vzome.core.algebra;

/**
 * @author David Hall
 */
public class SqrtPhiField  extends ParameterizedField<Integer> {
    public SqrtPhiField() {
        super("sqrtPhi", 4, 0, new PentagonField()); // HACK: PentagonField as subfield allows golden VEF to work
    }

    @Override
    protected void validate() {
    }

    @Override
    public AlgebraicNumber getDefaultStrutScaling() {
        return this.one();
    }

    @Override
    protected void initializeCoefficients() {
        // Note that these coefficients are not in increasing order
        // but rather, the first two are in the same position as the golden subfield
        // then the remaining coefficients are in increasing order.
        // This is not necessary mathematically, but only as a practical matter
        // so that manually converting between the two fields in VEF format is easier
        // In addition, since createPower works on the first irrational, we want that to be phi whenever applicable
        final double PHI_VALUE = PentagonField.PHI_VALUE; // ( 1.0 + Math.sqrt( 5.0 ) ) / 2.0;
        coefficients[0] = 1.0d;
        coefficients[1] = PHI_VALUE;
        coefficients[2] =             Math.sqrt(PHI_VALUE);
        coefficients[3] = PHI_VALUE * Math.sqrt(PHI_VALUE);
    }

    /*

    Multiplication table:
    p = phi
    r = sqrt(phi)
    t = phi*sqrt(phi)

      *  |   1  | p   | r   | t
    -----+------+-----+-----+-----
      1  |   1  | p   | r   | t
      p  |   p  | 1+p | t   | r+t
      r  |   r  | t   | p   | 1+p
      t  |   t  | r+t | 1+p | 1+2p

     */
    @Override
    protected void initializeMultiplierMatrix() {
        int[][][] mm = {
            { // 1
              { 1, 0, 0, 0, },
              { 0, 1, 0, 0, },
              { 0, 0, 0, 1, },
              { 0, 0, 1, 1, },
            },
            { // p = phi
              { 0, 1, 0, 0, },
              { 1, 1, 0, 0, },
              { 0, 0, 1, 1, },
              { 0, 0, 1, 2, },
            },
            { // r = sqrt(phi)
              { 0, 0, 1, 0, },
              { 0, 0, 0, 1, },
              { 1, 0, 0, 0, },
              { 0, 1, 0, 0, },
            },
            { // t = phi*sqrt(phi)
              { 0, 0, 0, 1, },
              { 0, 0, 1, 1, },
              { 0, 1, 0, 0, },
              { 1, 1, 0, 0, },
            },
        };
        multiplierMatrix = mm;
    }

    @Override
    protected void initializeLabels() {
        irrationalLabels[1] = new String[]{"\u03C6", "phi"};
        irrationalLabels[2] = new String[]{"\u221A\u03C6", "sqrt(phi)"};
        irrationalLabels[3] = new String[]{"\u03C6\u221A\u03C6", "phi*sqrt(phi)"};
    }
}
