package com.darylteo.gradle.javassist.transformers;

import groovy.lang.Closure;
import javassist.CtClass;

/**
 * Created by dteo on 28/05/2014.
 */
public class ClosureBackedClassTransformation implements ClassTransformation {
  private Closure closure;

  public ClosureBackedClassTransformation(Closure closure) {
    this.closure = closure;
  }

  @Override
  public void applyTransformations(CtClass clazz) {
    this.closure.call(clazz);
  }
}
