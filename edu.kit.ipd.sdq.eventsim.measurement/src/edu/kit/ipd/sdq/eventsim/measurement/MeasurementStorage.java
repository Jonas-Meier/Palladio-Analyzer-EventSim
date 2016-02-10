package edu.kit.ipd.sdq.eventsim.measurement;

import java.util.function.Function;

public interface MeasurementStorage {

	/**
	 * Adds a mapping (by means of a {@link Function}) from {@link Object}s of a fixed type to {@link String}s that
	 * uniquely identify (id) objects of the specified {@code type}. This allows to extract ids from arbitrary
	 * {@link Object}s without dictating an interface.
	 * 
	 * @param type
	 *            the type's class
	 * @param extractionFunction
	 *            the function that maps {@code type} instances to id-Strings.
	 */
	void addIdExtractor(Class<? extends Object> type, Function<Object, String> extractionFunction);

	/**
	 * @see #addIdExtractor(Class, Function)
	 * 
	 * @param type
	 *            the type's class
	 * @param extractionFunction
	 *            the function that maps {@code type} instances to name-Strings.
	 */
	void addNameExtractor(Class<? extends Object> type, Function<Object, String> extractionFunction);

	/**
	 * Stores the given measurement.
	 * 
	 * @param m
	 *            the measurement to be added
	 * @param <E>
	 *            the measuring point's type (i.e. the type of the probed element)
	 */
	<E> void put(Measurement<E, ?> m);

	/**
	 * Stores the given measurement.
	 * 
	 * @param m
	 *            the measurement to be added
	 * @param <F>
	 *            first measuring point's type
	 * @param <S>
	 *            second measuring point's type
	 */
	<F, S> void putPair(Measurement<Pair<F, S>, ?> m);

	void finish();

}