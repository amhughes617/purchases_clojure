(ns purchases-clojure.core
    (:require [clojure.string :as str]
              [clojure.walk :as walk]
              [compojure.core :as c]
              [ring.adapter.jetty :as j]
              [ring.middleware.params :as p]
              [hiccup.core :as h])
  (:gen-class))

(def categories (atom []))
(def purchases (atom []))

(defn read-purchases []
  (reset! purchases (let [purchases (slurp "purchases.csv")
                          purchases (str/split-lines purchases)
                          purchases (map (fn [line]
                                          (str/split line #","))
                                      purchases)
                          header (first purchases)
                          purchases (rest purchases)
                          purchases (map (fn [line]
                                           (apply hash-map (interleave header line)))
                                      purchases)
                          purchases (walk/keywordize-keys purchases)]
                         (reset! categories (vec (set (map :category purchases))))
                     purchases)))

(defn purchases-html [purchases]
  (let [purchases (reverse (sort-by :date purchases))] 
    [:table {:style "width:100%" :border "1"}
     [:tr (map (fn [key]
                 [:th key])
           (keys (first purchases)))]        
     (map (fn [purchase]
            [:tr (map (fn [value]
                       [:td value])
                  (vals purchase))])        
          purchases)]))

(defn category-html []
  [:div
    [:a {:href "/"} "All"]
    " "
    (map (fn [category]
           [:span
            [:a {:href (str "/filter?category=" category)} category]
            " "])
      (sort @categories))])
    
(c/defroutes app
  (c/GET "/" request
    (h/html [:html
             [:body
              (category-html)
              (purchases-html @purchases)]]))
  (c/GET "/filter" request
    (let [params (:params request)
          category (get params "category")
          filtered-purchases (filter (fn [purchase]
                                      (= (:category purchase) category))
                              @purchases)]
      (h/html [:html 
               [:body 
                (category-html)
                (purchases-html filtered-purchases)]]))))


(defn -main []
  (read-purchases)
  (j/run-jetty (p/wrap-params app) {:port 3000}))