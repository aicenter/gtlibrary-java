#!/usr/bin/env bash
set -eux

# You can specify your own parallelization routine
PARALLEL_RUN="parallel -j 8 --progress eval ::::"

ALL_EXPERIMENTS="cfv_gadget expl_time exploration matches"

# Select which experiments to run
RUN_EXPERIMENTS="$ALL_EXPERIMENTS"

SMALL_DOMAINS="B-RPS IIGS-5 LD-116 GP-3322"
LARGE_DOMAINS="PTTT IIGS-13 LD-226 GP-4644"

# Select which domains to run
DOMAINS="${SMALL_DOMAINS} ${LARGE_DOMAINS}"

# Which match algs should be run
MATCH_ALGS="MCCR_reset_cfvWeighted_rootOpponentEps_useIST_buildGadget MCCR_keep_cfvWeighted_rootOpponentEps_useIST_buildGadget MCCFR OOS_PST OOS_IST RM UCT RND"

########################################################################################

function create_folders() {
	mkdir commands
	for DIR in raw_data processed_data plots; do
		mkdir $DIR;
		for EXPERIMENT in ${ALL_EXPERIMENTS}; do
			mkdir $DIR/$EXPERIMENT;
		done
	done

	mkdir raw_data/expl_time/{MCCR_reset,MCCR_keep,MCCFR,OOS_PST}

	eval "mkdir raw_data/matches/{${DOMAINS// /,}}"
	eval "mkdir raw_data/matches/{${DOMAINS// /,}}/{${MATCH_ALGS// /,}}"
	eval "mkdir raw_data/matches/{${DOMAINS// /,}}/{${MATCH_ALGS// /,}}/{${MATCH_ALGS// /,}}"
}

function compile() {
	pushd ../
	mvn -DskipTests -Djava.library.path=$(pwd)/libs/cplex package
	cp target/gtlibrary-jar-with-dependencies.jar mccr_experiments/
	popd
}

function clean() {
	rm -rf ${EXPERIMENT_DIRS} commands
	rm gtlibrary-jar-with-dependencies.jar
}

function generate_commands() {
    python commands_cfv_gadget.py ${DOMAINS} > commands/cfv_gadget;
    python commands_expl_time.py ${SMALL_DOMAINS} > commands/expl_time;
    python commands_exploration.py ${SMALL_DOMAINS} > commands/exploration;
    python commands_matches.py ${DOMAINS} > commands/matches;
}

function run_experiments() {
	for EXPERIMENT in ${RUN_EXPERIMENTS}; do
		${PARALLEL_RUN} commands/${EXPERIMENT};
	done
}

function collect_results() {
	# Experiment 1) Averaging of sampled CFVs
	for DOMAIN in ${DOMAINS}; do
		cat raw_data/cfv_gadget/${DOMAIN}_* | sort -r > processed_data/cfv_gadget/${DOMAIN}.csv;
	done

	# Experiment 2) Stability of CFVs
	python process_cfv.py ${DOMAINS}

	# Experiment 3) Comparison of expl.
	for DOMAIN in ${SMALL_DOMAINS}; do
	    for ALG in MCCR_reset MCCR_keep MCCFR OOS_PST; do
		    cat raw_data/expl_time/${ALG}/${DOMAIN}_* | sort -r > processed_data/expl_time/${DOMAIN}_${ALG}.csv;
	    done
	done

	# Experiment 4) exploration goes into appendix
	for DOMAIN in ${SMALL_DOMAINS}; do
		cat raw_data/exploration/${DOMAIN}_* | sort -r > processed_data/exploration/${DOMAIN}.csv;
	done

	# Experiment 5) matches
	for DOMAIN in ${DOMAINS}; do
        for ALG1 in $MATCH_ALGS; do
            for ALG2 in $MATCH_ALGS; do
                echo "$DOMAIN/$ALG1/$ALG2"
                cat "raw_data/matches/$DOMAIN/$ALG1/$ALG2/"* > "processed_data/matches/concat_${DOMAIN}_${ALG1}_${ALG2}"
            done
        done
    done
    for DOMAIN in ${DOMAINS}; do
        echo "$DOMAIN"
        echo  "alg1;alg2;rnd1;rnd2;utils0;utils1" > "processed_data/matches/${DOMAIN}.csv"
        cat "processed_data/matches/concat_${DOMAIN}_"* >> "processed_data/matches/${DOMAIN}.csv"
    done
}


function plot() {
	# Experiment 1) Averaging of sampled CFVs
	python plt_cfv_brps.py

	# Experiment 2) Stability of CFVs
	python plt_cfv_paper.py ${DOMAINS}

	# These go into appendix
	for DOMAIN in $DOMAINS; do
		for DIFF in abs rel; do
			python plt_cfv_summary.py $DOMAIN weighted large $DIFF
		done
	done

	# Experiment 3) Comparison of expl.
	python plt_expl_time.py IIGS-5 LD-116 GP-3322

	# Experiment 4) exploration goes into appendix
	for DOMAIN in IIGS-5 LD-116 GP-3322; do
		for VARIANT in keep reset; do
		    python plt_exploration.py ${DOMAIN} ${VARIANT} large;
		done
	done

	# Experiment 5) matches
	for DOMAIN in $DOMAINS; do
	    TEX=plots/matches/${DOMAIN}.tex
        python process_tournament.py processed_data/matches/${DOMAIN}.csv > $TEX

        sed -i s~lllllll~lrrrrrr~g ${TEX}
        sed -i s~alg1~~g ${TEX}
        sed -i s~alg2~\\\\textbf{${DOMAIN}}~g ${TEX}
        cat $TEX | grep -E -v  "^\s+(\\&\s+){7}\\\\\\\\" > ${TEX}~
        mv ${TEX}~ ${TEX}
    done
}

########################################################################################

# Run commands!
create_folders
compile
generate_commands
run_experiments
collect_results
plot
