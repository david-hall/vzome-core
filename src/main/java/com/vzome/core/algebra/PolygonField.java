package com.vzome.core.algebra;

import static com.vzome.core.algebra.PentagonField.PHI_VALUE;
import static java.lang.Math.PI;
import static java.lang.Math.sin;

public class PolygonField extends ParameterizedField<Integer> {

//    protected static final String[] GREEK_ALPHABET = {
//        "\u03B1", // alpha
//        "\u03B2", // beta
//        "\u03B3", // gamma
//        "\u03B4", // delta
//        "\u03B5", // epsilon
//        "\u03B6", // zeta
//        "\u03B7", // eta
//        "\u03B8", // theta
//        "\u03B9", // iota
//        "\u03BA", // kappa
//        "\u03BB", // lambda
//        "\u03BC", // mu
//        "\u03BD", // nu
//        "\u03BE", // xi
//        "\u03BF", // omicron
//        "\u03C0", // pi         // To avoid confusion, don't use pi (3.1415...) as the name of an irrational factor
//        "\u03C1", // rho
////        "\u03C2", // 'final_sigma' // Not to be confused with the actual lower case letter 'stigma' (with a 't' in it) @ "\u03DB".
//        "\u03C3", // sigma
//        "\u03C4", // tau
//        "\u03C5", // upsilon
//        "\u03C6", // phi
//        "\u03C7", // chi
//        "\u03C8", // psi
//        "\u03C9", // omega
//    };

    public PolygonField(int polygonSides) {
        this( "polygon" + polygonSides, polygonSides );
    }

    // this c'tor is only intended to allow PolygonField and HeptagonField
    // to be derived from PolygonField and still maintain their original legacy name
    protected PolygonField(String name, int polygonSides) {
        super( name, polygonSides/2, polygonSides );
    }

    public final static int MINIMUMSIDES = 4;

    @Override
    protected void validate() {
        if (polygonSides() < MINIMUMSIDES) {
            String msg = "polygon sides = " + polygonSides() + ". It must be at least " + MINIMUMSIDES + ".";
            throw new IllegalArgumentException(msg);
        }
    }

    @Override
    protected void initializeLabels() {
        switch(polygonSides()) {
            case 4:
                irrationalLabels[1] = new String[]{ "\u221A" + "2", "sqrtTwo" };
                break;

            case 5:
                irrationalLabels[1] = new String[]{ "\u03C6", "phi" };
                break;

            case 6:
                irrationalLabels[1] = new String[]{ "\u221A" + "3", "sqrtThree" };
                irrationalLabels[2] = new String[]{ "Two", "two" };
                break;

            case 7:
                irrationalLabels[1] = new String[]{ "\u03C1", "rho" };
                irrationalLabels[2] = new String[]{ "\u03C3", "sigma" };
                break;

//            case 9:
//                irrationalLabels[1] = new String[]{ "\u03B1", "alpha" };
//                irrationalLabels[2] = new String[]{ "\u03B2", "beta" };
//                irrationalLabels[3] = new String[]{ "\u03B3", "gamma" };
//                break;
//
//            case 11:
//                irrationalLabels[1] = new String[]{ "\u03B8", "theta"  };
//                irrationalLabels[2] = new String[]{ "\u03BA", "kappa"  };
//                irrationalLabels[3] = new String[]{ "\u03BB", "lambda" };
//                irrationalLabels[4] = new String[]{ "\u03BC", "mu"     };
//                break;

            default:
                final String alphabet = "abcdefghijklmnopqrstuvwxyz";
                int order = getOrder();
                if(order -1 <= alphabet.length()) {
                    for(int i = 1; i < order; i++) {
                        String name = alphabet.substring(i-1, i);
                        irrationalLabels[i] = new String[]{ name, name };
                    }
                }
                else {
                    // The article "Proof by Picture: Products and Reciprocals of Diagonal Length Ratios in the Regular Polygon"
                    // at http://forumgeom.fau.edu/FG2006volume6/FG200610.pdf uses one-based indexing for the diagonals,
                    // but I am going to use zero-based indexing so it corresponds to our coefficients and multiplierMatrix indices.
                    // irrationalLabels[0] should never be needed, so I'll leave it blank.
                    for(int i = 1; i < order; i++) {
                        irrationalLabels[i] = new String[]{ "d" + subscriptString(i), "d[" + i + "]" };
                    }
                }
                break;
        }
    }

