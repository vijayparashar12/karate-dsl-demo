package parashar.vijay.karate.demo.nakadi;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TokenHelper {
  public static String get(String tokenName) {
    String accessToken = null;
    final String credentials_dir = System.getenv("CREDENTIALS_DIR");
    try {
      accessToken =
          new String(
                  Files.readAllBytes(
                      Paths.get(credentials_dir + "/" + tokenName + "-token-secret")))
              .trim();
    } catch (IOException e) {
      e.printStackTrace();
      accessToken = "N/A";
    }

    return accessToken;
  }
}
