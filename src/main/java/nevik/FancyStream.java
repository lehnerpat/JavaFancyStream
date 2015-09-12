/**
 * ISC License terms (http://opensource.org/licenses/isc-license):
 * <p>
 * Copyright (c) 2015, Patrick Lehner <lehner (dot) patrick (at) gmx (dot) de>
 * <p>
 * Permission to use, copy, modify, and/or distribute this software for any purpose with or without fee is hereby
 * granted, provided that the above copyright notice and this permission notice appear in all copies.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT,
 * INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN
 * AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 * PERFORMANCE OF THIS SOFTWARE.
 */
package nevik;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author Patrick Lehner
 * @since 2015-08-26
 */
public class FancyStream<E> implements Stream<E> {
	protected final Stream<E> underlyingStream;

	/**
	 * Make a new {@code FancyStream} containing all the given elements in the given order.
	 *
	 * @param elements
	 * 		elements to create the stream from
	 * @param <E>
	 * 		type of elements in this stream
	 * @return a new {@code FancyStream} from the given {@code elements}
	 *
	 * @see #makeStream(Object[])
	 * @see java.util.stream.Stream#of(Object[])
	 */
	@SafeVarargs
	public static <E> FancyStream<E> of(E... elements) {
		return new FancyStream<>(Stream.of(elements));
	}

	/**
	 * Make a new {@code FancyStream} containing all the given elements in the given order.
	 * <p>
	 * Alias of {@link #of(Object[])} for use with static imports (to avoid name clashes and improve readability).
	 *
	 * @param elements
	 * 		elements to create the stream from
	 * @param <E>
	 * 		type of elements in this stream
	 * @return a new {@code FancyStream} from the given {@code elements}
	 *
	 * @see #of(Object[])
	 */
	@SafeVarargs
	public static <E> FancyStream<E> makeStream(final E... elements) {
		return new FancyStream<>(Stream.of(elements));
	}

	/**
	 * Make a new {@code FancyStream} from the given stream.
	 * <p>
	 * This is a convenience factory method that is equivalent to calling the constructor {@link
	 * #FancyStream(java.util.stream.Stream)}. The static method can be imported statically to make using code shorter
	 * and more readable.
	 *
	 * @param underlyingStream
	 * 		the stream to make fancy
	 * @param <E>
	 * 		type of elements in the resulting fancy stream
	 * @return a new {@code FancyStream} containing all the elements from the given stream
	 *
	 * @throws NullPointerException
	 * 		if {@code underlyingStream} is {@code null}
	 * @see #FancyStream(java.util.stream.Stream)
	 */
	public static <E> FancyStream<E> makeFancy(final Stream<E> underlyingStream) {
		return new FancyStream<>(underlyingStream);
	}

	/**
	 * Make a new {@code FancyStream} containing all the elements from the given collection, in the order returned by
	 * the collection's {@link java.util.Collection#stream() stream()} method.
	 *
	 * @param collection
	 * 		collection whose elements to stream
	 * @param <E>
	 * 		type of elements in the resulting fancy stream
	 * @return a new {@code FancyStream} containing all the elements from the given collection
	 *
	 * @throws NullPointerException
	 * 		if {@code collection} is {@code null}
	 */
	public static <E> FancyStream<E> makeStream(final Collection<E> collection) {
		return makeFancy(collection.stream());
	}

	/**
	 * Make a new (sequential) {@code FancyStream} containing all the elements from the given {@link Iterable}, in the
	 * order returned its {@link java.util.Spliterator}.
	 *
	 * @param iterable
	 * 		iterable whose elements to stream
	 * @param <E>
	 * 		type of elements in the resulting fancy stream
	 * @return a new {@code FancyStream} containing all the elements from the given iterable
	 *
	 * @throws NullPointerException
	 * 		if {@code iterable} is {@code null}
	 * @see java.util.stream.StreamSupport#stream(java.util.Spliterator, boolean)
	 */
	public static <E> FancyStream<E> makeStream(final Iterable<E> iterable) {
		return makeStream(iterable, /*parallel?*/false);
	}

