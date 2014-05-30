package com.darylteo.gradle.javassist.tasks;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import org.gradle.api.GradleException;
import org.gradle.api.internal.file.CopyActionProcessingStreamAction;
import org.gradle.api.internal.file.copy.CopyAction;
import org.gradle.api.internal.file.copy.CopyActionProcessingStream;
import org.gradle.api.internal.file.copy.FileCopyDetailsInternal;
import org.gradle.api.internal.tasks.SimpleWorkResult;
import org.gradle.api.tasks.WorkResult;

import java.io.*;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by dteo on 30/05/2014.
 */
class TransformationAction implements CopyAction {

  private String destinationDir;
  private List<File> sources = new LinkedList<>();

  public TransformationAction(String destinationDir, Collection<File> sources) {
    this.destinationDir = destinationDir;
    this.sources.addAll(sources);
  }

  @Override
  public WorkResult execute(CopyActionProcessingStream stream) {
    try {
      final ClassPool pool = createPool(this.sources);
      final LoaderAction action = new LoaderAction(pool, destinationDir);

      stream.process(action);
    } catch (Exception e) {
      throw new GradleException("Could not execute transformation", e);
    }

    return new SimpleWorkResult(true);
  }

  private ClassPool createPool(List<File> classpath) throws NotFoundException {
    final ClassPool pool = new ClassPool(true);

    // set up the classpath for the classpool
    for (File f : this.sources) {
      pool.appendClassPath(f.toString());
    }

    return pool;
  }

  // preloads all class files into the classpool and stores a list of class names
  private class LoaderAction implements CopyActionProcessingStreamAction {
    private final ClassPool pool;
    private final String destinationDir;

    public LoaderAction(ClassPool pool, String destinationDir) {
      this.pool = pool;
      this.destinationDir = destinationDir;
    }

    @Override
    public void processFile(FileCopyDetailsInternal details) {
      try {
        if (!details.isDirectory()) {
          CtClass clazz = loadClassFile(details.getFile());

          System.out.println(clazz.isFrozen());
          clazz.defrost();
          System.out.println(clazz.isFrozen());
          System.out.println("Trying to write File: " + this.destinationDir);
          clazz.writeFile(this.destinationDir);
        }
      } catch (Exception e) {
        throw new GradleException("An error occurred while trying to load class file ", e);
      }
    }

    private CtClass loadClassFile(File classFile) throws IOException {
      // read the file first to get the classname
      // much easier than trying to extrapolate from the filename (i.e. with anonymous classes etc.)
      InputStream stream = new DataInputStream(new BufferedInputStream(new FileInputStream(classFile)));
      CtClass clazz = pool.makeClass(stream);
      stream.close();

      return clazz;
    }
  }

  private class StreamAction implements CopyActionProcessingStreamAction {
    private final ClassPool pool;

    public StreamAction(ClassPool pool) {
      this.pool = pool;
    }

    @Override
    public void processFile(FileCopyDetailsInternal file) {
      if (!file.isDirectory()) {
        File f = file.getFile();
//
//        try {
//          // now we just process the class we just read
//          processClass(pool.get(clazz.getName()));
//        } catch (Exception e) {
//          throw new GradleException("Failed to write class file", e);
//        }
      }
    }

    private void processClass(CtClass clazz) throws Exception {
      clazz.writeFile(destinationDir);
    }
  }
}
