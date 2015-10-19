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


package cz.agents.gtlibrary.algorithms.runner;

import cz.agents.gtlibrary.algorithms.cfr.vanilla.VanillaCFR;
import cz.agents.gtlibrary.algorithms.sequenceform.FullSequenceEFG;

public class Runner {
	public static enum Algorithm {
		FullSequenceEFG, DO, VanillaCFR
	}

	public static enum Domain {
		KuhnPoker, GenericPoker, BPG, LiarsDice
	}

	public static void main(String[] args) {
		Algorithm algorithmToRun = Algorithm.FullSequenceEFG;
		Domain domain = Domain.GenericPoker;

		switch (algorithmToRun) {
		case FullSequenceEFG:
			switch (domain) {
                        case LiarsDice:
				FullSequenceEFG.runLiarsDice();
				break;

			case KuhnPoker:
				FullSequenceEFG.runKuhnPoker();
				break;

			case GenericPoker:
				FullSequenceEFG.runGenericPoker();
				break;

			case BPG:
				FullSequenceEFG.runBPG();
				break;
			default:
				throw new UnsupportedOperationException("Unsupported domain");
			}
			break;

		case VanillaCFR:
			switch (domain) {
			case KuhnPoker:
				VanillaCFR.runKuhnPoker();
				break;

			case GenericPoker:
				VanillaCFR.runGenericPoker();
				break;

			case BPG:
				VanillaCFR.runBPG();
				break;
			default:
				throw new UnsupportedOperationException("Unsupported domain");
			}

		case DO:
			switch (domain) {
			case KuhnPoker:
//				GeneralDoubleOracle.runKuhnPoker();
				break;

			case GenericPoker:
//				GeneralDoubleOracle.runGenericPoker();
				break;

			case BPG:
//				GeneralDoubleOracle.runBPG();
				break;

			default:
				throw new UnsupportedOperationException("Unsupported domain");
			}

			break;

		default:
			throw new UnsupportedOperationException("Unsupported algorithm");
		}

	}
}
