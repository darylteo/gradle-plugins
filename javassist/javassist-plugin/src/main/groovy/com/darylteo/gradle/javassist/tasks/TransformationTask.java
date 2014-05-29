package com.darylteo.gradle.javassist.tasks;

import com.darylteo.gradle.javassist.transformers.ClosureBackedClassTransformation;
import com.google.common.io.Files;
import groovy.lang.Closure;
import javassist.ClassPool;
import javassist.CtClass;
import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.*;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

public class TransformationTask extends DefaultTask {
  @Input
  public List<File> sources = new LinkedList<>();
  @Input
  public List<Transformation> transformations = new LinkedList<>();
  @OutputDirectory
  public File outputDir;

  public TransformationTask() {
    final TransformationTask that = this;
    Project project = this.getProject();

    this.outputDir = project.file(String.format("%s/transformations/%s", project.getBuildDir(), this.getName()));
    this.getInputs().property("sources", new Callable() {
      @Override
      public Object call() throws Exception {
        return that.sources;
      }

    });

    this.getOutputs().dir(new Callable() {
      @Override
      public Object call() throws Exception {
        return that.outputDir;
      }

    });

    project.afterEvaluate(new Action<Project>() {
      @Override
      public void execute(Project project) {
        if (project.hasProperty("sourceSets")) {
          for (SourceSet s : (SourceSetContainer) project.property("sourceSets")) {
            that.from(s.getOutput().getFiles());
            that.dependsOn(project.getTasks().getByName(s.getClassesTaskName()));
          }
        } else {
          throw new RuntimeException("This project does not have any sourcesets. Make sure you have applied the required plugins");
        }
      }
    });
  }

  public void from(Object... path) {
    this.sources.addAll(this.getProject().files(path).getFiles());
  }

  public void into(Object path) {
    this.outputDir = this.getProject().file(path);
  }

  public Transformation transform(final Closure action) {
    // defer resolution of outputDir until run
    Transformation transform = new Transformation(new ClosureBackedClassTransformation(action));
    transformations.add(transform);

    return transform;
  }

  @TaskAction
  public void run() throws Exception {
    final ClassPool parent = new ClassPool(true);
    final File output = this.outputDir;
    output.mkdirs();

    // set up the classpath for the classpool
    for (File f : this.getProject().getConfigurations().getByName("compile").getFiles()) {
      parent.appendClassPath(f.toString());
    }

    for (File src : this.sources) {
      parent.appendClassPath(src.toString());
    }

    // identify all the classes we're manipulating (only those in the sources)
    List<CtClass> classes = getClasses(parent, this.sources);

    for (Transformation t : this.transformations) {
      t.call(classes, this.outputDir);
    }
  }

  private List<CtClass> getClasses(ClassPool parent, Collection<File> sources) throws Exception {
    List<CtClass> result = new LinkedList<>();

    for (File src : this.sources) {
      // just in case
      if (!src.exists()) {
        continue;
      }

      String extension = Files.getFileExtension(src.getName()).toLowerCase();

      // extract archive types
      switch (extension) {
        case "jar":
        case "zip":
          result.addAll(getClasses(parent, this.getProject().zipTree(src).getFiles()));
          break;

        case "class":
          //TODO:
          System.out.println(src.getName());
          // result.add(parent.getCtClass(src.getName()));
          break;

        // directory
        default:
          result.addAll(getClasses(parent, this.getProject().fileTree(src).getFiles()));
          break;
      }
    }

    return result;
  }
}
