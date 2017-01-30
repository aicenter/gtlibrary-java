package cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions;

import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo;

public class IJCAI2017Experiments {
    public static void main(String[] args) {
        RandomGameInfo.MAX_DEPTH = Integer.parseInt(args[0]);
        RandomGameInfo.MAX_BF = Integer.parseInt(args[1]);
        RandomGameInfo.seed = Long.parseLong(args[2]);
        if(args[3].equals("IR"))
            IRFicticiousPlay.runBothIRRandomAbstractionGame();
        else
            IRFicticiousPlay.runCPRRBothIRRandomAbstractionGame();
    }
}
