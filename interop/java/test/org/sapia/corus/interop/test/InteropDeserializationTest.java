package org.sapia.corus.interop.test;

import junit.framework.TestCase;

import junit.textui.TestRunner;

import org.sapia.corus.interop.Ack;
import org.sapia.corus.interop.ConfirmShutdown;
import org.sapia.corus.interop.InteropProcessor;
import org.sapia.corus.interop.Param;
import org.sapia.corus.interop.Poll;
import org.sapia.corus.interop.Process;
import org.sapia.corus.interop.Restart;
import org.sapia.corus.interop.Server;
import org.sapia.corus.interop.Shutdown;
import org.sapia.corus.interop.Status;
import org.sapia.corus.interop.Context;
import org.sapia.corus.interop.soap.Body;
import org.sapia.corus.interop.soap.Envelope;
import org.sapia.corus.interop.soap.Fault;
import org.sapia.corus.interop.soap.Header;

import java.io.ByteArrayInputStream;


/**
 *
 *
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author
 * @version 1.0
 */
public class InteropDeserializationTest extends TestCase {
  static {
    org.apache.log4j.BasicConfigurator.configure();
  }

  private InteropProcessor _theProcessor = new InteropProcessor();

  public InteropDeserializationTest(String aName) {
    super(aName);
  }

  public static void main(String[] args) {
    TestRunner.run(InteropDeserializationTest.class);
  }

  /**
   *
   */
  public void testPollRequest() throws Exception {
    String anXmlBody =
      "<CORUS-IOP:Poll xmlns:CORUS-IOP=\"http://schemas.sapia-oss.org/corus/interoperability/\"" +
      " commandId=\"675432\" />";
    String aPollRequest = createSoapRequest(anXmlBody);

    Object aResult = _theProcessor.deserialize(new ByteArrayInputStream(aPollRequest.getBytes()));
    assertNotNull("The result object should not be null", aResult);
    assertTrue("The result object is not an Envelope",
               aResult instanceof Envelope);

    Envelope anEnvelope = (Envelope) aResult;
    assertRequest(anEnvelope);

    Body aBody = anEnvelope.getBody();
    assertEquals("The size of the object list of the body is invalid", 1,
                 aBody.getObjects().size());
    assertTrue("The object of the body is not a Poll",
               aBody.getObjects().get(0) instanceof Poll);

    Poll aPoll = (Poll) aBody.getObjects().get(0);
    assertEquals("The command id of the poll is invalid", "675432",
                 aPoll.getCommandId());
  }

  /**
   *
   */
  public void testStatusRequest() throws Exception {
    String anXmlBody =
      "<CORUS-IOP:Status xmlns:CORUS-IOP=\"http://schemas.sapia-oss.org/corus/interoperability/\"" +
            " commandId=\"675433\">" + "<CORUS-IOP:Context name=\"someContext\">" +
            "<CORUS-IOP:Param name=\"param1\" value=\"param1_value\" />" +
            "<CORUS-IOP:Param name=\"param2\" value=\"param2_value\" />" +
            "</CORUS-IOP:Context>" + "<CORUS-IOP:Context name=\"someOtherContext\">" +
            "<CORUS-IOP:Param name=\"param1\" value=\"param1_value\" />" +
            "<CORUS-IOP:Param name=\"param2\" value=\"param2_value\" />" +
            "</CORUS-IOP:Context>" + "</CORUS-IOP:Status>";
    String aPollRequest = createSoapRequest(anXmlBody);

    Object aResult = _theProcessor.deserialize(new ByteArrayInputStream(aPollRequest.getBytes()));
    assertNotNull("The result object should not be null", aResult);
    assertTrue("The result object is not an Envelope",
               aResult instanceof Envelope);

    Envelope anEnvelope = (Envelope) aResult;
    assertRequest(anEnvelope);

    Body aBody = anEnvelope.getBody();
    assertEquals("The size of the object list of the body is invalid", 1,
                 aBody.getObjects().size());
    assertTrue("The object of the body is not a Poll",
               aBody.getObjects().get(0) instanceof Status);

    Status aStatus = (Status) aBody.getObjects().get(0);
    assertEquals("The command id of the status is invalid", "675433",
                 aStatus.getCommandId());
    assertEquals("The list of context has an invalid size", 2,
                 aStatus.getContexts().size());

    Context aContext = (Context) aStatus.getContexts().get(0);
    assertEquals("The name of the context is invalid", "someContext",
                 aContext.getName());
    assertEquals("The list of params has an invalid size", 2,
                 aContext.getParams().size());

    Param anParam = (Param) aContext.getParams().get(0);
    assertEquals("", "param1", anParam.getName());
    assertEquals("", "param1_value", anParam.getValue());
    anParam = (Param) aContext.getParams().get(1);
    assertEquals("", "param2", anParam.getName());
    assertEquals("", "param2_value", anParam.getValue());

    Context anotherContext = (Context) aStatus.getContexts().get(1);
    assertEquals("The name of the context is invalid", "someOtherContext",
                 anotherContext.getName());
    assertEquals("The list of params has an invalid size", 2,
                 anotherContext.getParams().size());

    Param anotherParam = (Param) anotherContext.getParams().get(0);
    assertEquals("", "param1", anotherParam.getName());
    assertEquals("", "param1_value", anotherParam.getValue());
    anotherParam = (Param) aContext.getParams().get(1);
    assertEquals("", "param2", anotherParam.getName());
    assertEquals("", "param2_value", anotherParam.getValue());
  }

