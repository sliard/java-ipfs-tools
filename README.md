# java-ipfs-tools

A sample project to explain how calculate IPFS Cid for small data. That do not manage data bigger than 256Ko because I did not code chunk management.

I was wondering why I couldn't find the same IPFS Cid with my Java code as with cli.

    MessageDigest sha256Digest = MessageDigest.getInstance("SHA-256");
    byte[] certHash2 = sha256Digest.digest("hello world".getBytes());
    Multihash multihashHello = new Multihash(Multihash.Type.sha2_256, certHash2);
    Cid helloCid = Cid.build(0, Cid.Codec.Raw, multihashHello);
    cid = QmaozNR7DZHQK1ZcU9p7QdrshMvXqWK6gpu5rmrkPdT3L4


    > echo "hello world" | ipfs add -Q --only-hash
    cid = QmT78zSuBmuS4z925WZfrqQ1qHaJ56DQaTfyMUF7F8ff5o

My error was to forget to create a merkle DAG with my String. We also need to add END_OF_FILE character at the end of string.

    // Add END OF FILE
    byte[] data = "hello world".getBytes();
    byte[] allData = new byte[data.length + 1];
    System.arraycopy(data, 0, allData, 0, data.length);
    allData[allData.length-1] = 0xa;

    // create protobuf of Unixfs Data
    Unixfs.Data.Builder dataBuilder = Unixfs.Data.newBuilder();
    dataBuilder.setData(ByteString.copyFrom(allData));
    dataBuilder.setType(Unixfs.Data.DataType.File);
    dataBuilder.setFilesize(allData.length);
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

    // Make a hash SAH256 of Merkledag PBNode
    MessageDigest sha256Digest = MessageDigest.getInstance("SHA-256");
    byte[] nodeHash = sha256Digest.digest(nodeProtoStream.toByteArray());
    Multihash multiHashData = new Multihash(Multihash.Type.sha2_256, nodeHash);
    Cid.build(0, Cid.Codec.Raw, multiHashData);
    cid = QmT78zSuBmuS4z925WZfrqQ1qHaJ56DQaTfyMUF7F8ff5o

Hope that can help you to better understand IPFS Cid
