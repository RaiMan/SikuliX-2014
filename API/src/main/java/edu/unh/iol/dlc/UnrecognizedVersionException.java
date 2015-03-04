package edu.unh.iol.dlc;

/**
 * Runtime exception thrown when the parsed in string is not in the
 * recognized set
 */
public class UnrecognizedVersionException extends RuntimeException {
   public UnrecognizedVersionException(String message) {
      super(message);
   }
}
