#!/bin/bash

mail -s "Job started" "michal.sustr@gmail.com"

module add jdk-8
module add python-3.6.2-gcc

cd /storage/plzen1/home/sustrmic/GT
source venv/bin/activate

python consumer.py --n_processes=32
mail -s "Job finished" "michal.sustr@gmail.com"

# qsub -l select=1:ncpus=32:mem=64gb -l walltime=01:00:00 metacentrum_job.sh
# qsub -I -l select=1:ncpus=1:mem=1gb -l walltime=01:00:00 metacentrum_job.sh
