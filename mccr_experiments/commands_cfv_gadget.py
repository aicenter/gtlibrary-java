from common import *
import sys

domains = sys.argv[1:]
num_seeds = 50
cmd_memory = "-Xmx3900m -Xms1024m"

for domain in domains:
    params = domain_params[domain]

    for seed in range(num_seeds):
        if seed == 0:
            print(f"printHeader=true ", end="")
        if domain in small_domains:
            print(f"calcExploitability=true ", end="")
            print(f"calcTarget=true ", end="")
            print(f"memoryLimit=3.8 ", end="")
            cmd_memory = "-Xmx3900m -Xms1024m"
        if domain in big_domains:
            cmd_memory = "-Xmx510g -Xms510g"
            print(f"memoryLimit=508 ", end="")

        print(
            f"runMinutes=115 "
            f"java {cmd_use_serial} {cmd_memory} {cmd_runner} {seed} CFVgadget "
            f"{params} "
            f">> raw_data/cfv_gadget/{domain}_$(hostname)_$$ "
            f"2> /dev/null ")

