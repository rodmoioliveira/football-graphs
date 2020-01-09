// USAGE: node build-scripts/spit-game.js --id=2057979 --output=brazil_x_swi.json
const fs = require('fs-extra');
const minimist = require('minimist');
const matches = require('../src/main/data/soccer_match_event_dataset/matches_World_Cup.json');
const players = require('../src/main/data/soccer_match_event_dataset/players.json');
const events = require('../src/main/data/soccer_match_event_dataset/events_World_Cup.json');

const hashTo = key => (acc, cur) => Object.assign(acc, { [cur[key]]: cur });

const spit_game = () => {
  const { id, output } = minimist(process.argv.slice(2));
  const matches_hash = matches.reduce(hashTo('wyId'), {});
  const match = matches_hash[id];
  const teamsIds = Object.values(match.teamsData).map(({ teamId }) => teamId);
  const players_hash = players
    .filter(p => teamsIds.includes(p.currentNationalTeamId))
    .reduce(hashTo('wyId'), {});

  const data = {
    players: players_hash,
    match,
    events: events.filter(e => e.matchId === id),
  };

  fs.writeJson(`src/main/data/games/${output}`, data)
    .then(() => console.log('Sucess split game...'))
    .catch(err => console.error(err));
};

spit_game();

