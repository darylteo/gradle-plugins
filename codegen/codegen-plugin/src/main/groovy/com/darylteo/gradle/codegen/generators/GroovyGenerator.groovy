package com.darylteo.gradle.codegen.generators

import javassist.bytecode.ClassFile
import javassist.bytecode.FieldInfo
import javassist.bytecode.MethodInfo

public class GroovyGenerator implements Generator {

  @Override
  public void onClass(ClassFile file) {
    println "\nClass: $file.name"
  }

  @Override
  public void onField(FieldInfo field) {
    println "  Field: $field"
  }

  @Override
  public void onMethod(MethodInfo method) {
    println "  Method: $method"
  }
}
