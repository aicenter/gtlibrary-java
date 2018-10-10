package cz.agents.gtlibrary.domain.rps;

public class BRPSGameState extends RPSGameState {

    double[][] payoffs = {
            {0, -1, 100},
            {1, 0, -1},
            {-1, 1, 0}
    };

}
