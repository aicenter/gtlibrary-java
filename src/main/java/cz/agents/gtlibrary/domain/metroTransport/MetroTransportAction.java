package cz.agents.gtlibrary.domain.metroTransport;

import cz.agents.gtlibrary.iinodes.ActionImpl;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.utils.graph.Node;

/**
 * Created by Jakub Cerny on 05/10/2017.
 */
public class MetroTransportAction extends ActionImpl {

    protected Player actingPlayer;
    protected Node origin;
    protected Node destination;
    protected int departureTime;

    public MetroTransportAction(InformationSet informationSet, Node origin, Node destination, int departureTime) {
        super(informationSet);
        this.actingPlayer = informationSet.getPlayer();
        this.origin = origin;
        this.destination = destination;
        this.departureTime = departureTime;
    }

    @Override
    public void perform(GameState gameState) {
        MetroTransportGameState state = (MetroTransportGameState)gameState;
        if (actingPlayer.equals(MetroTransportGameInfo.DEFENDER))
            state.performDefenderAction(this);
        else
            state.performAttackerAction(this);
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
