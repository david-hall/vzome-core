package com.vzome.core.algebra;

/**
 * @author David Hall
 */
public class SqrtField extends ParameterizedField<Integer> {

    protected boolean isPerfectSquare; // don't initialize here. Only in initializeCoefficients()

    public SqrtField(int radicand) {
        this("sqrt" + radicand, radicand);
    }

    // this c'tor is only intended to allow RootTwoField and RootThreeField
    // to be derived from SqrtField and still maintain their original name
    protected SqrtField(String name, int radicand) {
        super(name, 2, radicand);
    }

    @Override
    protected void validate() {
        if (radicand() <= 0) {
            String msg = "radicand " + radicand() + " is not positive.";
            throw new IllegalArgumentException(msg);
        }
    }

    @Override
    protected void initializeLabels() {
        String r = Integer.toString(radicand());
        irrationalLabels[1] = new String[] {"\u221A" + r, "sqrt(" + r + ")"};
    }

    @Override
    protected void initializeCoefficients() {
        Double squareRoot = Math.sqrt(radicand());
        coefficients[0] = 1.0d;
        coefficients[1] = squareRoot;
        double floor = Math.floor(squareRoot);
        isPerfectSquare = ( squareRoot.equals( floor ) ) &&
                (floor * floor == radicand());
    }

    @Override
    protected void initializeMultiplierMatrix() {
        int u = 0;
        int r = 1;
        if (isPerfectSquare) {
            u = 1;
            r = 0;
        }
        int[][][] mm = {
            { // units
                {1, u},
                {u, radicand()},
            },
            { // rootN
                {0, r},
                {r, 0},
            }
        };
        multiplierMatrix = mm;
    }

    @Override
    void normalize(BigRational[] factors) {
        if(isPerfectSquare && ! factors[1].isZero()) { // isPerfectSquare will be false during c'tor call to super()
            factors[0] = factors[0].plus( factors[1].times( new BigRational( sqrt().longValue() ) ) );
            factors[1] = BigRational.ZERO;
        }
    }

    public Double sqrt() {
        return coefficients[1];
    }

    public Integer radicand() {
        return operand;
    }

    @Override
    public AlgebraicNumber getDefaultStrutScaling() {
        // we start with this value just because we did in RootTwoField and RootThreeField
        return createAlgebraicNumber(1, 0, 2, -3);
    }
}