	/**
	 * Make a new {@code FancyStream} containing all the elements from the given {@link Iterable}, in the order returned
	 * its {@link java.util.Spliterator}. The stream can be made parallel by setting the corresponding argument to
	 * {@code true}.
	 *
	 * @param iterable
	 * 		iterable whose elements to stream
	 * @param parallel
	 * 		whether to make the resulting stream parallel
	 * @param <E>
	 * 		type of elements in the resulting fancy stream
	 * @return a new {@code FancyStream} containing all the elements from the given iterable
	 *
	 * @throws NullPointerException
	 * 		if {@code iterable} is {@code null}
	 * @see java.util.stream.StreamSupport#stream(java.util.Spliterator, boolean)
	 * @see #makeStream(Iterable)
	 */
	private static <E> FancyStream<E> makeStream(final Iterable<E> iterable, final boolean parallel) {
		return makeFancy(StreamSupport.stream(Objects.requireNonNull(iterable.spliterator()), parallel));
	}

	/**
	 * Make a fancy stream containing the elements of {@code a}, followed by the elements of {@code b}.
	 *
	 * @param a
	 * 		first stream of elements
	 * @param b
	 * 		second stream of elements
	 * @param <E>
	 * 		type of elements in the resulting fancy stream
	 * @return new fancy stream containing all elements from {@code a} and {@code b}
	 *
	 * @throws NullPointerException
	 * 		if {@code a} or {@code b} is {@code null}
	 */
	public static <E> FancyStream<E> concat(final Stream<? extends E> a, final Stream<? extends E> b) {
		return makeFancy(Stream.concat(a, b));
	}

	/**
	 * Make a new sequential unordered {@code FancyStream} that contains the given {@code element} exactly {@code n}
	 * times.
	 *
	 * @param element
	 * 		the item to repeat; may be {@code null}
	 * @param n
	 * 		the number of times to repeat the item
	 * @param <E>
	 * 		the type of the element
	 * @return a new sequential unordered {@code FancyStream} that repeats {@code element} exactly {@code n} times
	 */
	public static <E> FancyStream<E> repeat(final E element, final long n) {
		return makeFancy(Stream.generate(() -> element).limit(n));
	}

	/**
	 * Make a new infinite sequential unordered {@code FancyStream} repeating the given {@code element}.
	 *
	 * @param element
	 * 		the item to repeat; may be {@code null}
	 * @param <E>
	 * 		the type of the element
	 * @return a new infinite sequential unordered {@code FancyStream} repeating {@code element}
	 */
	public static <E> FancyStream<E> repeat(final E element) {
		return makeFancy(Stream.generate(() -> element));
	}

	/**
	 * Make a new {@code FancyStream} from the given stream.
	 *
	 * @param underlyingStream
	 * 		the stream to make fancy
	 * @throws NullPointerException
	 * 		if {@code underlyingStream} is {@code null}
	 * @see #makeFancy(java.util.stream.Stream)
	 */
	public FancyStream(final Stream<E> underlyingStream) {
		this.underlyingStream = Objects.requireNonNull(underlyingStream);
	}

	/**
	 * Collect the elements of this stream into a {@link java.util.Set}.
	 *
	 * @return a set containing all elements of this stream
	 *
	 * @see java.util.stream.Collectors#toSet()
	 */
	public Set<E> toSet() {
		return collect(Collectors.toSet());
	}

	public Set<E> toUnmodifiableSet() {
		return Collections.unmodifiableSet(toSet());
	}

	/**
	 * Collect the elements of this stream into a {@link java.util.List}.
	 *
	 * @return a list containing all elements of this stream
	 *
	 * @see java.util.stream.Collectors#toList()
	 */
	public List<E> toList() {
		return collect(Collectors.toList());
	}

	public List<E> toUnmodifiableList() {
		return Collections.unmodifiableList(toList());
	}

	/**
	 * Collect the elements of this stream into a {@link java.util.Collection} created by the given {@code
	 * collectionFactory}.
	 *
	 * @param collectionFactory
	 * 		a factory to create a new, empty {@code Collection} of the appropriate type
	 * @param <C>
	 * 		the desired collection type
	 * @return a collection containing all elements of this stream
	 *
	 * @see java.util.stream.Collectors#toCollection(java.util.function.Supplier)
	 */
	public <C extends Collection<E>> C toCollection(final Supplier<C> collectionFactory) {
		return collect(Collectors.toCollection(collectionFactory));
	}

