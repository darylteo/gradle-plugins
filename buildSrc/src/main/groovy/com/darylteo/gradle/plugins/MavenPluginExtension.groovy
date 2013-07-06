package com.darylteo.gradle.plugins

import org.gradle.api.artifacts.maven.MavenPom

class MavenPluginExtension {
  // repository configuration
  String username = ''
  String password = ''
  String repository = ''
  String snapshotRepository = ''

  // artifact identifier
  boolean release = false

  MavenPom pom

  void project(Closure closure) {
    pom.project closure
  }
}