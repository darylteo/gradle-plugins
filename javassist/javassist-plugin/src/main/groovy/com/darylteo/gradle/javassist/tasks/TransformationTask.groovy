package com.darylteo.gradle.javassist.tasks

import javassist.ClassPool

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction


public class TransformationTask extends DefaultTask {
  @Input
  public def spec = []

  @Input
  public def transformations = []

  @OutputDirectory
  public def outputDir = {
    project.file("${project.buildDir}/transformations/${this.name}/")
  }

  /* Input Output */ 
  public void from(def path) {
    spec += (path as List)
  }

  public void into(def path) {
    this.outputDir = path
  }

  /* Selectors */
  public def all(Closure action) {
    // defer resolution of outputDir until run
    def transform = new Transformation({ c, dir ->
      action?.call(c)
    })
    transformations += transform

    return transform
  }


  @TaskAction
  def run() {
    def parent = new ClassPool(true)
    def output = project.file(outputDir)
    output.mkdirs()

    // set up the classpath for the classpool
    project.configurations.compile.files.each { cp ->
      parent.appendClassPath("$cp")
    }

    spec.each { dir ->
      parent.appendClassPath("$dir")
    }

    // identify all the classes we're manipulating (only those in the sources)
    def classNames = spec
      .flatten()
      .collect({ dir ->
        project.fileTree("$dir", { include '**/*.class' }) as List
      })
      .flatten()
      .collect({ file ->
        if(!file.exists()) {
          return null
        }

        def is = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))
        def clazz = parent.makeClass(is)
        is.close()

        return clazz.name
      })
      .findAll()

    // perform transformations. set up a temporary class pool so that each set of transformations are independent
    transformations.each { t ->
      def classes = classNames.collect({ name ->
        parent.get(name)
      })

      t(classes, output)
    }
  }
}