	/**
	 * Collect the elements of this stream into a map, using the stream's elements as the keys and use values provided
	 * by the given {@code valueMapper}.
	 *
	 * @param valueMapper
	 * 		a function to derive a value from a key
	 * @param <V>
	 * 		the type for values in the resulting map
	 * @return a map from all elements of this stream as keys to the values provided by the given {@code valueMapper}
	 */
	public <V> Map<E, V> asMapKeys(final Function<? super E, ? extends V> valueMapper) {
		return collect(Collectors.toMap(Function.identity(), valueMapper));
	}

	/**
	 * Return a stream that contains all elements from this stream followed by all elements of the given {@code
	 * otherStream}.
	 *
	 * @param otherStream
	 * 		another stream of elements to append to this one
	 * @return the combined stream of the elements in this and the other stream
	 *
	 * @throws NullPointerException
	 * 		if {@code otherStream} is {@code null}
	 * @see #concat(java.util.stream.Stream, java.util.stream.Stream)
	 */
	public FancyStream<E> append(final Stream<? extends E> otherStream) {
		return concat(this, otherStream);
	}

	/**
	 * Returns a stream consisting of the elements of this stream that match the given predicate.
	 * <p>
	 * This is an intermediate operation.
	 *
	 * @param predicate
	 * 		a <i>non-interfering</i>, <i>stateless</i> predicate to apply to each element to determine if it should be
	 * 		included
	 * @return the new stream
	 *
	 * @see java.util.stream.Stream#filter(java.util.function.Predicate)
	 */
	@Override
	public FancyStream<E> filter(final Predicate<? super E> predicate) {
		return makeFancy(underlyingStream.filter(predicate));
	}

	/**
	 * Returns a stream consisting of the elements of this stream that <b>do not</b> match the given predicate.
	 * <p>
	 * This is an intermediate operation.
	 * <p>
	 * (This method is like {@link #filter(java.util.function.Predicate)}, but uses the given predicate to determine
	 * which elements to remove instead of which to keep.)
	 *
	 * @param predicate
	 * 		a <i>non-interfering</i>, <i>stateless</i> predicate to apply to each element to determine if it should
	 * 		<b>not</b> be included
	 * @return the new stream
	 *
	 * @throws NullPointerException
	 * 		if {@code predicate} is {@code null}
	 * @see #filter(java.util.function.Predicate)
	 * @see java.util.stream.Stream#filter(java.util.function.Predicate)
	 */
	public FancyStream<E> filterInvert(final Predicate<? super E> predicate) {
		return filter(predicate.negate());
	}

	@Override
	public <R> FancyStream<R> map(final Function<? super E, ? extends R> mapper) {
		return makeFancy(underlyingStream.map(mapper));
	}

	public <R> FancyStream<R> splitMap(final Predicate<? super E> splitPredicate, //
			final Function<? super E, ? extends R> matchingMapper, //
			final Function<? super E, ? extends R> nonMatchingMapper) {
		Objects.requireNonNull(splitPredicate);
		Objects.requireNonNull(matchingMapper);
		Objects.requireNonNull(nonMatchingMapper);
		return map(e -> splitPredicate.test(e) ? matchingMapper.apply(e) : nonMatchingMapper.apply(e));
	}

	@Override
	public IntStream mapToInt(final ToIntFunction<? super E> mapper) {
		return underlyingStream.mapToInt(mapper);
	}

	@Override
	public LongStream mapToLong(final ToLongFunction<? super E> mapper) {
		return underlyingStream.mapToLong(mapper);
	}

	@Override
	public DoubleStream mapToDouble(final ToDoubleFunction<? super E> mapper) {
		return underlyingStream.mapToDouble(mapper);
	}

	@Override
	public <R> FancyStream<R> flatMap(final Function<? super E, ? extends Stream<? extends R>> mapper) {
		return makeFancy(underlyingStream.flatMap(mapper));
	}

	@Override
	public IntStream flatMapToInt(final Function<? super E, ? extends IntStream> mapper) {
		return underlyingStream.flatMapToInt(mapper);
	}

