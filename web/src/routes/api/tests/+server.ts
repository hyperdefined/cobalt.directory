import { json } from '@sveltejs/kit';
import { env } from '$env/dynamic/private';
import fs from 'node:fs';

export const GET = async () => {
	const filePath = env.API_RESULTS_JSON_PATH ?? 'data/results.json';
	const raw = fs.readFileSync(filePath, 'utf-8');
	const stats = fs.statSync(filePath);
	const data = JSON.parse(raw);

	return json({ lastUpdatedUTC: stats.mtime.toISOString(), data });
};
