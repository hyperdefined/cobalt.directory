import type { PageServerLoad } from './$types';
import type { Instances } from '$lib/types';
import { makeHash } from '$lib/server/hash';

export const load: PageServerLoad = async ({ fetch, params }) => {
  const res = await fetch('/api/tests', { cache: 'no-store' });
  if (!res.ok) throw new Error('Failed to fetch tests');

  // 🔽 extract from wrapped object
  const json = await res.json();
  const instances = json.data as Instances;
  const lastUpdatedUTC = json.lastUpdatedUTC as string;

  const getId = (inst: any) =>
    makeHash((inst.api ?? '').replace(/^https?:\/\//, ''));

  const inst = instances.find((i) => getId(i) === params.id);

  if (!inst) {
    return {
      notFound: true,
      id: params.id,
      lastUpdatedUTC
    };
  }

  // derive nice labels
  const apiHost = (inst.api ?? '').replace(/^https?:\/\//, '');
  let apiNick: string | null = null;

  if (apiHost.endsWith('.imput.net')) {
    apiNick = apiHost.split('.')[0] || null;
  }

  const titleHost = (inst.frontend ?? '').replace(/^https?:\/\//, '') || 'None';

  const entries = Object.entries(inst.tests ?? {}).map(([key, v]) => {
    const friendly = (v as any)?.friendly ?? key;
    const status = Boolean((v as any)?.status);
    const message = (v as any)?.message ?? '';
    return { key, friendly, status, message };
  });

  const sorted = [
    ...entries.filter((e) => e.key.toLowerCase() === 'frontend'),
    ...entries
      .filter((e) => e.key.toLowerCase() !== 'frontend')
      .sort((a, b) => a.friendly.localeCompare(b.friendly))
  ];

  const online = inst.online !== false;

  return {
    notFound: false,
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