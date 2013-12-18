package com.darylteo.gradle.javassist.transforms

import java.util.regex.Pattern

import javassist.ClassPool

public class TransformationSpec {
  def sources

  public TransformationSpec(def sources) {
    this.sources = sources
  }
}
