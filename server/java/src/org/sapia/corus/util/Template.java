package org.sapia.corus.util;

import java.util.List;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class Template {
  private List _elements;

  Template(List elements) {
    _elements = elements;
  }

  public String render(TemplateContext context) {
    TemplateElement elem;
    StringBuffer    buf = new StringBuffer();

    for (int i = 0; i < _elements.size(); i++) {
      elem = (TemplateElement) _elements.get(i);
      elem.render(context, buf);
    }

    return buf.toString();
  }
}
