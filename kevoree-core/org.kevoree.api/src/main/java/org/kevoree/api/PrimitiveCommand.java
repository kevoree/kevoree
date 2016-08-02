package org.kevoree.api;

public interface PrimitiveCommand {

  boolean execute();

  void undo();
}
