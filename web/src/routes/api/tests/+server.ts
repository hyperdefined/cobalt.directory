import { json } from '@sveltejs/kit';
import { API_RESULTS_JSON_PATH } from '$env/static/private';
import fs from 'node:fs';

export const GET = async () => {
	const filePath = API_RESULTS_JSON_PATH ?? 'data/results.json';
	const raw = fs.readFileSync(filePath, 'utf-8');
	const stats = fs.statSync(filePath);
	const data = JSON.parse(raw);

	return json({ lastUpdatedUTC: stats.mtime.toISOString(), data });
};
