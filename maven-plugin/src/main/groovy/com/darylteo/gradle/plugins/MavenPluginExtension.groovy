package com.darylteo.gradle.plugins

class MavenPluginExtension {
  // repository configuration 
  String username = ''
  String password = ''
  String repository = ''
  String snapshotRepository = ''
  
  // artifact identifier
  String group
  String name
  String version
  boolean release = false
}