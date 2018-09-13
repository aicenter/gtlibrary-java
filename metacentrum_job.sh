#!/bin/bash

mail -s "Job started" "michal.sustr@gmail.com" <<< "Job started: $PBS_O_HOST -- $PBS_QUEUE -- $PBS_JOBID -- $PBS_JOBNAME"

module add jdk-8
module add python-3.6.2-gcc

cd /storage/plzen1/home/sustrmic/GT
source venv/bin/activate

timeout -s SIGINT 7180 python consumer.py --n_processes=30
mail -s "Job finished" "michal.sustr@gmail.com" <<< "Job finished: $PBS_O_HOST -- $PBS_QUEUE -- $PBS_JOBID -- $PBS_JOBNAME"

#interactive job for testing
# qsub -I -l select=1:ncpus=1:mem=1gb -l walltime=01:00:00 metacentrum_job.sh
#run actual job like this:
# qsub -l select=1:ncpus=32:mem=60gb -l walltime=02:00:00 metacentrum_job28.sh


# for i in `seq 1 10`; do echo $i; qsub -l select=1:ncpus=4:mem=8gb -l walltime=02:00:00 metacentrum_job3.sh; done

