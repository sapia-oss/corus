package org.sapia.corus.interop.test;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;


/**
 *
 *
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author
 * @version 1.0
 */
public class ProfileInteropDeserialization implements Runnable {
  private static int  totalTestcase = 0;
  private static long startTime;
  private InteropDeserializationTest _theTest;

  public ProfileInteropDeserialization(InteropDeserializationTest aTest) {
    _theTest = aTest;
  }

  public static void main(String[] args) {
    InteropDeserializationTest aTest = new InteropDeserializationTest("main");

    try {
      System.out.println("Starting profiling of Interop deserialization...");
      Logger.getRootLogger().setLevel(Level.INFO);

      System.out.println("Initialization...");
      startTime = System.currentTimeMillis();

      for (int i = 0; i < 2; i++) {
        new Thread(new ProfileInteropDeserialization(aTest), "Test-" + (i + 1)).start();
      }

      Thread.sleep(5000);
      logStatus();

      // To eliminate the startup overhead...
      System.out.println("Reset counter to eliminate overhead...");
      totalTestcase = 0;
      startTime     = System.currentTimeMillis();

      while (true) {
        Thread.sleep(5000);
        logStatus();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void logStatus() {
    StringBuffer aBuffer = new StringBuffer();
    int          count   = totalTestcase;
    long         time    = System.currentTimeMillis() - startTime;
    double       average = ((count) * 1000.0) / time;
    aBuffer.append("Total test case: ").append(count).append("\tTotal time: ")
           .append(time).append("\tAverage: ").append(average);
    System.out.println(aBuffer.toString());
  }

  public void run() {
    try {
      while (true) {
        _theTest.testPollRequest();
        totalTestcase++;
        _theTest.testStatusRequest();
        totalTestcase++;
        _theTest.testRestartRequest();
        totalTestcase++;
        _theTest.testConfirmShutdownRequest();
        totalTestcase++;
        _theTest.testAckResponse();
        totalTestcase++;
        _theTest.testShutdownResponse();
        totalTestcase++;
        _theTest.testSOAPFaultResponse();
        totalTestcase++;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
