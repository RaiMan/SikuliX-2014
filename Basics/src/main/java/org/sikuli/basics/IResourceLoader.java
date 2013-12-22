/*
 * Copyright 2010-2013, Sikuli.org
 * Released under the MIT License.
 *
 * modified RaiMan 2012
 */
package org.sikuli.basics;

public interface IResourceLoader {

  /**
   * Can be used to initialize the NativeLoader. This method is called at the beginning of program
   * execution. The given parameters can be used to parse any specific custom options.
   *
   * @param args 
   */
  public void init(String[] args);
  
  /**
   * checks, wether a valid libs folder is available and accessible
   */
  public void check(String what);
  
  /**
   * copy the res stuff from the jar to the target file/folder
   */
  public boolean export(String res, String target);
  
  /**
   * to be called from a main() to support standalone features
   */
  public void install(String[] args);
  
  /**
   * generic interface to a special runner action
   * @param action identifies what to do
   * @param args contains the needed parameters
   * @return true if successful, false otherwise
   */
  public boolean doSomethingSpecial(String action, Object[] args);

  /**
   * 
   * @return the name of this loader
   */
  public String getName();
  
  /**
   * 
   * @return the supported resource types {"*TYPE*TYPE"}
   */
  public String getResourceTypes();
}
