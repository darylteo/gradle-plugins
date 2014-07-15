# Gradle Watcher

Simple plugin for running tasks on filesystem changes.

## Sample

```groovy
buildscript {
	repositories {
		mavenCentral()
	}

	repositories {
		classpath 'com.darylteo.gradle:gradle-watcher:0.1.2'
	}
}

import WatcherTask

apply plugin: 'java'
apply plugin: 'watcher'

// todo: configuration of underlying WatchService

task watchTask(type: WatcherTask) {
	// configure what you want your task to do when file system changes
	// by setting a list made up of Strings (task name) or Tasks
	tasks = ['build']

	// default ant style includes and excludes
	includes = ['src/**']
	excludes = []

	// by default, this task will block on completion. 
	// if you want to use this task as part of a chain of tasks, set this to false 
	// and block elsewhere so that gradle doesn't exit
	block = true

  // by default, nothing is run until a change is triggered
	// if you want the tasks to execute once immediately, set this to true
	runImmediately = false
}
````

## Usage

Just run the task you created.

```bash
gradle watchTask
````

Now make a change in your folders and watch the magic happen.