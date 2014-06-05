package com.darylteo.gradle.javassist.transformers;

import javassist.CtClass;

/**
 * Created by dteo on 28/05/2014.
 */
public class ClassTransformation implements ClassFilter, ClassTransformationFunction {
  @Override
  public void applyTransformations(CtClass clazz) throws Exception {
    return;
  }

  @Override
  public boolean filter(CtClass clazz) {
    return true;
  }
}
