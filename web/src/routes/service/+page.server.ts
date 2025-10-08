import type { PageServerLoad } from './$types';
import type { Instances } from '$lib/types';

export const load: PageServerLoad = async ({ fetch }) => {
  const res = await fetch('/api/tests', { cache: 'no-store' });
  if (!res.ok) throw new Error('Failed to fetch tests');

  const json = await res.json();
  const instances = json.data as Instances;
  const lastUpdatedUTC = json.lastUpdatedUTC as string;

  const servicesMap = new Map<string, string>();
  for (const inst of instances) {
    for (const [key, val] of Object.entries(inst.tests ?? {})) {
      if (key.toLowerCase() === 'frontend') continue;
      const friendly = (val as any)?.friendly ?? key;
      servicesMap.set(key, friendly);
    }
  }

  const services = Array.from(servicesMap.entries())
    .map(([key, name]) => ({ key, name }))
    .sort((a, b) => a.name.localeCompare(b.name));

  return {
    instances,
    services,
    lastUpdatedUTC
  };
};