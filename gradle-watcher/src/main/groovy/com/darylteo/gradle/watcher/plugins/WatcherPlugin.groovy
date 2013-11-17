package com.darylteo.gradle.watcher.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project

import com.darylteo.gradle.watcher.extensions.WatcherExtension

public class WatcherPlugin implements Plugin<Project>{
  @Override
  public void apply(Project project) {
    project.extensions.create 'watcher', WatcherExtension, project
  }
}