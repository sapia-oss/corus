package org.sapia.corus.util;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class TemplateParser {
  public static Template parse(String content) {
    String current;
    List   elements = new ArrayList();

    int    lastPos = 0;

    while (lastPos < content.length()) {
      int idx = content.indexOf("${", lastPos);

      if (idx < 0) {
        current = content.substring(lastPos);
        elements.add(new Text(current));

        break;
      } else {
        current = content.substring(lastPos, idx);
        elements.add(new Text(current));
        lastPos = idx + 2;

        if (lastPos >= content.length()) {
          current = content.substring(lastPos);
          elements.add(new Text(current));

          break;
        } else {
          idx = content.indexOf("}", lastPos);

          if (idx < 0) {
            current = "${" + content.substring(lastPos);
            elements.add(new Text(current));

            break;
          } else {
            current = content.substring(lastPos, idx);
            elements.add(new Var(current));
            lastPos = idx + 1;
          }
        }
      }
    }

    return new Template(elements);
  }

  public static void main(String[] args) {
    String   msg = "Hello World";
    Template t = TemplateParser.parse(msg);
    System.out.println(t.render(new DefaultContext()));
  }
}
