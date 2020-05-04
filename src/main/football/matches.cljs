(ns football.matches
  (:require
   ; [shadow.resource :as rc]
   ; [cljs.reader :as reader]
   ; [camel-snake-kebab.core :as csk]
   [clojure.pprint :refer [pprint]]))

; (defn world-cup-matches
;   []
;   [(-> (rc/inline "../data/analysis/argentina_croatia,_0_3.edn") reader/read-string)
;    (-> (rc/inline "../data/analysis/argentina_iceland,_1_1.edn") reader/read-string)
;    (-> (rc/inline "../data/analysis/australia_peru,_0_2.edn") reader/read-string)
;    (-> (rc/inline "../data/analysis/belgium_england,_2_0.edn") reader/read-string)
;    (-> (rc/inline "../data/analysis/belgium_japan,_3_2.edn") reader/read-string)
;    (-> (rc/inline "../data/analysis/belgium_panama,_3_0.edn") reader/read-string)
;    (-> (rc/inline "../data/analysis/belgium_tunisia,_5_2.edn") reader/read-string)
;    (-> (rc/inline "../data/analysis/brazil_belgium,_1_2.edn") reader/read-string)
;    (-> (rc/inline "../data/analysis/brazil_costa_rica,_2_0.edn") reader/read-string)
;    (-> (rc/inline "../data/analysis/brazil_mexico,_2_0.edn") reader/read-string)
;    (-> (rc/inline "../data/analysis/brazil_switzerland,_1_1.edn") reader/read-string)
;    (-> (rc/inline "../data/analysis/colombia_england,_1_1_(_p).edn") reader/read-string)
;    (-> (rc/inline "../data/analysis/colombia_japan,_1_2.edn") reader/read-string)
;    (-> (rc/inline "../data/analysis/costa_rica_serbia,_0_1.edn") reader/read-string)
;    (-> (rc/inline "../data/analysis/croatia_denmark,_1_1_(_p).edn") reader/read-string)
;    (-> (rc/inline "../data/analysis/croatia_england,_2_1_(_e).edn") reader/read-string)
;    (-> (rc/inline "../data/analysis/croatia_nigeria,_2_0.edn") reader/read-string)
;    (-> (rc/inline "../data/analysis/denmark_australia,_1_1.edn") reader/read-string)
;    (-> (rc/inline "../data/analysis/denmark_france,_0_0.edn") reader/read-string)
;    (-> (rc/inline "../data/analysis/egypt_uruguay,_0_1.edn") reader/read-string)
;    (-> (rc/inline "../data/analysis/england_belgium,_0_1.edn") reader/read-string)
;    (-> (rc/inline "../data/analysis/england_panama,_6_1.edn") reader/read-string)
;    (-> (rc/inline "../data/analysis/france_argentina,_4_3.edn") reader/read-string)
;    (-> (rc/inline "../data/analysis/france_australia,_2_1.edn") reader/read-string)
;    (-> (rc/inline "../data/analysis/france_belgium,_1_0.edn") reader/read-string)
;    (-> (rc/inline "../data/analysis/france_croatia,_4_2.edn") reader/read-string)
;    (-> (rc/inline "../data/analysis/france_peru,_1_0.edn") reader/read-string)
;    (-> (rc/inline "../data/analysis/germany_mexico,_0_1.edn") reader/read-string)
;    (-> (rc/inline "../data/analysis/germany_sweden,_2_1.edn") reader/read-string)
;    (-> (rc/inline "../data/analysis/iceland_croatia,_1_2.edn") reader/read-string)
;    (-> (rc/inline "../data/analysis/iran_portugal,_1_1.edn") reader/read-string)
;    (-> (rc/inline "../data/analysis/iran_spain,_0_1.edn") reader/read-string)
;    (-> (rc/inline "../data/analysis/japan_poland,_0_1.edn") reader/read-string)
;    (-> (rc/inline "../data/analysis/japan_senegal,_2_2.edn") reader/read-string)
;    (-> (rc/inline "../data/analysis/korea_republic_mexico,_1_2.edn") reader/read-string)
;    (-> (rc/inline "../data/analysis/mexico_sweden,_0_3.edn") reader/read-string)
;    (-> (rc/inline "../data/analysis/morocco_iran,_0_1.edn") reader/read-string)
;    (-> (rc/inline "../data/analysis/nigeria_argentina,_1_2.edn") reader/read-string)
;    (-> (rc/inline "../data/analysis/nigeria_iceland,_2_0.edn") reader/read-string)
;    (-> (rc/inline "../data/analysis/panama_tunisia,_1_2.edn") reader/read-string)
;    (-> (rc/inline "../data/analysis/peru_denmark,_0_1.edn") reader/read-string)
;    (-> (rc/inline "../data/analysis/poland_colombia,_0_3.edn") reader/read-string)
;    (-> (rc/inline "../data/analysis/poland_senegal,_1_2.edn") reader/read-string)
;    (-> (rc/inline "../data/analysis/portugal_morocco,_1_0.edn") reader/read-string)
;    (-> (rc/inline "../data/analysis/portugal_spain,_3_3.edn") reader/read-string)
;    (-> (rc/inline "../data/analysis/russia_croatia,_2_2_(_p).edn") reader/read-string)
;    (-> (rc/inline "../data/analysis/russia_egypt,_3_1.edn") reader/read-string)
;    (-> (rc/inline "../data/analysis/russia_saudi_arabia,_5_0.edn") reader/read-string)
;    (-> (rc/inline "../data/analysis/saudi_arabia_egypt,_2_1.edn") reader/read-string)
;    (-> (rc/inline "../data/analysis/senegal_colombia,_0_1.edn") reader/read-string)
;    (-> (rc/inline "../data/analysis/serbia_brazil,_0_2.edn") reader/read-string)
;    (-> (rc/inline "../data/analysis/serbia_switzerland,_1_2.edn") reader/read-string)
;    (-> (rc/inline "../data/analysis/spain_morocco,_2_2.edn") reader/read-string)
;    (-> (rc/inline "../data/analysis/spain_russia,_1_1_(_p).edn") reader/read-string)
;    (-> (rc/inline "../data/analysis/sweden_england,_0_2.edn") reader/read-string)
;    (-> (rc/inline "../data/analysis/sweden_korea_republic,_1_0.edn") reader/read-string)
;    (-> (rc/inline "../data/analysis/sweden_switzerland,_1_0.edn") reader/read-string)
;    (-> (rc/inline "../data/analysis/switzerland_costa_rica,_2_2.edn") reader/read-string)
;    (-> (rc/inline "../data/analysis/tunisia_england,_1_2.edn") reader/read-string)
;    (-> (rc/inline "../data/analysis/uruguay_france,_0_2.edn") reader/read-string)
;    (-> (rc/inline "../data/analysis/uruguay_portugal,_2_1.edn") reader/read-string)
;    (-> (rc/inline "../data/analysis/uruguay_russia,_3_0.edn") reader/read-string)
;    (-> (rc/inline "../data/analysis/uruguay_saudi_arabia,_1_0.edn") reader/read-string)])

