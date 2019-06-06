(ns cljs-siligong-2019.show
  (:require [reagent.core :as reagent]
            [jobim.core :as jobim
             :refer-macros [defshow]]
            [fipp.clojure :refer [pprint]]
            [cljs-siligong-2019.eval :as eval]
            [jobim.core.impl :as impl]
            [cljs.core.async :refer [put! chan >! <!]]
            [jobim.protocols :as protocols]))

;; setup for talk

;; with cursor in this file, run M-x cider-jack-in-clojurescript

;; open http://localhost:3449 in chrome

;; maximize chrome

;; bump font size on this and repl

;; full screen emacs

;; navigate to clj-block

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; immutable performance

(defn timed [f]
  (let [t0 (.now js/performance)]
    (f)
    (- (.now js/performance) t0)))

(defn copy-100 [n]
  (let [a (atom (vec (range n)))]
    (doseq [i (range 100)]
      (swap! a conj (+ n i)))))

(defn run-test [m k]
  (map (fn [n] (timed #(copy-100 n)))
       (take k (iterate (fn [x] (* x m)) 10))))

;; Results
;;
;; js copy 5, 10
;; [ 0, 0, 0, 1, 8, 50, 298, 1419, 5547, 29524 ]

;; js mutate 5 10
;; [ 0, 0, 0, 1, 1, 2, 5, 13, 41, 214 ]

;; cljs copy 5 10
;; [1 1 1 1 2 3 9 20 104 523]

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defonce state
  (reagent/atom
   {:page 0}))

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
;; Bullet Slide

(defn render-bullet [index cand-bullet num-visible]
  [:li {:class "jobim-li"
        :style (if (<= num-visible index)
                 {:visibility "hidden"}
                 {})}
   cand-bullet])

(defn render-bullets [bullets num-visible list-style-type]
  (into [:ul {:class "jobim-ul"
              :style {:list-style-type list-style-type}}]
        (map-indexed #(render-bullet %1 %2 num-visible)) bullets))

(defrecord BulletSlide [title bullets opts]
  protocols/Slide

  (render-slide [this state]
    (let [bullet-no  (get-in state [:slides title] 0)
          list-style (get opts :list-style-type "circle")]
      [:div
       [:h3 {:style {:text-align "center"}
             :class "jobim-list-title"}
        title]
       (render-bullets bullets bullet-no list-style)]))

  (next-slide [this state]
    (let [bullet-no (get-in state [:slides title] 0)]
      (if (= bullet-no (count bullets))
        (protocols/std-next this state)
        (update-in state [:slides title] inc))))

  (prev-slide [this state]
    (let [bullet-no (get-in state [:slides title] 0)]
      (if (= bullet-no 0)
        (protocols/std-prev this state)
        (update-in state [:slides title] dec)))))

(defn bullet-slide [title bullets opts]
  (->BulletSlide title bullets opts))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Code Blocks

(defn pprint-clj [s]
  (with-out-str (pprint s {:width 40})))

(defn highlight-block [lang s]
  (str "<pre><code>"
       (.-value (js/hljs.highlight lang s))
       "</code></pre>"))

(defn js-block [js-string]
  [:pre
   [:code
    {:class "javascript"
     :style {:font-size "1.2em"}
     :dangerouslySetInnerHTML
     #js{:__html (highlight-block "javascript" js-string)}}]])

(defn render-clj-and-result [code result]
  (let [result-s (if result
                   (str " ;; => " (pprint-clj result))
                   "")
        code-s (apply str (drop-last (pprint-clj code)))]
    (str code-s result-s)))

(defn clj-line [code & [result]]
  [:div
   {:style {:text-align     "left"
            :font-size      "1.2em"
            :padding-bottom "10px"}}
   [:pre
    [:code
     {:class "clj"
      :dangerouslySetInnerHTML
      #js{:__html (highlight-block "clj" (render-clj-and-result code result))}}]]])

(defn clj-block [codes]
  [:div {:style {:text-align     "left"
                 :font-size      "1.2em"
                 :padding-bottom "10px"}}
   (for [[s i] (zipmap codes (range (count codes)))]
     [:pre
      [:code
       {:class                   "clj"
        :dangerouslySetInnerHTML #js{:__html (highlight-block "clj" (pprint-clj s))}}]])])

(defn compare-page [block1 block2]
  (jobim/text
   [:div
    block1
    [:br]
    [:hr]
    [:br]
    block2]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The show
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defshow myshow
  state
  style

  (jobim/title
   "Clojurescript for javascript ppl"
   [:div
    [:p "Anthony Marcar"]
    [:p "@moocar"]])

  (bullet-slide
   "Clojure"
   ["Released 2007"
    "It's a Lisp"
    "Functional"
    "Immutable"
    "Dynamic"
    "Targets the JVM"]
   {})

  (bullet-slide
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

  (bullet-slide
   "Symbols"
   [(clj-block ['(def a?9-3+l "foobar")])
    (clj-block ['(def some-long-name "blah blah blah")])
    (clj-block ['(defn foo? (fn [x] true))])
    (clj-block ['(defn foo->bar (fn [foo] "bar"))])]
   {:list-style-type "none"})

  (jobim/text "Datastructures")

  (bullet-slide
   "Vectors"
   ["Like js arrays. Fast random access"
    [:br]
    (clj-line '(def v [1 2 3]))
    (clj-line '(def v [1 5.6 "foo"]))
    (clj-line '(nth v 2) '"foo")
    (clj-line '(conj v "bar") '[1 5.6 "foo" "bar"])]
   {:list-style-type "none"})

  (bullet-slide
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

  (bullet-slide
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

  (bullet-slide
   "Sets"
   ["Like js Sets but unordered"
    [:br]
    (clj-block ['(def s1 #{1 3 5 6})])
    (clj-block ['(def s2 #{5 6 8 9})])
    (clj-line '(set/union s1 s2) '#{1 6 3 5 9 8})
    (clj-line '(set/intersection s1 s2) '#{6 5})
    (clj-line '(#{1 2} 1) '1)
    (clj-line '(filter #{1 2} [0 1 2 3]) '(1 2))]
   {:list-style-type "none"})

  (bullet-slide
   "Immutability"
   ["Everything is a value"
    "Calling functions is safe"
    "Testing is more deterministic (pure functions)"
    "Another routine can't change your data"
    "So how do I get shit done?"]
   {})

  (bullet-slide
   "Atoms (like Redux)"
   [(clj-line '(def a (atom 1)))
    (clj-line '(reset! a 5) '5)
    (clj-line '@a '5)
    (clj-line '(swap! a inc) '6)
    (clj-line '@a '6)
    (clj-line '(swap! a (fn [x] (* x x))) '36)]
   {})

  (bullet-slide
   "Isn't Immutability slow?"
   ["No"
    "Because Shared-persistent datastructures"]
   {:list-style-type "none"})

  (jobim/captioned-img
   "/img/clojure-persistent-data-structures-sharing.png"
   "https://practicalli.github.io/")

  (jobim/code* "js"
   ["function mutate100(n) {"
    "  var a = [...Array(n).keys()]"
    "  for (var i = 0; i < 100; i++) {"
    "    a.push(n + i)"
    "  }"
    "}"])

  (jobim/pseudo-clj
   40
   (defn copy-100 [n]
     (let [a (atom (vec (range n)))]
       (doseq [i (range 100)]
         (swap! a conj (+ n i))))))

  (jobim/captioned-img
   "/img/js-vs-cljs-vector-performance.svg"
   "")

  (jobim/code*
   "js"
   ["function copy100(n) {"
    "  var a = [...Array(n).keys()]"
    "  for (var i = 0; i < 100; i++) {"
    "    a = [...a, n + i]"
    "  }"
    "}"])

  (jobim/captioned-img
   "/img/js-vs-cljs-vector-performance-with-js-copy.svg"
   "")

  (bullet-slide
   "Equality"
   ["42 === 42, \"foo\" === \"foo\""
    [:s "{a: 1} === {a: 1}"]
    (clj-line '(= {:a 1} {:a 2}) 'true)
    [:span [:br]
     "O(1) Deep Equality checks"]]
   {:list-style-type "none"})

  (jobim/pseudo-clj
   40
   (def my-key
     {:customerId "c1"
      :orderId    "o1"})
   (def m {my-key "someVal"})
   (get m my-key))

  (bullet-slide
   "Clojurescript ♥ React"
   ["React is data => UI"
    "Functional"
    "Auto shouldComponentUpdate"
    "Better Performance than React"]
   {})

  (jobim/bullets
   "React Frameworks"
   ["reagent"
    "reframe"
    "om"
    "fulcro"
    "rum"
    "quiescent"])

  (jobim/text "Bundle Size")

  (jobim/captioned-img
   "/img/web-framework-size-comparison.png"
   "https://www.freecodecamp.org/news/a-realworld-comparison-of-front-end-frameworks-with-benchmarks-2019-update-4be0d3c78075/")

  (jobim/captioned-img
   "/img/web-framework-loc-comparison.png"
   "https://www.freecodecamp.org/news/a-realworld-comparison-of-front-end-frameworks-with-benchmarks-2019-update-4be0d3c78075/")

  (bullet-slide
   "Use clojurescript if"
   ["You're building complex webapps (not sites)"
    "Hate boilerplate"
    "Insanely fast feedback loop, yeah!"
    "Lots of data processing"
    "Favor stability"]
   {})

  (jobim/text "the-end))))))))))))))))
)))))))))))))))))))))))
)))))))))))))))))))))))
)))))))))))))))))))))))
)))))))))))))))))))))))
)))))))))))))))))))))))
)))))))))))))))))))))))
)))))))))))))))))))))))
)))))))))))))))))))))))
)))))))))))))))))))))))
")
)
