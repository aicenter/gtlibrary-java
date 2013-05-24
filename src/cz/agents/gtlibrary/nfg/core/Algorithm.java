package cz.agents.gtlibrary.nfg.core;

/**
 * Abstract skelet for an algorithm.
 * Author: Ondrej Vanek
 * Date: 11/16/11
 * Time: 2:38 PM
 */
public abstract class Algorithm {

    //private PlayerGraph graph;

    protected static final boolean DEBUG_PRINT = false;

    public Algorithm() {
        //this.graph = graph;
    }

    public abstract Measure execute();
}
