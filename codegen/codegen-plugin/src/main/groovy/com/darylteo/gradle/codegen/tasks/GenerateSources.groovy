package com.darylteo.gradle.codegen.tasks

import javassist.ClassPool
import javassist.CtClass

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.incremental.IncrementalTaskInputs

import com.darylteo.gradle.codegen.generators.DefaultGenerator
import com.darylteo.gradle.codegen.generators.Generator


public class GenerateSources extends DefaultTask {
  /* Source of files to tweak */
  List sources = []

  /* Classpaths to add to the ClassPool */
  List classpath = []

  public void from(Project project) {
    def dirs = project.sourceSets*.output.classesDir

    dirs.each { dir ->
      sources.add(dir)
      inputs.dir(dir)

      classpath.add(dir)
    }

    classpath.addAll(project.configurations.compile?.files)

    dependsOn project.tasks.findAll { task -> task.name.startsWith('compile') }
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
  Generator generator = new DefaultGenerator()

  public void setGenerator(def value) {
    generator = value as Generator
  }

  @TaskAction
  void run(IncrementalTaskInputs inputs) {
    def sourceFiles = sources.collect({ dir ->
      project.fileTree(dir) { include '**/*.class' }.files
    }).flatten()

    def generated = []

    ClassPool pool = new ClassPool(true)
    classpath.each { path ->
      pool.appendClassPath("$path")
    }

    File dest = project.file(outputDir)
    dest.deleteDir()
    dest.mkdirs()

    sourceFiles.each { File file ->
      def is = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))
      CtClass clazz = pool.makeClass(is)
      is.close()

      generator.onClass(clazz)

      generated.add(clazz)
    }

    generated.each { CtClass clazz ->
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
