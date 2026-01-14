<script lang="ts">
	import type { PageData } from './$types';
	import { isOfficial } from '$lib/utils/official';
	import OfficialBlurb from '$lib/components/OfficialBlurb.svelte';
	import CommunityBlurb from '$lib/components/CommunityBlurb.svelte';
	import { page } from '$app/state';
	import { fmtTime } from '$lib/utils/time';

	const currentUrl = page.url.href;

	export let data: PageData;

	const { instances, services, lastUpdatedUTC } = data;

	let selectedKey = '';

	const formatOnlineTime = (ms?: number | null) => {
		if (!ms || ms <= 0) return '—';

		const totalSeconds = Math.floor(ms / 1000);
		const days = Math.floor(totalSeconds / 86400);
		const hours = Math.floor((totalSeconds % 86400) / 3600);
		const minutes = Math.floor((totalSeconds % 3600) / 60);

		if (days > 0) return `${days}d, ${hours}h, ${minutes}m`;
		if (hours > 0) return `${hours}h, ${minutes}m`;
		return `${minutes}m`;
	};

	const unixToMs = (v?: number | null) => {
		if (!v) return null;
		return v < 1e12 ? v * 1000 : v; // seconds -> ms
	};

	const safeHost = (h?: string | null) => h?.replace(/^https?:\/\//, '') ?? '';

	const rowClass = (r: { online: boolean; working: boolean }) =>
		!r.online ? 'rating-offline' : r.working ? 'rating-working' : 'rating-not-working';

	const canLinkFrontend = (r: { online: boolean; frontend: string | null }) =>
		r.online && r.frontend && r.frontend.trim().length > 0;

	// derive friendly name from selection
	$: friendlyName = services.find((s) => s.key === selectedKey)?.name ?? selectedKey;
	// compute table rows dynamically
	$: rows = (() => {
		if (!selectedKey) return { official: [], community: [] };

		const nowMs = Date.now();
		const items = instances
			.map((inst) => {
				const entry = inst.tests?.[selectedKey];
				const online = inst.online !== false;

				const startMs = unixToMs(inst.startTime);
				const onlineForMs = online && startMs && startMs <= nowMs ? nowMs - startMs : null;

				if (!entry && !online) {
					return {
						officialComputed: isOfficial(inst.api),
						frontend: inst.frontend ?? null,
						api: inst.api ?? '',
						online,
						onlineForMs,
						working: false,
						message: 'Offline'
					};
				}

				if (!entry && online) {
					return {
						officialComputed: isOfficial(inst.api),
						frontend: inst.frontend ?? null,
						api: inst.api ?? '',
						online,
						onlineForMs,
						working: false,
						message: 'Unsupported'
					};
				}

				return {
					officialComputed: isOfficial(inst.api),
					frontend: inst.frontend ?? null,
					api: inst.api ?? '',
					online,
					onlineForMs,
					working: online ? Boolean(entry?.status) : false,
					message: online ? (entry ? entry.message : 'Unsupported') : 'Offline'
				};
			})
			.filter(Boolean);

		const official = items.filter((r) => r.officialComputed);
		const community = items.filter((r) => !r.officialComputed);

		const host = (h?: string | null) => (h ?? '').replace(/^https?:\/\//, '');
		official.sort((a, b) => host(a.frontend).localeCompare(host(b.frontend)));
		community.sort((a, b) => host(a.frontend).localeCompare(host(b.frontend)));

		return { official, community };
	})();
</script>

<svelte:head>
	<title>Search By Service</title>
	<meta property="og:url" content={currentUrl} />
	<meta property="og:title" content="Search By Service" />
	<meta
		property="og:description"
		content="All services cobalt can download and their status per instance."
	/>
	<meta property="twitter:url" content={currentUrl} />
	<meta name="twitter:title" content="Search By Service" />
	<meta
		name="twitter:description"
		content="All services cobalt can download and their status per instance."
	/>
</svelte:head>

<div>
	<h2>Search By Service</h2>
	<p>Select a service to view its status across all instances.</p>
	<p>Last updated (UTC): {fmtTime(lastUpdatedUTC)}</p>

	<div>
		<label for="service">Service:</label>
		<select id="service" bind:value={selectedKey}>
			<option value="">-- Select a service --</option>
			{#each services as s (s.key)}
				<option value={s.key}>{s.name}</option>
			{/each}
		</select>
	</div>

	{#if !selectedKey}
		<p>Please select a service from the dropdown above.</p>
	{:else}
		<h3>{friendlyName}</h3>

		<!-- OFFICIAL -->
		<section>
			<h4>Official Instances</h4>
			<OfficialBlurb />

			<div class="table-container">
				<table class="service-table">
					<thead>
						<tr>
							<th>Frontend</th>
							<th>API</th>
							<th>Uptime</th>
							<th>Working?</th>
							<th>Status</th>
						</tr>
					</thead>
					<tbody>
						{#if rows.official.length === 0}
							<tr><td colspan="4">No official instances found.</td></tr>
						{:else}
							{#each rows.official as r (r.api)}
								<tr class={rowClass(r)}>
									<td>
										{#if !r.frontend || r.frontend.trim() === ''}
											None
										{:else if canLinkFrontend(r)}
											<a href={`https://${safeHost(r.frontend)}`} target="_blank" rel="noopener">
												{safeHost(r.frontend)}
											</a>
										{:else}
											{safeHost(r.frontend)}
										{/if}
									</td>
									<td>{safeHost(r.api) || '—'}</td>
									<td>{r.online ? formatOnlineTime(r.onlineForMs) : 'Offline'}</td>
									<td>{r.working ? '✅' : '❌'}</td>
									<td>{r.message}</td>
								</tr>
							{/each}
						{/if}
					</tbody>
				</table>
			</div>
		</section>

		<!-- COMMUNITY -->
		<section>
			<h4>Community Instances</h4>
			<CommunityBlurb />

			<div class="table-container">
				<table class="service-table">
					<thead>
						<tr>
							<th>Frontend</th>
							<th>API</th>
							<th>Uptime</th>
							<th>Working?</th>
							<th>Status</th>
						</tr>
					</thead>
					<tbody>
						{#if rows.community.length === 0}
							<tr><td colspan="4">No community instances found.</td></tr>
						{:else}
							{#each rows.community as r (r.api)}
								<tr class={rowClass(r)}>
									<td>
										{#if !r.frontend || r.frontend.trim() === ''}
											None
										{:else if canLinkFrontend(r)}
											<a href={`https://${safeHost(r.frontend)}`} target="_blank" rel="noopener">
												{safeHost(r.frontend)}
											</a>
										{:else}
											{safeHost(r.frontend)}
										{/if}
									</td>
									<td>{safeHost(r.api) || '—'}</td>
									<td>{r.online ? formatOnlineTime(r.onlineForMs) : 'Offline'}</td>
									<td>{r.working ? '✅' : '❌'}</td>
									<td>{r.message}</td>
								</tr>
							{/each}
						{/if}
					</tbody>
				</table>
			</div>
		</section>
	{/if}
</div>
