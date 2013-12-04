package com.darylteo.gradle.codegen.generators

import javassist.CtClass

public interface Generator {
  public void onClass(CtClass clazz)
}
