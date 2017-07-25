package cz.agents.gtlibrary.algorithms.efce.multiplayer;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.stackelberg.correlated.multiplayer.CompleteSefceLP;
import cz.agents.gtlibrary.algorithms.stackelberg.correlated.strategy.PureStrategyImpl;
import cz.agents.gtlibrary.iinodes.PlayerImpl;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by Jakub Cerny on 24/07/2017.
 */
public class CompleteEfceLP extends CompleteSefceLP {

    public CompleteEfceLP(GameInfo info) {
        super(new PlayerImpl(-1), info);
    }

    @Override
    protected void generateObjective(){
        System.out.println("No Objective");
    }

    @Override
    public String getInfo(){
        return "Complete EFCE LP";
    }

}
