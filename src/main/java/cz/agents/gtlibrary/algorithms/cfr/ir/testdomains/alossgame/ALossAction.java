package cz.agents.gtlibrary.algorithms.cfr.ir.testdomains.alossgame;

import cz.agents.gtlibrary.iinodes.ActionImpl;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

public class ALossAction extends ActionImpl {

    private String name;

    public ALossAction(InformationSet informationSet, String name) {
        super(informationSet);
        this.name = name;
    }

    @Override
    public void perform(GameState gameState) {
        ((ALossGameState) gameState).addObservations();
        ((ALossGameState) gameState).switchPlayers();
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ALossAction)) return false;
        if (!super.equals(o)) return false;

        ALossAction that = (ALossAction) o;

        return name != null ? name.equals(that.name) : that.name == null;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public String toString() {
        return name;
    }
}
