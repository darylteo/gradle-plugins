package com.darylteo.gradle.watcher.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.tasks.TaskAction
import org.gradle.tooling.BuildLauncher
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection

import com.darylteo.nio.DirectoryChangedSubscriber
import com.darylteo.nio.DirectoryWatchService
import com.darylteo.nio.ThreadPoolDirectoryWatchService

class AutoGradleBuild extends DefaultTask {
  def tasks = []
  def includes = ['src/**']
  def excludes = []

  @TaskAction
  def action() {
    // setup project connection
    ProjectConnection connection = GradleConnector.newConnector()
      .forProjectDirectory(project.projectDir)
      .connect()

    // setup watcher
    DirectoryWatchService service = new ThreadPoolDirectoryWatchService()
    def watcher = service.newWatcher(project.projectDir.path)
    
    includes?.each { path ->
      watcher.include path 
    }
    
    excludes?.each { path ->
      watcher.excludes path
    }

    // setup builder
    // coerce into String array to pass into varargs parameter -> List get caught by the Iterable<> overload
    def tasks = this.tasks instanceof String ? [this.tasks]: this.tasks
    tasks = tasks.collect { task ->
      task instanceof Task ? task.name : task
    } as String[]

    BuildLauncher build = connection.newBuild()
      .forTasks(tasks)

    watcher.subscribe({ Object[] args ->
      def src, path = args

      println "File Changed: $path"

      build.run()
    } as DirectoryChangedSubscriber)
  }
}