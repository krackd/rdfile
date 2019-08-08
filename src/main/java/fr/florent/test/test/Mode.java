package fr.florent.test.test;

public enum Mode {
  PLAY,
  DELETE;
  
  public static Mode fromValue(String value) {
    switch (value) {
    case "delete":
      return Mode.DELETE;
    case "play":
    default:
      return Mode.PLAY;
    }
  }
}
