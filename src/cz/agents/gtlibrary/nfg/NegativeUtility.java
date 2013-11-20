package cz.agents.gtlibrary.nfg;


/**
 * Class representing Utility formulation given two strategies.
 * Author: Ondrej Vanek
 * Date: 11/16/11
 * Time: 1:13 PM
 */
public class NegativeUtility<T extends PureStrategy, U extends PureStrategy> extends Utility<T,U>{

    private Utility<U, T> utility;

    public NegativeUtility(Utility<U,T> utility){
        this.utility = utility;
    }

    @Override
    public double getUtility(T s1, U s2) {
        return -utility.getUtility(s2,s1);
    }
}
