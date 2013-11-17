package com.darylteo.gradle.watcher.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.tasks.TaskAction
import org.gradle.tooling.BuildLauncher
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection

import com.darylteo.nio.DirectoryChangedSubscriber
import com.darylteo.nio.DirectoryWatchService

class WatcherTask extends DefaultTask {
  boolean block = false

  def tasks = []
  def includes = ['src/**']
  def excludes = []

  public WatcherTask() {
    this.group = null
  }

  @TaskAction
  def action() {
    // setup project connection
    ProjectConnection connection = GradleConnector.newConnector()
      .forProjectDirectory(project.projectDir)
      .connect()

    // setup watcher
    DirectoryWatchService service = project.watcher.service
    def watcher = service.newWatcher(project.projectDir.path)

    includes?.each { path -> watcher.include path  }
    excludes?.each { path -> watcher.excludes path }

    // setup builder
    // coerce into String array to pass into varargs parameter -> BuildLauncher only accepts the Task from .model package.
    def tasks = this.tasks.collect { task ->
      task instanceof Task ? "$task.project.path:$task.name" : task
    } as String[]

    println tasks

    BuildLauncher build = connection.newBuild()
      .forTasks(tasks)

    watcher.subscribe({ Object[] args ->
      def src, path = args

      println "File Changed: $path"

      // Errors must be absorbed else the watcher will crap itself.
      try {
        build.run()
      }catch(Throwable e) {
        e.printStackTrace()
      }
    } as DirectoryChangedSubscriber)

    blockTask()
  }

  private def blockTask() {
    if(this.block) {
      def lock = {}

      synchronized(lock) {
        lock.wait() // block until Ctrl-C
      }
    }
  }
}