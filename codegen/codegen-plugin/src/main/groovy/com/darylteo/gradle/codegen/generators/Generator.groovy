package com.darylteo.gradle.codegen.generators

import javassist.bytecode.ClassFile
import javassist.bytecode.FieldInfo
import javassist.bytecode.MethodInfo

public interface Generator {
  public void onClass(ClassFile file)

  public void onField(FieldInfo field)

  public void onMethod(MethodInfo method)
}
