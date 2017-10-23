package com.vzome.core.algebra;

/**
 * @author David Hall
 */
public class SnubCubeField  extends ParameterizedField<Integer> {
    public SnubCubeField() {
        super("snubCube", 3, 0);
    }

    @Override
    protected void initializeCoefficients() {
        // Tribonacci constant is a root of x^3 − x^2 − x − 1 and satisfies x + x^(−3) = 2
        final double tribonacciConstant = (1.0d
                + Math.cbrt(19.0d - (3.0d * Math.sqrt(33))) // this term has a minus in the middle
                + Math.cbrt(19.0d + (3.0d * Math.sqrt(33))) // this term has a plus  in the middle
                ) / 3.0d;
        coefficients[0] = 1.0d;
        coefficients[1] = tribonacciConstant;
        coefficients[2] = tribonacciConstant * tribonacciConstant;
    }

    @Override
    protected void initializeMultiplierMatrix() {
        int[][][] mm = {
            { // 1
                {1, 0, 0,},
                {0, 0, 1,},
                {0, 1, 1,},
            },
            { // psi
                {0, 1, 0,},
                {1, 0, 1,},
                {0, 1, 2,},
            },
            { // psi^2
                {0, 0, 1,},
                {0, 1, 1,},
                {1, 1, 2,},
            },
        };

        multiplierMatrix = mm;
    }

    @Override
    protected void initializeLabels() {
        irrationalLabels[1] = new String[]{"\u03C8", "psi"};
        irrationalLabels[2] = new String[]{"\u03C8\u00B2", "psi^2"};
    }
}
