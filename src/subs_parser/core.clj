(ns subs-parser.core
  (:require [instaparse.core :as insta]
            [clj-time.format :as format]
            [clj-time.coerce :as coerce])
  (:use clojure.pprint))

(def transform-options
  {:id read-string
   :date #(and (seq %)
               (coerce/to-date (format/parse (format/formatter "yy-MM-dd mm:ss") %)))
   :subscription (fn [& rest] (apply str (butlast rest)))})

(def sub-parser
  "parser of blocks, see tests for examples"
  (insta/parser
   "content = < blank-line* > user-block*
    user-block = (user before-section after-section < blank-line* >)
    user = < prefix separator > id < separator > name < newline >
    before-section = < before > lines error-line*
    after-section = < after > lines
    before = < 'BEFORE' newline >
    after = < 'AFTER' newline >
    prefix = < 'User' >
    <lines> = line*
    <line> = < #'\\s+' > subscription date* < newline >
    error-line = ( '(no dates!)' | 'FIXUP!' ) < newline >
    blank-line = #'\\s*\n'
    name = #'.*'
    subscription = ( #'.*?(?=\\s+-)' separator ) +
    date = #'\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}'
    newline = <'\n'>
    <separator> = #'[ -]+'
    id = #'[0-9]+'

"))

;; (pprint (sub-parser (slurp "/Users/lewang/src/my.agworld/input_small_sub_with_delim.txt")))
;;         ;;; ⇒ nil
;;         ;;; ⇒ <out>
;;         ;;;   [:content
;;         ;;;    [:user-block
;;         ;;;     [:user [:id "5712"] [:name "John Doe"]]
;;         ;;;     [:before-section [:subscription "foo1"] [:date "2013-09-26 13:44"]]
;;         ;;;     [:after-section [:subscription "foo1"] [:date "2013-09-26 13:44"]]]
;;         ;;;    [:user-block
;;         ;;;     [:user [:id "8960"] [:name "John Doe"]]
;;         ;;;     [:before-section
;;         ;;;      [:subscription "foo2 blah"]
;;         ;;;      [:date "2013-12-30 16:00"]
;;         ;;;      [:subscription "foo1"]
;;         ;;;      [:date "2013-12-30 16:00"]
;;         ;;;      [:subscription "foo3 baz" "with delimiter"]
;;         ;;;      [:date "2013-12-30 16:00"]]
;;         ;;;     [:after-section
;;         ;;;      [:subscription "foo44444"]
;;         ;;;      [:date "2013-12-30 16:00"]]]]

;; (pprint (->> (sub-parser (slurp "/Users/lewang/src/my.agworld/input_small_sub_with_delim.txt"))
;;              (insta/transform transform-options)))
;;         ;;; ⇒ nil
;;         ;;; ⇒ <out>
;;         ;;;   [:content
;;         ;;;    [:user-block
;;         ;;;     [:user 5712 [:name "John Doe"]]
;;         ;;;     [:before-section "foo1" #<DateTime 2013-09-26T00:13:44.000Z>]
;;         ;;;     [:after-section "foo1" #<DateTime 2013-09-26T00:13:44.000Z>]]
;;         ;;;    [:user-block
;;         ;;;     [:user 8960 [:name "John Doe"]]
;;         ;;;     [:before-section
;;         ;;;      "foo2 blah"
;;         ;;;      #<DateTime 2013-12-30T00:16:00.000Z>
;;         ;;;      "foo1"
;;         ;;;      #<DateTime 2013-12-30T00:16:00.000Z>
;;         ;;;      "foo3 baz - with delimiter"
;;         ;;;      #<DateTime 2013-12-30T00:16:00.000Z>]
;;         ;;;     [:after-section "foo44444" #<DateTime 2013-12-30T00:16:00.000Z>]]]

