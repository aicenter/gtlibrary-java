import sys

from common import *

domains = sys.argv[1:]

root_its = 300  # 300 ms
gadget_its = [int(pow(10, i)) for i in np.arange(0, 3.5, .5)][::-1] # 10^i ms
num_seeds = 50
player = 1
cmd_memory = "-Xmx15000m -Xms1024m"

for domain in domains:
    params = domain_params[domain]

    for gadgetit in gadget_its:
        args = f"iterationsInRoot={root_its} " + \
               f"iterationsPerGadgetGame={gadgetit} " + \
               f"player={player} " + \
               f"numSeeds={num_seeds} " + \
               f"resolveTime=true " + \
               f"epsExploration=0.4 " + \
               f"TARGTYPE=PST " + \
               f"resolveTime=true "
        if gadgetit == 10:
            args += "printHeader=true "

        args += f"java {cmd_use_serial} {cmd_memory} {cmd_runner} 0"

        # MCCR reset
        print(
            f"resetData=true "
            f"deltaTargeting=0 " +
            args + f" MCCRavg {params} "
                   f">> raw_data/expl_time/MCCR_reset/{domain}_$(hostname)_$$ "
                   f"2> /dev/null")

        # MCCR keep
        print(
            f"resetData=false "
            f"deltaTargeting=0 " +
            args + f" MCCRavg {params} "
                   f">> raw_data/expl_time/MCCR_keep/{domain}_$(hostname)_$$ "
                   f"2> /dev/null")

        # MCCFR
        print(
            f"resetData=false "
            f"deltaTargeting=0 " +
            args + f" OOSavg {params} "
                   f">> raw_data/expl_time/MCCFR/{domain}_$(hostname)_$$ "
                   f"2> /dev/null")

        # OOS (PST)
        if not domain.startswith("IIGS"):
            print(
                f"resetData=false "
                f"deltaTargeting=0.9 " +
                args + f" OOSavg {params} "
                       f">> raw_data/expl_time/OOS_PST/{domain}_$(hostname)_$$ "
                       f"2> /dev/null")
