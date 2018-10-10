package cz.agents.gtlibrary.domain.rps;

public class BRPSGameInfo extends RPSGameInfo {
    @Override
    public double getMaxUtility() {
        return 100;
    }

    @Override
    public String getInfo() {
        return "Biased Rock Paper Scissors";
    }
}
