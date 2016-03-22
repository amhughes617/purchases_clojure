(ns purchases-clojure.core
    (:require [clojure.string :as str]
            [clojure.walk :as walk])
  (:gen-class))

(def categories (atom []))

(defn -main []
  (loop []
    (println "Pick a number between 1 and 6. Type q to quit.")
    (let [input (read-line)]
      (when (not= input "q")
        (let [purchases (slurp "purchases.csv")
              purchases (str/split-lines purchases)
              purchases (map (fn [line]
                              (str/split line #","))
                          purchases)
              header (first purchases)
              purchases (rest purchases)
              purchases (map (fn [line]
                               (apply hash-map (interleave header line)))
                          purchases)
              purchases (walk/keywordize-keys purchases)
              _ (when (empty? @categories) (reset! categories (vec (set (map :category purchases)))))  
              cat-choice (get @categories (- (Integer/valueOf input) 1))
              purchases (filter (fn [line]
                                 (= (:category line) cat-choice))
                         purchases)]
          
          (spit (str cat-choice "_purchases.edn") (pr-str purchases))
          (println purchases)
          (recur))))))