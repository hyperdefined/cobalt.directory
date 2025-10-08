import type { PageServerLoad } from './$types';
import type { Instance, ServiceResult } from '$lib/types';
import { makeHash } from '$lib/server/hash';

type TestsResponse = {
	data: Instance[];
	lastUpdatedUTC: string;
};

type ServiceRow = {
	key: string;
	friendly: string;
	status: boolean;
	message: string;
};

const stripProto = (v?: string) => (v ?? '').replace(/^https?:\/\//, '');

export const load: PageServerLoad = async ({ fetch, params }) => {
	const res = await fetch('/api/tests', { cache: 'no-store' });
	if (!res.ok) throw new Error('Failed to fetch tests');

	const { data: instances, lastUpdatedUTC } = (await res.json()) as TestsResponse;

	const getId = (inst: Instance) => makeHash(stripProto(inst.api));
	const inst = instances.find((i) => getId(i) === params.id);

	if (!inst) {
		return {
			notFound: true as const,
			id: params.id,
			lastUpdatedUTC
		};
	}

	// derive nice labels
	const apiHost = stripProto(inst.api);
	const titleHost = stripProto(inst.frontend) || 'None';

	let apiNick: string | null = null;
	if (apiHost.endsWith('.imput.net')) {
		apiNick = apiHost.split('.')[0] || null;
	}

	const tests: Record<string, ServiceResult> = inst.tests ?? {};

	const entries: ServiceRow[] = Object.entries(tests).map(([key, v]) => {
		const friendly = v.friendly ?? key;
		const status = Boolean(v.status);
		const message = v.message ?? '';
		return { key, friendly, status, message };
	});

	const sorted: ServiceRow[] = [
		...entries.filter((e) => e.key.toLowerCase() === 'frontend'),
		...entries
			.filter((e) => e.key.toLowerCase() !== 'frontend')
			.sort((a, b) => a.friendly.localeCompare(b.friendly))
	];

	const online = inst.online !== false;

	return {
		notFound: false as const,
		id: params.id,
		instance: {
			...inst,
			titleHost,
			apiHost,
			apiNick,
			services: sorted,
			online
		},
		lastUpdatedUTC
	};
};
