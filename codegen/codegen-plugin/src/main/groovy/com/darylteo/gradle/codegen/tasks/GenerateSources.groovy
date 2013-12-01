package com.darylteo.gradle.codegen.tasks

import javassist.bytecode.ClassFile
import javassist.bytecode.FieldInfo
import javassist.bytecode.MethodInfo

import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskAction

import com.darylteo.gradle.codegen.generators.GroovyGenerator

public class GenerateSources extends DefaultTask {
  @Input
  FileCollection classFiles

  private def outputDir = { "$project.buildDir/codegen/groovy" }

  @OutputDirectory
  public File getOutputDir() {
    return project.file(outputDir)
  }

  public void setOutputDir(def outputDir) {
    this.outputDir = outputDir
  }

  public void classFiles(SourceSet ss) {
    if(this.classFiles) {
      this.classFiles.add(project.fileTree(ss.output.classesDir))
    } else {
      this.classFiles = project.fileTree(ss.output.classesDir)
    }
  }

  @TaskAction
  public void run() {
    def generator = new GroovyGenerator()
    File dest = project.file(outputDir)

    classFiles.each { File file ->
      def is = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))

      ClassFile classFile = new ClassFile(is)

      generator.onClass(classFile)

      classFile.fields.each { FieldInfo field ->
        generator.onField(field)
      }

      classFile.methods.each { MethodInfo method ->
        generator.onMethod(method)
      }

      def fileName = classToPath(classFile)
      def destFile = project.file("$dest/$fileName")
      destFile.parentFile.mkdirs()
      destFile.createNewFile()

      def os = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(destFile)))
      classFile.write(os)

      os.close()
      is.close()
    }
  }

  protected String classToPath(ClassFile clazz) {
    return clazz.name.replace((char)'.', File.separatorChar) + '.class'
  }

  protected String destinationFile(String classpath, String path) {
    return (path - classpath - File.separator - ~/\.class$/).replaceAll(File.separator,'.')
  }
}
