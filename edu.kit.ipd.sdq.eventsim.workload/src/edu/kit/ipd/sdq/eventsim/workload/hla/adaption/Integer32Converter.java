package edu.kit.ipd.sdq.eventsim.workload.hla.adaption;

import hla.rti1516e.encoding.DecoderException;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.encoding.HLAinteger32BE;

public class Integer32Converter extends AbstractConverter<Integer>{

	
	public Integer32Converter(EncoderFactory factory) {
		super(factory);
	}
	
	@Override
	public Integer fromByteArray(byte[] arr){
		HLAinteger32BE i = factory.createHLAinteger32BE();
		try {
			i.decode(arr);
		} catch (DecoderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return i.getValue();
	}
	
	@Override
	public byte[] toByteArray(Integer i){
		HLAinteger32BE s = factory.createHLAinteger32BE(i);
		return s.toByteArray();
	}
	
	@Override
	public String getConversionType() {
		return Integer.class.getTypeName();
	}

}
