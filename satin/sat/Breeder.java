// File: $Id$

/**
 * @author Kees van Reeuwijk
 * @version $Revision$
 */

import java.io.File;
import java.util.Random;

public final class Breeder implements BreederInterface {
    static final int MAXGEN = 400;
    static final int GENSIZE = 20;

    static class Result {
        Genes genes;    // The genes used.
        int decisions;  // The number of decisions needed.

        public Result( Genes g, int d )
        {
            genes = g;
            decisions = d;
        }

        public String toString()
        {
            return "(" + decisions + ") " + genes;
        }
    };


    public int run( SATProblem pl[], Genes genes, int cutoff )
    {
        return BreederSolver.run( pl, genes, cutoff );
    }
 
    // Given genes, and old, slightly worse genes, generate an
    // extrapolated clone.
    private static Genes extrapolateGenes( float scale, Genes g, Genes oldG, Genes max, Genes min )
    {
	Genes res = (Genes) g.clone();

	// We blindly assume oldG has same null-s and array lengths as g.
	if( g.floats != null ){
	    float f[] = g.floats;
	    float fo[] = oldG.floats;

	    for( int ix=0; ix<f.length; ix++ ){
		float delta = f[ix]-fo[ix];

		res.floats[ix] += scale*delta;
		if( res.floats[ix]<min.floats[ix] ){
		    res.floats[ix] = min.floats[ix];
		}
		if( res.floats[ix]>max.floats[ix] ){
		    res.floats[ix] = max.floats[ix];
		}
	    }
	}
	// For the boolean array nothing useful can be done.

	// TODO: also do something for the ints.
	return res;
    }

    // Given genes, return a mutated clone (hur, hur).
    private static Genes mutateGenes( Random rng, Genes g, float step, Genes max, Genes min )
    {
	Genes res = (Genes) g.clone();
	int ix = rng.nextInt( res.floats.length );
	if( ix<g.floats.length ){
	    res.floats[ix] += ((step/2.0f) - (step*rng.nextFloat()));
	    if( res.floats[ix]<min.floats[ix] ){
		res.floats[ix] = min.floats[ix];
	    }
	    if( res.floats[ix]>max.floats[ix] ){
		res.floats[ix] = max.floats[ix];
	    }
	}
	return res;
    }

    private Genes prevBestGenes = null;

    /**
     * Breed the next generation.
     */
    private Result breedNextGeneration( Random rng, SATProblem pl[], Genes genes, int bestD, Genes maxGenes, Genes minGenes )
    {
        boolean extrapolating = false;
        float step = 0.2f;
        int res[] = new int[GENSIZE];
        Genes g[] = new Genes[GENSIZE];
        int slot = 0;

        if( prevBestGenes != null ){
            // If we have changed best genes, fill one trial slot
            // with an extrapolation of the change in genes.
            g[slot] = extrapolateGenes( 0.3f, genes, prevBestGenes, maxGenes, minGenes );
            extrapolating = true;
            res[slot] = BreederSolver.run( pl, g[slot], (3*bestD)/2 );
            slot++;
        }
        prevBestGenes = null;
        for( int i=slot; i<GENSIZE; i++ ){
            // Fill all remaining slots with mutations
            g[i] = mutateGenes( rng, genes, step, maxGenes, minGenes );
            res[i] = BreederSolver.run( pl, g[i], (3*bestD)/2 );
        }

        // Now evaluate the results.
        for( int i=0; i<GENSIZE; i++ ){
            int nextD = res[i];
            Genes nextGenes = g[i];

            if( nextD>=0 ){
                System.err.print( nextD + " " );
                if( nextD<bestD ){
                    bestD = nextD;
                    prevBestGenes = genes;
                    genes = nextGenes;
                }
            }
            else {
                System.err.print( "** " );
            }
        }
        System.err.println();
        return new Result( genes, bestD );
    }

    public void run( SATProblem pl[] ){
	Genes maxGenes = BreederSolver.getMaxGenes();
	Genes minGenes = BreederSolver.getMinGenes();
	Random rng = new Random( 2 );

	Genes bestGenes = BreederSolver.getInitialGenes();
        int bestD = Integer.MAX_VALUE;

        for( int gen = 0; gen<MAXGEN; gen++ ){
            Result res = breedNextGeneration( rng, pl, bestGenes, bestD, maxGenes, minGenes );
            bestGenes = res.genes;
            bestD = res.decisions;
            System.err.println( "g" + gen + "->" + res );
        }
	System.out.println( "Best result (" + bestD + ") " + bestGenes );
    }

    /**
     * Allows execution of the class.
     * @param args The command-line arguments.
     */
    public static void main( String args[] ) throws java.io.IOException
    {

	if( args.length == 0 ){
	    System.err.println( "A list of filename arguments required." );
	    System.exit( 1 );
	}

	SATProblem pl[] = new SATProblem[args.length];

	for( int i=0; i<args.length; i++ ){
	    File f = new File( args[i] );
	    if( !f.exists() ){
		System.err.println( "File does not exist: " + f );
		System.exit( 1 );
	    }
	    SATProblem p = SATProblem.parseDIMACSStream( f );
	    System.err.println( "Problem file: " + args[i] );
	    p.report( System.out );
	    p.optimize();
	    p.report( System.out );
	    pl[i] = p;
	}

        Breeder b = new Breeder();

        b.run( pl );

    }
}
