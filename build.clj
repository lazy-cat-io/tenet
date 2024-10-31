(ns build
  (:require
   [clojure.string :as str]
   [clojure.tools.build.api :as b]))

(def lib 'io.lazy-cat/tenet)
(def version (-> "version" (slurp) (str/trim-newline)))
(def class-dir "target/classes")
(def basis (delay (b/create-basis {:project "deps.edn"})))
(def jar-file "target/tenet.jar")
(def src-dirs ["src/main/clojure" "src/main/resources"])

(defn jar [_]
  (println "Writing pom.xml...")
  (b/write-pom {:class-dir class-dir
                :lib lib
                :version version
                :basis @basis
                :src-dirs src-dirs})
  (println "Copying sources...")
  (b/copy-dir {:src-dirs src-dirs
               :target-dir class-dir})
  (println "Building jar...")
  (b/jar {:class-dir class-dir
          :jar-file jar-file})
  (println "Done..."))

