package com.darylteo.gradle.javassist.transforms

public class Transformer {
  public def dir

  public Transformer(def dir) {
    this.dir = dir
  }

  public def save(def clazz) {
    println "Saving $clazz.name to $dir"
    clazz.writeFile("$dir")
    return this
  }
}
