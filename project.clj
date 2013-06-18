(defproject io.zig/exception "0.0.1"
            :description "Code to serialize exceptions"
            :url "http://www.duramec.com"
            :license {:name "Copyright 2013 Duramec LLC"
                      :url "http://www.duramec.com"}
            :profiles {:dev {:plugins [[com.duramec/lein-scalac "0.1.1"]
                                       [com.duramec/lein-scalatest "0.0.2"]]
                             :scala-test-paths ["test/scala"]
                             :scala-version "2.10.1"}}
            :java-source-paths ["src/java"]
            :javac-options ["-target"   "1.7"
                            "-source"   "1.7"
                            "-Xlint:-options"]
            :omit-source true
            :dependencies [[io.zig/data "0.1.2"]])