    private static String subscriptString(int i) {
        return Integer.toString(i)
                .replace("0", "\u2080")
                .replace("1", "\u2081")
                .replace("2", "\u2082")
                .replace("3", "\u2083")
                .replace("4", "\u2084")
                .replace("5", "\u2085")
                .replace("6", "\u2086")
                .replace("7", "\u2087")
                .replace("8", "\u2088")
                .replace("9", "\u2089")
                .replace("-", "\u208B")
                ;
    }

    @Override
    protected void initializeCoefficients() {
        int order = getOrder();
        int nSides = polygonSides();
        double unitLength = sin(PI / nSides);

        // The units position should always be exactly 1.0d.
        // We avoid any trig or rounding errors by specifically assigning it that value.
        coefficients[0] = 1.0d;
        // now initialize the rest, starting from i = 1
        for (int i = 1; i < order; i++) {
            coefficients[i] = sin((i+1) * PI / nSides) / unitLength;
        }

        // I discovered that a few significant values don't appear to be calculated "correctly" at first glance.
        // I found a great explanation at https://randomascii.wordpress.com/2012/02/25/comparing-floating-point-numbers-2012-edition/
        if(polygonSides() == 6) {
            // Since PI is irrational and cannot be exactly represented in a double,
            // the trig functions may not produce the exact result we expect.
            // Specifically, for a hexagon, the calculated value of coefficients[2] is 2.0000000000000004
            // I want to have the exact correct value, so I'm going to hard code it.
            // I'm pretty sure that Niven's theorem https://en.wikipedia.org/wiki/Niven%27s_theorem
            // implies that this will be the only case where we'll get a rational result,
            // although I have not thought through other cases where polygonSides() may be some multiple of 6
            // Emperically, I can see that it doesn't happen when polygonSides() == 12
            // If there is found to be some other case where we get a rational coefficient,
            // (notice that I say rational coefficient, not just integer coefficient)
            // then it should be checked here, and normalize() should reflect that case as well.
            coefficients[2] = 2.0d;
            // Similarly, the calculated value of coefficients[1] is 1.7320508075688774 and should exactly equal sqrt(3) which is 1.73205080756887729...
            coefficients[1] = Math.sqrt(3);
        }
        if(polygonSides() == 5) {
            // Similarly, for pentagons, the trig calculation for coefficients[1] differs from PHI_VALUE by 0.0000000000000002220446049250313
            // PHI_VALUE       = 1.618033988749895
            // coefficients[1] = 1.618033988749897
            // WolframAlpha says 1.618033988749894848204586834365...
            // I want to have the same value in either case, so I'm going to hard code it.
//            System.out.println("phi = " + PHI_VALUE);
//            System.out.printf("diff= %1.40f\n", (PHI_VALUE - coefficients[1]));
            coefficients[1] = PHI_VALUE;
        }
//        if(polygonSides() == 4) {
//            // No difference found between sqrt(2) and coefficients[1]
//            System.out.println("sqrt(2) = " + Math.sqrt(2.0d));
//        }
//        if(polygonSides() == 7) {
//            // For heptagons, the trig calculation for coefficients[1] and coefficients[2]
//            // had more significant digits than RHO_VALUE and SIGMA_VALUE,
//            // so I updated the constant definitions to use more digits calculated on WolframAlpha
//            // the values are now equal:
//            // coefficients[1]  = 1.80193773580483825d;
//            // RHO_VALUE        = 1.80193773580483825d;   // root of x^3 - x^2 -2x +1
//            // coefficients[2]  = 2.24697960371746706d;
//            // SIGMA_VALUE      = 2.24697960371746706d;   // root of x^3 -2x^2 - x +1
//            System.out.printf("  RHO_VALUE = %1.16f\n", HeptagonField.RHO_VALUE);
//            System.out.printf("SIGMA_VALUE = %1.16f\n", HeptagonField.SIGMA_VALUE);
//
//        }
//        System.out.println(coefficientsToString());
    }

