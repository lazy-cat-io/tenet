(ns build
  (:require
   [clojure.tools.build.api :as b]
   [deps-deploy.deps-deploy :as d]))

(def lib 'io.lazy-cat/tenet)
(def class-dir "target/classes")
(def basis (delay (b/create-basis {:project "deps.edn"})))
(def jar-file "target/tenet.jar")
(def src-dirs ["src/main/clojure" "src/main/resources"])

(defn jar
  [{:keys [version]}]
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

(defn install
  [_]
  (d/deploy {:installer :local
             :artifact jar-file
             :pom-file (b/pom-path {:lib lib, :class-dir class-dir})}))

(defn deploy
  [_]
  (d/deploy {:installer :remote
             :artifact jar-file
             :pom-file (b/pom-path {:lib lib, :class-dir class-dir})}))
