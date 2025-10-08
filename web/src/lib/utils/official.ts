export function isOfficial(api?: string): boolean {
	if (!api) return false;
	const host = api.replace(/^https?:\/\//, '').toLowerCase();
	// exact match or any subdomain
	return /(^|\.)imput\.net$/.test(host);
}
