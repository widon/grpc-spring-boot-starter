package net.devh.springboot.autoconfigure.grpc.client;

import io.grpc.Attributes;

public class AttributesTest {

	public static void main(String[] args) {
		Attributes attr = Attributes.EMPTY;
		
		Attributes.Builder  builder = Attributes.newBuilder();
        Attributes.Key<String> nameKey = Attributes.Key.create("personName");
    	builder.set(nameKey, "hwd");
    	attr = builder.build();
    	System.out.println(attr);
    	
    	System.out.println(attr.get(nameKey));
    	
    	 Attributes.Key<String> nameKey1 = Attributes.Key.create("personName");
    	 
    	 System.out.println(attr.get(nameKey1));
	}

}
