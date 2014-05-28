package com.darylteo.gradle.javassist.tasks

public class Transformation {
  def transforms = []

  def filter = null
  def action = null

  public Transformation() {
    this(null, null)
  }

  public Transformation(def action) {
    this(null, action)
  }

  public Transformation(def filter, def action) {
    this.filter = filter
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
    def filtered = this.classes

    if(this.filter != null) {
      filtered = filtered.findAll { c ->
        this.filter.call(c)
      }
    }
    classes.findAll ({ c ->
      return !this.filter || c.name.matches(this.filter)
    }).each { c ->
      result += action?.call(c,dir)
    }

    result = result.findAll()
    transforms*.call(result,dir)
  }
}
