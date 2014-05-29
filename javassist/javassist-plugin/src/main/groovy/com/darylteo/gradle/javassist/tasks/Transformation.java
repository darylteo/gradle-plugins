package com.darylteo.gradle.javassist.tasks;

import com.darylteo.gradle.javassist.transformers.ClassFilter;
import com.darylteo.gradle.javassist.transformers.ClassTransformation;
import com.darylteo.gradle.javassist.transformers.ClosureBackedClassTransformation;
import groovy.lang.Closure;
import javassist.CtClass;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class Transformation {
  public Transformation child = null;
  public ClassFilter filter = null;
  public ClassTransformation transformation = null;

  public Transformation() {
    this(null, null);
  }

  public Transformation(ClassTransformation transformation) {
    this(null, transformation);
  }

  public Transformation(ClassFilter filter, ClassTransformation transformation) {
    this.filter = filter;
    this.transformation = transformation;
  }

  public Transformation write(final String dir) throws Exception {
    return this.add(new ClassTransformation() {
      public void applyTransformations(CtClass clazz) throws Exception {
        clazz.writeFile(dir);
      }
    });
  }

  public Transformation edit(Closure action) {
    return this.add(new ClosureBackedClassTransformation(action));
  }

  private Transformation add(Closure action) {
    return this.add(new ClosureBackedClassTransformation(action));
  }

  private Transformation add(ClassTransformation transformation) {
    Transformation child = new Transformation(transformation);
    this.child = child;

    return child;
  }

  public void call(List<CtClass> classes, File dir) throws Exception {
    List<CtClass> result = new LinkedList<CtClass>();

    for (CtClass c : classes) {
      if (this.filter != null && this.filter.filter(c)) {
        if (this.transformation != null) {
          this.transformation.applyTransformations(c);
        }

        // pass through class to next transformation
        result.add(c);
      }
    }

    child.call(result, dir);
  }
}
