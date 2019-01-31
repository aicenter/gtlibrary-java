import random
import sys

from common import *

domains = sys.argv[1:]

num_matches = {
    "B-RPS":    0,
    "IIGS-5":   8000,
    "LD-116":   2500,
    "GP-3322":  2500,
    "IIGS-13":  500,
    "LD-226":   500,
    "GP-4644":  500,
    "PTTT":     500,
}

small_preplayTime = 300
small_roundTime = 100
big_preplayTime = 5000
big_roundTime = 1000

algs = [
    "MCCR_reset_cfvWeighted_rootOpponentEps_useIST_buildGadget",
    "MCCR_keep_cfvWeighted_rootOpponentEps_useIST_buildGadget",
    "MCCFR",
    "OOS_PST",
    "OOS_IST",
    "RM",
    "UCT",
    "RND",
]

cmd_memory = "-Xmx3900m -Xms1024m"

for domain in domains:
    params = domain_params[domain]

    if domain in small_domains:
        preplayTime = small_preplayTime
        roundTime = small_roundTime
    else:
        preplayTime = big_preplayTime
        roundTime = big_roundTime

    if "B-RPS" == domain:
        preplayTime /= 10
        roundTime /= 10

    for alg1 in algs:
        for alg2 in algs:
            if alg1 == alg2:
                continue

            preplayTime1 = preplayTime
            roundTime1 = roundTime
            preplayTime2 = preplayTime
            roundTime2 = roundTime

            for k in range(num_matches[domain]):
                seed1 = random.randint(0, 1e9)
                seed2 = random.randint(0, 1e9)
                nature_seed = random.randint(0, 1e9)

                print(
                    f"preplayTime1={int(preplayTime1)} "
                    f"roundTime1={int(roundTime1)} "
                    f"preplayTime2={int(preplayTime2)} "
                    f"roundTime2={int(roundTime2)} "
                    f"rnd1={seed1} "
                    f"rnd2={seed2} "
                    f"alg1={alg1} "
                    f"alg2={alg2} "
                    f"runTime=true "
                    f"java {cmd_use_serial} {cmd_memory} {cmd_runner} 0 match {params} "
                    f">> raw_data/matches/{domain}/{alg1}/{alg2}/$(hostname)_$$ "
                    f"2> /dev/null""")
