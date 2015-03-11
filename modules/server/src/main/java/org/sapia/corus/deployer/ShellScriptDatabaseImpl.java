package org.sapia.corus.deployer;

import java.util.Comparator;
import java.util.List;

import org.sapia.corus.client.services.database.DbMap;
import org.sapia.corus.client.services.deployer.ScriptNotFoundException;
import org.sapia.corus.client.services.deployer.ShellScript;
import org.sapia.corus.client.services.deployer.ShellScriptCriteria;
import org.sapia.corus.util.IteratorFilter;
import org.sapia.corus.util.Matcher;

/**
 * Implements the {@link ShellScriptDatabase} interface on top of a
 * {@link DbMap}.
 * 
 * @author yduchesne
 * 
 */
public class ShellScriptDatabaseImpl implements ShellScriptDatabase {

  private DbMap<String, ShellScript> scripts;

  /**
   * @param map
   *          the {@link DbMap} that this instance will use.
   */
  public ShellScriptDatabaseImpl(DbMap<String, ShellScript> map) {
    this.scripts = map;
  }

  @Override
  public void addScript(ShellScript script) {
    scripts.put(script.getAlias(), script);
  }

  @Override
  public void removeScript(String alias) {
    scripts.remove(alias);
  }

  @Override
  public List<ShellScript> removeScript(final ShellScriptCriteria criteria) {
    List<ShellScript> toReturn = new IteratorFilter<ShellScript>(new Matcher<ShellScript>() {
      @Override
      public boolean matches(ShellScript script) {
        return criteria.getAlias().matches(script.getAlias());
      }
    }).filter(scripts.values()).sort(new ShellScriptComparator()).get();

    for (ShellScript s : toReturn) {
      scripts.remove(s.getAlias());
    }

    return toReturn;
  }

  @Override
  public List<ShellScript> getScripts() {
    return new IteratorFilter<ShellScript>(new Matcher.MatchAll<ShellScript>()).filter(scripts.values()).sort(new ShellScriptComparator()).get();
  }

  @Override
  public List<ShellScript> getScripts(final ShellScriptCriteria criteria) {
    return new IteratorFilter<ShellScript>(new Matcher<ShellScript>() {
      @Override
      public boolean matches(ShellScript script) {
        return criteria.getAlias().matches(script.getAlias());
      }
    }).filter(scripts.values()).sort(new ShellScriptComparator()).get();

  }

  @Override
  public ShellScript getScript(String alias) throws ScriptNotFoundException {
    ShellScript script = scripts.get(alias);
    if (script == null) {
      throw new ScriptNotFoundException("No script found for: " + alias);
    }
    return script;
  }

  // ==========================================================================
  // Inner classes

  public class ShellScriptComparator implements Comparator<ShellScript> {

    @Override
    public int compare(ShellScript s1, ShellScript s2) {
      return s1.getAlias().compareTo(s2.getAlias());
    }

  }
}
