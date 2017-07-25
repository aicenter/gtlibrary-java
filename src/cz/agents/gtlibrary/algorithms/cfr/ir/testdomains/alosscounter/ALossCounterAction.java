package cz.agents.gtlibrary.algorithms.cfr.ir.testdomains.alosscounter;

import cz.agents.gtlibrary.iinodes.ActionImpl;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.interfaces.Player;

public class ALossCounterAction extends ActionImpl {

    private String name;
    private int[] observations;

    public ALossCounterAction(InformationSet informationSet, String name, int[] observations) {
        super(informationSet);
        this.name = name;
        this.observations = observations;
    }

    @Override
    public void perform(GameState gameState) {
        ((ALossCounterGameState) gameState).addObservations(this);
        ((ALossCounterGameState) gameState).switchPlayers();
    }

    public int getObservationFor(Player player) {
        return observations[player.getId()];
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ALossCounterAction)) return false;
        if (!super.equals(o)) return false;

        ALossCounterAction that = (ALossCounterAction) o;

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
