import { json } from '@sveltejs/kit';
import fs from 'node:fs';
import path from 'node:path';

export const GET = async ({ url }) => {
  const type = url.searchParams.get('type') ?? 'api';

  const allowed = new Set(['api', 'frontends']);
  if (!allowed.has(type)) {
    return json(
      { error: 'Invalid type parameter. Must be "api" or "frontends".' },
      { status: 400 }
    );
  }

  const fileMap = {
    api: process.env.API_JSON_PATH ?? '/data/api.json',
    frontends: process.env.API_FRONTENDS_JSON_PATH ?? '/data/api_frontends.json'
  };

  const filePath = fileMap[type];

  try {
    const resolved = path.resolve(filePath);
    const stats = fs.statSync(resolved);
    const raw = fs.readFileSync(resolved, 'utf-8');
    const data = JSON.parse(raw);

    return json({
      lastUpdatedUTC: stats.mtime.toISOString(),
      data
    });
  } catch (err) {
    console.error(`Error reading ${filePath}:`, err);
    return json({ error: `Failed to read file for type: ${type}` }, { status: 500 });
  }
};
