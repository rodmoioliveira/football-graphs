#!/bin/bash
for championship in "England" "European_Championship" "France" "Germany" "Italy" "Spain" "World_Cup"
do
  printf "===========================================================\n"
  printf "Getting ids for championship $championship \n"
  printf "===========================================================\n"
  clj src/main/io/spit_match_ids.clj --championship=$championship
  printf "Done!\n"
done
