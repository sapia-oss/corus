package org.sapia.corus.util.docker;

import org.apache.commons.lang.StringUtils;
import org.sapia.corus.client.common.ObjectUtil;
import org.sapia.corus.client.common.OptionalValue;

/**
 * Corresponds to a Docker image name, whose literal format is: <code>[user/]image[:tag]</code>
 * 
 * @author yduchesne
 *
 */
public class DockerImageName {

  private static final String DEFAULT_TAG = "latest";
  
  private static final int USER_PART = 0;
  private static final int NAME_PART = 1;

  private static final int NAME_INDEX = 0;
  private static final int TAG_INDEX  = 1;
  
  private String image;
  private String tag = DEFAULT_TAG;
  private OptionalValue<String> user = OptionalValue.none();

  public DockerImageName(String image) {
    this.image = image;
  }
  
  public String getImage() {
    return image;
  }

  public String getTag() {
    return tag;
  }
  
  public OptionalValue<String> getUser() {
    return user;
  }
  
  /**
   * @param user a username.
   * @return a copy of this instance, with the given user.
   */
  public DockerImageName withUser(String user) {
    DockerImageName copy = new DockerImageName(image);
    copy.tag = tag;
    copy.user = OptionalValue.of(user);
    return copy;
  }
  
  /**
   * @param tag a Docker image tag.
   * @return a copy of this instance, with the given tag.
   */
  public DockerImageName withTag(String tag) {
    DockerImageName copy = new DockerImageName(image);
    copy.user = user;
    copy.tag  = tag;
    return copy;
  }
  
  /**
   * @param newImage the new image name.
   * @return a copy of this instance, with the given image name.
   */
  public DockerImageName withImage(String newImage) {
    DockerImageName copy = new DockerImageName(newImage);
    copy.tag = tag;
    copy.user = user;
    return copy;
  }
  
  /**
   * @param imageName the Docker image name to parse.
   * @return the DockerImageName resulting from this operation.
   */
  public static DockerImageName parse(String imageName) {
    String[] parts = StringUtils.split(imageName, "/");
    if (parts.length == 1) {
      String[] nameVersion = StringUtils.split(parts[0], ":");
      if (nameVersion.length == 1) {
        return new DockerImageName(nameVersion[NAME_INDEX]);
      } else if (nameVersion.length == 2){
        DockerImageName img = new DockerImageName(nameVersion[NAME_INDEX]);
        img.tag = nameVersion[TAG_INDEX];
        return img;
      } else {
        throw new IllegalArgumentException("Expected <user>/<image-name>[:tag]. Got: " + imageName);
      }
    } else if (parts.length == 2) {
      String   user        = parts[USER_PART];
      String[] nameVersion = StringUtils.split(parts[NAME_PART], ":");
      if (nameVersion.length == 1) {
        DockerImageName img = new DockerImageName(nameVersion[NAME_INDEX]);
        img.user = OptionalValue.of(user);
        return img;
      } else if (nameVersion.length == 2){
        DockerImageName img = new DockerImageName(nameVersion[NAME_INDEX]);
        img.tag = nameVersion[TAG_INDEX];
        img.user = OptionalValue.of(user);
        return img;
      } else {
        throw new IllegalArgumentException("Expected <user>/<image-name>[:tag]. Got: " + imageName);
      }
    } else {
      throw new IllegalArgumentException("Expected <user>/<image-name>[:tag]. Got: " + imageName);
    }
  }
  
  // --------------------------------------------------------------------------
  // Object overrides
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    if (user.isSet()) {
      sb.append(user.get()).append("/");
    }
    sb.append(image).append(":").append(tag);
    return sb.toString();
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof DockerImageName) {
      DockerImageName other = (DockerImageName) obj;
      return ObjectUtil.safeEquals(user, other.user)
          && ObjectUtil.safeEquals(image, other.image)
          && ObjectUtil.safeEquals(tag, other.tag);
    }
    return false;
  }
  
  @Override
  public int hashCode() {
    return ObjectUtil.safeHashCode(user, image, tag);
  }

}