  /**
   *
   */
  public void testRestartRequest() throws Exception {
    String anXmlBody =
            "<CORUS-IOP:Restart xmlns:CORUS-IOP=\"http://schemas.sapia-oss.org/corus/interoperability/\"" +
            " commandId=\"6754335\" />";
    String aRestartRequest = createSoapRequest(anXmlBody);

    Object aResult = _theProcessor.deserialize(new ByteArrayInputStream(aRestartRequest.getBytes()));
    assertNotNull("The result object should not be null", aResult);
    assertTrue("The result object is not an Envelope",
               aResult instanceof Envelope);

    Envelope anEnvelope = (Envelope) aResult;
    assertRequest(anEnvelope);

    Body aBody = anEnvelope.getBody();
    assertEquals("The size of the object list of the body is invalid", 1,
                 aBody.getObjects().size());
    assertTrue("The object of the body is not a Poll",
               aBody.getObjects().get(0) instanceof Restart);

    Restart aRestart = (Restart) aBody.getObjects().get(0);
    assertEquals("The command id of the restart is invalid", "6754335",
                 aRestart.getCommandId());
  }

  /**
   *
   */
  public void testConfirmShutdownRequest() throws Exception {
    String anXmlBody =
            "<CORUS-IOP:ConfirmShutdown xmlns:CORUS-IOP=\"http://schemas.sapia-oss.org/corus/interoperability/\"" +
            " commandId=\"675434\" />";
    String aConfirmShutdownRequest = createSoapRequest(anXmlBody);

    Object aResult = _theProcessor.deserialize(new ByteArrayInputStream(aConfirmShutdownRequest.getBytes()));
    assertNotNull("The result object should not be null", aResult);
    assertTrue("The result object is not an Envelope",
               aResult instanceof Envelope);

    Envelope anEnvelope = (Envelope) aResult;
    assertRequest(anEnvelope);

    Body aBody = anEnvelope.getBody();
    assertEquals("The size of the object list of the body is invalid", 1,
                 aBody.getObjects().size());
    assertTrue("The object of the body is not a ConfirmShutdown",
               aBody.getObjects().get(0) instanceof ConfirmShutdown);

    ConfirmShutdown aConfirmShutdown = (ConfirmShutdown) aBody.getObjects().get(0);
    assertEquals("The command id of the shutdown request is invalid", "675434",
                 aConfirmShutdown.getCommandId());
  }

  /**
   *
   */
  public void testAckResponse() throws Exception {
    String anXmlBody =
            "<CORUS-IOP:Ack xmlns:CORUS-IOP=\"http://schemas.sapia-oss.org/corus/interoperability/\"" +
            " commandId=\"675435\" />";
    String anAckResponse = createSoapResponse(anXmlBody);

    Object aResult = _theProcessor.deserialize(new ByteArrayInputStream(anAckResponse.getBytes()));
    assertNotNull("The result object should not be null", aResult);
    assertTrue("The result object is not an Envelope",
               aResult instanceof Envelope);

    Envelope anEnvelope = (Envelope) aResult;
    assertResponse(anEnvelope);

    Body aBody = anEnvelope.getBody();
    assertEquals("The size of the object list of the body is invalid", 1,
                 aBody.getObjects().size());
    assertTrue("The object of the body is not an Ack",
               aBody.getObjects().get(0) instanceof Ack);

    Ack anAck = (Ack) aBody.getObjects().get(0);
    assertEquals("The command id of the ack response is invalid", "675435",
                 anAck.getCommandId());
  }

  /**
   *
   */
  public void testShutdownResponse() throws Exception {
    String anXmlBody =
            "<CORUS-IOP:Shutdown xmlns:CORUS-IOP=\"http://schemas.sapia-oss.org/corus/interoperability/\"" +
            " commandId=\"1234\"" +
            " requestor=\"InteropDeserializationTest\" />";
    String aShutdownResponse = createSoapResponse(anXmlBody);

    Object aResult = _theProcessor.deserialize(new ByteArrayInputStream(aShutdownResponse.getBytes()));
    assertNotNull("The result object should not be null", aResult);
    assertTrue("The result object is not an Envelope",
               aResult instanceof Envelope);

    Envelope anEnvelope = (Envelope) aResult;
    assertResponse(anEnvelope);

    Body aBody = anEnvelope.getBody();
    assertEquals("The size of the object list of the body is invalid", 1,
                 aBody.getObjects().size());
    assertTrue("The object of the body is not a Shutdown",
               aBody.getObjects().get(0) instanceof Shutdown);

    Shutdown aShutdown = (Shutdown) aBody.getObjects().get(0);
    assertEquals("The command id of the shutdown response is invalid", "1234",
                 aShutdown.getCommandId());
    assertEquals("The requestor of the shutdown command is invalid",
                 "InteropDeserializationTest", aShutdown.getRequestor());
  }

