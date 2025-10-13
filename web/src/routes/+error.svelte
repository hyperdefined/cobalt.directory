<script lang="ts">
	import { page } from '$app/state';
	import { Img } from 'flowbite-svelte';

	const currentUrl = page.url.href;
	const status = String(page.status);
	const message = page.error?.message ?? 'Something went wrong.';
</script>

<svelte:head>
	<title>{status}</title>
	<meta property="og:url" content={currentUrl} />
	<meta property="og:title" content={status} />
	<meta property="og:description" content="There was an issue fetching this page: {message}." />
	<meta property="twitter:url" content={currentUrl} />
	<meta name="twitter:title" content={status} />
	<meta name="twitter:description" content="There was an issue fetching this page: {message}." />
</svelte:head>

<h2>{status} - {message}</h2>
<p>There was an issue fetching this page: {message}</p>
<Img src="https://http.cat/{status}" alt="Cat under papers with 404 message" />
