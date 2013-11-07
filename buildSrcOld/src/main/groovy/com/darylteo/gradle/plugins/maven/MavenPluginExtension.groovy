package com.darylteo.gradle.plugins.maven

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.DeferredConfigurable

class MavenPluginExtension{
  final Project project

  public MavenPluginExtension(Project project) {
    this.project = project
  }

  void pom(Closure<?> closure) {
    this.project.install.repositories.mavenInstaller.pom(closure)
    this.project.uploadArchives.repositories.mavenDeployer.pom(closure)
  }

  boolean getRelease() {
    return !this.project.version.endsWith('-SNAPSHOT')
  }
}