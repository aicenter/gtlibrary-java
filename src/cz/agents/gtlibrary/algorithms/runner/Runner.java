package cz.agents.gtlibrary.algorithms.runner;

import cz.agents.gtlibrary.algorithms.cfr.vanilla.VanillaCFR;
import cz.agents.gtlibrary.algorithms.sequenceform.GeneralFullSequenceEFG;

public class Runner {
	public static enum Algorithm {
		FullSequenceEFG, DO, VanillaCFR
	}

	public static enum Domain {
		KuhnPoker, GenericPoker, BPG
	}

	public static void main(String[] args) {
		Algorithm algorithmToRun = Algorithm.FullSequenceEFG;
		Domain domain = Domain.KuhnPoker;

		switch (algorithmToRun) {
		case FullSequenceEFG:
			switch (domain) {
			case KuhnPoker:
				GeneralFullSequenceEFG.runKuhnPoker();
				break;

			case GenericPoker:
				GeneralFullSequenceEFG.runGenericPoker();
				break;

			case BPG:
				GeneralFullSequenceEFG.runBPG();
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
