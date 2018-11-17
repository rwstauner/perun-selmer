(ns perun-selmer.core
  {:boot/export-tasks true}
  (:require [boot.core :as boot :refer [deftask]]
            [boot.pod :as pod]
            [clojure.java.io :as io]
            [clojure.string :as s]
            [io.perun :refer [content-paths content-passthru content-task yaml-metadata]]
            [io.perun.core :as perun]))

(defn- create-pod' [deps]
  (-> (boot/get-env)
      (update-in [:dependencies] into deps)
      pod/make-pod))

(defn- create-pod
  [deps]
  (future (create-pod' deps)))

(def ^:private ^:deps selmer-deps
  '[[org.clojure/tools.namespace "0.3.0-alpha3"]
    [perun  "0.4.2-SNAPSHOT"]
    [selmer "1.12.3"]])

(def ^:private +selmer-defaults+
  {:out-dir "public"
   :out-ext ".html"
   :extensions [".selmer"]
   :filterer identity
   :meta {:original true
          :include-rss true
          :include-atom true}})

(deftask selmer*
  "Parse selmer files

  This task will look for files ending with `.selmer`
  and writes a file that contain the result from
  processing the selmer file's content. It will _not_ parse
  YAML metadata at the head of the file."
  [d out-dir    OUTDIR str  "the output directory"
   x out-ext    OUTEXT str  "the output extension"
   _ filterer   FILTER code "predicate to use for selecting entries (default: `identity`)"
   e extensions EXTS   edn  "parsing extensions to be used by the selmer parser"
   m meta       META   edn  "metadata to set on each entry; keys here will be overridden by metadata in each file"]

  (let [pod (create-pod selmer-deps)
        options (merge +selmer-defaults+ *opts*)]
    (content-task
     {:render-form-fn (fn [data] `(perun-selmer.parser/process ~data))
      :paths-fn #(content-paths % options)
      :passthru-fn content-passthru
      :task-name "selmer"
      :tracer :io.perun/selmer
      :rm-originals true
      :pod pod})))

(deftask selmer
  "Parse selmer files with yaml front matter

  This task will look for files ending with `selmer`
  and writes a file that contains the result from
  processing the selmer file's content. It will parse YAML
  metadata at the head of the file, and add any data found to
  the output's metadata."
  [d out-dir    OUTDIR str  "the output directory"
   x out-ext    OUTEXT str  "the output extension"
   _ filterer   FILTER code "predicate to use for selecting entries (default: `identity`)"
   e extensions EXTS   edn  "parsing extensions to be used by the selmer parser"
   m meta       META   edn  "metadata to set on each entry; keys here will be overridden by metadata in each file"]
  (let [{:keys [out-dir
                out-ext
                filterer
                extensions
                meta]} (merge +selmer-defaults+ *opts*)]
    (comp (yaml-metadata :filterer filterer :extensions extensions)
          (selmer* :out-dir out-dir
                   :out-ext out-ext
                   :filterer filterer
                   :extensions extensions
                   :meta meta))))
