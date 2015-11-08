package org.sapia.corus.cloud.aws.image.userdata;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates a {@link List} of {@link UserDataPopulator}s, which can be modified by
 * inserting in front of it, or at the end of it. It can also be cleared completely.
 * 
 * @author yduchesne
 *
 */
public class UserDataPopulatorChain implements UserDataPopulator {

  private List<UserDataPopulator> delegates = new ArrayList<UserDataPopulator>();

  /**
   * @param delegate a {@link UserDataPopulator} to insert to the front of this 
   * instance's list of of current {@link UserDataPopulator}s.
   */
  public void insert(UserDataPopulator delegate) {
    delegates.add(0, delegate);
  }
  
  /**
   * @param delegate a {@link UserDataPopulator} to append to this instance's list of 
   * of current {@link UserDataPopulator}s.
   */
  public void append(UserDataPopulator delegate) {
    delegates.add(delegate);
  }
  
  @Override
  public void addTo(UserDataContext context) {
    for (UserDataPopulator d : delegates) {
      d.addTo(context);
    }
  }
  
  /**
   * Clears this instance's internal list.
   */
  public void clear() {
    delegates.clear();
  }

  public static UserDataPopulatorChain getDefaultInstance() {
    UserDataPopulatorChain chain = new UserDataPopulatorChain();

    chain.append(new InstallAwsCli());
    chain.append(new YumUpdate());
    chain.append(new YumInstallGit());
    chain.append(new InstallChefSolo());
    chain.append(new InstallCookbooks());
    chain.append(new InstallRecipeAttributes());
    chain.append(new RunChefSolo());
    chain.append(new SetEnv());
    chain.append(new AddCorusInstallStatusProperty());
    
    return chain;
    
  }
  
}
