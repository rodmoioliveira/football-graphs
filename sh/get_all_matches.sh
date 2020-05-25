#!/bin/bash

. ./sh/match_ids.sh $1

printf "===========================================================\n"
printf "Fetching matches of $championship from dataset. Please wait...\n"
printf "===========================================================\n"
clj src/main/io/spit_match.clj --id="$ids" --championship=$championship
printf "Done!\n"
