import { json } from '@sveltejs/kit';
import fs from 'node:fs';
import path from 'node:path';

export async function GET() {
  const filePath = path.resolve('data/results.json');

  try {
    // read JSON
    const data = JSON.parse(fs.readFileSync(filePath, 'utf-8'));

    // get file metadata
    const stats = fs.statSync(filePath);
    const lastModified = stats.mtime.toISOString();

    return json({
      lastUpdatedUTC: lastModified,
      data
    });
  } catch (err) {
    console.error('Error reading tests.json:', err);
    return json({ error: 'Failed to read test data' }, { status: 500 });
  }
}