	@Override
	public LongStream flatMapToLong(final Function<? super E, ? extends LongStream> mapper) {
		return underlyingStream.flatMapToLong(mapper);
	}

	@Override
	public DoubleStream flatMapToDouble(final Function<? super E, ? extends DoubleStream> mapper) {
		return underlyingStream.flatMapToDouble(mapper);
	}

	@Override
	public FancyStream<E> distinct() {
		return makeFancy(underlyingStream.distinct());
	}

	@Override
	public FancyStream<E> sorted() {
		return makeFancy(underlyingStream.sorted());
	}

	@Override
	public FancyStream<E> sorted(final Comparator<? super E> comparator) {
		return makeFancy(underlyingStream.sorted(comparator));
	}

	@Override
	public FancyStream<E> peek(final Consumer<? super E> action) {
		return makeFancy(underlyingStream.peek(action));
	}

	@Override
	public FancyStream<E> limit(final long maxSize) {
		return makeFancy(underlyingStream.limit(maxSize));
	}

	@Override
	public FancyStream<E> skip(final long n) {
		return makeFancy(underlyingStream.skip(n));
	}

	@Override
	public void forEach(final Consumer<? super E> action) {
		underlyingStream.forEach(action);
	}

	@Override
	public void forEachOrdered(final Consumer<? super E> action) {
		underlyingStream.forEachOrdered(action);
	}

	@Override
	public Object[] toArray() {
		return underlyingStream.toArray();
	}

	@Override
	public <A> A[] toArray(final IntFunction<A[]> generator) {
		return underlyingStream.toArray(generator);
	}

	@Override
	public E reduce(final E identity, final BinaryOperator<E> accumulator) {
		return underlyingStream.reduce(identity, accumulator);
	}

	@Override
	public Optional<E> reduce(final BinaryOperator<E> accumulator) {
		return underlyingStream.reduce(accumulator);
	}

	@Override
	public <U> U reduce(final U identity, final BiFunction<U, ? super E, U> accumulator,
			final BinaryOperator<U> combiner) {
		return underlyingStream.reduce(identity, accumulator, combiner);
	}

	@Override
	public <R> R collect(final Supplier<R> supplier, final BiConsumer<R, ? super E> accumulator,
			final BiConsumer<R, R> combiner) {
		return underlyingStream.collect(supplier, accumulator, combiner);
	}

	@Override
	public <R, A> R collect(final Collector<? super E, A, R> collector) {
		return underlyingStream.collect(collector);
	}

	@Override
	public Optional<E> min(final Comparator<? super E> comparator) {
		return underlyingStream.min(comparator);
	}

	@Override
	public Optional<E> max(final Comparator<? super E> comparator) {
		return underlyingStream.max(comparator);
	}

	@Override
	public long count() {
		return underlyingStream.count();
	}

	@Override
	public boolean anyMatch(final Predicate<? super E> predicate) {
		return underlyingStream.anyMatch(predicate);
	}

	@Override
	public boolean allMatch(final Predicate<? super E> predicate) {
		return underlyingStream.allMatch(predicate);
	}

	@Override
	public boolean noneMatch(final Predicate<? super E> predicate) {
		return underlyingStream.noneMatch(predicate);
	}

	@Override
	public Optional<E> findFirst() {
		return underlyingStream.findFirst();
	}

	@Override
	public Optional<E> findAny() {
		return underlyingStream.findAny();
	}

	@Override
	public Iterator<E> iterator() {
		return underlyingStream.iterator();
	}

	@Override
	public Spliterator<E> spliterator() {
		return underlyingStream.spliterator();
	}

	@Override
	public boolean isParallel() {
		return underlyingStream.isParallel();
	}

	@Override
	public FancyStream<E> sequential() {
		return makeFancy(underlyingStream.sequential());
	}

	@Override
	public FancyStream<E> parallel() {
		return makeFancy(underlyingStream.parallel());
	}

	@Override
	public FancyStream<E> unordered() {
		return makeFancy(underlyingStream.unordered());
	}

	@Override
	public FancyStream<E> onClose(final Runnable closeHandler) {
		return makeFancy(underlyingStream.onClose(closeHandler));
	}

	@Override
	public void close() {
		underlyingStream.close();
	}
}
