package org.sapia.corus.interop.soap;

import org.sapia.util.xml.Namespace;
import org.sapia.util.xml.idefix.SerializationContext;
import org.sapia.util.xml.idefix.SerializationException;
import org.sapia.util.xml.idefix.SerializerIF;
import org.sapia.util.xml.idefix.SerializerNotFoundException;
import org.sapia.util.xml.idefix.XmlBuffer;


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
public class SoapSerializer implements SerializerIF {
  /**
   * Serializes the Envelope passed in into XML.
   *
   * @param anEnvelope The envelope obkect to serialize.
   * @param aNamespace The namespace definition to use.
   * @param aContext The context to use to serialize the envelope.
   * @throws SerializationException If an error occurs while serializing the envelope.
   */
  public void serializeEnvelope(Envelope anEnvelope, Namespace aNamespace,
                                SerializationContext aContext)
                         throws SerializationException {
    try {
      XmlBuffer aBuffer = aContext.getXmlBuffer();

      // Start the Envelope element 
      aBuffer.addNamespace(aNamespace.getURI(), aNamespace.getPrefix())
             .startElement(aNamespace.getURI(), "Envelope").endAttribute();

      // Serialize the Header
      if (anEnvelope.getHeader().getObjects().size() > 0) {
        SerializerIF aSerializer = aContext.getSerializerFactory()
                                           .getSerializer(anEnvelope.getHeader()
                                                                    .getObjects()
                                                                    .getClass());

        aBuffer.startElement(aNamespace.getURI(), "Header");
        aSerializer.serialize(anEnvelope.getHeader().getObjects(), aContext);
        aBuffer.endElement(aNamespace.getURI(), "Header");
      }

      // Serialize the Body    
      if (anEnvelope.getBody().getObjects().size() > 0) {
        SerializerIF aSerializer = aContext.getSerializerFactory()
                                           .getSerializer(anEnvelope.getBody()
                                                                    .getObjects()
                                                                    .getClass());

        aBuffer.startElement(aNamespace.getURI(), "Body");
        aSerializer.serialize(anEnvelope.getBody().getObjects(), aContext);
        aBuffer.endElement(aNamespace.getURI(), "Body");
      }

      // End the Envelope Element
      aBuffer.endElement(aNamespace.getURI(), "Envelope").removeNamespace(aNamespace.getURI());
    } catch (SerializerNotFoundException snfe) {
      throw new SerializationException("Unable to get a serializer for the content of the Envelope",
                                       snfe);
    } catch (SerializationException se) {
      throw new SerializationException("Unable to serialize the content of the Envelope",
                                       se);
    }
  }

  /**
   * Serializes the Fault passed in into XML.
   *
   * @param aFault The fault object to serialize.
   * @param aNamespace The namespace definition to use.
   * @param aContext The context to use to serialize the fault.
   * @throws SerializationException If an error occurs while serializing the fault.
   */
  public void serializeFault(Fault aFault, Namespace aNamespace,
                             SerializationContext aContext)
                      throws SerializationException {
    XmlBuffer aBuffer = aContext.getXmlBuffer();

    // Start the Fault element 
    aBuffer.addNamespace(aNamespace.getURI(), aNamespace.getPrefix())
           .startElement(aNamespace.getURI(), "Fault").endAttribute();

    // Serialize the fault code
    if (aFault.getFaultcode() != null) {
      aBuffer.startElement("faultcode").addContent(aFault.getFaultcode())
             .endElement("faultcode");
    }

    // Serialize the fault actor
    if (aFault.getFaultactor() != null) {
      aBuffer.startElement("faultactor").addContent(aFault.getFaultactor())
             .endElement("faultactor");
    }

    // Serialize the fault string
    if (aFault.getFaultstring() != null) {
      aBuffer.startElement("faultstring").addContent(aFault.getFaultstring())
             .endElement("faultstring");
    }

    // Serialize the detail
    if (aFault.getDetail() != null) {
      try {
        SerializerIF aSerializer = aContext.getSerializerFactory()
                                           .getSerializer(aFault.getDetail()
                                                                .getClass());

        aBuffer.startElement("detail");
        aSerializer.serialize(aFault.getDetail(), aContext);
        aBuffer.endElement("detail");
      } catch (SerializerNotFoundException snfe) {
        throw new SerializationException("Unable to get a serializer for the detail of the Fault",
                                         snfe);
      } catch (SerializationException se) {
        throw new SerializationException("Unable to serialize the detail of the Fault",
                                         se);
      }
    }

    // End the Fault element             
    aBuffer.endElement(aNamespace.getURI(), "Fault").removeNamespace(aNamespace.getURI());
  }

  /**
   * Transforms the object passed in into an XML representation.
   *
   * @param anObject The object to serialize.
   * @param aContext The serialization context to use.
   * @exception SerializationException If an error occurs serializing the object.
   */
  public void serialize(Object anObject, SerializationContext aContext)
                 throws SerializationException {
    Namespace aNamespace = aContext.getNamespaceFactory().getNamespaceFor(anObject.getClass());
    serialize(anObject, aNamespace, "", aContext);
  }

  /**
   * Transforms the object passed in into an XML representation. This method is called when the
   * object to transform is nested inside another object.
   *
   * @param anObject The object to serialize.
   * @param aNamespace The namespace of the object to serialize.
   * @param anObjectName The name of the object to serialize.
   * @param aContext The serialization context to use.
   * @exception SerializationException If an error occurs serializing the object.
   */
  public void serialize(Object anObject, Namespace aNamespace,
                        String anObjectName, SerializationContext aContext)
                 throws SerializationException {
    if (anObject instanceof Envelope) {
      serializeEnvelope((Envelope) anObject, aNamespace, aContext);
    } else if (anObject instanceof Fault) {
      serializeFault((Fault) anObject, aNamespace, aContext);
    } else {
      throw new SerializationException("Unable to serialize the object: " +
                                       anObject);
    }
  }
}
