package com.darylteo.gradle.javassist.tasks

public class Transformation {
  def transforms = []

  def pattern = null
  def action = null

  public Transformation(def action) {
    this(null, action)
  }

  public Transformation(def pattern, def action) {
    this.pattern = pattern
    this.action = action
  }

  public Transformation write() {
    this.add({ c, dir ->
      c.writeFile("$dir")
    })
  }

  public Transformation edit(Closure action) {
    this.add({ c, dir ->
      action(c)
    })
  }

  def add(Closure action) {
    def transform = new Transformation(action)
    transforms.add transform

    return transform
  }

  def call(def classes, def dir) {
    def result = []

    classes.findAll ({ c ->
      return !this.pattern || c.name.matches(this.pattern)
    }).each { c ->
      result += action?.call(c,dir)
    }

    result = result.findAll()
    transforms*.call(result,dir)
  }
}
