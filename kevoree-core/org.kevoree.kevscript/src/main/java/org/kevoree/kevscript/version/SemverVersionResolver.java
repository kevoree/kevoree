package org.kevoree.kevscript.version;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.kevoree.TypeDefinition;
import org.kevoree.kevscript.util.Pair;

import com.github.zafarkhaja.semver.ParseException;
import com.github.zafarkhaja.semver.Version;

public class SemverVersionResolver implements IVersionResolver {

	@Override
	public TypeDefinition findBestVersion(final String typeDefName, final String expectedVersion,
			final List<TypeDefinition> availableTypeDef) {
		final TypeDefinition ret;
		if (availableTypeDef != null) {
			if (expectedVersion != null) {
				/* if a version is specified we look for it only */
				ret = lookStrictly(expectedVersion, availableTypeDef);
			} else {
				/*
				 * if no precise version as asked for, we look for the most
				 * recent version
				 */
				ret = searchLatestStable(availableTypeDef);
			}
		} else {
			ret = null;
		}
		return ret;
	}

	private Optional<Pair<TypeDefinition, Version>> getVersion(final TypeDefinition typeDefinition) {
		Optional<Pair<TypeDefinition, Version>> v;
		try {
			v = Optional.of(
					new Pair<TypeDefinition, Version>(typeDefinition, Version.valueOf(typeDefinition.getVersion())));
		} catch (final IllegalArgumentException e) {
			v = Optional.empty();
		} catch (final ParseException e) {
			v = Optional.empty();
		}
		return v;
	}

	private TypeDefinition lookStrictly(final String expectedVersion, final List<TypeDefinition> availableTypeDef) {
		final Optional<TypeDefinition> findFirst = availableTypeDef.stream()
				.filter(a -> expectedVersion.equals(a.getVersion())).findFirst();
		return findFirst.orElse(null);
	}

	private TypeDefinition searchLatestStable(final List<TypeDefinition> availableTypeDef) {

		/*
		 * we keep only not null type definitions with semantically valid
		 * versions (each valid type definition is paired with it parsed
		 * version).
		 */
		final Stream<Pair<TypeDefinition, Version>> goodValuesOnly = availableTypeDef.stream().filter(x -> x != null)
				.map(x -> getVersion(x)).flatMap(x -> x.isPresent() ? Stream.of(x.get()) : Stream.empty());

		/*
		 * we generate two lists, one of final version (true) and one of
		 * pre-release versions (false)
		 */
		final Map<Boolean, List<Pair<TypeDefinition, Version>>> collect = goodValuesOnly
				.collect(Collectors.groupingBy(x -> x.snd.getPreReleaseVersion().equals("")));

		final Comparator<? super Pair<TypeDefinition, Version>> comparator = (e1, e2) -> e2.snd.compareTo(e1.snd);
		Stream<Pair<TypeDefinition, Version>> sorted;
		if (collect.containsKey(true)) {
			/*
			 * if a stable versions are available we opt for the most recent of
			 * them
			 */
			sorted = collect.get(true).stream().sorted(comparator);
		} else if (collect.containsKey(false)) {
			/* or else we option for the most recent pre-release version. */
			sorted = collect.get(false).stream().sorted(comparator);
		} else {
			sorted = Stream.empty();
		}
		return sorted.findFirst().map(x -> x.fst).orElse(null);

	}

}
