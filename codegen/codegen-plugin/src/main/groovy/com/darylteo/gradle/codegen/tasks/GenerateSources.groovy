package com.darylteo.gradle.codegen.tasks

import javassist.ClassPool
import javassist.CtClass

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

import com.darylteo.gradle.codegen.generators.DefaultGenerator
import com.darylteo.gradle.codegen.generators.Generator


public class GenerateSources extends DefaultTask {
  /* Source of files to tweak */
  @Input
  List sources = []

  /* Classpaths to add to the ClassPool */
  @Input
  List classpath = []

  public void from(Project project) {
    sources.addAll(project.sourceSets*.output.classesDir)
    classpath.addAll(project.sourceSets*.output.classesDir)

    classpath.addAll(project.configurations.compile?.files)
  }

  /* Where the files will go */
  def outputDir = { "$project.buildDir/codegen/groovy" }

  @OutputDirectory
  public File getOutputDir() {
    return project.file(outputDir)
  }

  public void setOutputDir(def outputDir) {
    this.outputDir = outputDir
  }

  /* The generator */
  Generator generator = new DefaultGenerator()

  public void setGenerator(def value) {
    generator = value as Generator
  }



  @TaskAction
  public void run() {
    File dest = project.file(outputDir)

    def sourceFiles = sources.collect({ dir ->
      project.fileTree(dir) { include '**/*.class' }.files
    }).flatten()

    ClassPool pool = new ClassPool(true)
    classpath.each { path ->
      println "$path"
      pool.appendClassPath("$path")
    }

    sourceFiles.each { File file ->
      def is = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))
      CtClass clazz = pool.makeClass(is)

      generator.onClass(clazz)

      clazz.writeFile(dest.path)

      is.close()
    }
  }

  protected String classToPath(CtClass clazz) {
    return clazz.name.replace((char)'.', File.separatorChar) + '.class'
  }

  protected String destinationFile(String classpath, String path) {
    return (path - classpath - File.separator - ~/\.class$/).replaceAll(File.separator,'.')
  }
}
