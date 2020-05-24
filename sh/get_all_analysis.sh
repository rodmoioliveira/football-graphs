#!/bin/bash

. ./sh/match_ids.sh $1

printf "===========================================================\n"
printf "Calculating metrics for $championship \n"
printf "This operations may take a while, please be patient...\n"
printf "===========================================================\n"
clj src/main/io/spit_graph_analysis.clj --id="$ids" --championship=$championship
printf "Done!\n"
