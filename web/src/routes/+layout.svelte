<script lang="ts">
	import '../app.css';
	import Header from '$lib/components/Header.svelte';
	import Footer from '$lib/components/Footer.svelte';

	import { afterNavigate } from '$app/navigation';
	import { onMount } from 'svelte';
	import { page } from '$app/state';
	import { dev } from '$app/environment';

	const SITE_SUFFIX = ' - cobalt.directory';

	function updateTitle() {
		const path = page.url.pathname;
		if (path === '/' || path === '') {
			document.title = 'cobalt.directory';
			return;
		}

		// otherwise add suffix
		const base = document.title.replace(SITE_SUFFIX, '');
		document.title = base + SITE_SUFFIX;
	}

	onMount(updateTitle);
	afterNavigate(updateTitle);
</script>

<svelte:head>
	{#if !dev}
		<script defer data-domain="cobalt.directory" src="https://plausible.canine.tools/js/script.js">
		</script>
	{/if}
</svelte:head>

<Header />
<main>
	<slot />
</main>
<Footer />