    @Override
    protected void initializeMultiplierMatrix() {
        // <editor-fold defaultstate="collapsed">
/*
        multiplierMatrix( polygon4 ) =
        {
          {
            { 1, 0, },
            { 0, 2, },
          },
          {
            { 0, 1, },
            { 1, 0, },
          },
        }


        multiplierMatrix( polygon5 ) =
        {
          {
            { 1, 0, },
            { 0, 1, },
          },
          {
            { 0, 1, },
            { 1, 1, },
          },
        }


        // hexagon is a special case as described below
        multiplierMatrix( polygon6 ) =
        {
          {
            { 1, 0, 2, },
            { 0, 3, 0, },
            { 2, 0, 4, },
          },
          {
            { 0, 1, 0, },
            { 1, 0, 2, },
            { 0, 2, 0, },
          },
          {
            { 0, 0, 0, },
            { 0, 0, 0, },
            { 0, 0, 0, },
          },
        }


        multiplierMatrix( polygon7 ) =
        {
          {
            { 1, 0, 0, },
            { 0, 1, 0, },
            { 0, 0, 1, },
          },
          {
            { 0, 1, 0, },
            { 1, 0, 1, },
            { 0, 1, 1, },
          },
          {
            { 0, 0, 1, },
            { 0, 1, 1, },
            { 1, 1, 1, },
          },
        }


        multiplierMatrix( polygon8 ) =
        {
          {
            { 1, 0, 0, 0, },
            { 0, 1, 0, 0, },
            { 0, 0, 1, 0, },
            { 0, 0, 0, 2, },
          },
          {
            { 0, 1, 0, 0, },
            { 1, 0, 1, 0, },
            { 0, 1, 0, 2, },
            { 0, 0, 2, 0, },
          },
          {
            { 0, 0, 1, 0, },
            { 0, 1, 0, 2, },
            { 1, 0, 2, 0, },
            { 0, 2, 0, 2, },
          },
          {
            { 0, 0, 0, 1, },
            { 0, 0, 1, 0, },
            { 0, 1, 0, 1, },
            { 1, 0, 1, 0, },
          },
        }


        multiplierMatrix( polygon9 ) =
        {
          {
            { 1, 0, 0, 0, },
            { 0, 1, 0, 0, },
            { 0, 0, 1, 0, },
            { 0, 0, 0, 1, },
          },
          {
            { 0, 1, 0, 0, },
            { 1, 0, 1, 0, },
            { 0, 1, 0, 1, },
            { 0, 0, 1, 1, },
          },
          {
            { 0, 0, 1, 0, },
            { 0, 1, 0, 1, },
            { 1, 0, 1, 1, },
            { 0, 1, 1, 1, },
          },
          {
            { 0, 0, 0, 1, },
            { 0, 0, 1, 1, },
            { 0, 1, 1, 1, },
            { 1, 1, 1, 1, },
          },
        }


        multiplierMatrix( polygon10 ) =
        {
          {
            { 1, 0, 0, 0, 0, },
            { 0, 1, 0, 0, 0, },
            { 0, 0, 1, 0, 0, },
            { 0, 0, 0, 1, 0, },
            { 0, 0, 0, 0, 2, },
          },
          {
            { 0, 1, 0, 0, 0, },
            { 1, 0, 1, 0, 0, },
            { 0, 1, 0, 1, 0, },
            { 0, 0, 1, 0, 2, },
            { 0, 0, 0, 2, 0, },
          },
          {
            { 0, 0, 1, 0, 0, },
            { 0, 1, 0, 1, 0, },
            { 1, 0, 1, 0, 2, },
            { 0, 1, 0, 2, 0, },
            { 0, 0, 2, 0, 2, },
          },
          {
            { 0, 0, 0, 1, 0, },
            { 0, 0, 1, 0, 2, },
            { 0, 1, 0, 2, 0, },
            { 1, 0, 2, 0, 2, },
            { 0, 2, 0, 2, 0, },
          },
          {
            { 0, 0, 0, 0, 1, },
            { 0, 0, 0, 1, 0, },
            { 0, 0, 1, 0, 1, },
            { 0, 1, 0, 1, 0, },
            { 1, 0, 1, 0, 1, },
          },
        }


        multiplierMatrix( polygon11 ) =
        {
          {
            { 1, 0, 0, 0, 0, },
            { 0, 1, 0, 0, 0, },
            { 0, 0, 1, 0, 0, },
            { 0, 0, 0, 1, 0, },
            { 0, 0, 0, 0, 1, },
          },
          {
            { 0, 1, 0, 0, 0, },
            { 1, 0, 1, 0, 0, },
            { 0, 1, 0, 1, 0, },
            { 0, 0, 1, 0, 1, },
            { 0, 0, 0, 1, 1, },
          },
          {
            { 0, 0, 1, 0, 0, },
            { 0, 1, 0, 1, 0, },
            { 1, 0, 1, 0, 1, },
            { 0, 1, 0, 1, 1, },
            { 0, 0, 1, 1, 1, },
          },
          {
            { 0, 0, 0, 1, 0, },
            { 0, 0, 1, 0, 1, },
            { 0, 1, 0, 1, 1, },
            { 1, 0, 1, 1, 1, },
            { 0, 1, 1, 1, 1, },
          },
          {
            { 0, 0, 0, 0, 1, },
            { 0, 0, 0, 1, 1, },
            { 0, 0, 1, 1, 1, },
            { 0, 1, 1, 1, 1, },
            { 1, 1, 1, 1, 1, },
          },
        }


        multiplierMatrix( polygon12 ) =
        {
          {
            { 1, 0, 0, 0, 0, 0, },
            { 0, 1, 0, 0, 0, 0, },
            { 0, 0, 1, 0, 0, 0, },
            { 0, 0, 0, 1, 0, 0, },
            { 0, 0, 0, 0, 1, 0, },
            { 0, 0, 0, 0, 0, 2, },
          },
          {
            { 0, 1, 0, 0, 0, 0, },
            { 1, 0, 1, 0, 0, 0, },
            { 0, 1, 0, 1, 0, 0, },
            { 0, 0, 1, 0, 1, 0, },
            { 0, 0, 0, 1, 0, 2, },
            { 0, 0, 0, 0, 2, 0, },
          },
          {
            { 0, 0, 1, 0, 0, 0, },
            { 0, 1, 0, 1, 0, 0, },
            { 1, 0, 1, 0, 1, 0, },
            { 0, 1, 0, 1, 0, 2, },
            { 0, 0, 1, 0, 2, 0, },
            { 0, 0, 0, 2, 0, 2, },
          },
          {
            { 0, 0, 0, 1, 0, 0, },
            { 0, 0, 1, 0, 1, 0, },
            { 0, 1, 0, 1, 0, 2, },
            { 1, 0, 1, 0, 2, 0, },
            { 0, 1, 0, 2, 0, 2, },
            { 0, 0, 2, 0, 2, 0, },
          },
          {
            { 0, 0, 0, 0, 1, 0, },
            { 0, 0, 0, 1, 0, 2, },
            { 0, 0, 1, 0, 2, 0, },
            { 0, 1, 0, 2, 0, 2, },
            { 1, 0, 2, 0, 2, 0, },
            { 0, 2, 0, 2, 0, 2, },
          },
          {
            { 0, 0, 0, 0, 0, 1, },
            { 0, 0, 0, 0, 1, 0, },
            { 0, 0, 0, 1, 0, 1, },
            { 0, 0, 1, 0, 1, 0, },
            { 0, 1, 0, 1, 0, 1, },
            { 1, 0, 1, 0, 1, 0, },
          },
        }


        multiplierMatrix( polygon13 ) =
        {
          {
            { 1, 0, 0, 0, 0, 0, },
            { 0, 1, 0, 0, 0, 0, },
            { 0, 0, 1, 0, 0, 0, },
            { 0, 0, 0, 1, 0, 0, },
            { 0, 0, 0, 0, 1, 0, },
            { 0, 0, 0, 0, 0, 1, },
          },
          {
            { 0, 1, 0, 0, 0, 0, },
            { 1, 0, 1, 0, 0, 0, },
            { 0, 1, 0, 1, 0, 0, },
            { 0, 0, 1, 0, 1, 0, },
            { 0, 0, 0, 1, 0, 1, },
            { 0, 0, 0, 0, 1, 1, },
          },
          {
            { 0, 0, 1, 0, 0, 0, },
            { 0, 1, 0, 1, 0, 0, },
            { 1, 0, 1, 0, 1, 0, },
            { 0, 1, 0, 1, 0, 1, },
            { 0, 0, 1, 0, 1, 1, },
            { 0, 0, 0, 1, 1, 1, },
          },
          {
            { 0, 0, 0, 1, 0, 0, },
            { 0, 0, 1, 0, 1, 0, },
            { 0, 1, 0, 1, 0, 1, },
            { 1, 0, 1, 0, 1, 1, },
            { 0, 1, 0, 1, 1, 1, },
            { 0, 0, 1, 1, 1, 1, },
          },
          {
            { 0, 0, 0, 0, 1, 0, },
            { 0, 0, 0, 1, 0, 1, },
            { 0, 0, 1, 0, 1, 1, },
            { 0, 1, 0, 1, 1, 1, },
            { 1, 0, 1, 1, 1, 1, },
            { 0, 1, 1, 1, 1, 1, },
          },
          {
            { 0, 0, 0, 0, 0, 1, },
            { 0, 0, 0, 0, 1, 1, },
            { 0, 0, 0, 1, 1, 1, },
            { 0, 0, 1, 1, 1, 1, },
            { 0, 1, 1, 1, 1, 1, },
            { 1, 1, 1, 1, 1, 1, },
          },
        }


        multiplierMatrix( polygon14 ) =
        {
          {
            { 1, 0, 0, 0, 0, 0, 0, },
            { 0, 1, 0, 0, 0, 0, 0, },
            { 0, 0, 1, 0, 0, 0, 0, },
            { 0, 0, 0, 1, 0, 0, 0, },
            { 0, 0, 0, 0, 1, 0, 0, },
            { 0, 0, 0, 0, 0, 1, 0, },
            { 0, 0, 0, 0, 0, 0, 2, },
          },
          {
            { 0, 1, 0, 0, 0, 0, 0, },
            { 1, 0, 1, 0, 0, 0, 0, },
            { 0, 1, 0, 1, 0, 0, 0, },
            { 0, 0, 1, 0, 1, 0, 0, },
            { 0, 0, 0, 1, 0, 1, 0, },
            { 0, 0, 0, 0, 1, 0, 2, },
            { 0, 0, 0, 0, 0, 2, 0, },
          },
          {
            { 0, 0, 1, 0, 0, 0, 0, },
            { 0, 1, 0, 1, 0, 0, 0, },
            { 1, 0, 1, 0, 1, 0, 0, },
            { 0, 1, 0, 1, 0, 1, 0, },
            { 0, 0, 1, 0, 1, 0, 2, },
            { 0, 0, 0, 1, 0, 2, 0, },
            { 0, 0, 0, 0, 2, 0, 2, },
          },
          {
            { 0, 0, 0, 1, 0, 0, 0, },
            { 0, 0, 1, 0, 1, 0, 0, },
            { 0, 1, 0, 1, 0, 1, 0, },
            { 1, 0, 1, 0, 1, 0, 2, },
            { 0, 1, 0, 1, 0, 2, 0, },
            { 0, 0, 1, 0, 2, 0, 2, },
            { 0, 0, 0, 2, 0, 2, 0, },
          },
          {
            { 0, 0, 0, 0, 1, 0, 0, },
            { 0, 0, 0, 1, 0, 1, 0, },
            { 0, 0, 1, 0, 1, 0, 2, },
            { 0, 1, 0, 1, 0, 2, 0, },
            { 1, 0, 1, 0, 2, 0, 2, },
            { 0, 1, 0, 2, 0, 2, 0, },
            { 0, 0, 2, 0, 2, 0, 2, },
          },
          {
            { 0, 0, 0, 0, 0, 1, 0, },
            { 0, 0, 0, 0, 1, 0, 2, },
            { 0, 0, 0, 1, 0, 2, 0, },
            { 0, 0, 1, 0, 2, 0, 2, },
            { 0, 1, 0, 2, 0, 2, 0, },
            { 1, 0, 2, 0, 2, 0, 2, },
            { 0, 2, 0, 2, 0, 2, 0, },
          },
          {
            { 0, 0, 0, 0, 0, 0, 1, },
            { 0, 0, 0, 0, 0, 1, 0, },
            { 0, 0, 0, 0, 1, 0, 1, },
            { 0, 0, 0, 1, 0, 1, 0, },
            { 0, 0, 1, 0, 1, 0, 1, },
            { 0, 1, 0, 1, 0, 1, 0, },
            { 1, 0, 1, 0, 1, 0, 1, },
          },
        }


        multiplierMatrix( polygon15 ) =
        {
          {
            { 1, 0, 0, 0, 0, 0, 0, },
            { 0, 1, 0, 0, 0, 0, 0, },
            { 0, 0, 1, 0, 0, 0, 0, },
            { 0, 0, 0, 1, 0, 0, 0, },
            { 0, 0, 0, 0, 1, 0, 0, },
            { 0, 0, 0, 0, 0, 1, 0, },
            { 0, 0, 0, 0, 0, 0, 1, },
          },
          {
            { 0, 1, 0, 0, 0, 0, 0, },
            { 1, 0, 1, 0, 0, 0, 0, },
            { 0, 1, 0, 1, 0, 0, 0, },
            { 0, 0, 1, 0, 1, 0, 0, },
            { 0, 0, 0, 1, 0, 1, 0, },
            { 0, 0, 0, 0, 1, 0, 1, },
            { 0, 0, 0, 0, 0, 1, 1, },
          },
          {
            { 0, 0, 1, 0, 0, 0, 0, },
            { 0, 1, 0, 1, 0, 0, 0, },
            { 1, 0, 1, 0, 1, 0, 0, },
            { 0, 1, 0, 1, 0, 1, 0, },
            { 0, 0, 1, 0, 1, 0, 1, },
            { 0, 0, 0, 1, 0, 1, 1, },
            { 0, 0, 0, 0, 1, 1, 1, },
          },
          {
            { 0, 0, 0, 1, 0, 0, 0, },
            { 0, 0, 1, 0, 1, 0, 0, },
            { 0, 1, 0, 1, 0, 1, 0, },
            { 1, 0, 1, 0, 1, 0, 1, },
            { 0, 1, 0, 1, 0, 1, 1, },
            { 0, 0, 1, 0, 1, 1, 1, },
            { 0, 0, 0, 1, 1, 1, 1, },
          },
          {
            { 0, 0, 0, 0, 1, 0, 0, },
            { 0, 0, 0, 1, 0, 1, 0, },
            { 0, 0, 1, 0, 1, 0, 1, },
            { 0, 1, 0, 1, 0, 1, 1, },
            { 1, 0, 1, 0, 1, 1, 1, },
            { 0, 1, 0, 1, 1, 1, 1, },
            { 0, 0, 1, 1, 1, 1, 1, },
          },
          {
            { 0, 0, 0, 0, 0, 1, 0, },
            { 0, 0, 0, 0, 1, 0, 1, },
            { 0, 0, 0, 1, 0, 1, 1, },
            { 0, 0, 1, 0, 1, 1, 1, },
            { 0, 1, 0, 1, 1, 1, 1, },
            { 1, 0, 1, 1, 1, 1, 1, },
            { 0, 1, 1, 1, 1, 1, 1, },
          },
          {
            { 0, 0, 0, 0, 0, 0, 1, },
            { 0, 0, 0, 0, 0, 1, 1, },
            { 0, 0, 0, 0, 1, 1, 1, },
            { 0, 0, 0, 1, 1, 1, 1, },
            { 0, 0, 1, 1, 1, 1, 1, },
            { 0, 1, 1, 1, 1, 1, 1, },
            { 1, 1, 1, 1, 1, 1, 1, },
          },
        }
*/
        // </editor-fold>

        int order = getOrder();

        // initialize everything to 0
        for (int i = 0; i < order; i++) {
            for (int j = 0; j < order; j++) {
                for (int k = 0; k < order; k++) {
                    multiplierMatrix[i][j][k] = 0;
                }
            }
        }
            
        // initialize all of the \<->\ SouthEasterly diagonal paths
        for (int layer = 0; layer < order; layer++) {
            int midWay = layer/2;
            for (int bx = layer, by = 0; bx > midWay || bx == by; bx--, by++) {
                for (int x = bx, y = by; x < order && y < order; x++, y++) {
                    // Simple assignment would work here 
                    // but incrementing the value identifies unwanted duplicates. Ditto for the mirror.
                    multiplierMatrix[layer][y][x] += 1;
                    if(x != y) {
                        multiplierMatrix[layer][x][y] += 1; // mirror around x == y
                    }
                }
            }
        }

        // initialize the remaining /<->/ SouthWesterly diagonal paths
        int box = polygonSides() - 2;
        int parity = (polygonSides()+1) % 2;
        for (int layer = 0; layer < order-parity; layer++) {
            int base = box - layer;
            for (int xb = base, yb = 0; xb >= 0; xb--, yb++) {
                int x=xb;
                int y=yb;
                while(x<order && y<order) {
                    multiplierMatrix[layer][y][x] += 1;
                    x++;
                    y++;
                }
            }
        }

        // Hexagons are a special case because the length of the 2nd diagonal 
        // is an integer multiple of the unit edge,
        // so that "carry" must be transferred to the units position
        // much like the situation for perfect squares in SqrtField.
        // We could hard code the values, but the code below makes the reasoning a little clearer.
        if(polygonSides() == 6) {
            for(int x=0; x<order; x++) {
                for(int y=0; y<order; y++) {
                    int xfer = 2 * multiplierMatrix[2][x][y];
                    multiplierMatrix[0][x][y] += xfer;
                    multiplierMatrix[2][x][y] = 0;
                }
            }

//            multiplierMatrix = new int[][][] {
//                {
//                  { 1, 0, 2, },
//                  { 0, 3, 0, },
//                  { 2, 0, 4, },
//                },
//                {
//                  { 0, 1, 0, },
//                  { 1, 0, 2, },
//                  { 0, 2, 0, },
//                },
//                {
//                  { 0, 0, 0, },
//                  { 0, 0, 0, },
//                  { 0, 0, 0, },
//                },
//              };
        }
    }

