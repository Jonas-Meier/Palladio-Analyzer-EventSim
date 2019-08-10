package edu.kit.ipd.sdq.eventsim.workload.hla.adaption;

import java.util.Map;


//TODO: save conversion types of AbstractConverters s.t. we can find the correct conversion we want, given 
//TODO: maybe add methods fromByteArray() and toByteArray() to AbstractConverter ? 
public class EncoderService {

	
	protected Map<String, AbstractConverter> conversionTypeToConverter;
	
	
	public void addConverter(AbstractConverter converter) {
		conversionTypeToConverter.put(converter.getConversionType(), converter);
	}
	
	@SuppressWarnings("unchecked")
	public <E> E convert(String requiredClass, byte[] array){
		return (E) conversionTypeToConverter.get(requiredClass).fromByteArray(array);
	}
	
	@SuppressWarnings("unchecked")
	public <E> byte[] convert(E value) {
		return conversionTypeToConverter.get(value.getClass().getTypeName()).toByteArray(value);
	}
	
}
