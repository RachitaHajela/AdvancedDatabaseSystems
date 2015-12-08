package nyu.edu.src.exec;

import java.io.BufferedReader;
import java.io.FileReader;

import nyu.edu.src.transaction.TransactionManager;

public class Executor {

    /**
     * This class is the Runner Class for this App. We read the input and take
     * the call the required methods from this Executor class.
     * 
     * @author Rachita & Anto
     */

    private String inputFile;
    private TransactionManager transactionManager;

    public Executor(String inputFile) {
        this.inputFile = inputFile;
        transactionManager = new TransactionManager();
    }

    /**
     * Reads the input and splits into the function that is requested, and the
     * arguments for that function. Calls the required methods from here.
     * 
     * @author Rachita & Anto
     */
    public void readFromInput() {
        BufferedReader br;

        try {
            br = new BufferedReader(new FileReader(inputFile));
            String command = br.readLine();
            int timestamp = 1;

            while (command != null) {
              //  System.out.println(command);
                if (command.startsWith("//") || command.isEmpty()) {
                    command = br.readLine();
                    continue;
                } else if (command.startsWith("=")) {
                    break;
                }

                String[] commandSplit = command.split(";");

                for (int i = 0; i < commandSplit.length; i++) {

                    String temp = commandSplit[i].trim();
                    String functionToDo = temp.substring(0, temp.indexOf("("))
                            .trim();
                    String inputToFunction = temp.substring(
                            temp.indexOf("(") + 1, temp.indexOf(")")).trim();

                    if (functionToDo.equalsIgnoreCase("begin")) {
                        transactionManager.begin(timestamp, inputToFunction);
                    } else if (functionToDo.equalsIgnoreCase("beginro")) {
                        transactionManager.beginRO(timestamp, inputToFunction);
                    } else if (functionToDo.equalsIgnoreCase("end")) {
                        transactionManager.end(timestamp, inputToFunction);
                    } else if (functionToDo.equalsIgnoreCase("fail")) {
                        int siteID = Integer.parseInt(inputToFunction);
                        transactionManager.getDataManager().fail(timestamp, siteID);
                    } else if (functionToDo.equalsIgnoreCase("recover")) {
                        int siteID = Integer.parseInt(inputToFunction);
                        transactionManager.getDataManager().recover(siteID);
                    } else if (functionToDo.equalsIgnoreCase("w")) {
                        String T = inputToFunction.split(",")[0].trim();
                        String var = inputToFunction.split(",")[1].trim();
                        String val = inputToFunction.split(",")[2].trim();
                        transactionManager.writeRequest(timestamp, T, var, val);
                    } else if (functionToDo.equalsIgnoreCase("r")) {
                        String T = inputToFunction.split(",")[0].trim();
                        String var = inputToFunction.split(",")[1].trim();
                        transactionManager.readRequest(timestamp, T, var);
                    } else if (functionToDo.equalsIgnoreCase("dump")) {
                        if (inputToFunction.equals("")) {
                            transactionManager.dump();
                        } else if (inputToFunction.toLowerCase()
                                .startsWith("x")) {
                            transactionManager.dump(inputToFunction);
                        } else {
                            int siteID = Integer.parseInt(inputToFunction);
                            transactionManager.dump(siteID);
                        }
                    }
                }

                timestamp++;
                transactionManager.tick();
                command = br.readLine();
            }
            br.close();

        } catch (Exception e) {
            System.out.println("Problem with File");
            e.printStackTrace();
        }
    }

    /**
     * Main method starts up the Executor and calls method readFromInput()
     * 
     * @author Rachita & Anto
     */
    public static void main(String[] args) {
        Executor exec = new Executor(args[0]);
        exec.readFromInput();
    }
}