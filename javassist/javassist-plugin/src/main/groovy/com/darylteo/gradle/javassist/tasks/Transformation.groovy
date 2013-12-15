package com.darylteo.gradle.javassist.tasks

import javassist.ClassPool
import javassist.CtClass

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.incremental.IncrementalTaskInputs

import com.darylteo.gradle.codegen.generators.Generator

public class Transformation extends DefaultTask {
  /* Source of files to tweak */
  def sources = []

  /* Classpaths to add to the ClassPool */
  def classpath = []

  public void from(Project project) {
    def dirs = project.sourceSets*.output.classesDir

    dirs.each { dir ->
      inputs.dir(dir)

      sources.add(dir)
      classpath.add(dir)
    }

    classpath.addAll(project.configurations.compile?.files)

    dependsOn project.tasks.findAll { task -> task.name.startsWith('compile') }
  }

  public void from(Iterable path) {
    sources.addAll(path)
    classpath.addAll(path)
  }

  public void from(String ... path) {
    sources.addAll(path)
    classpath.addAll(path)
  }

  /* Where the files will go */
  def outputDir = { "$project.buildDir/codegen" }

  @OutputDirectory
  public File getOutputDir() {
    return project.file(outputDir)
  }

  public void setOutputDir(def dir) {
    this.outputDir = dir
  }

  public void into(def dir) {
    this.outputDir = dir
  }

  /* The generator */
  List generators = []

  public void addGenerator(def value) {
    this.generators.add(value as Generator)
  }

  @TaskAction
  void run(IncrementalTaskInputs inputs) {
    def sourceFiles = (this.sources.collect { src ->
      src = project.file(src)
      src.isDirectory() ? project.fileTree(src) { include '**/*.class' }.files : src
    }).flatten()

    def generated = []

    ClassPool pool = new ClassPool(true)
    classpath.each { path ->
      pool.appendClassPath("$path")
    }

    File dest = project.file(outputDir)
    dest.deleteDir()
    dest.mkdirs()

    sourceFiles.each { def file ->
      file = project.file(file)
      if(!file.exists()) {
        return
      }

      def is = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))
      CtClass clazz = pool.makeClass(is)
      is.close()

      generators.each { generator ->
        generator.onClass(clazz)
      }

      clazz.writeFile(dest.path)
    }

    setDidWork(true)
  }

  protected String classToPath(CtClass clazz) {
    return clazz.name.replace((char)'.', File.separatorChar) + '.class'
  }

  protected String destinationFile(String classpath, String path) {
    return (path - classpath - File.separator - ~/\.class$/).replaceAll(File.separator,'.')
  }
}
