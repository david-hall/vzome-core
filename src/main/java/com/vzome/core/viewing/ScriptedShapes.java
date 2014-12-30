/*
 * Created on Jun 25, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.vzome.core.viewing;

import java.io.File;
import java.io.InputStream;

import com.vzome.core.math.Polyhedron;
import com.vzome.core.math.symmetry.Direction;
import com.vzome.core.math.symmetry.IcosahedralSymmetry;
import com.vzome.core.math.symmetry.Symmetry;
import com.vzome.core.parts.DefaultStrutGeometry;
import com.vzome.core.parts.StrutGeometry;
import com.vzome.core.parts.ZomicPolyhedronModelInterpreter;
import com.vzome.core.parts.ZomicStrutGeometry;
import com.vzome.core.zomic.parser.Parser;
import com.vzome.core.zomic.program.Anything;


public class ScriptedShapes extends AbstractShapes
{
    private static final String NODE_SCRIPT = "connector.zomic";
    
    private final AbstractShapes delegate, fallback;

    public ScriptedShapes( File prefsFolder, String pkgName, String name, IcosahedralSymmetry symm )
    {
        this( prefsFolder, pkgName, name, symm, null );
    }

    public ScriptedShapes( File prefsFolder, String pkgName, String name, IcosahedralSymmetry symm, AbstractShapes fallback )
    {
        super( pkgName, name, symm );
        delegate = new ExportedVEFShapes( prefsFolder, pkgName, name, symm, null );
        this.fallback = fallback;
    }
    
    protected StrutGeometry createStrutGeometry( Direction dir )
    {
        StrutGeometry result = delegate .createStrutGeometry( dir );
        if ( ! (result instanceof DefaultStrutGeometry ) )
            return result;
        // else is default, let's try to improve on that
        ZomicStrutGeometry zsg = new ZomicStrutGeometry( mPkgName, dir, mSymmetry );
        if ( zsg .isDefined() )
        	return zsg;
        if ( fallback != null )
            return fallback .createStrutGeometry( dir );
        return result;
    }
    
    protected Polyhedron buildConnectorShape( String pkgName )
    {
        String prefix = ZomicStrutGeometry.SCRIPT_PREFIX + pkgName + "/";
        InputStream nodeScript = Thread.currentThread()
                .getContextClassLoader().getResourceAsStream(
                        prefix + NODE_SCRIPT );
        if ( nodeScript == null )
            throw new IllegalStateException( "missing script: " + prefix
                    + NODE_SCRIPT );
        Anything connScript = Parser.parse( nodeScript, (IcosahedralSymmetry) mSymmetry );
        ZomicPolyhedronModelInterpreter zpmi = new ZomicPolyhedronModelInterpreter( mSymmetry,
                connScript, new int[]{ 0,1,0,1 }, mSymmetry.getPermutation( 0 ),
                Symmetry.PLUS );
        return zpmi.getPolyhedron();
    }
}