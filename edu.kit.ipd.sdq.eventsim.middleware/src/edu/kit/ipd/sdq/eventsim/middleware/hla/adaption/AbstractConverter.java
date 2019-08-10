package edu.kit.ipd.sdq.eventsim.middleware.hla.adaption;

import hla.rti1516e.encoding.EncoderFactory;

public abstract class AbstractConverter<E> {

	protected EncoderFactory factory;
	
	public AbstractConverter(EncoderFactory factory) {
		this.factory = factory;
	}
	
	abstract E fromByteArray(byte[] array);
	
	abstract byte[] toByteArray(E value);
	
	abstract String getConversionType();
	
	public EncoderFactory getFactory() {
		return this.factory;
	}
}
