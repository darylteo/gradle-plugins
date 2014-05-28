package com.darylteo.gradle.javassist.transformers;

import javassist.CtClass;

/**
 * Created by dteo on 28/05/2014.
 */
public interface ClassFilter {
  public boolean filter(CtClass clazz);
}
