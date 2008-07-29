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

import java.io.ByteArrayOutputStream;


/**
 * Class documentation
 *
 * @author <a href="mailto:jc@sapia-oss.org">Jean-Cedric Desrochers</a>
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">
 *     Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *     <a href="http://www.sapia-oss.org/license.html" target="sapia-license">license page</a>
 *     at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class InteropSerializationTest extends TestCase {
  static {
    org.apache.log4j.BasicConfigurator.configure();
  }

  private static InteropProcessor _theProcessor = new InteropProcessor();

  /**
   *
   */
  public InteropSerializationTest(String aName) {
    super(aName);
  }

  /**
   *
   */
  public static void main(String[] args) {
    TestRunner.run(InteropSerializationTest.class);
  }

  /**
   *
   */
  public void testPollRequest() throws Exception {
    // Create the poll command
    Poll aPollCommand = new Poll();
    aPollCommand.setCommandId("675432");

    // Create the SOAP envelope
    Envelope anEnveloppe = createSoapRequest();
    anEnveloppe.getBody().addObject(aPollCommand);

    // Serialize the envelope with the idefix processor
    ByteArrayOutputStream anOutput = new ByteArrayOutputStream();
    _theProcessor.serialize(anEnveloppe, anOutput);

    String aResult = anOutput.toString("UTF-8");

    String aSoapBody =
      "<CORUS-IOP:Poll xmlns:CORUS-IOP=\"http://schemas.sapia-oss.org/corus/interoperability/\"" +
      " commandId=\"675432\" />";

    assertRequest(aSoapBody, aResult);
  }

  /**
   *
   */
  public void testStatusRequest() throws Exception {
    // Create the status command
    Status aStatusCommand = new Status();
    aStatusCommand.setCommandId("675433");

    Context aContext = new Context("someContext");
    aContext.addParam(new Param("param1", "param1_value"));
    aContext.addParam(new Param("param2", "param2_value"));
    aStatusCommand.addContext(aContext);

    aContext = new Context("someOtherContext");
    aContext.addParam(new Param("param3", "param3_value"));
    aContext.addParam(new Param("param4", "param4_value"));
    aStatusCommand.addContext(aContext);

    // Create the SOAP envelope
    Envelope anEnveloppe = createSoapRequest();
    anEnveloppe.getBody().addObject(aStatusCommand);

    // Serialize the envelope with the idefix processor
    ByteArrayOutputStream anOutput = new ByteArrayOutputStream();
    _theProcessor.serialize(anEnveloppe, anOutput);

    String aResult = anOutput.toString("UTF-8");

    String aSoapBody =
      "<CORUS-IOP:Status xmlns:CORUS-IOP=\"http://schemas.sapia-oss.org/corus/interoperability/\"" +
            " commandId=\"675433\">" + "<CORUS-IOP:Context name=\"someContext\">" +
            "<CORUS-IOP:Param name=\"param1\" value=\"param1_value\" />" +
            "<CORUS-IOP:Param name=\"param2\" value=\"param2_value\" />" +
            "</CORUS-IOP:Context>" + "<CORUS-IOP:Context name=\"someOtherContext\">" +
            "<CORUS-IOP:Param name=\"param3\" value=\"param3_value\" />" +
            "<CORUS-IOP:Param name=\"param4\" value=\"param4_value\" />" +
            "</CORUS-IOP:Context>" + "</CORUS-IOP:Status>";

    assertRequest(aSoapBody, aResult);
  }

  /**
   *
   */
  public void testRestartRequest() throws Exception {
    Restart aRestartCommand = new Restart();
    aRestartCommand.setCommandId("6754335");

    // Create the SOAP envelope
    Envelope anEnveloppe = createSoapRequest();
    anEnveloppe.getBody().addObject(aRestartCommand);

    // Serialize the envelope with the idefix processor
    ByteArrayOutputStream anOutput = new ByteArrayOutputStream();
    _theProcessor.serialize(anEnveloppe, anOutput);

    String aResult = anOutput.toString("UTF-8");

    String aSoapBody =
            "<CORUS-IOP:Restart xmlns:CORUS-IOP=\"http://schemas.sapia-oss.org/corus/interoperability/\"" +
            " commandId=\"6754335\" />";

    assertRequest(aSoapBody, aResult);
  }

  /**
   *
   */
  public void testConfirmShutdownRequest() throws Exception {
    ConfirmShutdown aConfirmCommand = new ConfirmShutdown();
    aConfirmCommand.setCommandId("675434");

    // Create the SOAP envelope
    Envelope anEnveloppe = createSoapRequest();
    anEnveloppe.getBody().addObject(aConfirmCommand);

    // Serialize the envelope with the idefix processor
    ByteArrayOutputStream anOutput = new ByteArrayOutputStream();
    _theProcessor.serialize(anEnveloppe, anOutput);

    String aResult = anOutput.toString("UTF-8");

    String aSoapBody =
            "<CORUS-IOP:ConfirmShutdown xmlns:CORUS-IOP=\"http://schemas.sapia-oss.org/corus/interoperability/\"" +
            " commandId=\"675434\" />";

    assertRequest(aSoapBody, aResult);
  }

  /**
   *
   */
  public void testAckResponse() throws Exception {
    Ack anAckResponse = new Ack();
    anAckResponse.setCommandId("675435");

    // Create the SOAP envelope
    Envelope anEnveloppe = createSoapResponse();
    anEnveloppe.getBody().addObject(anAckResponse);

    // Serialize the envelope with the idefix processor
    ByteArrayOutputStream anOutput = new ByteArrayOutputStream();
    _theProcessor.serialize(anEnveloppe, anOutput);

    String aResult = anOutput.toString("UTF-8");

    String aSoapBody =
            "<CORUS-IOP:Ack xmlns:CORUS-IOP=\"http://schemas.sapia-oss.org/corus/interoperability/\"" +
            " commandId=\"675435\" />";

    assertResponse(aSoapBody, aResult);
  }

  /**
   *
   */
  public void testShutdownResponse() throws Exception {
    Shutdown aShutdownResponse = new Shutdown();
    aShutdownResponse.setCommandId("1234");
    aShutdownResponse.setRequestor("InteropSerializationTest");

    // Create the SOAP envelope
    Envelope anEnveloppe = createSoapResponse();
    anEnveloppe.getBody().addObject(aShutdownResponse);

    // Serialize the envelope with the idefix processor
    ByteArrayOutputStream anOutput = new ByteArrayOutputStream();
    _theProcessor.serialize(anEnveloppe, anOutput);

    String aResult = anOutput.toString("UTF-8");

    String aSoapBody =
            "<CORUS-IOP:Shutdown xmlns:CORUS-IOP=\"http://schemas.sapia-oss.org/corus/interoperability/\"" +
            " requestor=\"InteropSerializationTest\"" +
            " commandId=\"1234\" />";

    assertResponse(aSoapBody, aResult);
  }

  /**
   *
   */
  public void testSOAPFaultResponse() throws Exception {
    Fault aFault = new Fault();
    aFault.setFaultcode("... some code ...");
    aFault.setFaultactor("... some actor ...");
    aFault.setFaultstring("... some message ...");
    aFault.setDetail("... some details ...");

    // Create the SOAP envelope
    Envelope anEnveloppe = createSoapResponse();
    anEnveloppe.getBody().addObject(aFault);

    // Serialize the envelope with the idefix processor
    ByteArrayOutputStream anOutput = new ByteArrayOutputStream();
    _theProcessor.serialize(anEnveloppe, anOutput);

    String aResult = anOutput.toString("UTF-8");

    String aSoapBody = "<SOAP-ENV:Fault>" +
                       "<faultcode>... some code ...</faultcode>" +
                       "<faultactor>... some actor ...</faultactor>" +
                       "<faultstring>... some message ...</faultstring>" +
                       "<detail>... some details ...</detail>" +
                       "</SOAP-ENV:Fault>";

    assertResponse(aSoapBody, aResult);
  }

  private Envelope createSoapRequest() {
    Process aProcess = new Process();
    aProcess.setCorusPid("2045");
    aProcess.setRequestId("134");

    Header aHeader = new Header();
    aHeader.addObject(aProcess);

    Envelope anEnveloppe = new Envelope();
    anEnveloppe.setHeader(aHeader);
    anEnveloppe.setBody(new Body());

    return anEnveloppe;
  }

  private void assertRequest(String aRequest, String aResult) {
    String anExpectedResult = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
                              "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                              "<SOAP-ENV:Header>" +
                              "<CORUS-IOP:Process xmlns:CORUS-IOP=\"http://schemas.sapia-oss.org/corus/interoperability/\"" +
                              " corusPid=\"2045\"" + " requestId=\"134\" />" +
                              "</SOAP-ENV:Header>" + "<SOAP-ENV:Body>" +
                              aRequest + "</SOAP-ENV:Body>" +
                              "</SOAP-ENV:Envelope>";

    assertEquals(anExpectedResult, aResult);
  }

  private Envelope createSoapResponse() {
    Server aServer = new Server();
    aServer.setRequestId("134");
    aServer.setProcessingTime(250);

    Header aHeader = new Header();
    aHeader.addObject(aServer);

    Envelope anEnveloppe = new Envelope();
    anEnveloppe.setHeader(aHeader);
    anEnveloppe.setBody(new Body());

    return anEnveloppe;
  }

  private void assertResponse(String aRequest, String aResult) {
    String anExpectedResult = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
                              "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                              "<SOAP-ENV:Header>" +
                              "<CORUS-IOP:Server xmlns:CORUS-IOP=\"http://schemas.sapia-oss.org/corus/interoperability/\"" +
                              " processingTime=\"250\"" +
                              " requestId=\"134\" />" + "</SOAP-ENV:Header>" +
                              "<SOAP-ENV:Body>" + aRequest +
                              "</SOAP-ENV:Body>" + "</SOAP-ENV:Envelope>";

    assertEquals(anExpectedResult, aResult);
  }
}
