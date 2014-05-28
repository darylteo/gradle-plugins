package com.darylteo.gradle.javassist.transformers;

import groovy.lang.Closure;
import javassist.CtClass;

/**
 * Created by dteo on 28/05/2014.
 */
public class ClosureBackedClassFilter implements ClassFilter {
  private Closure closure;

  public ClosureBackedClassFilter(Closure closure) {
    this.closure = closure;
  }

  @Override
  public boolean filter(CtClass clazz) {
    return (boolean) this.closure.call(clazz);
  }
}
