import crypto from 'node:crypto';
export function makeHash(input: string): string {
	const clean = input.trim().toLowerCase();
	const hash = crypto.createHash('sha256').update(clean).digest('hex');
	return hash.substring(0, 10);
}
