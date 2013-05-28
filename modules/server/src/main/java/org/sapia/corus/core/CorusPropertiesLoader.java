package org.sapia.corus.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.sapia.corus.client.common.PropertiesStrLookup;
import org.sapia.corus.util.IOUtil;
import org.sapia.corus.util.PropertiesFilter;
import org.sapia.corus.util.PropertiesTransformer;
import org.sapia.corus.util.PropertiesUtil;
import org.sapia.corus.util.Supplier;

/**
 * Helper class used to load the Corus properties.
 * 
 * @author yduchesne
 *
 */
class CorusPropertiesLoader {

  private CorusPropertiesLoader() {
  }
  
  /**
   * @param corusProps the {@link Properties} to populate.
   * @param corusConfigFiles the Corus config files to load, 
   * in the order in which they're specified in the given list.
   * @throws IOException if an IO error occurs trying to load the given properties in
   * the given file.
   */
  static void load(Properties corusProps, List<File> corusConfigFiles) throws IOException {
    
    List<Supplier<InputStream>> configs = new ArrayList<Supplier<InputStream>>();

    final InputStream defaults = Thread.currentThread().getContextClassLoader().getResourceAsStream("org/sapia/corus/default.properties");
    if(defaults == null){
      throw new IllegalStateException("Resource 'org/sapia/corus/default.properties' not found");
    }

    configs.add(new Supplier<InputStream>() {
      @Override
      public InputStream get() {
        return defaults;
      }
    });
    
    for (final File f : corusConfigFiles) {
      configs.add(new Supplier<InputStream>() {
        @Override
        public InputStream get() {
          try {
            return new FileInputStream(f);
          } catch (IOException e) {
            throw new IllegalStateException("Could not access properties " + f.getAbsolutePath(), e);
          }
        }
      });
    }
    
    doLoad(corusProps, configs);
  }
  
  private static void doLoad(Properties corusProps, List<Supplier<InputStream>> propertySuppliers) throws IOException {
    PropertiesUtil.copy(System.getProperties(), corusProps);
    
    for (Supplier<InputStream> s : propertySuppliers) {
      InputStream supplied = s.get();
      InputStream tmp = IOUtil.replaceVars(new PropertiesStrLookup(corusProps), s.get());
      corusProps.load(tmp);
      supplied.close();
      tmp.close();
    }
    
    // transforming Corus properties that correspond 1-to-1 to Ubik properties into their Ubik counterpart
    PropertiesUtil.transform(
        corusProps, 
        PropertiesTransformer.MappedPropertiesTransformer.createInstance()
          .add(CorusConsts.PROPERTY_CORUS_ADDRESS_PATTERN, org.sapia.ubik.rmi.Consts.IP_PATTERN_KEY)
          .add(CorusConsts.PROPERTY_CORUS_MCAST_ADDRESS,  org.sapia.ubik.rmi.Consts.MCAST_ADDR_KEY)
          .add(CorusConsts.PROPERTY_CORUS_MCAST_PORT,       org.sapia.ubik.rmi.Consts.MCAST_PORT_KEY)
    );
    
    // copying Ubik-specific properties to the System properties. 
    PropertiesUtil.copy(
        PropertiesUtil.filter(corusProps, PropertiesFilter.NamePrefixPropertiesFilter.createInstance("ubik")), 
        System.getProperties()
    );
  }
  
}
