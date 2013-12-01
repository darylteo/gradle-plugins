package com.darylteo.gradle.codegen.generators

import javassist.bytecode.ClassFile

public interface Generator {
  public void onClass(ClassFile file)
}
