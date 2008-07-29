package org.sapia.corus.interop;

import org.sapia.corus.interop.soap.Envelope;
import org.sapia.corus.interop.soap.Fault;
import org.sapia.corus.interop.soap.SoapSerializer;

import org.sapia.util.xml.Namespace;
import org.sapia.util.xml.ProcessingException;
import org.sapia.util.xml.confix.CompositeObjectFactory;
import org.sapia.util.xml.confix.ConfixProcessorFactory;
import org.sapia.util.xml.confix.ConfixProcessorIF;
import org.sapia.util.xml.confix.ReflectionFactory;
import org.sapia.util.xml.idefix.CompositeNamespaceFactory;
import org.sapia.util.xml.idefix.DefaultNamespaceFactory;
import org.sapia.util.xml.idefix.DefaultSerializerFactory;
import org.sapia.util.xml.idefix.IdefixProcessorFactory;
import org.sapia.util.xml.idefix.IdefixProcessorIF;
import org.sapia.util.xml.idefix.PatternNamespaceFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * The Interop processor provides methods to serializa to xml and deserialize
 * from xml the interop messages. It act as the central processor to perform
 * these operations.
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
public class InteropProcessor {
  /** Defines the namespace URI of inerop */
  public static final String INTEROP_NAMESPACE_URI = "http://schemas.sapia-oss.org/corus/interoperability/";

  /** Defines the namespace URI of SOAP */
  public static final String SOAP_NAMESPACE_URI = "http://schemas.xmlsoap.org/soap/envelope/";

  /** The Confix processor to transform the XML into objects */
  private ConfixProcessorIF _theConfixProcessor;

  /** The Idefic processor to transform the XML into objects */
  private IdefixProcessorIF _theIdefixProcessor;

  /**
   * Creates a new InteropProcessor instance.
   */
  public InteropProcessor() {
    initializeConfix();
    initializeIdefix();
  }

  /**
   * Initialize the Confix processor to perform the deserialization.
   */
  private void initializeConfix() {
    // Create the reflection factories for the different packages
    ReflectionFactory anInteropObjectFactory = new ReflectionFactory(new String[] {
                                                                       "org.sapia.corus.interop"
                                                                     });
    ReflectionFactory aSoapObjectFactory = new ReflectionFactory(new String[] {
                                                                   "org.sapia.corus.interop.soap"
                                                                 });

    // Create the composite factory associating the XML namespace for each factory
    CompositeObjectFactory aCompositeFactory = new CompositeObjectFactory();
    aCompositeFactory.registerFactory(SOAP_NAMESPACE_URI, aSoapObjectFactory);
    aCompositeFactory.registerFactory(INTEROP_NAMESPACE_URI,
                                      anInteropObjectFactory);

    // Create the Confix processor
    _theConfixProcessor = ConfixProcessorFactory.newFactory().createProcessor(aCompositeFactory);
  }

  /**
   * Initialize the Confix processor to perform the serialization.
   */
  private void initializeIdefix() {
    // Define the namespace factories for SOAP and Interop
    PatternNamespaceFactory aPatternNSFactory = new PatternNamespaceFactory();
    aPatternNSFactory.addNamespace("org.sapia.corus.interop.soap.*",
                                   new Namespace(SOAP_NAMESPACE_URI, "SOAP-ENV"));
    aPatternNSFactory.addNamespace("org.sapia.corus.interop.*",
                                   new Namespace(INTEROP_NAMESPACE_URI,
                                                 "CORUS-IOP"));

    // Defines the composite namespace factory with the fallback default namespace           
    CompositeNamespaceFactory aCompositeNSFactory = new CompositeNamespaceFactory();
    aCompositeNSFactory.registerNamespaceFactory(aPatternNSFactory);
    aCompositeNSFactory.registerNamespaceFactory(new DefaultNamespaceFactory());

    // Define a specific serializer for SOAP objects in the serializer factory
    DefaultSerializerFactory aSerializerFactory = new DefaultSerializerFactory();
    SoapSerializer           aSoapSerializer = new SoapSerializer();
    aSerializerFactory.registerSerializer(Envelope.class, aSoapSerializer);
    aSerializerFactory.registerSerializer(Fault.class, aSoapSerializer);

    // Create the Idefix processor
    _theIdefixProcessor = IdefixProcessorFactory.newFactory().createProcessor(aSerializerFactory,
                                                                              aCompositeNSFactory);
  }

  /**
   * Processes the input stream passed in using the Confix processor and
   * return the result <CODE>Envelope</CODE> object.
   *
   * @param anInput The input stream to process.
   * @return The create Envelope object.
   * @throws ProcessingException If an error occurs processing the stream or
   *         if the resulted object is not an <CODE>Envelope</CODE>.
   */
  public Envelope deserialize(InputStream anInput) throws ProcessingException {
    Object anObject = null;

    try {
      anObject = _theConfixProcessor.process(anInput);

      return (Envelope) anObject;
    } catch (ClassCastException cce) {
      throw new ProcessingException("The object created from the input stream is not an Envelope: " +
                                    anObject, cce);
    }
  }

  /**
   * Processes the <CODE>Envelope</CODE> object passed in and add the result
   * XML string the output stream.
   *
   * @param anEnvelope The envelope to process.
   * @param anOutput The output stream in which the result is added.
   * @throws ProcessingException If an error occurs while processing the envelope.
   * @throws IOException If an error occurs while writing to the output stream.
   */
  public void serialize(Envelope anEnvelope, OutputStream anOutput)
                 throws ProcessingException, IOException {
    _theIdefixProcessor.process(anEnvelope, anOutput);
  }
}
