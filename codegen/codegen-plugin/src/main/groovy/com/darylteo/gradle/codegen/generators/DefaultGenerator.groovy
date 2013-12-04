package com.darylteo.gradle.codegen.generators

import javassist.CtClass
import javassist.CtField
import javassist.CtMethod

public class DefaultGenerator implements Generator {

  @Override
  public void onClass(CtClass clazz) {
    println "[Default Generator] Class: $clazz.name"

    clazz.fields.each { CtField field -> println "[Default Generator]  Field: $field.name" }

    clazz.methods.each { CtMethod method ->println "[Default Generator]  Method: $method.name $method.parameterTypes $method.returnType" }
  }
}
