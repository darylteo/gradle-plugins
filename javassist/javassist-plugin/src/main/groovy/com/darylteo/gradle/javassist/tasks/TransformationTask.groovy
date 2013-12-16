package com.darylteo.gradle.javassist.tasks

import javassist.ClassPool

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

public class TransformationTask extends DefaultTask {
  @Input
  public def spec

  @OutputDirectory
  public def outputDir = { project.file("${project.buildDir}/transformations/${spec.name}/") }

  @TaskAction
  def run() {
    println "Begin $spec.name transformation"

    def parent = new ClassPool(true)
    def output = project.file(outputDir)
    output.mkdirs()

    // set up the classpath for the classpool
    spec.classpath.each { cp ->
      parent.appendClassPath(project.file(cp).toString())
    }

    spec.sources.each { dir ->
      parent.appendClassPath(dir.toString())
    }

    // identify all the classes we're manipulating (only those in the sources)
    def classes = spec.sources.collect({ dir ->
      project.fileTree(dir, { include '**/*.class' }).files
    })
    .flatten()
    .collect({ file ->
      project.file(file)
    })
    .findAll({ file ->
      file.exists()
    })
    .collect({ file ->
      def is = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))
      def clazz = parent.makeClass(is)
      is.close()

      return clazz.name
    })

    // perform transformations. set up a temporary class pool so that each set of transformations are independent
    spec.transformations.each { t ->
      classes.each { name ->
        def clazz = parent.get(name)
        t(clazz, output)
      }
    }
  }
}
