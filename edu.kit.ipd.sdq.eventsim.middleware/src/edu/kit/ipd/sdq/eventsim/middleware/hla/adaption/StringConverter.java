package edu.kit.ipd.sdq.eventsim.middleware.hla.adaption;

import hla.rti1516e.encoding.DecoderException;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.encoding.HLAASCIIstring;

public class StringConverter extends AbstractConverter<String>{
	
	public StringConverter(EncoderFactory factory) {
		super(factory);
	}
	
	
	@Override
	public String fromByteArray(byte[] array) {
		HLAASCIIstring s = factory.createHLAASCIIstring();
		try {
			s.decode(array);
		} catch (DecoderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return s.getValue();
	}
	
	@Override
	public byte[] toByteArray(String str){
		HLAASCIIstring s = factory.createHLAASCIIstring(str);
		return s.toByteArray();
	}
	
	@Override
	public String getConversionType() {
		return String.class.getTypeName();
	}
}
