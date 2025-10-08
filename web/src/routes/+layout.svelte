<script lang="ts">
	import '../app.css';
	import Header from '$lib/components/Header.svelte';
	import Footer from '$lib/components/Footer.svelte';

	import { afterNavigate } from '$app/navigation';
	import { onMount } from 'svelte';
	import { page } from '$app/state';

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

<Header />
<main>
	<slot />
</main>
<Footer />