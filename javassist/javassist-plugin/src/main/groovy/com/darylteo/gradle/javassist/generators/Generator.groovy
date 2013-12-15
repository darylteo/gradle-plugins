package com.darylteo.gradle.javassist.generators

import javassist.CtClass

public interface Generator {
  public void onClass(CtClass clazz)
}
