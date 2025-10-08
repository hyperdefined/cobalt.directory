import type { RequestHandler } from './$types';
import fs from 'node:fs';

export const GET: RequestHandler = async () => {
  const filePath = process.env.API_JSON_PATH ?? '/app/jsons/api.json';
  const body = fs.readFileSync(filePath, 'utf-8');
  return new Response(body, {
    headers: { 'content-type': 'application/json; charset=utf-8' }
  });
};