; (->> world-cup-matches
;      (map (fn [{:keys [match-id label]}]
;             {:match-id match-id
;              :label label
;              :filename (str "" (-> label csk/->snake_case) ".edn")}))
;      (reduce (fn [acc cur] (assoc-in acc [(-> cur :match-id str keyword)] cur)) {})
;      pprint)


(def matches-files-hash
  {:2057965
   {:match-id 2057965,
    :label "Spain - Morocco, 2 - 2",
    :filename "spain_morocco,_2_2.edn"},
   :2057960
   {:match-id 2057960,
    :label "Portugal - Spain, 3 - 3",
    :filename "portugal_spain,_3_3.edn"},
   :2058010
   {:match-id 2058010,
    :label "Uruguay - France, 0 - 2",
    :filename "uruguay_france,_0_2.edn"},
   :2057976
   {:match-id 2057976,
    :label "Nigeria - Argentina, 1 - 2",
    :filename "nigeria_argentina,_1_2.edn"},
   :2057963
   {:match-id 2057963,
    :label "Iran - Spain, 0 - 1",
    :filename "iran_spain,_0_1.edn"},
   :2057964
   {:match-id 2057964,
    :label "Iran - Portugal, 1 - 1",
    :filename "iran_portugal,_1_1.edn"},
   :2058015
   {:match-id 2058015,
    :label "Croatia - England, 2 - 1",
    :filename "croatia_england,_2_1_(_e).edn"},
   :2057955
   {:match-id 2057955,
    :label "Egypt - Uruguay, 0 - 1",
    :filename "egypt_uruguay,_0_1.edn"},
   :2057972
   {:match-id 2057972,
    :label "Argentina - Iceland, 1 - 1",
    :filename "argentina_iceland,_1_1.edn"},
   :2057959
   {:match-id 2057959,
    :label "Saudi Arabia - Egypt, 2 - 1",
    :filename "saudi_arabia_egypt,_2_1.edn"},
   :2058016
   {:match-id 2058016,
    :label "Belgium - England, 2 - 0",
    :filename "belgium_england,_2_0.edn"},
   :2057986
   {:match-id 2057986,
    :label "Germany - Sweden, 2 - 1",
    :filename "germany_sweden,_2_1.edn"},
   :2057957
   {:match-id 2057957,
    :label "Uruguay - Saudi Arabia, 1 - 0",
    :filename "uruguay_saudi_arabia,_1_0.edn"},
   :2057967
   {:match-id 2057967,
    :label "Peru - Denmark, 0 - 1",
    :filename "peru_denmark,_0_1.edn"},
   :2057987
   {:match-id 2057987,
    :label "Korea Republic - Mexico, 1 - 2",
    :filename "korea_republic_mexico,_1_2.edn"},
   :2058002
   {:match-id 2058002,
    :label "Uruguay - Portugal, 2 - 1",
    :filename "uruguay_portugal,_2_1.edn"},
   :2057994
   {:match-id 2057994,
    :label "England - Belgium, 0 - 1",
    :filename "england_belgium,_0_1.edn"},
   :2057979
   {:match-id 2057979,
    :label "Costa Rica - Serbia, 0 - 1",
    :filename "costa_rica_serbia,_0_1.edn"},
   :2057989
   {:match-id 2057989,
    :label "Mexico - Sweden, 0 - 3",
    :filename "mexico_sweden,_0_3.edn"},
   :2057997
   {:match-id 2057997,
    :label "Colombia - Japan, 1 - 2",
    :filename "colombia_japan,_1_2.edn"},
   :2057968
   {:match-id 2057968,
    :label "France - Peru, 1 - 0",
    :filename "france_peru,_1_0.edn"},
   :2057980
   {:match-id 2057980,
    :label "Brazil - Costa Rica, 2 - 0",
    :filename "brazil_costa_rica,_2_0.edn"},
   :2057985
   {:match-id 2057985,
    :label "Sweden - Korea Republic, 1 - 0",
    :filename "sweden_korea_republic,_1_0.edn"},
   :2057990
   {:match-id 2057990,
    :label "Belgium - Panama, 3 - 0",
    :filename "belgium_panama,_3_0.edn"},
   :2057999
   {:match-id 2057999,
    :label "Japan - Senegal, 2 - 2",
    :filename "japan_senegal,_2_2.edn"},
   :2057977
   {:match-id 2057977,
    :label "Iceland - Croatia, 1 - 2",
    :filename "iceland_croatia,_1_2.edn"},
   :2058011
   {:match-id 2058011,
    :label "Brazil - Belgium, 1 - 2",
    :filename "brazil_belgium,_1_2.edn"},
   :2058014
   {:match-id 2058014,
    :label "France - Belgium, 1 - 0",
    :filename "france_belgium,_1_0.edn"},
   :2058012
   {:match-id 2058012,
    :label "Russia - Croatia, 2 - 2",
    :filename "russia_croatia,_2_2_(_p).edn"},
   :2058000
   {:match-id 2058000,
    :label "Japan - Poland, 0 - 1",
    :filename "japan_poland,_0_1.edn"},
   :2057969
   {:match-id 2057969,
    :label "Denmark - Australia, 1 - 1",
    :filename "denmark_australia,_1_1.edn"},
   :2057970
   {:match-id 2057970,
    :label "Denmark - France, 0 - 0",
    :filename "denmark_france,_0_0.edn"},
   :2058006
   {:match-id 2058006,
    :label "Brazil - Mexico, 2 - 0",
    :filename "brazil_mexico,_2_0.edn"},
   :2058008
   {:match-id 2058008,
    :label "Sweden - Switzerland, 1 - 0",
    :filename "sweden_switzerland,_1_0.edn"},
   :2057982
   {:match-id 2057982,
    :label "Serbia - Brazil, 0 - 2",
    :filename "serbia_brazil,_0_2.edn"},
   :2057971
   {:match-id 2057971,
    :label "Australia - Peru, 0 - 2",
    :filename "australia_peru,_0_2.edn"},
   :2057966
   {:match-id 2057966,
    :label "France - Australia, 2 - 1",
    :filename "france_australia,_2_1.edn"},
   :2058004
   {:match-id 2058004,
    :label "Spain - Russia, 1 - 1",
    :filename "spain_russia,_1_1_(_p).edn"},
   :2057962
   {:match-id 2057962,
    :label "Portugal - Morocco, 1 - 0",
    :filename "portugal_morocco,_1_0.edn"},
   :2057954
   {:match-id 2057954,
    :label "Russia - Saudi Arabia, 5 - 0",
    :filename "russia_saudi_arabia,_5_0.edn"},
   :2057995
   {:match-id 2057995,
    :label "Panama - Tunisia, 1 - 2",
    :filename "panama_tunisia,_1_2.edn"},
   :2057993
   {:match-id 2057993,
    :label "England - Panama, 6 - 1",
    :filename "england_panama,_6_1.edn"},
   :2057984
   {:match-id 2057984,
    :label "Germany - Mexico, 0 - 1",
    :filename "germany_mexico,_0_1.edn"},
   :2057996
   {:match-id 2057996,
    :label "Poland - Senegal, 1 - 2",
    :filename "poland_senegal,_1_2.edn"},
   :2058007
   {:match-id 2058007,
    :label "Belgium - Japan, 3 - 2",
    :filename "belgium_japan,_3_2.edn"},
   :2057974
   {:match-id 2057974,
    :label "Argentina - Croatia, 0 - 3",
    :filename "argentina_croatia,_0_3.edn"},
   :2057978
   {:match-id 2057978,
    :label "Brazil - Switzerland, 1 - 1",
    :filename "brazil_switzerland,_1_1.edn"},
   :2057981
   {:match-id 2057981,
    :label "Serbia - Switzerland, 1 - 2",
    :filename "serbia_switzerland,_1_2.edn"},
   :2058013
   {:match-id 2058013,
    :label "Sweden - England, 0 - 2",
    :filename "sweden_england,_0_2.edn"},
   :2058003
   {:match-id 2058003,
    :label "France - Argentina, 4 - 3",
    :filename "france_argentina,_4_3.edn"},
   :2058001
   {:match-id 2058001,
    :label "Senegal - Colombia, 0 - 1",
    :filename "senegal_colombia,_0_1.edn"},
   :2057975
   {:match-id 2057975,
    :label "Nigeria - Iceland, 2 - 0",
    :filename "nigeria_iceland,_2_0.edn"},
   :2058009
   {:match-id 2058009,
    :label "Colombia - England, 1 - 1",
    :filename "colombia_england,_1_1_(_p).edn"},
   :2057991
   {:match-id 2057991,
    :label "Tunisia - England, 1 - 2",
    :filename "tunisia_england,_1_2.edn"},
   :2057973
   {:match-id 2057973,
    :label "Croatia - Nigeria, 2 - 0",
    :filename "croatia_nigeria,_2_0.edn"},
   :2058017
   {:match-id 2058017,
    :label "France - Croatia, 4 - 2",
    :filename "france_croatia,_4_2.edn"},
   :2057998
   {:match-id 2057998,
    :label "Poland - Colombia, 0 - 3",
    :filename "poland_colombia,_0_3.edn"},
   :2057983
   {:match-id 2057983,
    :label "Switzerland - Costa Rica, 2 - 2",
    :filename "switzerland_costa_rica,_2_2.edn"},
   :2057961
   {:match-id 2057961,
    :label "Morocco - Iran, 0 - 1",
    :filename "morocco_iran,_0_1.edn"},
   :2057992
   {:match-id 2057992,
    :label "Belgium - Tunisia, 5 - 2",
    :filename "belgium_tunisia,_5_2.edn"},
   :2057956
   {:match-id 2057956,
    :label "Russia - Egypt, 3 - 1",
    :filename "russia_egypt,_3_1.edn"},
   :2057958
   {:match-id 2057958,
    :label "Uruguay - Russia, 3 - 0",
    :filename "uruguay_russia,_3_0.edn"},
   :2058005
   {:match-id 2058005,
    :label "Croatia - Denmark, 1 - 1",
    :filename "croatia_denmark,_1_1_(_p).edn"}})



