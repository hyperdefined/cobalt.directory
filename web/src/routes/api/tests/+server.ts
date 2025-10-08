import { json } from '@sveltejs/kit';
import fs from 'node:fs';

export const GET = async () => {
  const filePath = process.env.TESTS_JSON_PATH ?? '/data/tests.json'; // default
  const raw = fs.readFileSync(filePath, 'utf-8');
  const stats = fs.statSync(filePath);
  const data = JSON.parse(raw);

  return json({ lastUpdatedUTC: stats.mtime.toISOString(), data });
};