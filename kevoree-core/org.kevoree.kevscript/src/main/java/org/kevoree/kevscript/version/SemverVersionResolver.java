package org.kevoree.kevscript.version;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	private Pair<TypeDefinition, Version> getVersion(final TypeDefinition typeDefinition) {
		Pair<TypeDefinition, Version> v;
		try {
			v = new Pair<TypeDefinition, Version>(typeDefinition, Version.valueOf(typeDefinition.getVersion()));
		} catch (final IllegalArgumentException e) {
			v = null;
		} catch (final ParseException e) {
			v = null;
		}
		return v;
	}

	private TypeDefinition lookStrictly(final String expectedVersion, final List<TypeDefinition> availableTypeDef) {

		TypeDefinition ret = null;
		if (availableTypeDef != null) {
			for (final TypeDefinition td : availableTypeDef) {
				if (expectedVersion.equals(td.getVersion())) {
					ret = td;
					break;
				}
			}
		}
		return ret;
	}

	private <T> TypeDefinition searchLatestStable(final List<TypeDefinition> availableTypeDef) {

		/*
		 * we keep only not null type definitions with semantically valid
		 * versions (each valid type definition is paired with it parsed
		 * version).
		 */

		final List<Pair<TypeDefinition, Version>> goodValuesOnly = filterGoodVersions(availableTypeDef);

		/*
		 * we generate two lists, one of final version (true) and one of
		 * pre-release versions (false)
		 */

		final Map<Boolean, List<Pair<TypeDefinition, Version>>> collect = groupByReleaseStatus(goodValuesOnly);

		List<Pair<TypeDefinition, Version>> sorted;
		if (collect.containsKey(true)) {
			/*
			 * if a stable versions are available we opt for the most recent of
			 * them
			 */
			sorted = collect.get(true);
		} else if (collect.containsKey(false)) {
			/* or else we option for the most recent pre-release version. */
			sorted = collect.get(false);
		} else {
			sorted = new ArrayList<>();
		}

		final Comparator<? super Pair<TypeDefinition, Version>> c = new Comparator<Pair<TypeDefinition, Version>>() {

			@Override
			public int compare(final Pair<TypeDefinition, Version> o1, final Pair<TypeDefinition, Version> o2) {

				return o2.snd.compareTo(o1.snd);
			}
		};
		Collections.sort(sorted, c);
		final TypeDefinition ret;
		if (sorted == null || sorted.isEmpty()) {
			ret = null;
		} else {
			ret = sorted.get(0).fst;
		}
		return ret;

	}

	private Map<Boolean, List<Pair<TypeDefinition, Version>>> groupByReleaseStatus(
			final List<Pair<TypeDefinition, Version>> goodValuesOnly) {
		final Map<Boolean, List<Pair<TypeDefinition, Version>>> collect = new HashMap<>();
		for (final Pair<TypeDefinition, Version> td : goodValuesOnly) {
			final boolean side = "".equals(td.snd.getPreReleaseVersion());
			if (!collect.containsKey(side)) {
				collect.put(side, new ArrayList<Pair<TypeDefinition, Version>>());
			}
			collect.get(side).add(td);
		}
		return collect;
	}

	private List<Pair<TypeDefinition, Version>> filterGoodVersions(final List<TypeDefinition> availableTypeDef) {
		final List<Pair<TypeDefinition, Version>> goodValuesOnly = new ArrayList<>();
		for (final TypeDefinition td : availableTypeDef) {
			final Pair<TypeDefinition, Version> z = getVersion(td);
			if (z != null) {
				goodValuesOnly.add(z);

			}
		}
		return goodValuesOnly;
	}

}
