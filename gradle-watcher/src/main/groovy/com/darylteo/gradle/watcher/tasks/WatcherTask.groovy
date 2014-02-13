package com.darylteo.gradle.watcher.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.tasks.TaskAction
import org.gradle.tooling.BuildLauncher
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProgressListener
import org.gradle.tooling.ProjectConnection

import com.darylteo.nio.DirectoryChangedSubscriber
import com.darylteo.nio.DirectoryWatchService

class WatcherTask extends DefaultTask {
  boolean block = true

  def tasks = []
  def includes = ['src/**']
  def excludes = []

  def _timer
  def _currentTask

  public WatcherTask() {
    this.group = null
  }

  @TaskAction
  def action() {
    _timer = new Timer()

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

    BuildLauncher build = connection.newBuild()
      .forTasks(tasks)

    watcher.subscribe({ Object[] args ->
      def src, path = args
      if(_currentTask){
        _currentTask.cancel()
        _timer.purge()
      }

      _currentTask = {
        _currentTask = null
        build.run()
      } as TimerTask

      // Errors must be absorbed else the watcher will crap itself.
      try {
        _timer.schedule(_currentTask, 2000)
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