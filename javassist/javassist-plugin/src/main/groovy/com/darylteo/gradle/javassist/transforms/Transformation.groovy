package com.darylteo.gradle.javassist.transforms

public class Transformation {
  def transforms = []

  def pattern = null
  def action = null

  public Transformation(def pattern, def action) {
    this.pattern = pattern
    this.action = action
  }

  public Transformation save() {
    this.add({ def c, dir ->
      println "Saving $c.name to $dir"
      c.writeFile(dir.toString())
    })
  }

  public Transformation create(String name) {
    this.add({ c, dir ->
      def pool = c.classPool
      return pool.makeClass(name)
    })
  }

  public Transformation edit(Closure action) {
    this.add({ c, dir -> action(c) })
  }

  def add(Closure action) {
    def transform = new Transformation(~/.*/, action)
    transforms.add transform

    return transform
  }

  def call(def c, def dir) {
    if(!c.name.matches(this.pattern)) {
      return
    }

    if(action) {
      action(c,dir)
    }

    transforms.each { t ->
      t(c, dir)
    }

    c.writeFile("$dir")
  }
}
