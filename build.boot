(def project 'perun-selmer)
(def version "0.1.0")

(set-env! :resource-paths #{"src"}
          :source-paths   #{"test"}
          :dependencies   '[[org.clojure/clojure "1.8.0" :scope "provided"]
                            [boot/core "2.8.2" :scope "provided"]
                            [perun  "0.4.2-SNAPSHOT"]
                            [adzerk/bootlaces "0.1.13" :scope "test"]])

(require 'perun-selmer.core)
(def pod-deps
  (->> (ns-interns 'perun-selmer.core)
       vals
       (filter #(:deps (meta %)))
       (map deref)
       (reduce concat)
       (map #(conj % :scope "test"))))

(set-env! :dependencies #(into % pod-deps))

(ns-unmap 'boot.user 'test)

(require '[adzerk.bootlaces :refer :all]
         '[boot.test :refer [runtests test-report test-exit]]
          'perun-selmer.core-test)

(bootlaces! version)

(task-options!
 pom {:project     project
      :version     version
      :description "Perun boot task to process selmer templates"
      :url         "https://github.com/rwstauner/perun-selmer"
      :scm         {:url "https://github.com/rwstauner/perun-selmer"}
      :license     {"Eclipse Public License"
                    "http://www.eclipse.org/legal/epl-v10.html"}})

(deftask test []
  (comp (runtests)
        (test-report)
        (test-exit)))
