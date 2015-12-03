package nyu.edu.src.store;

import java.util.HashMap;
import java.util.Map;

import nyu.edu.src.lock.Lock;
import nyu.edu.src.lock.LockTable;
import nyu.edu.src.transcation.TransactionManager;

public class Site {
  
  private int id;
  private Map<Integer, Variable> variables;
  private Map<Integer, Variable> uncommittedVariables;
  private LockTable lockTable;
  
  public enum ServerStatus { 
    UP, DOWN; 
  }
  
  private ServerStatus status; 
  
  public Site(int id) {
    this.id = id;
    variables = new HashMap<Integer, Variable>();
    uncommittedVariables = new HashMap<Integer, Variable>();
    lockTable = new LockTable();
    status = ServerStatus.UP;
  }
  
  public void addVariableToSite(Variable variable) {
    variables.put(variable.getId(), variable);
  }
  
  public void addVariableToSite(int id, int value) {
    variables.put(id, new Variable(id, value));
  }
  
  public boolean variableExistsOnThisSite(String id) {
    return variables.containsKey(id);
  }
  
  public void failure() {
    status = ServerStatus.DOWN;
  }
  
  public void recover() {
    status = ServerStatus.UP;
  }
  
  public String getVariables(){
    if(status == ServerStatus.DOWN) {
      return "Failure!!";
    }
    
    StringBuilder str = new StringBuilder();
    for(int i = 1; i <= TransactionManager.numberOfTotalVariables; i++ ) {
      if( variables.containsKey(i)) {
        str.append(i + ":" + variables.get(i).getValue() + ",");
      }
    }
    
    str.replace(str.lastIndexOf(","), str.length(), "");
    
    return str.toString();
  }
  
  public Lock isItLocked(String id) {
    return isItLocked(getWithoutStartingX(id));
  }
  
  public Lock isItLocked(int id) {
    if(lockTable.getLockTable().containsKey(id)) {
      return lockTable.getLockTable().get(id);
    }
    
    return null;
  }
  
  public int read(String id) {
    return read(getWithoutStartingX(id));  
  }
  
  public int read(int id) {
    return variables.get(id).getValue();
  }
  
  public void write(String id, int value) {
    write(getWithoutStartingX(id), value);
  }
  
  public void write(int id, int value) {
    uncommittedVariables.put(id, new Variable(id, value));
  }
  
  public void rollback(String id) {
    rollback(getWithoutStartingX(id));
  }
  
  public void rollback(int id) {
    uncommittedVariables.remove(id);
  }
  
  public void commit(String id) {
    commit(getWithoutStartingX(id));
  }
  
  public void commit(int id) {
    Variable variable = uncommittedVariables.get(id);
    variables.put(id, variable);
    uncommittedVariables.remove(id);
  }
  
  public boolean isVariableUncommitted(int id) {
    return uncommittedVariables.containsKey(id);
  }
  
  public int getWithoutStartingX(String id){
    if(id.startsWith("x")) {
      int idWithoutX = Integer.parseInt(id.substring(1));
      return idWithoutX;
    }
    
    //ID was not properly provided (not in the form x1, x2, ..., x20)
    return -1;
  }
}
