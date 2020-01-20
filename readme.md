# Visual Reference

![image](https://i0.wp.com/tsj101sports.com/wp-content/uploads/2018/06/ateamsstrate.png?w=1344&ssl=1)
![image](https://www.optasports.com/media/3988/pirlo-pass-map-sp-branded.png)
![image](https://media.springernature.com/full/springer-static/image/art%3A10.1038%2Fs41597-019-0247-7/MediaObjects/41597_2019_247_Fig6_HTML.png?as=webp)
![image](https://media.springernature.com/full/springer-static/image/art%3A10.1038%2Fs41598-019-49969-2/MediaObjects/41598_2019_49969_Fig1_HTML.png?as=webp)
![image](https://pbs.twimg.com/media/DhCNEuaW4AAQT-Y.jpg)
![image](https://www.researchgate.net/profile/Bruno_Goncalves12/publication/313141210/figure/fig3/AS:456706820972546@1485898799477/Visual-representation-from-U17-match-analysis-Upper-panel-a-passing-network-nodes.png)
![image](https://scx2.b-cdn.net/gfx/news/hires/journal_pone_0010937_g005.jpg)
![image](https://scientometrics.files.wordpress.com/2012/04/sf_l1_munchen-madrid.png)

---

# Passing Networks

- https://tsj101sports.com/2018/06/20/football-with-graph-theory/
- https://www.optasports.com/news/opta-legends-series-andrea-pirlo/
- https://www.nature.com/articles/s41597-019-0247-7
- https://www.ncbi.nlm.nih.gov/pmc/articles/PMC6186964/
- https://www.nature.com/articles/s41598-019-49969-2
- https://community.tableau.com/thread/295135
- https://karun.in/blog/interactive-passing-networks.html#all-together-now
- https://www.frontiersin.org/articles/10.3389/fpsyg.2019.01738/full
- https://www.researchgate.net/publication/313141210_Exploring_Team_Passing_Networks_and_Player_Movement_Dynamics_in_Youth_Association_Football
- https://phys.org/news/2010-06-science-true-soccer-stars.html
- https://scientometrics.files.wordpress.com/2012/04/sf_l1_munchen-madrid.png

---

# Dataset

- https://figshare.com/collections/Soccer_match_event_dataset/4415000/2
- https://apidocs.wyscout.com/

---

# Dependencies to be installed

- [Clojure](https://clojure.org/guides/getting_started)
- [Java](https://java.com/en/download/help/download_options.xml)
- [Node](https://nodejs.org/en/download/)

# Run Project
```sh
npm install && npm start
```

# IO Usage

## Params
```sh
--id   [Id of a match]
--type [Filetype output]
```

#### Metadata
```sh
clj src/main/io/meta_data.clj --type=json
```
or
```sh
clj src/main/io/meta_data.clj --type=edn
```

#### Spit match
```sh
clj src/main/io/spit_match.clj --id=2057978 --type=json
```
or
```sh
clj src/main/io/spit_match.clj --id=2057978 --type=edn
```

#### Spit graph
```sh
clj src/main/io/spit_graph.clj --id=2057978 --type=json
```
or
```sh
clj src/main/io/spit_graph.clj --id=2057978 --type=edn
```

#### Spit graph analysis
```sh
clj src/main/io/spit_graph_analysis.clj --id=2057978 --type=json
```
or
```sh
clj src/main/io/spit_graph_analysis.clj --id=2057978 --type=edn
```

#### Spit All World Cup Matches
```sh
# just once
chmod +x get_all_world_cup_matches.sh
./get_all_world_cup_matches.sh
```

#### Spit all Brazil matches graphs
```sh
# just once
chmod +x get_all_brazil_graphs.sh
./get_all_brazil_graphs.sh
```

#### Spit all Brazil analysis
```sh
# just once
chmod +x get_all_brazil_analysis.sh
./get_all_brazil_graphs.sh
```
