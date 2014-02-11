package com.darylteo.gradle.javassist.tasks

import javassist.ClassPool

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction


public class TransformationTask extends DefaultTask {
  @Input
  public def sources = []

  @Input
  public def transformations = []

  @OutputDirectory
  def File outputDir

  /* Constructor */
  public TransformationTask() {
    this.outputDir = project.file("${project.buildDir}/transformations/${this.name}")

    this.inputs.property('sources', { this.sources })
    this.outputs.dir({ this.outputDir })

    this.from(project.sourceSets*.output.files)

    this.dependsOn project.sourceSets*.classesTaskName
  }

  /* Input Output */
  public void from(def path) {
    sources += (path as List)
  }

  public void into(def path) {
    this.outputDir = project.file(path)
  }

  /* Selectors */
  public def transform(def pattern = null, Closure action) {
    // defer resolution of outputDir until run
    def transform = new Transformation(pattern, { c, dir -> 
      action?.call(c)
    })
    transformations += transform

    return transform
  }

  /* Task Action */
  @TaskAction
  def run() {
    def parent = new ClassPool(true)
    def output = this.outputDir
    output.mkdirs()

    // set up the classpath for the classpool
    project.configurations.compile.files.each { cp ->
      parent.appendClassPath("$cp")
    }

    sources.each { dir ->
      parent.appendClassPath("$dir")
    }

    // identify all the classes we're manipulating (only those in the sources)
    def classNames = sources
      .flatten()
      .collect({ dir ->
        def list
        if(dir.name.matches(~/^.*\.(jar|zip)$/)) {
          list = project.zipTree(dir)
        } else {
          list = project.fileTree(dir)
        }

        return (list.matching({ include '**/*.class' })) as List
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
