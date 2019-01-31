import sys

from common import *

domains = sys.argv[1:]

gadget_its = [int(pow(10, i)) for i in np.arange(2, 6.5, .5)][::-1]
rootit = 10000
num_seeds = 30
player = 1

cmd_memory = "-Xmx3900m -Xms1024m"

for domain in domains:
    params = domain_params[domain]
    first = True
    for gadgetit in gadget_its:
        for resetData in {"true", "false"}:
            for exploration in {0.2, 0.4, 0.6, 0.8}:
                if first:
                    print("printHeader=true ", end="")
                print(
                    f"iterationsInRoot={rootit} "
                    f"iterationsPerGadgetGame={gadgetit} "
                    f"numSeeds={num_seeds} "
                    f"resetData={resetData} "
                    f"epsExploration={exploration} "
                    f"player={player} "
                    f"java {cmd_use_serial} {cmd_memory} {cmd_runner} 0 MCCRavg {params} "
                    f">> raw_data/exploration/{domain}_$(hostname)_$$ "
                    f"2> /dev/null""")
