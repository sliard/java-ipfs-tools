package com.github.ipfs.tools;

import com.github.ipfs.tools.proto.merkledag.Merkledag;
import com.github.ipfs.tools.proto.unixfs.Unixfs;
import com.google.protobuf.ByteString;
import io.ipfs.cid.Cid;
import io.ipfs.multihash.Multihash;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CidTools {

  private static final int MAX_CHUNK_SIZE = 256 * 1024;
  private static final int DEFAULT_CID_VERSION = 1;
  private static final int END_OF_FILE = 0xa;

  public static Cid getCidForData(int cidVersion, String data) throws IOException, NoSuchAlgorithmException {
    if (data == null) {
      return null;
    }
    if (data.length() > MAX_CHUNK_SIZE) {
      throw new IllegalArgumentException("data bigger than " + MAX_CHUNK_SIZE);
    }
    byte[] allData = new byte[data.length() + 1];
    System.arraycopy(data.getBytes(), 0, allData, 0, data.length());
    allData[allData.length-1] = END_OF_FILE;
    return getCidForData(cidVersion, allData);
  }

  public static Cid getCidForData(String data) throws IOException, NoSuchAlgorithmException {
    return getCidForData(DEFAULT_CID_VERSION, data);
  }

  public static Cid getCidForData(byte[] data) throws IOException, NoSuchAlgorithmException {
    return getCidForData(DEFAULT_CID_VERSION, data);
  }

  public static Cid getCidForData(int cidVersion, byte[] data) throws IOException, NoSuchAlgorithmException {
    if (data == null) {
      return null;
    }
    if (data.length > MAX_CHUNK_SIZE) {
      throw new IllegalArgumentException("data bigger than " + MAX_CHUNK_SIZE);
    }
    // create protobuf of Unixfs Data
    Unixfs.Data.Builder dataBuilder = Unixfs.Data.newBuilder();
    dataBuilder.setData(ByteString.copyFrom(data));
    dataBuilder.setType(Unixfs.Data.DataType.File);
    dataBuilder.setFilesize(data.length);
    Unixfs.Data fileDataProto = dataBuilder.build();
    ByteArrayOutputStream fileDataProtoStream = new ByteArrayOutputStream();
    fileDataProto.writeTo(fileDataProtoStream);

    // create protobuf of Merkledag PBNode
    Merkledag.PBNode.Builder nodeBuilder = Merkledag.PBNode.newBuilder();
    nodeBuilder.clear();
    nodeBuilder.setData(ByteString.copyFrom(fileDataProtoStream.toByteArray()));
    Merkledag.PBNode nodeProto = nodeBuilder.build();
    ByteArrayOutputStream nodeProtoStream = new ByteArrayOutputStream();
    nodeProto.writeTo(nodeProtoStream);

    MessageDigest sha256Digest = MessageDigest.getInstance("SHA-256");
    byte[] nodeHash = sha256Digest.digest(nodeProtoStream.toByteArray());
    Multihash multiHashData = new Multihash(Multihash.Type.sha2_256, nodeHash);

    return Cid.build(cidVersion, Cid.Codec.Raw, multiHashData);
  }
}
