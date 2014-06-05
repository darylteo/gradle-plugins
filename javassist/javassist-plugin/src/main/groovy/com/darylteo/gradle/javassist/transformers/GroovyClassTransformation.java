package com.darylteo.gradle.javassist.transformers;

import groovy.lang.Closure;
import javassist.CtClass;

/**
 * Created by dteo on 28/05/2014.
 */
public class GroovyClassTransformation extends ClassTransformation {
  private Closure transform;
  private Closure filter;

  public GroovyClassTransformation(Closure transform) {
    this.transform = transform;
    this.filter = null;
  }

  public GroovyClassTransformation(Closure transform, Closure filter) {
    this.transform = transform;
    this.filter = filter;
  }

  @Override
  public void applyTransformations(CtClass clazz) {
    this.transform.call(clazz);
  }

  @Override
  public boolean filter(CtClass clazz) {
    return this.filter == null || (boolean) this.filter.call(clazz);
  }

  public void transform(Closure transform) {
    this.transform = transform;
  }

  public void where(Closure filter) {
    this.filter = filter;
  }
}
