(ns cljs-siligong-2019.show
  (:require [reagent.core :as reagent]
            [jobim.core :as jobim
             :refer-macros [defshow]]
            [fipp.clojure :refer [pprint]]
            [cljs-siligong-2019.eval :as eval]
            [jobim.core.impl :as impl]
            [cljs.core.async :refer [put! chan >! <!]]
            [jobim.protocols :as protocols]))

;; with cursor in this file, run M-x cider-jack-in-clojurescript

(defonce state
  (reagent/atom
   {:page 0
    :repls {:vectors {:input (str [1 2 3 4])}}
    :input (str '(+ 1 9))}))

(def style
  (merge
   jobim/default-style
   {:background-color "#222222"
    :color            "#EDEDED"
    :font-family      "Droid Sans Mono, monospace"
    :font-weight      "100"
    :font-size        "2em"
    }))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn render-bullet [index cand-bullet num-visible]
  [:li {:class "jobim-li"
        :style (if (<= num-visible index) {:visibility "hidden"} {})}
   cand-bullet])

(defn render-bullets [bullets num-visible]
  (into [:ul {:class "jobim-ul"}]
        (map-indexed #(render-bullet %1 %2 num-visible)) bullets))

(defrecord MySlide [title bullets]
  protocols/Slide

  (render-slide [this state]
    (let [bullet-no (get-in state [:custom title] 0)]
      [:div
       [:h3 {:style {:text-align "center"}
             :class "jobim-list-title"}
        title]
       (render-bullets bullets bullet-no)]))

  (next-slide [this state]
    (let [bullet-no (get-in state [:custom title] 0)]
      (if (= bullet-no (count bullets))
        (protocols/std-next this state)
        (update-in state [:custom title] inc))))

  (prev-slide [this state]
    (let [bullet-no (get-in state [:custom title] 0)]
      (if (= bullet-no 0)
        (protocols/std-prev this state)
        (update-in state [:custom title] dec)))))

(defn my-bullets [title bullets]
  (->MySlide title bullets))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn js-block [js-string]
  [:pre
   [:code
    {:class "javascript"
     :dangerouslySetInnerHTML
     #js{:__html (str "<pre><code>"
                      (.-value (js/hljs.highlight "javascript" js-string))
                      "</code></pre>")}}]])
(defn clj-block [codes]
  [:div
   {:style {:text-align "left"}}
   (for [[s i] (zipmap codes (range (count codes)))]
     [:pre
      [:code
       {:class "clj"
        :dangerouslySetInnerHTML
        #js{:__html (str "<pre><code>"
                         (do (prn s)
                             (.-value (js/hljs.highlight "clj" (with-out-str (pprint s {:width 40})))))
                         "</code></pre>")}}]])])

(defn compare-page [block1 block2]
  (jobim/text
   [:div
    block1
    [:br]
    [:hr]
    [:br]
    block2]))

(defshow myshow
  state
  style
  (jobim/title "Intro to Clojurescript" "Anthony Marcar")
  (my-bullets
   "Clojure"
   ["Released 2007"
    "It's a Lisp"
    "Functional"
    "Immutable"
    "Targets the JVM"])
  (my-bullets
   "Clojurescript"
   ["Released 2012"
    "Same language as Clojure, but"
    "Targets javascript"
    "Will use interchangeably"])
  (jobim/text "(Lisp)")
  (jobim/captioned-img "/img/48-John-McCarthy-AP.jpg" "John McCarthy (1927 - 2011)")
  (jobim/captioned-img "/img/programming-languages-graph.png" "Lisp invented 1958")
  (compare-page
   (js-block "foo(\"bar\", \"baz\")")
   (clj-block ['(foo "bar" "baz")]))
  (compare-page
   (js-block "foo.bar(\"baz\")")
   (clj-block ['(.bar foo "baz")]))
  (jobim/text
   [:div
    [:h1 "symbols"]
    (clj-block ['foo 'foo? 'some-long-name 'foo->bar 'foo+bar '+])])
  (jobim/text "Datastructures")
  (jobim/code*
   "javascript"
   "foo.bar(\"baz\")"))
