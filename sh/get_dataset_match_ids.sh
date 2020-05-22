#!/bin/bash
for championship in "England" "European_Championship" "France" "Germany" "Italy" "Spain" "World_Cup"
do
  printf "===========================================================\n"
  printf "Getting ids for championship $championship \n"
  printf "===========================================================\n"
  clj src/main/io/spit_match_ids.clj --type=edn --championship=$championship
  clj src/main/io/spit_match_ids.clj --type=json --championship=$championship
  printf "Done!\n"
done
