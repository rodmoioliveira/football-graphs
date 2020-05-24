#!/bin/bash

. ./sh/match_ids.sh $1

printf "===========================================================\n"
printf "Creating graphs for $championship. Please wait...\n"
printf "===========================================================\n"
clj src/main/io/spit_graph.clj --id="$ids" --championship=$championship
printf "Done!\n"
