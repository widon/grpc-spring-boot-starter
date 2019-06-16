package net.devh.springboot.autoconfigure.grpc.client;

import io.grpc.Attributes;

public class GrpcClientConstants {
	
    static final Attributes.Key<String> VERSION_ATTR_KEY = Attributes.Key.create("version");
    	
    static final Attributes.Key<String> NAME_ATTR_KEY = Attributes.Key.create("serverName");

}
