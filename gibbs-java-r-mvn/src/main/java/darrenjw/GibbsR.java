import java.util.*;
import cern.jet.random.tdouble.*;
import cern.jet.random.tdouble.engine.*;
 
class GibbsR
{
 
    public static void main(String[] arg)
    {
    if (arg.length != 3) {
            System.err.println("Usage: java GibbsR <Iters> <Thin> <Seed>");
            System.exit(1);  
        }
    int N=Integer.parseInt(arg[0]);
    int thin=Integer.parseInt(arg[1]);
    int seed=Integer.parseInt(arg[2]);
    double[][] mat=gibbs(N,thin,seed);
    System.out.println("Iter x y");
    for (int i=0;i<N;i++) {
        System.out.println(""+i+" "+mat[0][i]+" "+mat[1][i]);
    }   
    }
 
    public static double[][] gibbs(int N,int thin,int seed)
    {
    DoubleRandomEngine rngEngine=new DoubleMersenneTwister(seed);
    Normal rngN=new Normal(0.0,1.0,rngEngine);
    Gamma rngG=new Gamma(1.0,1.0,rngEngine);
    double x=0,y=0;
    double[][] mat=new double[2][N];
    for (int i=0;i<N;i++) {
        for (int j=0;j<thin;j++) {
        x=rngG.nextDouble(3.0,y*y+4);
        y=rngN.nextDouble(1.0/(x+1),1.0/Math.sqrt(x+1));
        }
        mat[0][i]=x; mat[1][i]=y;
    }
    return mat;
    }
 
}


