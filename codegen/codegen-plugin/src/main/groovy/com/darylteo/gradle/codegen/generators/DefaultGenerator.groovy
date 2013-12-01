package com.darylteo.gradle.codegen.generators

import javassist.bytecode.ClassFile
import javassist.bytecode.FieldInfo
import javassist.bytecode.MethodInfo

public class DefaultGenerator implements Generator {

  @Override
  public void onClass(ClassFile file) {
    println "[Default Generator] Class: $file.name"

    
    file.fields.each { FieldInfo field -> println "[Default Generator]  Field: $field.descriptor $field.name" }

    file.methods.each { MethodInfo method ->println "[Default Generator]  Method: $method.name" }
  }
}
