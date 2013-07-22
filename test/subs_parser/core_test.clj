(ns subs-parser.core-test
  (:require [instaparse.core :as insta]
            [clojure.test :refer :all]
            [subs-parser.core :refer :all]
            [clojure.pprint :refer :all]))

(def inputs
  [[:base
    "
User 5712 - John Doe
BEFORE
 foo1  - 2013-09-26 13:44
AFTER
 foo1  - 2013-09-26 13:44

"
    [:content
     [:user-block
      [:user [:id "5712"] [:name "John Doe"]]
      [:before-section
       [:subscription "foo1" "  - "]
       [:date "2013-09-26 13:44"]]
      [:after-section
       [:subscription "foo1" "  - "]
       [:date "2013-09-26 13:44"]]]]]
   [:two-sections
    "
User 5712 - John Doe
BEFORE
 foo1  - 2013-09-26 13:44
AFTER
 foo1  - 2013-09-26 13:44
User 8960 - John Doe
BEFORE
 foo2 blah  - 2013-12-30 16:00
  foo1  - 2013-12-30 16:00
  foo3 baz  - 2013-12-30 16:00
AFTER
 foo44444  - 2013-12-30 16:00
"
    [:content
     [:user-block
      [:user [:id "5712"] [:name "John Doe"]]
      [:before-section
       [:subscription "foo1" "  - "]
       [:date "2013-09-26 13:44"]]
      [:after-section
       [:subscription "foo1" "  - "]
       [:date "2013-09-26 13:44"]]]
     [:user-block
      [:user [:id "8960"] [:name "John Doe"]]
      [:before-section
       [:subscription "foo2 blah" "  - "]
       [:date "2013-12-30 16:00"]
       [:subscription "foo1" "  - "]
       [:date "2013-12-30 16:00"]
       [:subscription "foo3 baz" "  - "]
       [:date "2013-12-30 16:00"]]
      [:after-section
       [:subscription "foo44444" "  - "]
       [:date "2013-12-30 16:00"]]]]]
   [:with-error
    "
User 408 - Matt McLoughlan
BEFORE
 Agronomist Subscription  - 2014-02-21 11:35
  Farm Planning- Consultant Subscription  - 2014-02-21 11:35
  Device Sync Subscription  - 2014-02-21 11:35
  Farm Planning- Farmer Subscription  -
FIXUP!
AFTER
 Farm Planning- Farmer Subscription  -
  Agronomist Mobile + Planning Subscription  - 2014-02-21 11:35
"
    [:content
     [:user-block
      [:user [:id "408"] [:name "Matt McLoughlan"]]
      [:before-section
       [:subscription "Agronomist Subscription" "  - "]
       [:date "2014-02-21 11:35"]
       [:subscription "Farm Planning- Consultant Subscription" "  - "]
       [:date "2014-02-21 11:35"]
       [:subscription "Device Sync Subscription" "  - "]
       [:date "2014-02-21 11:35"]
       [:subscription "Farm Planning- Farmer Subscription" "  -"]
       [:error-line "FIXUP!"]]
      [:after-section
       [:subscription "Farm Planning- Farmer Subscription" "  -"]
       [:subscription "Agronomist Mobile + Planning Subscription" "  - "]
       [:date "2014-02-21 11:35"]]]]

    ]
   [:with-dashes-in-subscription
    "
User 8960 - John Doe
BEFORE
 foo2 blah  - 2013-12-30 16:00
  foo1  - 2013-12-30 16:00
  foo3 baz - with delimiter - 2013-12-30 16:00
AFTER
 foo44444  - 2013-12-30 16:00
"
    [:content
     [:user-block
      [:user [:id "8960"] [:name "John Doe"]]
      [:before-section
       [:subscription "foo2 blah" "  - "]
       [:date "2013-12-30 16:00"]
       [:subscription "foo1" "  - "]
       [:date "2013-12-30 16:00"]
       [:subscription "foo3 baz" " - " "with delimiter" " - "]
       [:date "2013-12-30 16:00"]]
      [:after-section
       [:subscription "foo44444" "  - "]
       [:date "2013-12-30 16:00"]]]]

    ]
   [:big-input
    (slurp (clojure.java.io/resource "biggish-sub-input.txt"))
    (read (java.io.PushbackReader.
           (clojure.java.io/reader
            (clojure.java.io/resource "biggish-sub-input.edn"))))]])

(deftest parsing-subscriptions-test
  (doseq [[name str expected] inputs]
    (testing name
      (is (= (sub-parser str)
             expected)))))

(deftest transform-options-test
  (is (= (insta/transform transform-options
                        [:content
                         [:user-block
                          [:user [:id "5712"] [:name "John Doe"]]
                          [:before-section
                           [:subscription "foo1" "  - "]
                           [:date "2013-09-26 13:44"]]
                          [:after-section
                           [:subscription "foo1" "  - "]
                           [:date "2013-09-26 13:44"]]]])
         [:content
          [:user-block
           [:user 5712 [:name "John Doe"]]
           [:before-section "foo1" #inst "2013-09-26T00:13:44.000-00:00"]
           [:after-section "foo1" #inst "2013-09-26T00:13:44.000-00:00"]]])))