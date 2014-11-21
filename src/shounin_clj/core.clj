(ns shounin-clj.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.data.json :as json]
            [org.httpkit.client :as http]
            )
  (:gen-class))


(defn exit-with
  [st & funcs]
  (doseq [f funcs] (f))
  (System/exit st)
  )


(def shounin-options
  [
   ["-l" "--list" "list of vocabularies"
    :default false]
   ["-a" "--add WORD" "add a new vocabulary"]
   ["-d" "--delete INDEX" "delete a vocabulary"]
   ["-h" "--help"]])


(use '[clojure.string :only (join)])
(defn show-summary [options-summary]
  (->> ["API Client for 承認君(automatic shounin system)"
        ""
        "Usage: program-name [options]"
        ""
        "Options:"
        options-summary
        ]
       (join \newline)))


(defn get-vocabularies []
  (let [response1 (http/get "http://www.shounin.jp/vocabularies")]
    (for [vocabulary
          (get (json/read-str (:body @response1)) "vocabularies")]
      (when-not (= vocabulary nil) (format "%s: %s" (get vocabulary 0) (get vocabulary 1))))))


(defn add-new-vocabulary [word]
  (let [response (http/post "http://www.shounin.jp/vocabularies" {:form-params {:text word}})]
    (let [{:keys [body]} @response]
      (get (json/read-str body) "message"))))


(defn delete-vocabulary [index]
  (let [response (http/delete (str "http://www.shounin.jp/vocabularies/" index))]
    (let [{:keys [body]} @response]
      (get (json/read-str body) "message"))))


(defn -main [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args shounin-options)]
    (cond
      (= (count options) 0) (exit-with 1 (fn [] (println "no arguments")))
      (:help options) (exit-with 0 #(-> summary show-summary println))
      (:list options) (exit-with 0 #(println (join "\n" (get-vocabularies))))
      (:add options) (exit-with 0 #(println (add-new-vocabulary (:add options))))
      (:delete options) (exit-with 0 #(println (delete-vocabulary (:delete options))))
      )))
