(ns perun-selmer.parser
  (:require [clojure.java.io :as io]
            [io.perun.core  :as perun]
            [selmer.parser :as selmer]))

(defn process [{:keys [entry]}]
  (perun/report-debug "selmer" "processing selmer" (:filename entry))
  (let [file-content (-> entry :full-path io/file slurp)
        context entry
        rendered (selmer/render file-content context)]
    (assoc entry :rendered rendered)))
