package hh.slackbot.slackbot.models;

public class UserMessageDto {
  private String user;
  private String message;

  public UserMessageDto() {
  }

  public String getUser() {
    return this.user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public String getMessage() {
    return this.message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  @Override
  public String toString() {
    return "{" + " user='" + getUser() + "'" + ", message='" + getMessage() + "'" + "}";
  }

}
