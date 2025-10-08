import type { PageServerLoad } from './$types';
import type { Instance } from '$lib/types';
import { isOfficial } from '$lib/utils/official';
import { makeHash } from '$lib/server/hash';

const hostOnly = (v?: string) => (v ?? '').replace(/^https?:\/\//, '');

export const load: PageServerLoad = async ({ fetch }) => {
	const res = await fetch('/api/tests');
	if (!res.ok) throw new Error('Failed to fetch tests');

	const json = await res.json();
	const instances = json.data as Instance[];
	const lastUpdatedUTC = json.lastUpdatedUTC as string;

	const enriched = instances.map((inst) => {
		const apiHost = hostOnly(inst.api);
		const id = apiHost ? makeHash(apiHost) : null;

		const entries = Object.values(inst.tests ?? {});
		const total = entries.length;
		const up = entries.filter((t) => t.status).length;
		const scorePct = total > 0 ? Math.round((up / total) * 100) : 0;

		return {
			...inst,
			id,
			totals: { up, total },
			scorePct,
			officialComputed: isOfficial(inst.api),
			online: inst.online !== false
		};
	});

	const sorter = (a: (typeof enriched)[number], b: (typeof enriched)[number]) =>
		b.scorePct - a.scorePct || (a.frontend || '').localeCompare(b.frontend || '');

	const official = enriched.filter((i) => i.officialComputed).sort(sorter);
	const community = enriched.filter((i) => !i.officialComputed).sort(sorter);

	return { official, community, lastUpdatedUTC };
};
