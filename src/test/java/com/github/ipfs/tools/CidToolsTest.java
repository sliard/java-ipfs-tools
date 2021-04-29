package com.github.ipfs.tools;

import io.ipfs.cid.Cid;
import io.ipfs.multihash.Multihash;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.assertj.core.api.Assertions.assertThat;

public class CidToolsTest {

  private final static String HELLO_WORLD = "hello world";

  @Test
  public void helloWorld() throws IOException, NoSuchAlgorithmException {
    Cid helloCid = CidTools.getCidForData(0, HELLO_WORLD);
    assertThat(helloCid.toString()).isEqualTo("QmT78zSuBmuS4z925WZfrqQ1qHaJ56DQaTfyMUF7F8ff5o");

    Cid helloV1 = CidTools.getCidForData(1, HELLO_WORLD);
    assertThat(helloV1.toString()).isEqualTo("zb2rhbQmUW7RdAT2Aza4cKxYjnUbZHnnaY9VEU3f98h1QdqHX");
  }

  @Test
  public void helloWorldFile() throws URISyntaxException, IOException, NoSuchAlgorithmException {
    URL resource = getClass().getClassLoader().getResource("hello.txt");
    assertThat(resource).isNotNull();
    File file = new File(resource.toURI());
    byte[] fileData = Files.readAllBytes(file.toPath());
    Cid hello = CidTools.getCidForData(0, fileData);
    assertThat(hello.toString()).isEqualTo("QmT78zSuBmuS4z925WZfrqQ1qHaJ56DQaTfyMUF7F8ff5o");
  }

  @Test
  public void badWay() throws NoSuchAlgorithmException {
    MessageDigest sha256Digest = MessageDigest.getInstance("SHA-256");
    byte[] certHash2 = sha256Digest.digest(HELLO_WORLD.getBytes());
    Multihash multihashHello = new Multihash(Multihash.Type.sha2_256, certHash2);
    Cid helloCid = Cid.build(0, Cid.Codec.Raw, multihashHello);
    assertThat(helloCid.toString()).isEqualTo("QmaozNR7DZHQK1ZcU9p7QdrshMvXqWK6gpu5rmrkPdT3L4");
  }
}