    @Override
    void normalize(BigRational[] factors) {
        // polygonSides() will be null during c'tor call to super()
        // so rather than checking if polygonSides() == 6,
        // we'll use a different test to determine if this is a hex polygon
        // which is the only case that needs to be normalized
        // as explained above regarding Niven's theorem.
        // Note that the order of these tests is significant
        // because coefficients[] has not initialized when this method
        // is indirectly called by the AlgebraicNumber c'tor at the end of the AlgebraicField c'tor.
        if(getOrder() == 3 && ! factors[2].isZero() && coefficients[2] == 2.0d) {
            factors[0] = factors[0].plus(factors[2].times(new BigRational( 2 )));
            factors[2] = BigRational.ZERO;
        }
    }

    public Integer polygonSides() {
        return operand;
    }

    public final boolean isEven() {
        return operand % 2 == 0;
    }

    public final boolean isOdd() {
        return !isEven();
    }

    @Override
    public void defineMultiplier(StringBuffer buf, int i) {
        if (i >= 2) {
            buf.append( getIrrational(i, EXPRESSION_FORMAT) )
                    .append(" = ")
                    .append(coefficients[i - 1]);
        } else {
            super.defineMultiplier(buf, i);
        }
    }

    @Override
    public AlgebraicNumber getAffineScalar() {
        return createAlgebraicNumber(getOrder() == 2
                ? new int[]{0, 1} // e.g. PolygonField(4) and PolygonField(5)
                : new int[]{0, 0, 1} // e.g. any PolygonField > 5
        );
    }

    @Override
    public AlgebraicNumber getDefaultStrutScaling() {
        switch(polygonSides()) {
            case 5: // legacy PentagonField
                return createAlgebraicNumber(-1, 1, 2, 0);

//            case 7: // legacy HeptagonField only needs to be specified if the default is different
//                return this .one();
        }
        return this .one();
    }
}
