package cz.agents.gtlibrary.algorithms.crswfabstraction;

import cz.agents.gtlibrary.iinodes.IRInformationSetImpl;
import cz.agents.gtlibrary.interfaces.GameState;

/**
 * The most basic possible information set
 */
public class CrswfInformationSet extends IRInformationSetImpl {

    private static long nextID = 0;
    private long ID;
    private double natureProbability;

    public static void resetIDCounter() {
        nextID = 0;
    }

    public CrswfInformationSet(GameState state) {
        super(state);
        this.ID = nextID++;
        natureProbability = -2;
    }

    @Override
    public String toString() {
        return "[IS:" + player + ":" + statesInformationSet + "]";
    }

    public double getNatureProbability() {
        if (natureProbability < 0) {
            natureProbability = 0;
            for (GameState state : getAllStates()) {
                natureProbability += state.getNatureProbability();
            }
        }
        return natureProbability;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CrswfInformationSet that = (CrswfInformationSet) o;

        return ID == that.ID;

    }

    @Override
    public int hashCode() {
        int result;
        result = (int) (ID ^ (ID >>> 32));
        return result;
    }

    public void mergeWith(CrswfInformationSet informationSet) {
        this.ID = informationSet.ID;
        statesInformationSet.addAll(informationSet.getAllStates());
        informationSet.addAllStatesToIS(statesInformationSet);
    }

    public long getID() {
        return ID;
    }
}