  /**
   *
   */
  public void testSOAPFaultResponse() throws Exception {
    String anXmlBody = "<SOAP-ENV:Fault>" +
                       "<faultcode>... some code ...</faultcode>" +
                       "<faultactor>... some actor ...</faultactor>" +
                       "<faultstring>... some message ...</faultstring>" +
                       "<detail>... some details ...</detail>" +
                       "</SOAP-ENV:Fault>";
    String aFaultResponse = createSoapResponse(anXmlBody);

    Object aResult = _theProcessor.deserialize(new ByteArrayInputStream(aFaultResponse.getBytes()));
    assertNotNull("The result object should not be null", aResult);
    assertTrue("The result object is not an Envelope",
               aResult instanceof Envelope);

    Envelope anEnvelope = (Envelope) aResult;
    assertResponse(anEnvelope);

    Body aBody = anEnvelope.getBody();
    assertEquals("The size of the object list of the body is invalid", 1,
                 aBody.getObjects().size());
    assertTrue("The object of the body is not a Fault",
               aBody.getObjects().get(0) instanceof Fault);

    Fault aFault = (Fault) aBody.getObjects().get(0);
    assertEquals("The code of the fault response is invalid",
                 "... some code ...", aFault.getFaultcode());
    assertEquals("The actor of the fault response is invalid",
                 "... some actor ...", aFault.getFaultactor());
    assertEquals("The string of the fault response is invalid",
                 "... some message ...", aFault.getFaultstring());
    assertEquals("The details of the fault response is invalid",
                 "... some details ...", aFault.getDetail());
  }

  /**
   *
   */
  private String createSoapRequest(String aBody) {
    return "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" +
           "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
           "<SOAP-ENV:Header>" +
           "<CORUS-IOP:Process xmlns:CORUS-IOP=\"http://schemas.sapia-oss.org/corus/interoperability/\"" +
           " corusPid=\"2045\"" + " requestId=\"134\" />" +
           "</SOAP-ENV:Header>" + "<SOAP-ENV:Body>" + aBody +
           "</SOAP-ENV:Body>" + "</SOAP-ENV:Envelope>";
  }

  /**
   *
   */
  private String createSoapResponse(String aBody) {
    return "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" +
           "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
           "<SOAP-ENV:Header>" +
           "<CORUS-IOP:Server xmlns:CORUS-IOP=\"http://schemas.sapia-oss.org/corus/interoperability/\"" +
           " requestId=\"134\"" + " processingTime=\"250\" />" +
           "</SOAP-ENV:Header>" + "<SOAP-ENV:Body>" + aBody +
           "</SOAP-ENV:Body>" + "</SOAP-ENV:Envelope>";
  }

  private void assertRequest(Envelope anEnvelope) {
    assertNotNull("The header of the envelope should not be null",
                  anEnvelope.getHeader());
    assertNotNull("The body of the envelope should not be null",
                  anEnvelope.getBody());

    Header aHeader = anEnvelope.getHeader();
    assertEquals("The size of the object list of the header is invalid", 1,
                 aHeader.getObjects().size());
    assertTrue("The object of the header is not a Process",
               aHeader.getObjects().get(0) instanceof Process);

    Process aProcess = (Process) aHeader.getObjects().get(0);
    assertEquals("The corus pid of the process is invalid", "2045",
                 aProcess.getCorusPid());
    assertEquals("The request id of the process is invalid", "134",
                 aProcess.getRequestId());
  }

  private void assertResponse(Envelope anEnvelope) {
    assertNotNull("The header of the envelope should not be null",
                  anEnvelope.getHeader());
    assertNotNull("The body of the envelope should not be null",
                  anEnvelope.getBody());

    Header aHeader = anEnvelope.getHeader();
    assertEquals("The size of the object list of the header is invalid", 1,
                 aHeader.getObjects().size());
    assertTrue("The object of the header is not a Server",
               aHeader.getObjects().get(0) instanceof Server);

    Server aServer = (Server) aHeader.getObjects().get(0);
    assertEquals("The processing time of the server header is invalid", 250,
                 aServer.getProcessingTime());
    assertEquals("The request id of the server header is invalid", "134",
                 aServer.getRequestId());
  }
}
