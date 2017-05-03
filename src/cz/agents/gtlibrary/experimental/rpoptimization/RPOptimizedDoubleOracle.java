/*
Copyright 2014 Faculty of Electrical Engineering at CTU in Prague

This file is part of Game Theoretic Library.

Game Theoretic Library is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Game Theoretic Library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with Game Theoretic Library.  If not, see <http://www.gnu.org/licenses/>.*/


package cz.agents.gtlibrary.experimental.rpoptimization;

import java.io.IOException;
import java.util.Map;
import java.util.Random;

import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.GeneralDoubleOracle;
import cz.agents.gtlibrary.domain.poker.generic.GPGameInfo;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerExpander;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerGameState;
import cz.agents.gtlibrary.domain.poker.generic.RPOptimizedGPExpander;
import cz.agents.gtlibrary.domain.randomgame.RPOptimizedRandomExpander;
import cz.agents.gtlibrary.domain.randomgame.RandomGameExpander;
import cz.agents.gtlibrary.domain.randomgame.RandomGameInfo;
import cz.agents.gtlibrary.domain.randomgame.RandomGameState;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.HighQualityRandom;
import cz.agents.gtlibrary.utils.io.FileManager;

public class RPOptimizedDoubleOracle {

	public static void main(String[] args) throws IOException {
//		saveRPForGenericPoker();
//		runGenericPokerWithDomainExpander();
		saveRPForRandomGame();
//		runRandomGameWithDomainExpander();
	}

	public static void saveRPForGenericPoker() throws IOException {
		Map<Player, Map<Sequence, Double>> realizationPlans;
		GameState rootState = new GenericPokerGameState();
		GameInfo gameInfo = new GPGameInfo();
		DoubleOracleConfig<DoubleOracleInformationSet> tempConfig = new DoubleOracleConfig<DoubleOracleInformationSet>(rootState, gameInfo);

		realizationPlans =  new GeneralDoubleOracle(rootState, new GenericPokerExpander<DoubleOracleInformationSet>(tempConfig), gameInfo, tempConfig).generate(null);
		new FileManager<Map<Player, Map<Sequence, Double>>>().saveObject(realizationPlans, "GenericPokerRP");
		
		for (int i = 0; i < 10; i++) {
			realizationPlans = new FileManager<Map<Player, Map<Sequence, Double>>>().loadObject("GenericPokerRP"); 
			tempConfig = new DoubleOracleConfig<DoubleOracleInformationSet>(rootState, gameInfo);
			
			long time = System.currentTimeMillis();
			Map<Player, Map<Sequence, Double>> realizationPlans1 =  new GeneralDoubleOracle(rootState, new RPOptimizedGPExpander<DoubleOracleInformationSet>(tempConfig, realizationPlans), gameInfo, tempConfig).generate(null);
			time = System.currentTimeMillis() - time;
			realizationPlans = new RPMerger(rootState, new GenericPokerExpander<DoubleOracleInformationSet>(tempConfig)).mergePlans(realizationPlans1, realizationPlans);
				new FileManager<Map<Player, Map<Sequence, Double>>>().saveObject(realizationPlans, "GenericPokerRP");
		}
		System.out.println(gameInfo.getInfo());
	}

	public static void runGenericPokerWithDomainExpander() throws IOException {
		GameState rootState = new GenericPokerGameState();
		GameInfo gameInfo = new GPGameInfo();
		DoubleOracleConfig<DoubleOracleInformationSet> algConfig = new DoubleOracleConfig<DoubleOracleInformationSet>(rootState, gameInfo);
		Map<Player, Map<Sequence, Double>> realizationPlans;
		
		realizationPlans = new FileManager<Map<Player, Map<Sequence, Double>>>().loadObject("GenericPokerRP");
		GeneralDoubleOracle doefg = new GeneralDoubleOracle(rootState, new RPOptimizedGPExpander<DoubleOracleInformationSet>(algConfig, realizationPlans), gameInfo, algConfig);
		
		doefg.generate(null);
	}

	public static void saveRPForRandomGame() throws IOException {
		Map<Player, Map<Sequence, Double>> realizationPlans;
		GameState rootState = new RandomGameState();
		GameInfo gameInfo = new RandomGameInfo();
		DoubleOracleConfig<DoubleOracleInformationSet> tempConfig = new DoubleOracleConfig<DoubleOracleInformationSet>(rootState, gameInfo);

		realizationPlans =  new GeneralDoubleOracle(rootState, new RandomGameExpander<DoubleOracleInformationSet>(tempConfig), gameInfo, tempConfig).generate(null);
		new FileManager<Map<Player, Map<Sequence, Double>>>().saveObject(realizationPlans, "RandomGameRP");
		
		for (int i = 0; i < 10; i++) {
			realizationPlans = new FileManager<Map<Player, Map<Sequence, Double>>>().loadObject("RandomGameRP"); 
			tempConfig = new DoubleOracleConfig<DoubleOracleInformationSet>(rootState, gameInfo);
			RandomGameInfo.rnd = new HighQualityRandom(RandomGameInfo.seed);
			long time = System.currentTimeMillis();
			Map<Player, Map<Sequence, Double>> realizationPlans1 =  new GeneralDoubleOracle(rootState, new RPOptimizedRandomExpander<DoubleOracleInformationSet>(tempConfig, realizationPlans), gameInfo, tempConfig).generate(null);
			time = System.currentTimeMillis() - time;
			RandomGameInfo.rnd = new HighQualityRandom(RandomGameInfo.seed);
			realizationPlans = new RPMerger(rootState, new RandomGameExpander<DoubleOracleInformationSet>(tempConfig)).mergePlans(realizationPlans1, realizationPlans);
				new FileManager<Map<Player, Map<Sequence, Double>>>().saveObject(realizationPlans, "RandomGameRP");
		}
	}

	public static void runRandomGameWithDomainExpander() throws IOException {
		GameState rootState = new RandomGameState();
		GameInfo gameInfo = new RandomGameInfo();
		DoubleOracleConfig<DoubleOracleInformationSet> algConfig = new DoubleOracleConfig<DoubleOracleInformationSet>(rootState, gameInfo);
		Map<Player, Map<Sequence, Double>> realizationPlans;
//		DoubleOracleConfig<DoubleOracleInformationSet> tempConfig = new DoubleOracleConfig<DoubleOracleInformationSet>(rootState, gameInfo);

//		realizationPlans =  new GeneralDoubleOracle(rootState, new GenericPokerExpander<DoubleOracleInformationSet>(tempConfig), gameInfo, tempConfig).generate();
		
		realizationPlans = new FileManager<Map<Player, Map<Sequence, Double>>>().loadObject("RandomGameRP");
		GeneralDoubleOracle doefg = new GeneralDoubleOracle(rootState, new RPOptimizedRandomExpander<DoubleOracleInformationSet>(algConfig, realizationPlans), gameInfo, algConfig);
		
		doefg.generate(null);
	}

}
