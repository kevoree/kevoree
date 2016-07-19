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

	private Pair<TypeDefinition, Long> getVersion(final TypeDefinition typeDefinition) {
		return new Pair<TypeDefinition, Long>(typeDefinition, Long.parseLong(typeDefinition.getVersion()));
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

		//
		
		/*
		 * we keep only not null type definitions with semantically valid
		 * versions (each valid type definition is paired with it parsed
		 * version).
		 */

		final List<Pair<TypeDefinition, Long>> goodValuesOnly = filterGoodVersions(availableTypeDef);


		final Comparator<? super Pair<TypeDefinition, Long>> c = new Comparator<Pair<TypeDefinition, Long>>() {

			@Override
			public int compare(final Pair<TypeDefinition, Long> o1, final Pair<TypeDefinition, Long> o2) {

				return o2.snd.compareTo(o1.snd);
			}
		};
		Collections.sort(goodValuesOnly, c);
		final TypeDefinition ret;
		if (goodValuesOnly == null || goodValuesOnly.isEmpty()) {
			ret = null;
		} else {
			ret = goodValuesOnly.get(0).fst;
		}
		return ret;

	}

	private List<Pair<TypeDefinition, Long>> filterGoodVersions(final List<TypeDefinition> availableTypeDef) {
		final List<Pair<TypeDefinition, Long>> goodValuesOnly = new ArrayList<>();
		for (final TypeDefinition td : availableTypeDef) {
			final Pair<TypeDefinition, Long> z = getVersion(td);
			if (z != null) {
				goodValuesOnly.add(z);

			}
		}
		return goodValuesOnly;
	}

}
