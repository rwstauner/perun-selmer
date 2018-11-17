(ns perun-selmer.core-test
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [clojure.test :refer :all]
            [boot.core :as boot :refer [deftask]]
            [boot.test :refer :all]
            [perun-selmer.core :refer :all]))
(defn clj?
  [f]
  (string/ends-with? f ".clj"))

(defn fs-paths [fs]
  (->> fs
       (boot/ls)
       (map boot/tmp-path)
       (remove clj?)
       sort))

(defn fs-contents [fs]
  (->> fs
       (boot/ls)
       (remove (comp clj? boot/tmp-path))
       (sort-by boot/tmp-path)
       (map boot/tmp-file)
       (map slurp)))

(def template
"---
stuff:
  - a
  - list
---

list:
{% for item in stuff %}
= {{ item }}
{% endfor %}
")

(def processed
"
list:

= a

= list

")

(deftask populate
  []
  (boot/with-pre-wrap fileset
    (let [tmp (boot/tmp-dir!)
          files {"foo.txt" "foo" "bar.selmer" template}]
      (doseq [[file content] files]
        (-> file
            (->> (io/file tmp))
            (doto io/make-parents)
            (spit content)))
      (-> fileset
          (boot/add-resource tmp)
          boot/commit!))))

(deftask expect
  [_ paths VAL [str] "expected paths"
   _ contents VAL [str] "expected contents"]
  (boot/with-pass-thru fs
    (testing "file paths"
      (is (= paths (fs-paths fs)))
    (testing "file contents"
      (is (= contents (fs-contents fs)))))))

(deftesttask no-op []
  "Without changes"
  (comp (populate)
        (expect :paths ["bar.selmer" "foo.txt"]
                :contents [template "foo"])))

(deftesttask defaults []
  "convert selmer to html"
  (comp (populate)
        (selmer)
        (expect :paths ["foo.txt" "public/bar.html"]
                :contents ["foo" processed])))
