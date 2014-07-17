package com.darylteo.gradle.watcher.plugins;

import com.darylteo.gradle.watcher.extensions.WatcherExtension;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class WatcherPlugin implements Plugin<Project> {
  @Override
  public void apply(Project project) {
    project.getExtensions().create("watcher", WatcherExtension.class, project);
  }
}
