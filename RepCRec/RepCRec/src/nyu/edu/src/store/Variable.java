package nyu.edu.src.store;

import nyu.edu.src.transcation.TransactionManager;

public class Variable {
  
  private int id;
  private int value;
  
  public Variable(int id, int value) {
    if(id < 1 || id > TransactionManager.numberOfTotalVariables) {
      throw new NullPointerException("Variable ID is not in bounds!!");
    }
    
    this.id = id;
    this.value = value;
  }
  
  public int getId() {
    return id;
  }
  
  public void setId(int id) {
    this.id = id;
  }
  
  public int getValue() {
    return value;
  }
  
  public void setValue(int value) {
    this.value = value;
  }
  
}
