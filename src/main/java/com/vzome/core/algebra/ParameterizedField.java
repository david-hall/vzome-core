package com.vzome.core.algebra;

public abstract class ParameterizedField<T extends Object> extends AlgebraicField {

    protected final T operand;
    protected final double[] coefficients;
    protected int[][][] multiplierMatrix;
    protected final String[][] irrationalLabels;
    
    public ParameterizedField(String name, int order, T operand) {
        this(name, order, operand, null);
    }

    public ParameterizedField(String name, int order, T operand, AlgebraicField subfield) {
        super(name, order, subfield);
        this.operand = operand;
        // These arrays are allocated here, but all non-zero values will be initialized in the derived classes.
        coefficients = new double[order];
        multiplierMatrix = new int[order][order][order];
        irrationalLabels = new String[order][2];
        irrationalLabels[0] = new String[] {"", ""}; // unused placeholder for easier indexing
        // overridable methods intentionally called from c'tor. Be sure all member variables are initialized first.
        initialize();
    }

    protected void initialize()
    {
        // In some cases, the coefficients may eventually be determined
        // simply by evaluating the only possible solutions to the multiplierMatrix.
        // The labels are initialized last because they could possibly utilize the other values.
        validate();
        initializeMultiplierMatrix();
        initializeCoefficients();
        initializeLabels();
    }

    protected void validate() {};

    protected abstract void initializeLabels();
    
    protected abstract void initializeCoefficients();
    
    protected abstract void initializeMultiplierMatrix();

    @Override
    protected BigRational[] multiply( BigRational[] v1, BigRational[] v2 )
    {
        int order = getOrder();
        BigRational[] result = new BigRational[order];
        for(int i = 0; i < order; i++) {
            result[i] = BigRational.ZERO;
            for (int j = 0; j < order; j++) {
                for (int k = 0; k < order; k++) {
                    int multiplier = multiplierMatrix[i][j][k];
                    // We would get the same result if we do the long math even when multiplier is 0 or 1
                    // but the checks for the two special cases (0 and 1) are quicker than the overhead of BigRational math
                    // so they are included here as performance optimizations.
                    if(multiplier != 0) {
                        BigRational product = v1[j]. times( v2[k] );
                        if(multiplier != 1) {
                            product = product. times( new BigRational(multiplier) );
                        }
                        result[i] = result[i].plus(product);
                    }
                }
            }
        }
        return result;
    }

    @Override
    BigRational[] scaleBy(BigRational[] factors, int whichIrrational)
    {
        if (whichIrrational == 0) {
            return factors;
        }
        int order = getOrder();
        BigRational[] result = new BigRational[order];
        for(int i = 0; i < order; i++) {
            result[i] = BigRational.ZERO;
            for (int j = 0; j < order; j++) {
                int multiplier = multiplierMatrix[i][j][whichIrrational];
                // We would get the same result if we do the long math even when multiplier is 0 or 1
                // but the check for the two special case (0 and 1) is lots quicker than the overhead of BigRational math
                // so they are included here as performance optimizations.
                if(multiplier != 0) {
                    if (multiplier == 1) {
                        result[i] = result[i].plus(factors[j]);
                    } else {
                        result[i] = result[i].plus(factors[j].times(new BigRational(multiplier)));
                    }
                }
            }
        }
        normalize( result );
        return result;
    }

    @Override
    double evaluateNumber(BigRational[] factors) {
        double result = 0.0d;
        int order = getOrder();
        for (int i = 0; i < order; i++) {
            result += factors[i].getReal() * coefficients[i];
        }
        return result;
    }

    @Override
    public final String getIrrational(int i, int format) {
        return irrationalLabels[i][format];
    }

    @Override
    public void defineMultiplier(StringBuffer buf, int w) {
        // used by POVRayExporter
        buf.append("");
    }

    public double getCoefficient(int i) {
        return coefficients[i];
    }
    
    @Override
    public int hashCode() {
        int prime = 43;
        int result = super.hashCode();
        result = prime * result + ((operand == null) ? 0 : operand.hashCode());
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
        if( !getClass().equals(obj.getClass()) ) {
            return false;
        }
        if( !super.equals(obj) ) {
            return false;
        }

        @SuppressWarnings("unchecked")
        ParameterizedField<T> other = (ParameterizedField<T>) obj;
        if( ( operand == null) != (other.operand == null) ) {
            return false;
        }
        return operand == null ? true : operand.equals( other.operand );
    }

    @Override
    public AlgebraicNumber getDefaultStrutScaling() {
        return this.one();
    }

}
