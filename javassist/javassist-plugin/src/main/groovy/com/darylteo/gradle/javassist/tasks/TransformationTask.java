package com.darylteo.gradle.javassist.tasks;

import com.darylteo.gradle.javassist.transformers.ClassTransformation;
import com.darylteo.gradle.javassist.transformers.GroovyClassTransformation;
import groovy.lang.Closure;
import javassist.CtClass;
import org.gradle.api.internal.file.copy.CopyAction;
import org.gradle.api.tasks.AbstractCopyTask;

public class TransformationTask extends AbstractCopyTask {

  private String destinationDir;

  public String getDestinationDir() {
    return destinationDir;
  }

  public void setDestinationDir(String destinationDir) {
    this.destinationDir = destinationDir;
  }

  private ClassTransformation transformation;

  public ClassTransformation getTransformation() {
    return transformation;
  }

  public void setTransformation(ClassTransformation transformation) {
    this.transformation = transformation;
  }

  public void transform(Closure closure) {
    this.transformation = new GroovyClassTransformation(closure);
  }

  public TransformationTask() {
    this.transformation = new ClassTransformation() {
      @Override
      public void applyTransformations(CtClass clazz) throws Exception {
        System.out.println("No transformations applied to this task.");
      }
    };
  }

  @Override
  protected CopyAction createCopyAction() {
    String dir = this.destinationDir;
    if (dir == null) {
      dir = String.format("%s/transformations/%s", this.getProject().getBuildDir(), this.getName());
    }

    return new TransformationAction(dir, this.getSource().getFiles(), this.transformation);
  }

}
