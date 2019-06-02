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

(defn render-bullets [bullets num-visible list-style-type]
  (into [:ul {:class "jobim-ul"
              :style {:list-style-type list-style-type}}]
        (map-indexed #(render-bullet %1 %2 num-visible)) bullets))

(defrecord MySlide [title bullets opts]
  protocols/Slide

  (render-slide [this state]
    (let [bullet-no (get-in state [:custom title] 0)
          list-style (get opts :list-style-type "circle")]
      [:div
       [:h3 {:style {:text-align "center"}
             :class "jobim-list-title"}
        title]
       (render-bullets bullets bullet-no list-style)]))

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

(defn my-bullets [title bullets opts]
  (->MySlide title bullets opts))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn js-block [js-string]
  [:pre
   [:code
    {:class "javascript"
     :style {:font-size "1.2em"}
     :dangerouslySetInnerHTML
     #js{:__html (str "<pre><code>"
                      (.-value (js/hljs.highlight "javascript" js-string))
                      "</code></pre>")}}]])

(defn clj-line [code & [result]]
  [:div
   {:style {:text-align "left"
            :font-size "1.2em"
            :padding-bottom "10px"}}
   [:pre
    [:code
     {:class "clj"
      :dangerouslySetInnerHTML
      #js{:__html (str "<pre><code>"
                       (let [result-s (if result (str " ;; => " (with-out-str (pprint result {:width 40}))) "")
                             main (apply str (drop-last (with-out-str (pprint code {:width 40}))))]
                         (.-value (js/hljs.highlight "clj" (str main result-s))))
                       "</code></pre>")}}]]])

(defn clj-block [codes]
  [:div
   {:style {:text-align "left"
            :font-size "1.2em"
            :padding-bottom "10px"}}
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
    "Dynamic"
    "Targets the JVM"]
   {})

  (my-bullets
   "Clojurescript"
   ["Released 2012"
    "Same language as Clojure, but"
    "Targets javascript"
    "Will use Clojure(script) interchangeably"]
   {})

  (jobim/text "(Lisp)")

  ;; One of the founding fathers of AI. Coined term
  ;; Also Invented Recursion, and Conditional Expressions
  ;; Invented Garbage Collection
  (jobim/captioned-img "/img/48-John-McCarthy-AP.jpg" "John McCarthy (1927 - 2011)")

  ;; Other Lisps you may have heard of are Scheme, Racket and Common Lisp
  (jobim/captioned-img "/img/programming-languages-graph.png" "Lisp invented 1958")

  (jobim/text "Syntax")

  (compare-page
   (js-block "foo(\"bar\", \"baz\")")
   (clj-block ['(foo "bar" "baz")]))

  (compare-page
   (js-block "foo.bar(\"baz\")")
   (clj-block ['(.bar foo "baz")]))

  (my-bullets
   "Symbols"
   [(clj-block ['(def a?9-3+l "foobar")])
    (clj-block ['(def some-long-name "blah blah blah")])
    (clj-block ['(defn foo? (fn [x] true))])
    (clj-block ['(defn foo->bar (fn [foo] "bar"))])]
   {:list-style-type "none"})

  (jobim/text "Datastructures")

  (my-bullets
   "Vectors"
   ["Like js arrays. Fast random access"
    [:br]
    (clj-line '(def v [1 2 3]))
    (clj-line '(def v [1 5.6 "foo"]))
    (clj-line '(nth v 2) '"foo")
    (clj-line '(conj v "bar") '[1 5.6 "foo" "bar"])]
   {:list-style-type "none"})

  (my-bullets
   "Lists"
   ["Singly-linked lists"
    [:br]
    (clj-line '(def l '(1 2 3)))
    (clj-line '(conj l 4) '(4 1 2 3))
    (clj-line '(def codes '(if x 3 4)))
    (clj-line '(conj codes 'wtf) '(wtf if x 3 4))
    [:span [:br]
     "Code is Data"]
    ]
   {:list-style-type "none"})

  (my-bullets
   "Maps"
   ["Like js Maps. unordered"
    [:br]
    (clj-block ['(def m {"foo" "bar"})])
    (clj-line '(get m "foo") '"bar")
    (clj-block ['(def m {:foo "bar"})])
    (clj-line '(:foo m) '"bar")
    (clj-block ['(def ms [{:a 1} {:a 2} {:a 3}])])
    (clj-line '(map :a ms) '(1 2 3))]
   {:list-style-type "none"})

  (my-bullets
   "Sets"
   ["Like js Sets but unordered"
    [:br]
    (clj-block ['(def s1 #{1 3 5 6})])
    (clj-block ['(def s2 #{5 6 8 9})])
    (clj-line '(set/union s1 s2) '#{1 6 3 5 9 8})
    (clj-line '(set/intersection s1 s2) '#{6 5})]
   {:list-style-type "none"}))
